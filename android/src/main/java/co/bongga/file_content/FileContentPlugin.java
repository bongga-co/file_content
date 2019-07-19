package co.bongga.file_content;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import co.bongga.file_content.utils.FileUtils;
import co.bongga.file_content.utils.Schema;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class FileContentPlugin implements
        MethodCallHandler,
        EventChannel.StreamHandler,
        PluginRegistry.NewIntentListener {

  private Registrar registrar;
  private Map<String, Object> dataResult = null;
  private EventChannel.EventSink eventSink = null;

  private Uri fileUri = null;
  private String fileSchema = null;
  private String mimeType = null;
  private boolean isInitial = false;

  public static void registerWith(Registrar registrar) {
    if (registrar.activity() == null) {
      return;
    }

    String CHANNEL_FILE = "plugins.bongga.co/receive";
    String EVENT_CHANNEL_FILE = "plugins.bongga.co/event-receive";

    final MethodChannel channel = new MethodChannel(registrar.messenger(), CHANNEL_FILE);
    final EventChannel eventChannel = new EventChannel(registrar.messenger(), EVENT_CHANNEL_FILE);

    FileContentPlugin plugin = new FileContentPlugin(registrar);

    eventChannel.setStreamHandler(plugin);
    channel.setMethodCallHandler(plugin);
  }

  private FileContentPlugin(Registrar registrar) {
    this.registrar = registrar;
    this.onReceiveContent(registrar.activity().getIntent(),true);
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    switch (call.method) {
      case "getFile":
        result.success(dataResult);
        break;

      case "reset":
        resetData(result);
        break;

      default:
        result.notImplemented();
        break;
    }
  }

  @Override
  public void onListen(Object args, EventChannel.EventSink eventSink) {
    if(args.equals("file")) {
      this.eventSink = eventSink;
    }
  }

  @Override
  public void onCancel(Object args) {
    if(args.equals("file")) {
      this.eventSink = null;
    }
  }

  @Override
  public boolean onNewIntent(Intent intent) {
    onReceiveContent(intent, false);
    return false;
  }

  private void onReceiveContent(Intent intent, boolean initial) {
    if (
          intent != null &&
          (Intent.ACTION_VIEW.equals(intent.getAction()) || Intent.ACTION_SEND.equals(intent.getAction()))
    ) {

      this.mimeType = intent.getType();
      Uri data = intent.getData();

      Bundle extras = intent.getExtras();

      if(extras != null && extras.containsKey(Intent.EXTRA_STREAM)) {
        data = extras.getParcelable(Intent.EXTRA_STREAM);
      }

      if(data != null) {
        this.fileUri = data;
      }

      this.isInitial = initial;

      checkFileType();
    }
  }

  private void checkFileType() {
    String scheme = this.fileUri.getScheme();

    if(scheme != null) {
      this.fileSchema = scheme;

      if (scheme.equalsIgnoreCase(Schema.CONTENT)) {
        getFileFromContent();
        return;
      }

      if (scheme.equalsIgnoreCase(Schema.FILE)) {
        getFileFromStorage();
      }
    } else {
      sendData(true);
    }
  }

  private void getFileFromStorage() {
    sendData(false);
  }

  private void getFileFromContent() {
    if (!Environment.getExternalStorageState().equalsIgnoreCase("mounted")) {
      Toast.makeText(this.registrar.activity(), "Este dispositivo no cuenta con almacenamiento externo.", Toast.LENGTH_SHORT).show();
    } else {
      sendData(false);
    }
  }

  private void resetData(Result result) {
    this.dataResult = null;
    this.mimeType = null;
    this.fileUri = null;
    this.fileSchema = null;

    result.success(null);
  }

  private void copyFile() {
    // TODO: create async task to copy the file locally.
  }

  private void sendData(boolean isEmpty) {
    if(isEmpty) return;

    this.dataResult = new HashMap<>();

    this.dataResult.put("name", FileUtils.getNameFromUri(this.registrar.activity(), this.fileUri));
    this.dataResult.put("size", FileUtils.getFileSize(this.registrar.activity(), this.fileUri));
    this.dataResult.put("path", this.fileUri.toString());

    if(this.eventSink != null) {
      this.eventSink.success(this.dataResult);
    }
  }
}

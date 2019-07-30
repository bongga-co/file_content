package co.bongga.file_content;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import co.bongga.file_content.interfaces.CopyTaskListener;
import co.bongga.file_content.utils.CopyTask;
import co.bongga.file_content.utils.FileUtils;
import co.bongga.file_content.utils.ImportTask;
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
  private String fileScheme = null;
  private String mimeType = null;

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
    if(this.eventSink == null) {
      this.eventSink = eventSink;
    }
  }

  @Override
  public void onCancel(Object args) {
    if(this.eventSink != null) {
      this.eventSink.endOfStream();
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

      if(intent.getData() != null) {
        this.fileUri = intent.getData();
      } else {
        Bundle extras = intent.getExtras();

        if(extras != null && extras.containsKey(Intent.EXTRA_STREAM)) {
          this.fileUri = extras.getParcelable(Intent.EXTRA_STREAM);
        }
      }

      if(this.fileUri != null) {
        checkFileScheme();
      }
    }
  }

  //TODO: dleete
  private void test(String data) {
    this.dataResult = new HashMap<>();

    this.dataResult.put("data", data);

    if(this.eventSink != null) {
      this.eventSink.success(this.dataResult);
    }
  }

  private void checkFileScheme() {
    String scheme = this.fileUri.getScheme();

    if(scheme != null) {
      this.fileScheme = scheme;

      if (scheme.equalsIgnoreCase(Schema.CONTENT)) {
        getFileFromContent();
        return;
      }

      if (scheme.equalsIgnoreCase(Schema.FILE)) {
        getFileFromStorage();
      }
    } else {
      sendData(true, null);
    }
  }

  private void getFileFromStorage() {
    copyFile();
  }

  private void getFileFromContent() {
    if (!Environment.getExternalStorageState().equalsIgnoreCase("mounted")) {
      Toast.makeText(this.registrar.activity(), "Este dispositivo no cuenta con almacenamiento externo.", Toast.LENGTH_SHORT).show();
    } else {
      copyFile();
    }
  }

  private ProgressDialog getProgressDialog(Context context) {
    ProgressDialog progressDialog = new ProgressDialog(context);
    progressDialog.setTitle("DRMaps");
    progressDialog.setMessage("Cargando...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    progressDialog.setProgress(0);
    progressDialog.setMax(100);
    progressDialog.setCancelable(false);

    return progressDialog;
  }

  private ProgressBar getProgressBar(Context context) {
    ProgressBar progress = new ProgressBar(context);

    progress.setMax(100);
    progress.setProgress(0);
    progress.setVisibility(View.VISIBLE);

    return progress;
  }

  private void sendData(boolean isEmpty, String uri) {
    if(isEmpty) return;

    this.dataResult = new HashMap<>();

    this.dataResult.put("name", FileUtils.getNameFromUri(this.registrar.activity(), this.fileUri));
    this.dataResult.put("size", FileUtils.getFileSize(this.registrar.activity(), this.fileUri));
    this.dataResult.put("path", uri);
    this.dataResult.put("mime", this.mimeType);

    if(this.eventSink != null) {
      this.eventSink.success(this.dataResult);
    }
  }

  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void copyFile() {
    Context context = this.registrar.activity();

    String name = FileUtils.getNameFromUri(context, this.fileUri);
    long size = FileUtils.getFileSize(context, this.fileUri);

    ProgressDialog dialog = getProgressDialog(context);
    dialog.show();

    final ImportTask copyTask = new ImportTask(context, name, size, dialog, new CopyTaskListener() {
      @Override
      public void onCopyFileFinished(String result) {
        FileContentPlugin.this.sendData(false, result);
      }
    });

    copyTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.fileUri);
  }

  private void resetData(Result result) {
    this.dataResult = null;
    this.mimeType = null;
    this.fileUri = null;
    this.fileScheme = null;

    result.success(null);
  }
}

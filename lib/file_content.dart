import 'dart:async';
import 'package:flutter/services.dart';

class FileContent {
  static const String _CHANNEL_FILE = 'plugins.bongga.co/receive';
  static const String _EVENT_CHANNEL_FILE = 'plugins.bongga.co/event-receive';

  static const MethodChannel _channel = const MethodChannel(_CHANNEL_FILE);
  static const EventChannel _event = const EventChannel(_EVENT_CHANNEL_FILE);

  static Stream<dynamic> _streamFiles;

  static Future<dynamic> getFile() async {
    final file = await _channel.invokeMethod('getFile');
    return file;
  }
  
  static Stream<dynamic> getFileStream() {
    if (_streamFiles == null) {
      _streamFiles = _event.receiveBroadcastStream("file");
    }

    return _streamFiles;
  }

  static void reset() {
    _channel.invokeMethod('reset').then((_) {});
  }
}

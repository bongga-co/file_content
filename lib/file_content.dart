import 'dart:async';
import 'dart:convert';
import 'package:flutter/services.dart';
import 'package:file_content/models/SharedFile.dart';

class FileContent {
  static const String _CHANNEL_FILE = 'plugins.bongga.co/receive';
  static const String _EVENT_CHANNEL_FILE = 'plugins.bongga.co/event-receive';

  static const MethodChannel _channel = const MethodChannel(_CHANNEL_FILE);
  static const EventChannel _event = const EventChannel(_EVENT_CHANNEL_FILE);

  static Stream<dynamic> _streamFiles;

  static Future<List<SharedFile>> getImage() async {
    final String json = await _channel.invokeMethod('getImage');
    if (json == null) return null;
    final encoded = jsonDecode(json);
    return encoded
        .map<SharedFile>((file) => SharedFile.fromJson(file))
        .toList();
  }

  static Future<List<SharedFile>> getFile() async {
    final String json = await _channel.invokeMethod('getFile');
    if (json == null) return null;
    final encoded = jsonDecode(json);
    return encoded
        .map<SharedFile>((file) => SharedFile.fromJson(file))
        .toList();
  }

  static Stream<List<SharedFile>> getFileStream() {
    if (_streamFiles == null) {
      final stream =
          _event.receiveBroadcastStream("file").cast<String>();
      _streamFiles = stream.transform<List<SharedFile>>(
        new StreamTransformer<String, List<SharedFile>>.fromHandlers(
          handleData: (String data, EventSink<List<SharedFile>> sink) {
            if (data == null) {
              sink.add(null);
            } else {
              final encoded = jsonDecode(data);
              sink.add(encoded
                  .map<SharedFile>(
                      (file) => SharedFile.fromJson(file))
                  .toList());
            }
          },
        ),
      );
    }
    return _streamFiles;
  }

  static Stream<List<SharedFile>> getImageStream() {
    if (_streamFiles == null) {
      final stream =
          _event.receiveBroadcastStream("image").cast<String>();
      _streamFiles = stream.transform<List<SharedFile>>(
        new StreamTransformer<String, List<SharedFile>>.fromHandlers(
          handleData: (String data, EventSink<List<SharedFile>> sink) {
            if (data == null) {
              sink.add(null);
            } else {
              final encoded = jsonDecode(data);
              sink.add(encoded
                  .map<SharedFile>(
                      (file) => SharedFile.fromJson(file))
                  .toList());
            }
          },
        ),
      );
    }
    return _streamFiles;
  }

  static void reset() {
    _channel.invokeMethod('reset').then((_) {});
  }
}

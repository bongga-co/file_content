# file_content

A new Flutter plugin to receive shared content from other apps.

## Example

```dart

import 'dart:async';
import 'package:flutter/material.dart';
import 'package:file_content/models/SharedFile.dart';
import 'package:file_content/file_content.dart';

class Home extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<Home> {
  StreamSubscription _streamSubscription;

  @override
  void initState() {
    super.initState();
    
    _streamSubscription = FileContent.getFileStream().listen((List<SharedFile> values) {
      _getFileMap(values);
    });

    FileContent.getFile().then((List<SharedFile> values) {
      _getFileMap(values);
    });
  }

  @override
  void dispose() {
    _streamSubscription.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('FileContent'),
        ),
        body: Center(
          child: Container(
            child: Text('File Content example app'),
          )
        ),
      ),
    );
  }

  void _getFileMap(List<SharedFile> values) {
    if(values != null && values.first != null) {
      final SharedFile data = values.first;
      print(data.path);
    }
  }
}


```

## Getting Started

This project is a starting point for a Flutter
[plug-in package](https://flutter.dev/developing-packages/),
a specialized package that includes platform-specific implementation code for
Android and/or iOS.

For help getting started with Flutter, view our 
[online documentation](https://flutter.dev/docs), which offers tutorials, 
samples, guidance on mobile development, and a full API reference.

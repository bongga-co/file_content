import 'package:flutter/material.dart';
import 'dart:async';
import 'package:file_content/file_content.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  StreamSubscription _streamSubscription;
  dynamic _resp;

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  @override
  void dispose() {
    _streamSubscription.cancel();
    super.dispose();
  }

  Future<void> initPlatformState() async {
    _streamSubscription = FileContent.getFileStream().listen((value) {
      setState(() => _resp = value);
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Container(
            child: _resp != null 
              ? Text('Running on: ${_resp.toString()}', textAlign: TextAlign.center,)
              : Text('No Proceesed'),
          )
        ),
      ),
    );
  }
}

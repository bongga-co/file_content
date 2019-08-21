import 'package:flutter/material.dart';
import 'dart:async';
import 'package:file_content/file_content.dart';

class Home extends StatefulWidget {
  @override
  _HomeState createState() => _HomeState();
}

class _HomeState extends State<Home> {
  StreamSubscription _streamSubscription;
  dynamic _resp;

  @override
  void initState() {
    super.initState();
    
    _streamSubscription = FileContent.getFileStream().listen((value) {
      print(value);
      setState(() => _resp = value);
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

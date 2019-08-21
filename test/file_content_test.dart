import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
// import 'package:file_content/file_content.dart';

void main() {
  const MethodChannel channel = MethodChannel('file_content');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    // expect(await FileContent.platformVersion, '42');
  });
}

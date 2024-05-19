import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:acs_card_reader_thailand/acs_card_reader_thailand.dart';

void main() {
  const MethodChannel channel = MethodChannel('acs_card_reader_thailand');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await AcsCardReaderThailand.platformVersion, '42');
  });
}

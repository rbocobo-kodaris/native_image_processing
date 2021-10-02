
import 'dart:async';

import 'package:flutter/services.dart';

class NativeImageProcessing {
  static const MethodChannel _channel = MethodChannel('com.kodaris/native_image_processing');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<String?> get platBatteryLevel async {
    final String? version = await _channel.invokeMethod('getBatteryLevel');
    return version;
  }

  static Future<dynamic> getExifData(String filePath) async {
    final exifData = await _channel.invokeMethod("getExifData", { "filePath": filePath });
    return exifData;
  }
}


import 'dart:io';

import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:native_image_processing/native_image_processing.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';
  String _batteryLevel = 'Unknown';

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> initPlatformState() async {
    String platformVersion;
    // Platform messages may fail, so we use a try/catch PlatformException.
    // We also handle the message potentially returning null.
    try {
      platformVersion =
          await NativeImageProcessing.platformVersion ?? 'Unknown platform version';
    } on PlatformException {
      platformVersion = 'Failed to get platform version.';
    }

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {
      _platformVersion = platformVersion;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Center(
              child: Text('Running on: $_platformVersion\n'),
            ),
            Text('Battery Level: $_batteryLevel'),
            TextButton(onPressed: () async {
              final batteryLevel = await NativeImageProcessing.platBatteryLevel;
              setState(() {
                _batteryLevel = batteryLevel!;
              });
            }, child: Text('Get Battery Level')),
            TextButton(
              onPressed: () async {
                if(await Permission.manageExternalStorage.request().isGranted){
                  final filePath = File('/storage/emulated/0/DCIM/Camera/20210908_202844.jpg').path;
                  final exifData = await NativeImageProcessing.getExifData(filePath);
                  print(filePath);
                  print(exifData);
                }
              },
              child: Text('Get Exif Data'),
            ),
            TextButton(
              onPressed: () async {
                if(await Permission.manageExternalStorage.request().isGranted){
                  final filePath = File('/storage/emulated/0/DCIM/Camera/20210908_202844.jpg').path;
                  final byteData = await NativeImageProcessing.bakeOrientation(filePath);
                  print(filePath);
                  print(byteData);
                }
              },
              child: Text('Bake Orientation '),
            )
          ],
        ),
      ),
    );
  }
}

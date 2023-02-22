import 'dart:developer';

import 'package:flutter/material.dart';

import 'dart:io';

import 'package:flutter/services.dart';
import 'package:image/image.dart' as s;

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'Flutter Demo Home Page'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  static const platform = MethodChannel("com.pytorch");

  String caller = '';
  String imagePath = '';
  String documentsPath = '';

  @override
  void initState() {
    super.initState();
  }

  // Use code below
  Future<void> _getHuaweiMLKitSegmentation() async {
    // Convert Image under here to ByteData
    final ByteData imageData = await rootBundle.load("assets/people4.jpeg");
    // final s.Image? imgg = await s.decodeImageFile("assets/woman_small.jpg");
    // final resized = s.copyResize(imgg!, width: 500, height: 500);
    try {
      final String result = await platform.invokeMethod(
        'huawei_segment',
        <String, dynamic>{
          'image_data': imageData.buffer
              .asUint8List(imageData.offsetInBytes, imageData.lengthInBytes),
          'data_offset': imageData.offsetInBytes,
          'data_length': imageData.lengthInBytes
        },
      );
      setState(() {
        imagePath = result;
      });
      // final Uint8List byte = Uint8List.fromList(result.toList());
      log("SUCESS ${result.toString()}");
    } catch (e) {
      log("ERROR ${e.toString()}");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: <Widget>[
            const Text(
              'You have pushed the button this testy times:',
            ),
            buildImage(),
            ElevatedButton(
                onPressed: () {
                  setState(() {});
                },
                child: Text("Reload"))
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () async {
          await _getHuaweiMLKitSegmentation();

          await Future.delayed(const Duration(seconds: 3));
          setState(() {});
          //Future
        }, //_getHuaweiMLKitSegmentation,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }

  Future<Uint8List> loadData() {
    File file = File(imagePath);
    return file.readAsBytes();
    // Uint8List bytes = file.readAsBytesSync();

    // return bytes;
  }

  Widget buildImage() {
    if (imagePath != "") {
      return FutureBuilder<Uint8List>(
        future: loadData(),
        builder: (context, snapshot) {
          if (!snapshot.hasData) return const SizedBox();
          return Container(
            color: Colors.yellow,
            child: Image.memory(snapshot.data!),
            // child: Image.file(
            //   File(imagePath),
            //   height: 500,
            // ),
            // child: Image.asset(
            //   "assets/temp_bitmap.png",
            //   height: 500,
            // ),
          );
        },
        // child:
      );
    } else {
      return SizedBox.shrink();
    }
  }
}

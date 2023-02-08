import 'dart:developer';

import 'package:flutter/material.dart';

import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:widget_mask/widget_mask.dart';
import 'dart:typed_data';

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

  void _incrementCounter() async {
    String val = await platform.invokeMethod("Print");
    setState(() {
      caller = val;
    });
  }

  @override
  void initState() {
    super.initState();
    _gettingModelFile().then((void value) => log('File Created Successfuly'));
  }

  Future<void> _gettingModelFile() async {
    final Directory directory = await getApplicationDocumentsDirectory();

    setState(() {
      documentsPath = directory.path;
    });

    final String u2netmodel = join(directory.path, 'u2netp_v7.ptl');
    final ByteData data = await rootBundle.load('assets/u2netp_v7.ptl');

    final List<int> bytes =
        data.buffer.asUint8List(data.offsetInBytes, data.lengthInBytes);

    if (!File(u2netmodel).existsSync()) {
      await File(u2netmodel).writeAsBytes(bytes);
    }
  }

  Future<void> _getPytorchSegmentation() async {
    final ByteData imageData = await rootBundle.load("assets/man.jpeg");
    try {
      final String result = await platform.invokeMethod(
        'pytorch_segment',
        <String, dynamic>{
          'model_path': '$documentsPath/u2netp_v7.ptl',
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

  Future<void> _getHuaweiMLKitSegmentation() async {
    final ByteData imageData = await rootBundle.load("assets/man.jpeg");
    try {
      final String result = await platform.invokeMethod(
        'huawei_segment',
        <String, dynamic>{
          'model_path': '$documentsPath/u2netp_v7.ptl',
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
          ],
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _getHuaweiMLKitSegmentation,
        tooltip: 'Increment',
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget buildImage() {
    if (imagePath != "") {
      // return Container(
      //   padding: EdgeInsets.all(8),
      //   color: Colors.red,
      //   child: WidgetMask(
      //       blendMode: BlendMode.darken,
      //       childSaveLayer: true,
      //       child: Image.file(File(imagePath)),
      //       mask: Image.asset("assets/man.jpeg")),
      // );
      return Image.file(File(imagePath));
    } else {
      return SizedBox.shrink();
    }
  }
}

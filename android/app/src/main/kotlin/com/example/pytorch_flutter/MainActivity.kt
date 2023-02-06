package com.example.pytorch_flutter

import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import  io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.pytorch").setMethodCallHandler {
                call, result ->
            if(call.method == "Print") {
                val string = "Hi"
                result.success(string)
            }
            else {
                result.notImplemented()
            }
        }
    }
}

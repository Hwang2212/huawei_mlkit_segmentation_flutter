package com.example.pytorch_flutter

import io.flutter.embedding.android.FlutterActivity
import androidx.annotation.NonNull
import io.flutter.embedding.engine.FlutterEngine
import  io.flutter.plugin.common.MethodChannel
import io.flutter.plugins.GeneratedPluginRegistrant
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import org.pytorch.IValue
import org.pytorch.MemoryFormat
import org.pytorch.Module
import org.pytorch.PyTorchAndroid
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.pytorch").setMethodCallHandler {
                call, result ->
            if(call.method == "Print") {
                    // val module = Module.load(assetFilePath(this, "u2netp_small_live_test.ptl"))
                    println("Success")
            //         val bitmap = BitmapFactory.decodeStream(
            //             assets.open("man.jpeg")
            //         )
            //         val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
            //     bitmap,
            //     TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
            //     TensorImageUtils.TORCHVISION_NORM_STD_RGB,
            //     MemoryFormat.CHANNELS_LAST
            // )
                // val outputs = module.forward(IValue.from(inputTensor)).toTuple()
                // try {
                // }
                // catch(IOException e) {
                //     Log.e("Error Loading Model", "ImageSeg",e)
                // }
                val string = "Hi"
                result.success(string)
            }
            else {
                result.notImplemented()
            }
        }
    }

    private fun transformTensors2Bitmap(output: Tensor): Bitmap {
        val height = output.shape()[2].toInt()
        val width = output.shape()[3].toInt()
        val outputArr = output.dataAsFloatArray
        for (i in outputArr.indices) {
            outputArr[i] = Math.min(Math.max(outputArr[i], 0f), 255f)
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        var loc = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                bitmap.setPixel(
                    x, y, Color.rgb(
                        outputArr[loc],
                        outputArr[loc],
                        outputArr[loc]
                    )
                )
                loc += 1
            }
        }
        return bitmap
    }

    private fun assetFilePath(context: Context, assetName: String): String? {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        try {
            context.assets.open(assetName).use { `is` ->
                FileOutputStream(file).use { os ->
                    val buffer = ByteArray(4 * 1024)
                    var read = 0
                    while (`is`.read(buffer).also { read = it } != -1) {
                        os.write(buffer, 0, read)
                    }
                    os.flush()
                }
                return file.absolutePath
            }
        } catch (e: IOException) {
            Log.e("MainActivity", "Error process asset $assetName to file path")
        }
        return null
    }
}

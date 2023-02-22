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
import android.graphics.Canvas
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File
import java.io.FileNotFoundException;
import java.io.FileOutputStream
import java.io.IOException
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import kotlin.concurrent.thread

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.pytorch").setMethodCallHandler {
                call, result ->
            if(call.method == "Print") {
                    println("Success")
                val string = "Hi"
                result.success(string)}

            else if(call.method == "huawei_segment"){
                // Set Singapore as Region of Huawei Service
                val REGION_DR_SINGAPORE :Int = 1007
                MLApplication.getInstance().setUserRegion(REGION_DR_SINGAPORE);
                val boffset = call.argument<Int>("data_offset")

                val blenght = call.argument<Int>("data_length")


                val nonNullbOffset = boffset!!

                val nonNullbLenght = blenght!!


                val byteStream = call.argument<ByteArray>("image_data")

                val segbitmap = BitmapFactory.decodeByteArray(byteStream, nonNullbOffset, nonNullbLenght)
//
//                // Use MLImageSegmentationSetting to customize the image segmentation analyzer.
                var setting = MLImageSegmentationSetting.Factory() // Set whether to support fine segmentation. The value true indicates fine segmentation, and the value false indicates fast segmentation.
                    .setExact(true) // Set the human body segmentation mode.
                    .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG) // Set returned result types.
                    .setScene(MLImageSegmentationScene.ALL)
                    .create()
                var analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)
                val mlFrame = MLFrame.fromBitmap(segbitmap)
//                Synchronous Call for Huawei
//                val segmentationResult = analyzer.analyseFrame(mlFrame)

                analyzer.asyncAnalyseFrame(mlFrame)
                    .addOnSuccessListener {
                        val outputBitmap:Bitmap = it.getForeground()

                        // Change Saved File Image if needed
                        val rootDirectory : String = getActivity().getFilesDir().toString()+"/temp_bitmap.png"
                        Log.i("Huawei ML Kit", "Success")
                        result.success(rootDirectory)

                        saveJPGE_After(outputBitmap, rootDirectory)
                    }
                    .addOnFailureListener {
                        Log.e("ERROR HUAWEI", "analyse -> asyncAnalyseFrame: ", it)
                    }
            }
            else {
                result.notImplemented()
            }
        }
    }





    private fun saveJPGE_After(bitmap: Bitmap, path: String) {

        val file:File =  File(path);
        try {
            val out: FileOutputStream =  FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 50, out)) {
                out.flush();
                out.close();
            }
        } catch (e:FileNotFoundException) {
            e.printStackTrace();
        } catch (e:IOException ) {
            e.printStackTrace();
        }
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

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
import org.pytorch.IValue
import org.pytorch.MemoryFormat
import org.pytorch.Module
import org.pytorch.LiteModuleLoader
import org.pytorch.PyTorchAndroid
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileNotFoundException;
import java.io.FileOutputStream
import java.io.IOException
import kotlin.concurrent.thread

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(@NonNull flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.pytorch").setMethodCallHandler {
                call, result ->
            if(call.method == "Print") {
                    println("Success")
                val string = "Hi"
                result.success(string)
            } else if(call.method == "segment_image"){
                
                 try {
                     Log.i("Pytorch: Main activity", "Enter")
                    val absPath = call.argument<String>("model_path")!!
                     Log.i("Pytorch: Main activity", "Enter 1")

                     val boffset = call.argument<Int>("data_offset")
                     Log.i("Pytorch: Main activity", "Enter 2")

                     val blenght = call.argument<Int>("data_length")
                     Log.i("Pytorch: Main activity", "Enter 3")


                     val nonNullbOffset = boffset!!
                     Log.i("Pytorch: Main activity", "Enter 4")

                     val nonNullbLenght = blenght!!
                     Log.i("Pytorch: Main activity", "Enter 5")


                     val byteStream = call.argument<ByteArray>("image_data")
                     Log.i("Pytorch: Main activity", "Enter 6")

                    val segbitmap = BitmapFactory.decodeByteArray(byteStream, nonNullbOffset, nonNullbLenght)
                     Log.i("Pytorch: Main activity", "Enter 7")
                     Log.i("Pytorch: Main activity", absPath)

                     val segmodule = LiteModuleLoader.load(absPath)
                     Log.i("Pytorch: Main activity", "Enter 8")


                     val seginputTensor:Tensor = TensorImageUtils.bitmapToFloat32Tensor(
                        segbitmap,
                        TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                        TensorImageUtils.TORCHVISION_NORM_STD_RGB
                    )
                     Log.i("Pytorch: Main activity", "Enter 9")

                     val segoTensor:Array<IValue> = segmodule.forward(
                        IValue.from(seginputTensor)
                    ).toTuple();
                     Log.i("Pytorch: Main activity", "Enter 10")

                     val outputTensor:Tensor = segoTensor[0].toTensor()
                     Log.i("Pytorch: Main activity", "Enter 11")

                     val scores : FloatArray = outputTensor.getDataAsFloatArray()
                     Log.i("Pytorch: Main activity", "Enter 12")

                     val outputBitmap :Bitmap = transformTensors2Bitmap(outputTensor)
                     Log.i("Pytorch: Main activity", "Enter 13")

                     val rootDirectory : String = getActivity().getFilesDir().toString()+"/temp_bitmap"
                     Log.i("Pytorch: Main activity", "Enter 14")

                     saveJPGE_After(outputBitmap, rootDirectory)
                     Log.i("Pytorch: Main activity", "Enter 15")

                     result.success(rootDirectory)
                    // val dict: Map<String,IValue> = segoTensor.toDictStringKey();

                    Log.i("Pytorch: Main activity", absPath)

                 } catch (e:Exception) {
                     Log.e("Pytorch: Main activity", "Error reading", e)
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
            if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                out.flush();
                out.close();
            }
        } catch (e:FileNotFoundException) {
            e.printStackTrace();
        } catch (e:IOException ) {
            e.printStackTrace();
        }
    }

    // fun mergeBitmaps(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    //     val merged = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
    //     val canvas = Canvas(merged)
    //     canvas.drawBitmap(bmp1, Matrix(), null)
    //     canvas.drawBitmap(bmp2, Matrix(), null)
    //     return merged
    // }   

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

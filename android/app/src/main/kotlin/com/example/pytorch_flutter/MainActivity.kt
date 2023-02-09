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
                result.success(string)
            } else if(call.method == "pytorch_segment"){
                
                 try {
                     Log.i("Pytorch: Main activity", "Enter")
                    val absPath = call.argument<String>("model_path")!!
                     Log.i("Pytorch: Main activity", "Enter 1")

                     val boffset = call.argument<Int>("data_offset")
                     Log.i("Pytorch: Main activity", boffset.toString())

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

            }else if(call.method == "huawei_segment"){
                val REGION_DR_SINGAPORE :Int = 1007
                MLApplication.getInstance().setUserRegion(REGION_DR_SINGAPORE);
                val boffset = call.argument<Int>("data_offset")

                val blenght = call.argument<Int>("data_length")


                val nonNullbOffset = boffset!!

                val nonNullbLenght = blenght!!


                val byteStream = call.argument<ByteArray>("image_data")

                val segbitmap = BitmapFactory.decodeByteArray(byteStream, nonNullbOffset, nonNullbLenght)
//                // Method 1: Use default parameter settings to configure the image segmentation analyzer.
//                // The default mode is human body segmentation in fine mode. All segmentation results of human body segmentation are returned (pixel-level label information, human body image with a transparent background, gray-scale image with a white human body and black background, and an original image for segmentation).
//                var analyzer = MLAnalyzerFactory.getInstance().imageSegmentationAnalyzer
//
//                // Method 2: Use MLImageSegmentationSetting to customize the image segmentation analyzer.
                var setting = MLImageSegmentationSetting.Factory() // Set whether to support fine segmentation. The value true indicates fine segmentation, and the value false indicates fast segmentation.
                    .setExact(false) // Set the human body segmentation mode.
                    .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG) // Set returned result types.
                    .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
                    .create()
                var analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)
                val mlFrame = MLFrame.fromBitmap(segbitmap)
//                Synchronous Call for Huawei
//                val segmentationResult = analyzer.analyseFrame(mlFrame)

                analyzer.asyncAnalyseFrame(mlFrame)
                    .addOnSuccessListener {
                        val outputBitmap:Bitmap = it.getForeground()
                        val rootDirectory : String = getActivity().getFilesDir().toString()+"/temp_bitmap"
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



    fun bitmapToFloatArray(bitmap: Bitmap):
                Array<Array<Array<FloatArray>>> {
            
            val width: Int = bitmap.width
            val height: Int = bitmap.height
            val intValues = IntArray(width * height)
            bitmap.getPixels(intValues, 0, width, 0, 0, width, height)

            // Create aa array to find the maximum value
            val fourDimensionalArray = Array(1) {
                Array(320) {
                    Array(320) {
                        FloatArray(3)
                    }
                }
            }
            // https://github.com/xuebinqin/U-2-Net/blob/f2b8e4ac1c4fbe90daba8707bca051a0ec830bf6/data_loader.py#L204
            for (i in 0 until width - 1) {
                for (j in 0 until height - 1) {
                    val pixelValue: Int = intValues[i * width + j]
                    fourDimensionalArray[0][i][j][0] =
                        Color.red(pixelValue)
                            .toFloat()
                    fourDimensionalArray[0][i][j][1] =
                        Color.green(pixelValue)
                            .toFloat()
                    fourDimensionalArray[0][i][j][2] =
                        Color.blue(pixelValue).toFloat()
                }

            }
            // Convert multidimensional array to 1D
            val oneDFloatArray = ArrayList<Float>()

            for (m in fourDimensionalArray[0].indices) {
                for (x in fourDimensionalArray[0][0].indices) {
                    for (y in fourDimensionalArray[0][0][0].indices) {
                        oneDFloatArray.add(fourDimensionalArray[0][m][x][y])
                    }
                }
            }

            val maxValue: Float = oneDFloatArray.maxOrNull() ?: 0f
            //val minValue: Float = oneDFloatArray.minOrNull() ?: 0f

            // Final array that is going to be used with interpreter
            val finalFourDimensionalArray = Array(1) {
                Array(320) {
                    Array(320) {
                        FloatArray(3)
                    }
                }
            }
            for (i in 0 until width - 1) {
                for (j in 0 until height - 1) {
                    val pixelValue: Int = intValues[i * width + j]
                    finalFourDimensionalArray[0][i][j][0] =
                        ((Color.red(pixelValue).toFloat() / maxValue) - 0.485f) / 0.229f
                    finalFourDimensionalArray[0][i][j][1] =
                        ((Color.green(pixelValue).toFloat() / maxValue) - 0.456f) / 0.224f
                    finalFourDimensionalArray[0][i][j][2] =
                        ((Color.blue(pixelValue).toFloat() / maxValue) - 0.406f) / 0.225f
                }

            }

            return finalFourDimensionalArray
        }

    private fun saveJPGE_After(bitmap: Bitmap, path: String) {

        val file:File =  File(path);
        try {
            val out: FileOutputStream =  FileOutputStream(file);
            if (bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
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

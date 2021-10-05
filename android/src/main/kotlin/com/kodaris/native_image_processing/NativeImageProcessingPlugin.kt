package com.kodaris.native_image_processing

import android.annotation.TargetApi
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.io.ByteArrayOutputStream
import java.io.InputStream


/** NativeImageProcessingPlugin */
class NativeImageProcessingPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var context : Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    context = flutterPluginBinding.getApplicationContext()
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.kodaris/native_image_processing")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }
      "getBatteryLevel" -> {
        val batterPercentage = getBatteryPercentage(context)
        result.success("${batterPercentage}%");
      }
      "getExifData" -> {
        val filePath: String? = call.argument<String>("filePath")
        if(filePath != null) {
          val exifData = getExifDataFromPath(filePath)
          result.success(exifData);
        } else {
          result.error("404", "File not found", null)
        }
      }
      "bakeOrientation" -> {
        val filePath: String? = call.argument<String>("filePath")
        if(filePath != null){
          val bmp = bakeOrientation(filePath)
          result.success(getByteArray(bmp))
        }else {
          result.error("404", "File not found", null)
        }
      }
      else -> result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  fun getBatteryPercentage(context: Context): Int {
    return if (Build.VERSION.SDK_INT >= 21) {
      val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
      bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

    } else {
      val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
      val batteryStatus = context.registerReceiver(null, iFilter)
      val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
      val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
      val batteryPct = level!! / scale?.toDouble()!!
      (batteryPct * 100).toInt()
    }
  }

  @TargetApi(Build.VERSION_CODES.N)
  fun getExifData(uri: Uri): ExifInterface? {
    val inputStream: InputStream?
    try {
      inputStream = context.contentResolver.openInputStream(uri)
      return inputStream?.let { ExifInterface(it) }
    } catch (ex: Exception) {
      return null
    }
  }

  fun getExifDataFromPath(filePath: String): HashMap<String, String>? {
    val exif = ExifInterface(filePath)
    val tagsToCheck = arrayOf(
      ExifInterface.TAG_DATETIME,
      ExifInterface.TAG_GPS_LATITUDE,
      ExifInterface.TAG_GPS_LONGITUDE,
      ExifInterface.TAG_EXPOSURE_TIME,
      ExifInterface.TAG_ORIENTATION
    )
    val hashMap = HashMap<String, String>()
    for (tag in tagsToCheck)
      exif.getAttribute(tag)?.let { hashMap[tag] = it }
    return hashMap;
  }

  fun bakeOrientation(filePath: String): Bitmap {
    //Get exif to check orientation data
    val exif = ExifInterface(filePath)
    var orientationData = exif.getAttribute(ExifInterface.TAG_ORIENTATION)
    val bitmap: Bitmap = BitmapFactory.decodeFile(filePath)
    when(orientationData){
      "2" -> {
        return flipHorizontal(bitmap)!!
      }
      "3" -> {
        return flipHorizontal(bitmap)?.let { flipVertical(it) }!!
      }
      "4" -> {
        return rotate(bitmap, 180f)?.let { flipHorizontal(it) }!!
      }
      "5" -> {
        return rotate(bitmap, 90f)?.let { flipHorizontal(it) }!!
      }
      "6" -> {
        return rotate(bitmap, 90f)!!
      }
      "7" -> {
        return rotate(bitmap, -90f)?.let { flipHorizontal(it) }!!
      }
      "8" -> {
        return rotate(bitmap, -90f)!!
      }
    }

    return bitmap
  }

  fun flipHorizontal(bitmap: Bitmap): Bitmap? {
    val matrix: Matrix = Matrix()
    matrix.setScale(-1.0f, 1.0f)
    matrix.postTranslate(bitmap.width.toFloat(), 0f)
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
  }
  fun flipVertical(bitmap: Bitmap): Bitmap? {
    val matrix: Matrix = Matrix()
    matrix.setScale(1.0f, -1.0f)
    matrix.postTranslate(0f, bitmap.height.toFloat())
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
  }

  fun rotate(bitmap: Bitmap, angle: Float): Bitmap? {
    val matrix: Matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(bitmap,0,0,bitmap.width, bitmap.height, matrix, true)
  }

  fun getByteArray(bmp: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    return stream.toByteArray()
  }
}

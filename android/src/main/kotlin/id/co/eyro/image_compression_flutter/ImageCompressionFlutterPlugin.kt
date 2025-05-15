package id.co.eyro.image_compression_flutter

import androidx.annotation.NonNull
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** ImageCompressionFlutterPlugin */
class ImageCompressionFlutterPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private var showLog: Boolean = false

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "image_compression_flutter")
    channel.setMethodCallHandler(this)
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when (call.method) {
      "showLog" -> {
        showLog = call.arguments as Boolean
        result.success(null)
      }
      "compressWithList" -> compressWithList(call, result)
      "compressWithFile" -> compressWithFile(call, result)
      "compressWithFileAndGetFile" -> compressWithFileAndGetFile(call, result)
      else -> result.notImplemented()
    }
  }

  private fun compressWithList(call: MethodCall, result: Result) {
    try {
      val args = call.arguments as List<*>
      val bytes = args[0] as ByteArray
      val minWidth = args[1] as Int
      val minHeight = args[2] as Int
      val quality = args[3] as Int
      val rotate = args[4] as Int
      val autoCorrectionAngle = args[5] as Boolean
      val format = args[6] as Int
      val keepExif = args[7] as Boolean
      val inSampleSize = args[8] as Int

      // Decode with inSampleSize for memory efficiency
      val options = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
      }
      var bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
      
      // Resize the bitmap if needed
      bitmap = resizeBitmap(bitmap, minWidth, minHeight)
      
      // Apply rotation if needed
      if (rotate != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
      }
      
      val outputStream = ByteArrayOutputStream()
      
      // Apply compression
      val bitmapFormat = when (format) {
        0 -> Bitmap.CompressFormat.JPEG
        1 -> Bitmap.CompressFormat.PNG
        2 -> {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
          } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
          }
        }
        else -> Bitmap.CompressFormat.JPEG
      }
      
      bitmap.compress(bitmapFormat, quality, outputStream)
      
      val resultBytes = outputStream.toByteArray()
      result.success(resultBytes)
    } catch (e: Exception) {
      if (showLog) {
        Log.e("ImageCompression", "Error compressing image", e)
      }
      result.error("compression_error", e.message, null)
    }
  }

  private fun compressWithFile(call: MethodCall, result: Result) {
    try {
      val args = call.arguments as List<*>
      val path = args[0] as String
      val minWidth = args[1] as Int
      val minHeight = args[2] as Int
      val quality = args[3] as Int
      val rotate = args[4] as Int
      val autoCorrectionAngle = args[5] as Boolean
      val format = args[6] as Int
      val keepExif = args[7] as Boolean
      val inSampleSize = args[8] as Int
      
      val file = File(path)
      if (!file.exists()) {
        result.error("file_not_found", "The file does not exist", null)
        return
      }
      
      // Decode with inSampleSize for memory efficiency
      val options = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
      }
      var bitmap = BitmapFactory.decodeFile(path, options)
      
      // Resize the bitmap if needed
      bitmap = resizeBitmap(bitmap, minWidth, minHeight)
      
      // Apply rotation if needed
      if (rotate != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
      }
      
      val outputStream = ByteArrayOutputStream()
      
      // Apply compression
      val bitmapFormat = when (format) {
        0 -> Bitmap.CompressFormat.JPEG
        1 -> Bitmap.CompressFormat.PNG
        2 -> {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
          } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
          }
        }
        else -> Bitmap.CompressFormat.JPEG
      }
      
      bitmap.compress(bitmapFormat, quality, outputStream)
      
      val resultBytes = outputStream.toByteArray()
      result.success(resultBytes)
    } catch (e: Exception) {
      if (showLog) {
        Log.e("ImageCompression", "Error compressing image", e)
      }
      result.error("compression_error", e.message, null)
    }
  }

  private fun compressWithFileAndGetFile(call: MethodCall, result: Result) {
    try {
      val args = call.arguments as List<*>
      val path = args[0] as String
      val minWidth = args[1] as Int
      val minHeight = args[2] as Int
      val quality = args[3] as Int
      val targetPath = args[4] as String
      val rotate = args[5] as Int
      val autoCorrectionAngle = args[6] as Boolean
      val format = args[7] as Int
      val keepExif = args[8] as Boolean
      val inSampleSize = args[9] as Int
      
      val file = File(path)
      if (!file.exists()) {
        result.error("file_not_found", "The file does not exist", null)
        return
      }
      
      // Decode with inSampleSize for memory efficiency
      val options = BitmapFactory.Options().apply {
        this.inSampleSize = inSampleSize
      }
      var bitmap = BitmapFactory.decodeFile(path, options)
      
      // Resize the bitmap if needed
      bitmap = resizeBitmap(bitmap, minWidth, minHeight)
      
      // Apply rotation if needed
      if (rotate != 0) {
        val matrix = Matrix()
        matrix.postRotate(rotate.toFloat())
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
      }
      
      val targetFile = File(targetPath)
      
      // Ensure directory exists
      val dir = targetFile.parentFile
      if (dir != null && !dir.exists()) {
        dir.mkdirs()
      }
      
      val outputStream = FileOutputStream(targetFile)
      
      // Apply compression
      val bitmapFormat = when (format) {
        0 -> Bitmap.CompressFormat.JPEG
        1 -> Bitmap.CompressFormat.PNG
        2 -> {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            Bitmap.CompressFormat.WEBP_LOSSY
          } else {
            @Suppress("DEPRECATION")
            Bitmap.CompressFormat.WEBP
          }
        }
        else -> Bitmap.CompressFormat.JPEG
      }
      
      bitmap.compress(bitmapFormat, quality, outputStream)
      outputStream.flush()
      outputStream.close()
      
      result.success(targetPath)
    } catch (e: Exception) {
      if (showLog) {
        Log.e("ImageCompression", "Error compressing image", e)
      }
      result.error("compression_error", e.message, null)
    }
  }
  
  private fun resizeBitmap(bitmap: Bitmap, minWidth: Int, minHeight: Int): Bitmap {
    val width = bitmap.width
    val height = bitmap.height
    
    // If the bitmap is already smaller than the desired dimensions, don't resize
    if (width <= minWidth && height <= minHeight) {
      return bitmap
    }
    
    val aspectRatio = width.toFloat() / height.toFloat()
    
    var newWidth = minWidth
    var newHeight = (newWidth / aspectRatio).toInt()
    
    // If height is too large, constrain by height instead
    if (newHeight > minHeight) {
      newHeight = minHeight
      newWidth = (newHeight * aspectRatio).toInt()
    }
    
    return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}

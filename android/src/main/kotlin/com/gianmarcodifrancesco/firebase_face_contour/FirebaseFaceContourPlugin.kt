package com.gianmarcodifrancesco.firebase_face_contour

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri

import java.io.File
import java.io.IOException

import androidx.exifinterface.media.ExifInterface
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FirebaseMlVisionPlugin  */
class FirebaseFaceContourPlugin private constructor(private val registrar: Registrar) : MethodCallHandler {

  override fun onMethodCall(call: MethodCall, result: Result) {
    val options = call.argument<Map<String, Any>>("options")

    val image: FirebaseVisionImage
    val imageData = call.arguments<Map<String, Any>>()
    try {
      image = dataToVisionImage(imageData)
    } catch (exception: IOException) {
      result.error("MLVisionDetectorIOError", exception.localizedMessage, null)
      return
    }

    when (call.method) {
      "FaceDetector#processImage" -> FaceDetector.instance.handleDetection(image, options, result)
      else -> result.notImplemented()
    }
  }

  @Throws(IOException::class)
  private fun dataToVisionImage(imageData: Map<String, Any>): FirebaseVisionImage {
    val imageType = imageData["type"] as String
    assert(imageType != null)

    when (imageType) {
      "file" -> {
        val imageFilePath = imageData["path"] as String
        val rotation = getImageExifOrientation(imageFilePath)

        if (rotation == 0) {
          val file = File(imageFilePath)
          return FirebaseVisionImage.fromFilePath(registrar.context(), Uri.fromFile(file))
        }

        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())

        val bitmap = BitmapFactory.decodeFile(imageFilePath)
        val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

        return FirebaseVisionImage.fromBitmap(rotatedBitmap)
      }
      "bytes" -> {
        val metadataData = imageData["metadata"] as Map<String, Any>

        val metadata = FirebaseVisionImageMetadata.Builder()
                .setWidth((metadataData["width"] as Double).toInt())
                .setHeight((metadataData["height"] as Double).toInt())
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(getRotation(metadataData["rotation"] as Int))
                .build()

        val bytes = imageData["bytes"] as ByteArray
        assert(bytes != null)

        return FirebaseVisionImage.fromByteArray(bytes, metadata)
      }
      else -> throw IllegalArgumentException(String.format("No image type for: %s", imageType))
    }
  }

  @Throws(IOException::class)
  private fun getImageExifOrientation(imageFilePath: String): Int {
    val exif = ExifInterface(imageFilePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

    when (orientation) {
      ExifInterface.ORIENTATION_ROTATE_90 -> return 90
      ExifInterface.ORIENTATION_ROTATE_180 -> return 180
      ExifInterface.ORIENTATION_ROTATE_270 -> return 270
      else -> return 0
    }
  }

  private fun getRotation(rotation: Int): Int {
    when (rotation) {
      0 -> return FirebaseVisionImageMetadata.ROTATION_0
      90 -> return FirebaseVisionImageMetadata.ROTATION_90
      180 -> return FirebaseVisionImageMetadata.ROTATION_180
      270 -> return FirebaseVisionImageMetadata.ROTATION_270
      else -> throw IllegalArgumentException(String.format("No rotation for: %d", rotation))
    }
  }

  companion object {

    /** Plugin registration.  */
    @JvmStatic
    fun registerWith(registrar: PluginRegistry.Registrar) {
      val channel = MethodChannel(registrar.messenger(), "plugins.flutter.io/firebase_ml_vision")
      channel.setMethodCallHandler(FirebaseFaceContourPlugin(registrar))
    }
  }
}

package com.gianmarcodifrancesco.firebase_face_contour;

import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.Map;

import io.flutter.plugin.common.MethodChannel;

interface Detector {
  void handleDetection(
          FirebaseVisionImage image, Map<String, Object> options, final MethodChannel.Result result);
}

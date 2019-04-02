import 'package:firebase_face_contour_example/face_contour_detection.dart';
import 'package:flutter/material.dart';

void main() => runApp(MyApp());

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Face Contour Detection',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: FaceContourDetectionScreen(),
    );
  }
}

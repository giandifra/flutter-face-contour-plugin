# Flutter Face Contour Detection Plugin

<img src="https://github.com/giandifra/flutter-face-contour-detection/blob/master/long_preview.gif" data-canonical-src="https://github.com/giandifra/flutter-face-contour-detection/blob/master/long_preview.gif" width="200" height="400" />

Only for Android (for now)

### Example

```dart
import 'package:camera/camera.dart';
import 'package:firebase_face_contour_example/face_contour_painter.dart';
import 'package:firebase_face_contour_example/utils.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:firebase_face_contour/firebase_face_contour.dart';

class FaceContourDetectionScreen extends StatefulWidget {
  @override
  _FaceContourDetectionScreenState createState() =>
      _FaceContourDetectionScreenState();
}

class _FaceContourDetectionScreenState
    extends State<FaceContourDetectionScreen> {
  final FaceDetector faceDetector = FirebaseVision.instance.faceDetector(
      FaceDetectorOptions(
          enableClassification: false,
          enableLandmarks: false,
          enableContours: true,
          enableTracking: false));
  List<Face> faces;
  CameraController _camera;
  bool cameraEnabled = true;
  bool _isDetecting = false;
  CameraLensDirection _direction = CameraLensDirection.back;

  @override
  void initState() {
    super.initState();
    _initializeCamera();
  }

  void _initializeCamera() async {
    CameraDescription description = await getCamera(_direction);
    ImageRotation rotation = rotationIntToImageRotation(
      description.sensorOrientation,
    );
    
    _camera = CameraController(
      description,
      defaultTargetPlatform == TargetPlatform.iOS
          ? ResolutionPreset.low
          : ResolutionPreset.medium,
    );
    await _camera.initialize();

    print("initialize Camera");
    print(description.lensDirection);
    print(_camera.description.lensDirection);

    _camera.startImageStream((CameraImage image) {
      if (_isDetecting) return;

      _isDetecting = true;

      detect(image, faceDetector.processImage, rotation).then(
        (dynamic result) {
          setState(() {
            faces = result;
          });

          _isDetecting = false;
        },
      ).catchError(
        (_) {
          _isDetecting = false;
        },
      );
    });
  }

  void _toggleCameraDirection() async {
    if (_direction == CameraLensDirection.back) {
      _direction = CameraLensDirection.front;
    } else {
      _direction = CameraLensDirection.back;
    }

    await _camera.stopImageStream();
    await _camera.dispose();

    setState(() {
      _camera = null;
    });

    _initializeCamera();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text("Flutter Face Contour Detection"),
        actions: <Widget>[
//          Center(child: Text(faces.length.toString() ?? '0')),
          IconButton(
              icon:
                  Icon(cameraEnabled ? Icons.visibility : Icons.visibility_off),
              onPressed: () {
                setState(() {
                  cameraEnabled = !cameraEnabled;
                });
              })
        ],
      ),
      body: _camera == null
          ? const Center(
              child: Text(
                'Initializing Camera...',
                style: TextStyle(
                  color: Colors.green,
                  fontSize: 30.0,
                ),
              ),
            )
          : LiveCameraWithFaceDetection(
              faces: faces,
              camera: _camera,
              cameraEnabled: cameraEnabled,
            ),
      floatingActionButton: FloatingActionButton(
        onPressed: _toggleCameraDirection,
        child: _direction == CameraLensDirection.back
            ? const Icon(Icons.camera_front)
            : const Icon(Icons.camera_rear),
      ),
    );
  }
}

class LiveCameraWithFaceDetection extends StatelessWidget {
  final List<Face> faces;
  final CameraController camera;
  final bool cameraEnabled;

  const LiveCameraWithFaceDetection(
      {Key key, this.faces, this.camera, this.cameraEnabled = true})
      : super(key: key);

  @override
  Widget build(BuildContext context) {
    print(camera.description.lensDirection);
    return Container(
      constraints: const BoxConstraints.expand(),
      child: Stack(
        fit: StackFit.expand,
        children: <Widget>[
          cameraEnabled
              ? CameraPreview(camera)
              : Container(
                  color: Colors.black,
                ),
          (faces != null && camera.value.isInitialized)
              ? CustomPaint(
                  painter: FaceContourPainter(
                      Size(
                        camera.value.previewSize.height,
                        camera.value.previewSize.width,
                      ),
                      faces,
                      camera.description.lensDirection),
                )
              : const Text('No results!'),
        ],
      ),
    );
  }
}
```
```dart
import 'dart:ui';
import 'package:camera/camera.dart';
import 'package:firebase_face_contour/firebase_face_contour.dart';
import 'package:flutter/material.dart';

class FacePaint extends CustomPaint {
  final CustomPainter painter;

  FacePaint({this.painter}) : super(painter: painter);
}

class FaceContourPainter extends CustomPainter {
  final Size imageSize;
  final List<Face> faces;
  final CameraLensDirection cameraLensDirection;

  FaceContourPainter(this.imageSize, this.faces, this.cameraLensDirection);

  @override
  void paint(Canvas canvas, Size size) {
    final paintRectStyle = Paint()
      ..color = Colors.red
      ..strokeWidth = 5.0
      ..style = PaintingStyle.stroke;

    final paint = Paint()..color = Colors.yellow;

    for (var i = 0; i < faces.length; i++) {

      //Scale rect to image size
      final rect = _scaleRect(
        rect: faces[i].boundingBox,
        imageSize: imageSize,
        widgetSize: size,
      );

//      canvas.drawRect(rect, paintRectStyle);

      final List<Offset> facePoints =
          faces[i].getContour(FaceContourType.face).points;
      final List<Offset> lowerLipBottom =
          faces[i].getContour(FaceContourType.lowerLipBottom).points;
      final List<Offset> lowerLipTop =
          faces[i].getContour(FaceContourType.lowerLipTop).points;
      final List<Offset> upperLipBottom =
          faces[i].getContour(FaceContourType.upperLipBottom).points;
      final List<Offset> upperLipTop =
          faces[i].getContour(FaceContourType.upperLipTop).points;
      final List<Offset> leftEyebrowBottom =
          faces[i].getContour(FaceContourType.leftEyebrowBottom).points;
      final List<Offset> leftEyebrowTop =
          faces[i].getContour(FaceContourType.leftEyebrowTop).points;
      final List<Offset> rightEyebrowBottom =
          faces[i].getContour(FaceContourType.rightEyebrowBottom).points;
      final List<Offset> rightEyebrowTop =
          faces[i].getContour(FaceContourType.rightEyebrowTop).points;
      final List<Offset> leftEye =
          faces[i].getContour(FaceContourType.leftEye).points;
      final List<Offset> rightEye =
          faces[i].getContour(FaceContourType.rightEye).points;
      final List<Offset> noseBottom =
          faces[i].getContour(FaceContourType.noseBottom).points;
      final List<Offset> noseBridge =
          faces[i].getContour(FaceContourType.noseBridge).points;


      final lipPaint = Paint()..strokeWidth = 3.0..color = Colors.pink;

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: lowerLipBottom, imageSize: imageSize, widgetSize: size),
          lipPaint);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: lowerLipTop, imageSize: imageSize, widgetSize: size),
          lipPaint );

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: upperLipBottom, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.green);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: upperLipTop, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.green);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: leftEyebrowBottom,
              imageSize: imageSize,
              widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.brown);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: leftEyebrowTop, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.brown);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: rightEyebrowBottom,
              imageSize: imageSize,
              widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.brown);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: rightEyebrowTop, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.brown);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: leftEye, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.blue);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: rightEye, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.blue);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: noseBottom, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.greenAccent);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: noseBridge, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.greenAccent);

      canvas.drawPoints(
          PointMode.polygon,
          _scalePoints(
              offsets: facePoints, imageSize: imageSize, widgetSize: size),
          Paint()
            ..strokeWidth = 3.0
            ..color = Colors.white);
    }
  }

  Offset _scalePoint({
    Offset offset,
    @required Size imageSize,
    @required Size widgetSize,
  }) {
    final double scaleX = widgetSize.width / imageSize.width;
    final double scaleY = widgetSize.height / imageSize.height;

    if(cameraLensDirection == CameraLensDirection.front){
      return Offset(widgetSize.width - (offset.dx * scaleX), offset.dy * scaleY);
    }
    return Offset(offset.dx * scaleX, offset.dy * scaleY);
  }

  List<Offset> _scalePoints({
    List<Offset> offsets,
    @required Size imageSize,
    @required Size widgetSize,
  }) {
    final double scaleX = widgetSize.width / imageSize.width;
    final double scaleY = widgetSize.height / imageSize.height;

    if(cameraLensDirection == CameraLensDirection.front){
      return offsets
          .map((offset) => Offset(widgetSize.width - (offset.dx * scaleX), offset.dy * scaleY))
          .toList();
    }
    return offsets
        .map((offset) => Offset(offset.dx * scaleX, offset.dy * scaleY))
        .toList();
  }

  Rect _scaleRect({
    @required Rect rect,
    @required Size imageSize,
    @required Size widgetSize,
  }) {
    final double scaleX = widgetSize.width / imageSize.width;
    final double scaleY = widgetSize.height / imageSize.height;

    if(cameraLensDirection == CameraLensDirection.front){
      print("qui");
      return Rect.fromLTRB(
        widgetSize.width - rect.left.toDouble() * scaleX,
        rect.top.toDouble() * scaleY,
        widgetSize.width - rect.right.toDouble() * scaleX,
        rect.bottom.toDouble() * scaleY,
      );
    }

    return Rect.fromLTRB(
      rect.left.toDouble() * scaleX,
      rect.top.toDouble() * scaleY,
      rect.right.toDouble() * scaleX,
      rect.bottom.toDouble() * scaleY,
    );
  }


  @override
  bool shouldRepaint(FaceContourPainter oldDelegate) {
    return imageSize != oldDelegate.imageSize || faces != oldDelegate.faces;
  }
}
```



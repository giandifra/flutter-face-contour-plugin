#import "FirebaseFaceContourPlugin.h"
#import <firebase_face_contour/firebase_face_contour-Swift.h>

@implementation FirebaseFaceContourPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFirebaseFaceContourPlugin registerWithRegistrar:registrar];
}
@end

#import "NativeImageProcessingPlugin.h"
#if __has_include(<native_image_processing/native_image_processing-Swift.h>)
#import <native_image_processing/native_image_processing-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "native_image_processing-Swift.h"
#endif

@implementation NativeImageProcessingPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftNativeImageProcessingPlugin registerWithRegistrar:registrar];
}
@end

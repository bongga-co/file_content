#import "FileContentPlugin.h"
#import <file_content/file_content-Swift.h>

@implementation FileContentPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFileContentPlugin registerWithRegistrar:registrar];
}
@end

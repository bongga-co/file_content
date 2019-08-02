import Flutter
import UIKit
import Photos

public class SwiftFileContentPlugin: NSObject, FlutterPlugin, FlutterStreamHandler {
  static let kMessagesChannel = "plugins.bongga.co/receive";
  static let kEventsChannelMedia = "plugins.bongga.co/event-receive";
  
  private var dataResult: [SharedFile]? = nil    
  private var eventSink: FlutterEventSink? = nil;

  public static func register(with registrar: FlutterPluginRegistrar) {
      let instance = SwiftFileContentPlugin()
      
      let channel = FlutterMethodChannel(name: kMessagesChannel, binaryMessenger: registrar.messenger())
      registrar.addMethodCallDelegate(instance, channel: channel)
      
      let chargingChannelMedia = FlutterEventChannel(name: kEventsChannelMedia, binaryMessenger: registrar.messenger())
      chargingChannelMedia.setStreamHandler(instance)
      
      registrar.addApplicationDelegate(instance)
  }
  
  public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
      
      switch call.method {
      case "getImage":
          result(toJson(data: self.dataResult));
      case "getFile":
          result(toJson(data: self.dataResult));
      case "reset":
          self.dataResult = nil
          result(nil);
      default:
          result(FlutterMethodNotImplemented);
      }
  }
  
  public func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [AnyHashable : Any] = [:]) -> Bool {
      if let url = launchOptions[UIApplication.LaunchOptionsKey.url] as? URL {
          return handleUrl(url: url, isInitial: true)
      } else if let activityDictionary = launchOptions[UIApplication.LaunchOptionsKey.userActivityDictionary] as? [AnyHashable: Any] { //Universal link
          for key in activityDictionary.keys {
              if let userActivity = activityDictionary[key] as? NSUserActivity {
                  if let url = userActivity.webpageURL {
                      return handleUrl(url: url, isInitial: true)
                  }
              }
          }
      }
      return false
  }
  
  public func application(_ application: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
      return handleUrl(url: url, isInitial: false)
  }
  
  public func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([Any]) -> Void) -> Bool {
      return handleUrl(url: userActivity.webpageURL, isInitial: true)
  }
  
  private func handleUrl(url: URL?, isInitial: Bool) -> Bool {
      if let url = url {
          let appDomain = Bundle.main.bundleIdentifier!
          let userDefaults = UserDefaults(suiteName: "group.\(appDomain)")
          if url.fragment == "media" || url.fragment == "file" {
              if let key = url.host?.components(separatedBy: "=").last,
                  let json = userDefaults?.object(forKey: key) as? Data {
                  let sharedArray = decode(data: json)
                  let sharedFiles: [SharedFile] = sharedArray.compactMap{
                      guard let path = getAbsolutePath(for: $0.path) else {
                          return nil
                      }
                      
                      return SharedFile.init(path: path, name: $0.name, size: $0.size)
                  }

                  dataResult = sharedFiles
                  eventSink?(toJson(data: dataResult))
              }
          }
          return true
      }
      
      dataResult = nil
      return false
  }
  
  public func onListen(withArguments arguments: Any?, eventSink events: @escaping FlutterEventSink) -> FlutterError? {
      eventSink = events;
      return nil;
  }
  
  public func onCancel(withArguments arguments: Any?) -> FlutterError? {
      eventSink = nil;
      return nil;
  }
  
  private func getAbsolutePath(for identifier: String) -> String? {
      if (identifier.starts(with: "file://") || identifier.starts(with: "/var/mobile/Media") || identifier.starts(with: "/private/var/mobile")) {
          return identifier.replacingOccurrences(of: "file://", with: "")
      }
      
      return identifier
  }
    
  private func decode(data: Data) -> [SharedFile] {
      let encodedData = try? JSONDecoder().decode([SharedFile].self, from: data)
      return encodedData!
  }
    
  private func toJson(data: [SharedFile]?) -> String? {
      if data == nil {
          return nil
      }
      let encodedData = try? JSONEncoder().encode(data)
        let json = String(data: encodedData!, encoding: .utf8)!
      return json
  }
    
  class SharedFile: Codable {
    var path: String;
    var name: String?;
    var size: Double?;
    
    init(path: String, name: String?, size: Double?) {
      self.path = path
      self.name = name
      self.size = size
    }
  }
}

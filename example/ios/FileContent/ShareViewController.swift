//
//  ShareViewController.swift
//  FileContent
//
//  Created by Bongga | On the Go on 7/31/19.
//  Copyright © 2019 The Chromium Authors. All rights reserved.
//

import UIKit
import Social
import MobileCoreServices
import Photos

class ShareViewController: SLComposeServiceViewController {
    
    enum RedirectType {
        case media
        case text
    }
    
    let hostAppBundleIdentifier = "co.bongga.fileContentExample"
    let sharedKey = "ShareKey"
    var sharedMedia: [SharedFile] = []
    let imageContentType = kUTTypeImage as String

    override func isContentValid() -> Bool {
        return true
    }
    
    override func viewDidLoad() {
        if let content = extensionContext!.inputItems[0] as? NSExtensionItem {
            if let contents = content.attachments {
                for (index, attachment) in (contents).enumerated() {
                    
                    if attachment.hasItemConformingToTypeIdentifier(imageContentType) {
                        handleImages(content: content, attachment: attachment, index: index)
                    }
                }
            }
        }
    }

    override func didSelectPost() {
        self.extensionContext!.completeRequest(returningItems: [], completionHandler: nil)
    }

    override func configurationItems() -> [Any]! {
        return []
    }
    
    private func handleImages (content: NSExtensionItem, attachment: NSItemProvider, index: Int) {
        attachment.loadItem(forTypeIdentifier: imageContentType, options: nil) { [weak self] data, error in
            
            if error == nil, let url = data as? URL, let this = self {
                
                // Always copy
                var fileExtension = url.lastPathComponent.components(separatedBy: ".")[safe: 1]
                if fileExtension == nil {
                    fileExtension = "png"
                }
                let newName = UUID().uuidString
                let newPath = FileManager.default
                    .containerURL(forSecurityApplicationGroupIdentifier: "group.\(this.hostAppBundleIdentifier)")!
                    .appendingPathComponent("\(newName).\(fileExtension!)")
                let copied = this.copyFile(at: url, to: newPath)
                if(copied) {
                    this.sharedMedia.append(SharedFile(name: nil, size: 0, path: newPath.absoluteString))
                }
                
                // If this is the last item, save imagesData in userDefaults and redirect to host app
                if index == (content.attachments?.count)! - 1 {
                    let userDefaults = UserDefaults(suiteName: "group.\(this.hostAppBundleIdentifier)")
                    userDefaults?.set(this.toData(data: this.sharedMedia), forKey: this.sharedKey)
                    userDefaults?.synchronize()
                    this.extensionContext!.completeRequest(returningItems: [], completionHandler: nil)
                    this.redirectToHostApp(type: .media)
                }
                
            } else {
                print("GETTING ERROR")
                let alert = UIAlertController(title: "Error", message: "Error loading image", preferredStyle: .alert)
                
                let action = UIAlertAction(title: "Error", style: .cancel) { _ in
                    self?.dismiss(animated: true, completion: nil)
                }
                
                alert.addAction(action)
                self?.present(alert, animated: true, completion: nil)
                self?.extensionContext!.completeRequest(returningItems: [], completionHandler: nil)
            }
        }
    }
    
    private func dismissWithError(){
        let alert = UIAlertController(title: "Error", message: "Error loading image", preferredStyle: .alert)
        
        let action = UIAlertAction(title: "Error", style: .cancel) { _ in
            self.dismiss(animated: true, completion: nil)
        }
        
        alert.addAction(action)
        present(alert, animated: true, completion: nil)
        extensionContext!.completeRequest(returningItems: [], completionHandler: nil)
    }
    
    private func redirectToHostApp(type: RedirectType) {
        let url = URL(string: "SharePhotos://dataUrl=\(sharedKey)#\(type)")
        var responder = self as UIResponder?
        let selectorOpenURL = sel_registerName("openURL:")
        
        while (responder != nil) {
            if (responder?.responds(to: selectorOpenURL))! {
                let _ = responder?.perform(selectorOpenURL, with: url)
            }
            responder = responder!.next
        }
    }
    
    func copyFile(at srcURL: URL, to dstURL: URL) -> Bool {
        do {
            if FileManager.default.fileExists(atPath: dstURL.path) {
                try FileManager.default.removeItem(at: dstURL)
            }
            try FileManager.default.copyItem(at: srcURL, to: dstURL)
        } catch ( _) {
            return false
        }
        return true
    }
    
    private func getThumbnailPath(for url: URL) -> URL {
        let fileName = Data(url.lastPathComponent.utf8).base64EncodedString().replacingOccurrences(of: "==", with: "")
        let path = FileManager.default
            .containerURL(forSecurityApplicationGroupIdentifier: "group.\(hostAppBundleIdentifier)")!
            .appendingPathComponent("\(fileName).jpg")
        return path
    }
    
    func toData(data: [SharedFile]) -> Data {
        let encodedData = try? JSONEncoder().encode(data)
        return encodedData!
    }
    
    // TODO: refactor
    class SharedFile: Codable {
        var path: String;
        var name: String?;
        var size: Double?;
        
        init(name: String?, size: Double?, path: String) {
            self.path = path
            self.name = name
            self.size = size
        }
    }
}

extension Array {
    subscript (safe index: UInt) -> Element? {
        return Int(index) < count ? self[Int(index)] : nil
    }
}

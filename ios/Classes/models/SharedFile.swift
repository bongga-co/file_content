class SharedFile: Codable {
    var name: String?;
    var size: Double?;
    var path: String;
    
    init(name: String?, size: Double?, path: String) {
        self.name = name
        self.size = size
        self.path = path
    }
}
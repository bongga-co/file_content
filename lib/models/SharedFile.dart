class SharedFile {
  final String path;
  final String name;
  final int size;

  SharedFile(this.path, this.name, this.size);

  SharedFile.fromJson(Map<String, dynamic> json)
      : path = json['path'],
        name = json['name'],
        size = json['size'];
}
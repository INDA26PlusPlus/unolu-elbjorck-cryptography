trait Response(val responseCode: Int)

case class ResUpload(val hashArray: Array[Array[Byte]]) extends Response(67)
case class ResDownload(val hashArray: Array[Array[Byte]], val fileData: Array[Byte]) extends Response(68)
case class ResListFiles(val files: Array[String]) extends Response(69)
case class ResFileInfo(val fileInfo: FileInfo) extends Response(70)



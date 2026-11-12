trait Request(val requestCode: Int)

case class ReqUpload(val fileInfo: FileInfo, val fileData: Array[Byte]) extends Request(1)
case class ReqDownload(val fileName: String) extends Request(2)
case class ReqListFiles() extends Request(3)
case class ReqFileInfo(val fileName: String) extends Request(4)



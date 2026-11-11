import java.net.ServerSocket
import java.net.Socket
@main def entrypoint(): Unit =
  println("Starting Server...")
  val servSock = ServerSocket(6767)
  val fstore = FileStorage()

  println("Server socket created")

  val oneSock = servSock.accept()

  println("Connected to " + oneSock.getInetAddress())

  val serConn = ServerConnection(oneSock)
  while (true)
    val req = readRequest(serConn)
    val res: Response = req match
      case ReqUpload(fileInfo, fileData) => {
        val hashes = fstore.uploadFile(fileInfo, fileData)
        ResUpload(hashes)
      }
      case ReqDownload(fileName) => {
        val (data, hashes) = fstore.downloadFile(fileName)
        ResDownload(hashes, data)
      }
      case ReqListFiles() => {
        val files = fstore.listFiles()
        ResListFiles(files)
      }

      case ReqFileInfo(fileName) => {
        val finfo = fstore.fileInfo(fileName)
        ResFileInfo(finfo)
      }
      case _ => throw Exception("WAAAAAAA")
    sendResponse(serConn, res)
   
    
  
  
  



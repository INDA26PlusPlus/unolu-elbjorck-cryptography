import java.net.Socket
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

sealed class ServerConnection(val socket: Socket):
    val istream: InputStream = socket.getInputStream()
    val ostream: OutputStream = socket.getOutputStream()

def sendResponse(sc: ServerConnection, response: Response) =  
    val message: ByteArrayOutputStream = ByteArrayOutputStream()  
    message.write(response.responseCode)
    response match
        case ResUpload(hashArray) => {
            message.write(hashArray.size)
            for (h <- hashArray)
                message.write(h)
        } 
        
        case ResDownload(hashArray, fileData) => {
            message.write(hashArray.size)
            message.write(ByteBuffer.allocate(4).putInt(fileData.size).array())
            for (h <- hashArray)
                message.write(h)
            message.write(fileData)
        } 

        case ResListFiles(files) => {
            var sz = 0
            for (fname <- files)
                sz = fname.length() + 1
            message.write(ByteBuffer.allocate(4).putInt(sz).array())
            for (fname <- files)
                message.write(fname.getBytes(StandardCharsets.US_ASCII))
                message.write(10) // \n
        }

        case ResFileInfo(fileInfo) => {
            message.write(fileInfo.fileName.length())
            message.write(fileInfo.fileName.getBytes(StandardCharsets.US_ASCII))
            message.write(
                ByteBuffer.allocate(16)
                .putLong(fileInfo.timeCreated)
                .putLong(fileInfo.timeUpdated)
                .array()
            )
            message.write(fileInfo.signature)
            message.write(fileInfo.nonce)
        }
        case _: Response => throw Exception("Knas") 
    println("SEND: " + message.toByteArray())
    sc.ostream.write(message.toByteArray())

def readRequest(sc: ServerConnection): Request =    
    sc.istream.read() match
        case 1 => { // Upload
            val signature: Array[Byte] = sc.istream.readNBytes(32)
            val nonce: Array[Byte] = sc.istream.readNBytes(32)
            val fnameSize: Int = sc.istream.read()
            val fileDataSize: Int = ByteBuffer.wrap(sc.istream.readNBytes(4)).getInt()
            val fname: String = String(sc.istream.readNBytes(fnameSize))
            val data: Array[Byte] = sc.istream.readNBytes(fileDataSize)
            val now: Long = System.currentTimeMillis()/1000
            ReqUpload(FileInfo(fname, signature, nonce, now, now), data)
        }
        case 2 => { // Download
            val fnameSize: Int = sc.istream.read()
            val fname: String = String(sc.istream.readNBytes(fnameSize))
            ReqDownload(fname)
        }
        case 3 => { // List Files
            ReqListFiles()
        }
        case 4 => { // Get file info
            val fnameSize: Int = sc.istream.read()
            val fname: String = String(sc.istream.readNBytes(fnameSize))
            ReqFileInfo(fname)
        }
        case _ => throw Exception("Ännu mer knas")
    

    

    






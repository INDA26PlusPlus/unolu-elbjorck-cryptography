import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.sql.PreparedStatement
import scala.util.Try
import scala.util.Success
import java.util.Date
import java.util.Calendar
import scala.collection.mutable.ArrayBuffer

/*

fname
last updated
created
hmac
nonce
*/
object FileCatalogue:
    def create(): Option[FileCatalogue] =
        Some(FileCatalogue("skbidi"))

class FileCatalogue private (databasePath: String):
    val files: collection.mutable.Map[String, FileInfo] = collection.mutable.Map()
    
    def uploadFile(fileInfo: FileInfo) =
        files(fileInfo.fileName) = fileInfo
    
    def listFiles(): Array[String] = 
        files.keys.toArray
    def fileInfo(fileName: String): FileInfo =
        files(fileName)

    /*
    private val databaseConnection: Connection = DriverManager.getConnection(s"jdbc:sqlite:$databasePath")
    private val uploadFileStatement: PreparedStatement = 
        databaseConnection.prepareStatement(
            """
            INSERT INTO FileCatalogue (FileName, Signature, Nonce, TimeUpdated, TimeCreated) 
            VALUES (?, ?, ?, ?, ?) 
            ON CONFLICT(FileName) DO UPDATE SET 
            TimeUpdated = excluded.TimeUpdated,
            Signature = excluded.Signature;
            """
        )
    private val listFilesStatement: PreparedStatement = databaseConnection.prepareStatement("SELECT FileName FROM FileCatalogue;")
    private val fileInfoStatement: PreparedStatement = databaseConnection.prepareStatement("SELECT * FROM FileCatalogue WHERE FileName = ? LIMIT 1;")

    def create(databasePath: String): Option[FileCatalogue] =
        try
          Some(FileCatalogue(databasePath)) 
        catch
            _ => None
    
    def uploadFile(fileInfo: FileInfo) =
        uploadFileStatement.setString(1, fileInfo.fileName)        
        uploadFileStatement.setBytes(2, fileInfo.signature)
        uploadFileStatement.setBytes(3, fileInfo.nonce)
        uploadFileStatement.setLong(4, fileInfo.timeUpdated)
        uploadFileStatement.setLong(5, fileInfo.timeCreated)
        uploadFileStatement.execute()
    
    def listFiles(): Array[String] = 
        val res = listFilesStatement.executeQuery()
        val fileNames: ArrayBuffer[String] = new ArrayBuffer() 
        while res.next() do 
            fileNames.append(res.getString(1)) 
        fileNames.toArray
    
    def fileInfo(fileName: String): FileInfo =
        fileInfoStatement.setString(1, fileName)
        val res = fileInfoStatement.executeQuery()
        FileInfo(res.getString(1), res.getBytes(2), res.getBytes(3), res.getLong(4), res.getLong(5))
        */


        



        



    

  

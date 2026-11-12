import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

// Message digest
// SHA-3

public class Client {

    private Socket socket;
    private BufferedInputStream inputStream;
    private BufferedOutputStream outputStream;

    private static String secretKey = "123456";

    public static void main(String[] args) {

        Client client = new Client();

        // try {
        //     ServerSocket server = new ServerSocket(6767);
        //     Socket clientSocket = server.accept();
        //     System.out.println("Accepted");
        // } catch (Exception e) {
        // }

        System.out.println("Starting connect");
        client.connectToServer("192.168.191.33");
        System.out.println("Connected");

    }

    private String getFileData(String fileName) {
        File file = new File("src/" + fileName);
        String content = "";
        try {
            Scanner fileReader = new Scanner(file);
            while (fileReader.hasNext()) {
                content += fileReader.nextLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }

    private void uploadFile(String fileName) {

        ArrayList<Byte> outData = new ArrayList<>();

        String fileData = getFileData(fileName);

        byte mByte;
        mByte = 1;
        outData.add(mByte);

        byte[] hmac;
        hmac = HmacUtil.initHmac(HmacUtil.hmacSHA256Algorithm, fileData, secretKey);
        for (byte b : hmac) {
            outData.add(b);
        }

        byte[] nonce;
        nonce = HmacUtil.genereateSecureNonce();
        for (byte b : nonce) {
            outData.add(b);
        }

        byte fileNameSize;
        if (fileName.getBytes().length < 128) {
            fileNameSize = (byte) fileName.getBytes().length;
            outData.add(fileNameSize);
        } else {
            return;
        }

        byte[] dataSize;
        dataSize = HmacUtil.intToBytes(fileData.getBytes().length);
        for (byte b : dataSize) {
            outData.add(b);
        }

        byte[] fileNameBytes;
        fileNameBytes = fileName.getBytes();
        for (byte b : fileNameBytes) {
            outData.add(b);
        }

        byte[] data;
        data = fileData.getBytes();
        for (byte b : data) {
            outData.add(b);
        }

        byte[] outArray = new byte[outData.size()];
        for (int i = 0; i < outArray.length; i++) {
            outArray[i] = outData.get(i);
        }

        try {
            outputStream.write(outArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void connectToServer(String ip) {
        try {

            socket = new Socket(ip, 6767);
            // System.out.println("Established socket");
            // outputStream = new BufferedOutputStream(socket.getOutputStream());
            // System.out.println("Connected out stream to server");
            // inputStream = new BufferedInputStream(socket.getInputStream());

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

}
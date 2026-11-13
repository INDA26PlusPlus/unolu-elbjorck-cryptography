import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import javax.crypto.SecretKey;

// Message digest
// SHA-3

public class Client implements Runnable {

    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    private SecretKey secretKey;

    private HashMap<String, byte[][]> hashes;
    private String lastSentFileName;

    public static void main(String[] args) {

        Client client = new Client();

        // try {
        // ServerSocket server = new ServerSocket(6767);
        // Socket clientSocket = server.accept();
        // System.out.println("Accepted");
        // } catch (Exception e) {
        // }

        System.out.println("Starting connect");
        client.connectToServer("192.168.191.33");
        System.out.println("Connected");

    }

    public Client() {
        secretKey = HmacUtil.generateSecretKey();
        hashes = new HashMap<>();
        lastSentFileName = "";
    }

    private String getFileData(String fileName) {
        File file = new File("src/" + fileName);
        String content = "";
        try {
            content = Files.readString(Path.of("src/" + fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return content;
    }

    private void uploadFile(String fileName) {

        ArrayList<Byte> outData = new ArrayList<>();

        // Skapa data som ska skickas

        byte[] nonce;
        nonce = HmacUtil.genereateSecureNonce();

        byte mByte;
        mByte = 1;

        byte fileNameSize = 0;
        if (fileName.getBytes().length < 128) {
            fileNameSize = (byte) fileName.getBytes().length;
        }

        byte[] fileNameBytes;
        fileNameBytes = fileName.getBytes();

        String fileData = getFileData(fileName);
        byte[] encryptedData = HmacUtil.encrpytData(fileData, secretKey, HmacUtil.AES_ALGORITHM,
                nonce);

        byte[] dataSize;
        dataSize = HmacUtil.intToBytes(encryptedData.length);

        byte[] hmac;
        hmac = HmacUtil.initHmac(HmacUtil.HMAC_SHA256_ALGORITHM, encryptedData, secretKey, nonce, fileNameBytes, mByte);

        // Lägg till bytes i outData -------------------

        outData.add(mByte);

        for (byte b : hmac) {
            outData.add(b);
        }

        for (byte b : nonce) {
            outData.add(b);
        }

        if (fileNameSize == 0) {
            return;
        }
        outData.add(fileNameSize);

        for (byte b : dataSize) {
            outData.add(b);
        }

        for (byte b : fileNameBytes) {
            outData.add(b);
        }

        for (byte b : encryptedData) {
            outData.add(b);
        }

        byte[] outArray = new byte[outData.size()];
        for (int i = 0; i < outArray.length; i++) {
            outArray[i] = outData.get(i);
        }

        try {
            lastSentFileName = fileName;
            outputStream.write(outArray);
            outputStream.flush();

            System.out.println("Uploaded file");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void downloadFile(String fileName) {
        byte mByte = (byte) 2;
        byte fileNameLength = (byte) fileName.getBytes().length;
        byte[] fileNameBytes = fileName.getBytes();
        byte[] packet = new byte[2 + fileNameLength];
        packet[0] = mByte;
        packet[1] = fileNameLength;
        for (int i = 2; i < packet.length; i++) {
            packet[i] = fileNameBytes[i - 2];
        }
        try {
            lastSentFileName = fileName;
            outputStream.write(packet);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void listFiles() {
        byte mByte = (byte) 3;
        try {
            outputStream.write(mByte);
            outputStream.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendPacket(String packetType) {
        Scanner scanner = new Scanner(System.in);
        switch (packetType) {
            case "1":
                System.out.println("What file do you want to upload?");
                String input = scanner.nextLine();
                uploadFile(input);
                break;
            case "2":
                System.out.println("What file do you want to download");
                input = scanner.nextLine();
                downloadFile(input);
                break;
            case "3":
                listFiles();
                break;

        }
    }

    private void connectToServer(String ip) {
        try {

            socket = new Socket(ip, 6767);
            // System.out.println("Established socket");
            outputStream = new DataOutputStream(socket.getOutputStream());
            // System.out.println("Connected out stream to server");
            inputStream = new DataInputStream(socket.getInputStream());

            Thread t = new Thread(this);

            t.start();

            Scanner scanner = new Scanner(System.in);
            while (true) {
                System.out.println("What type of packet to send (1 - upload, 2 - download, 3 - list files");
                String input = scanner.nextLine();
                sendPacket(input);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                int packetData = inputStream.readUnsignedByte();
                readPacket(packetData);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private void readPacket(int packetType) {
        switch (packetType) {
            case 67:
                storeHashes();
                break;
            case 68:
                readHashes();
                break;
            case 69:
                readFileList();
                break;
        }
    }

    private void storeHashes() {

        try {
            int hashesAmount;
            hashesAmount = inputStream.readUnsignedByte();
            byte[][] hashList = new byte[hashesAmount][];
            byte[] hash = new byte[32];
            for (int listIndex = 0; listIndex < hashesAmount; listIndex++) {
                for (int hashIndex = 0; hashIndex < 32; hashIndex++) {
                    hash[hashIndex] = (byte) inputStream.readUnsignedByte();
                }
                hashList[listIndex] = hash.clone();
            }
            this.hashes.put(lastSentFileName, hashList);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void readHashes() {

        int hashesAmount;
        try {
            hashesAmount = inputStream.readUnsignedByte();

            byte[] dataAmountBytes = inputStream.readNBytes(4);
            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(dataAmountBytes);
            buffer.rewind();
            int dataAmount = buffer.getInt();
            ArrayList<byte[]> uploadedHashes = new ArrayList<>();
            byte[] hash = new byte[32];
            for (int listIndex = 0; listIndex < hashesAmount; listIndex++) {
                for (int hashIndex = 0; hashIndex < 32; hashIndex++) {
                    hash[hashIndex] = inputStream.readByte();
                }
                uploadedHashes.add(hash.clone());
            }
            byte[] data = new byte[dataAmount];
            for (int i = 0; i < dataAmount; i++) {
                data[i] = inputStream.readByte();
            }

            if (HmacUtil.verifyHashes(uploadedHashes, hashes.get(lastSentFileName))) {
                byte[] decryptedData = HmacUtil.decryptData(data, secretKey, HmacUtil.AES_ALGORITHM);
                System.out.println(new String(decryptedData, StandardCharsets.US_ASCII));
            } else {
                System.out.println("Thats the wrong data");
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void readFileList() {
        byte[] dataAmountBytes;
        try {
            dataAmountBytes = inputStream.readNBytes(4);

            ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
            buffer.put(dataAmountBytes);
            buffer.rewind();
            int dataAmount = buffer.getInt();
            byte[] fileNameData = new byte[dataAmount];
            for (int i = 0; i < dataAmount; i++) {
                fileNameData[i] = inputStream.readByte();
            }
            String fileNames = new String(fileNameData, StandardCharsets.US_ASCII);
            System.out.println(fileNames);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
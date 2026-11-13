import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {

    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA3-256";
    public static final String AES_ALGORITHM = "AES/GCM/NoPadding";
    public static final int NONCE_LENGTH = 32;
    public static final int GMC_TAG_BIT_LENGTH = 128;

    public static SecretKey generateSecretKey() {
        SecretKey secretKey;
        try {

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            secretKey = keyGen.generateKey();

        } catch (Exception e) {

            secretKey = null;

            e.printStackTrace();

        }

        return secretKey;
    }

    public static byte[] decryptData(byte[] encryptedData, SecretKey key, String algorithm) {

        byte[] decryptedData;

        try {
            byte[] nonce = new byte[NONCE_LENGTH];
            ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
            buffer.get(nonce);
            GCMParameterSpec gmcSpec = new GCMParameterSpec(GMC_TAG_BIT_LENGTH, nonce);

            byte[] remainingBytes = new byte[buffer.remaining()];
            buffer.get(remainingBytes);

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, gmcSpec);

            decryptedData = cipher.doFinal(remainingBytes);
        } catch (Exception e) {
            e.printStackTrace();
            decryptedData = null;
        }

        return decryptedData;
    }

    public static boolean verifyHashes(ArrayList<byte[]> uploadHashes, byte[][] localHashes) {
        if (uploadHashes == null) {
            return false;
        }
        for (int i = 0; i < localHashes.length; i++) {
            byte[] uHash = uploadHashes.get(i);
            byte[] lHash = localHashes[i];
            if (uHash == null) {
                return false;
            }
            for (int j = 0; j < localHashes[i].length; j++) {
                if (uHash[j] != lHash[j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static byte[] encrpytData(String data, SecretKey key, String algorithm, byte[] nonce) {
        try {
            byte[] encryptedData;
            GCMParameterSpec gmcSpec = new GCMParameterSpec(GMC_TAG_BIT_LENGTH, nonce);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, gmcSpec);
            encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.US_ASCII));
            ByteBuffer buffer = ByteBuffer.allocate(nonce.length + encryptedData.length);
            buffer.put(nonce);
            buffer.put(encryptedData);
            return buffer.array();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] initHmac(String algorithm, byte[] data, SecretKey key, byte[] nonce, byte[] fileName,
            byte mByte) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getEncoded(), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            mac.update(nonce);
            mac.update(mByte);
            mac.update(fileName);
            mac.update(data);
            return mac.doFinal();
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static String bytesToHex(byte[] bytes) {
        String hexString = "";
        for (byte num : bytes) {
            hexString += byteToHex(num);
        }
        return hexString;
    }

    public static String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public static byte[] intToBytes(int i) {
        byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
        return bytes;
    }

    public static byte[] genereateSecureNonce() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] nonceBytes = new byte[NONCE_LENGTH];
        secureRandom.nextBytes(nonceBytes);
        return nonceBytes;
    }
}

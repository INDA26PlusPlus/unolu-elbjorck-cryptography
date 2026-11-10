import java.nio.ByteBuffer;
import java.security.SecureRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacUtil {
    
    public static String hmacSHA256Algorithm = "HmacSHA3-256";

    public static byte[] initHmac(String algorithm, String data, String key) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
            Mac mac = Mac.getInstance(algorithm);
            mac.init(secretKeySpec);
            return mac.doFinal(data.getBytes());
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
        byte[] nonceBytes = new byte[32];
        secureRandom.nextBytes(nonceBytes);
        return nonceBytes;
    }
}
 
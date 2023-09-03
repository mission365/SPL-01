import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.net.Socket;

public class EncryptionDecryption {
    public static BigInteger encrypt(BigInteger message, BigInteger publicKey, BigInteger modulus) {
        return message.modPow(publicKey, modulus);
    }

    public static BigInteger decrypt(BigInteger encryptedMessage, BigInteger privateKey, BigInteger modulus) {
        return encryptedMessage.modPow(privateKey, modulus);
    }
    
}



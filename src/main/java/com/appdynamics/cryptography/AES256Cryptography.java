package com.appdynamics.cryptography;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AES256Cryptography implements ICryptography {
    private static final Logger logger = LogManager.getFormatterLogger();
    private SecretKey secretKey;
    private IvParameterSpec ivSpec;
    private Cipher encryptCipher, decryptCipher;
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public AES256Cryptography (byte[] keyBytes, byte[] readIvBytes ) throws Exception {
        secretKey = new SecretKeySpec(keyBytes, "AES");
        ivSpec = new IvParameterSpec(readIvBytes);

        encryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

        decryptCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
    }

    public AES256Cryptography (String keyFileName, String ivFileName ) throws Exception {
        this( Files.readAllBytes(Paths.get(keyFileName)), Files.readAllBytes(Paths.get(ivFileName)) );
    }

    public AES256Cryptography( AES256Key aes256Key ) throws Exception {
        this( Base64.getDecoder().decode(aes256Key.AES256Key), Base64.getDecoder().decode(aes256Key.IV));
    }

    public AES256Cryptography( String keyFileName ) throws Exception {
        this( gson.fromJson(Files.readString(Path.of(keyFileName)), AES256Key.class));
    }

    public static void generateKey( String fileName ) {
        try {
            File keyFile = new File(fileName);

            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256);
            SecretKey generatedKey = keyGen.generateKey();

            SecureRandom secureRandom = new SecureRandom();
            byte[] ivBytes = new byte[16];
            secureRandom.nextBytes(ivBytes);

            AES256Key aes256Key = new AES256Key();
            aes256Key.Algorithm = generatedKey.getAlgorithm();
            aes256Key.AES256Key = Base64.getEncoder().encodeToString(generatedKey.getEncoded());
            aes256Key.IV = Base64.getEncoder().encodeToString(ivBytes);

            try (FileOutputStream keyFos = new FileOutputStream(keyFile)) {
                keyFos.write(gson.toJson(aes256Key).getBytes());
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String encrypt (String plainText) throws Exception {
        byte[] encrypted = this.encryptCipher.doFinal(plainText.getBytes("UTF-8"));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    @Override
    public String decrypt (String encryptedText) throws Exception {
        byte[] decrypted = decryptCipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decrypted, "UTF-8");
    }
}

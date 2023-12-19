package com.appdynamics.cryptography;

import java.util.Base64;

public interface ICryptography {
    String encrypt (String plainText) throws Exception;

    String decrypt (String encryptedText) throws Exception;
}

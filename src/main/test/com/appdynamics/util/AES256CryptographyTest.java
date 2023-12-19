package com.appdynamics.util;

import com.appdynamics.cryptography.AES256Cryptography;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AES256CryptographyTest {

    @Test
    void testEncryption () throws Exception {
        String input = "John Southerland";

        AES256Cryptography.generateKey("test");
        AES256Cryptography cryptography = new AES256Cryptography("test");
        String encrypted = cryptography.encrypt(input);
        String output = cryptography.decrypt(encrypted);
        assertEquals(input,output);
        assertNotEquals(encrypted,input);
    }
}
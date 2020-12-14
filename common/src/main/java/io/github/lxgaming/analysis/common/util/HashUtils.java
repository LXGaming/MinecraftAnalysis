/*
 * Copyright 2020 Alex Thomson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.lxgaming.analysis.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    
    private static final int BUFFER_SIZE = 4096;
    
    public static boolean sha1(InputStream inputStream, String hash) throws IOException {
        return sha1(inputStream).equalsIgnoreCase(hash);
    }
    
    public static boolean sha1(Path path, String hash) throws IOException {
        return sha1(path).equalsIgnoreCase(hash);
    }
    
    public static String sha1(InputStream inputStream) throws IOException {
        byte[] bytes = digest(sha1(), inputStream);
        return toString(bytes);
    }
    
    public static String sha1(Path path) throws IOException {
        byte[] bytes = digest(sha1(), path);
        return toString(bytes);
    }
    
    public static MessageDigest sha1() {
        return getDigest("SHA-1");
    }
    
    public static String toString(MessageDigest digest) {
        return toString(digest.digest());
    }
    
    public static String toString(byte[] bytes) {
        return String.format("%040x", new BigInteger(1, bytes));
    }
    
    private static byte[] digest(MessageDigest digest, Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            return digest(digest, inputStream);
        }
    }
    
    private static byte[] digest(MessageDigest digest, InputStream inputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, read);
        }
        
        return digest.digest();
    }
    
    private static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
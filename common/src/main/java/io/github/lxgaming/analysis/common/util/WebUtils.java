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

import io.github.lxgaming.analysis.common.Analysis;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;

public class WebUtils {
    
    public static void downloadFile(URL url, Path path) throws IOException {
        downloadFile(url, path, -1, null);
    }
    
    public static void downloadFile(URL url, Path path, long length, String hash) throws IOException {
        HttpURLConnection connection = createConnection(url);
        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException(String.format("Unexpected Response: %s", connection.getResponseCode()));
        }
        
        if (length != -1) {
            long contentLength = connection.getHeaderFieldLong("Content-Length", -1);
            if (contentLength != length) {
                throw new IllegalStateException(String.format("Mismatched Size (got %s, expected %s)", contentLength, length));
            }
        }
        
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        
        if (StringUtils.isBlank(hash)) {
            Files.copy(connection.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return;
        }
        
        String calculatedHash;
        try (DigestInputStream inputStream = new DigestInputStream(connection.getInputStream(), HashUtils.sha1())) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
            
            calculatedHash = HashUtils.toString(inputStream.getMessageDigest());
        }
        
        if (!calculatedHash.equalsIgnoreCase(hash)) {
            Files.delete(path);
            throw new IllegalStateException(String.format("Mismatched Hash (got %s, expected %s)", calculatedHash, hash));
        }
    }
    
    public static <T> T deserializeJson(URL url, Class<T> type) throws IOException {
        HttpURLConnection connection = createConnection(url);
        if (connection.getResponseCode() != 200) {
            throw new IllegalStateException(String.format("Unexpected Response: %s", connection.getResponseCode()));
        }
        
        try (Reader reader = new InputStreamReader(connection.getInputStream())) {
            return Toolbox.GSON.fromJson(reader, type);
        }
    }
    
    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", Analysis.USER_AGENT);
        return connection;
    }
}
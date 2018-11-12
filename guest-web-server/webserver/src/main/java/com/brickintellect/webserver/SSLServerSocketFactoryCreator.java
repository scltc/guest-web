package com.brickintellect.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;

import org.nanohttpd.protocols.http.NanoHTTPD;

public class SSLServerSocketFactoryCreator {

    private static File getFileWithNewExtension(File file, String extension) {
        File result;

        if (file == null) {
            result = null;
        } else {
            String path = file.toString();
            int index =  path.lastIndexOf('.');
            result = new File(((index < 0) ? path : path.substring(0, index)) + extension);
        }

        return result;
    }

    /**
     * Reads the first line (of possibly many) from a text file.
     */
    private static String readFirstLine(File file) throws IOException {
        String result;

        if (file == null || !file.exists()) {
            result = null;
        } else {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                result = reader.readLine();
            }
        }

        return result;
    }

    /**
     * Create a SSLSocketFactory for HTTPS.
     */
    public static SSLServerSocketFactory create(File keyStoreFile, String keyStorePassword)
            throws IOException {

        SSLServerSocketFactory result;

        if (keyStoreFile == null || !keyStoreFile.exists() || keyStorePassword == null) {
            result = null;
        } else {
            try (InputStream keystoreStream = new FileInputStream(keyStoreFile)) {
                KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
                keystore.load(keystoreStream, keyStorePassword.toCharArray());

                KeyManagerFactory keyManagerFactory = KeyManagerFactory
                        .getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keystore, keyStorePassword.toCharArray());
                result = NanoHTTPD.makeSSLSocketFactory(keystore, keyManagerFactory);
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
        return result;
    }

    /**
     * Create a SSLSocketFactory for HTTPS.
     */
    public static SSLServerSocketFactory create(File keyStoreFile) throws IOException {
        return create(keyStoreFile, readFirstLine(getFileWithNewExtension(keyStoreFile, ".password")));
    }
}
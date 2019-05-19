package com.brickintellect.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;


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
     * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and an
     * array of loaded KeyManagers. These objects must properly
     * loaded/initialized by the caller.
     */
    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManager[] keyManagers) throws IOException {
        SSLServerSocketFactory res = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(loadedKeyStore);
            SSLContext ctx = SSLContext.getInstance("TLSv1.2");
            ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
            res = ctx.getServerSocketFactory();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
        return res;
    }

    /**
     * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and a
     * loaded KeyManagerFactory. These objects must properly loaded/initialized
     * by the caller.
     */
    public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManagerFactory loadedKeyFactory) throws IOException {
        try {
            return makeSSLSocketFactory(loadedKeyStore, loadedKeyFactory.getKeyManagers());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        }
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
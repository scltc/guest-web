package com.brickintellect.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.security.KeyStore;

//import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
// import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
// import javax.net.ssl.TrustManagerFactory;

import org.nanohttpd.protocols.http.NanoHTTPD;

public class Main {

    public static File getFileWithNewExtension(File file, String extension) {
        File result;

        if (file == null) {
            result = null;
        } else {
            String path = file.toString();
            result = new File(path.substring(0, path.lastIndexOf('.')) + extension);
        }

        return result;
    }

    /**
     * Reads the first line (of possibly many) from a text file.
     */
    public static String readFirstLine(File file) throws IOException {
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
     * Creates an SSLSocketFactory for HTTPS.
     */
    public static SSLServerSocketFactory makeSSLSocketFactory(File keyStoreFile, String keyStorePassword)
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
     * Starts as a standalone file server and waits for Enter.
     */
    public static void main(String[] args) {

        // Set parameter defaults.
        String host = null; // bind to all interfaces by default
        int port = 80;
        File root = null;
        File keyStoreFile = null;
        boolean daemon = false;
        // boolean verbose = false;
        // String cors = null;

        // Parse the command-line.
        for (int i = 0; i < args.length; ++i) {
            if ("--daemon".equals(args[i])) {
                daemon = true;
            } else if ("--host".equals(args[i])) {
                host = args[++i];
            } else if ("--key-store".equals(args[i])) {
                keyStoreFile = new File(args[++i]).getAbsoluteFile();
            } else if ("--port".equals(args[i])) {
                port = Integer.parseInt(args[++i]);
                // } else if ("--verbose".equalsIgnoreCase(args[i])) {
                // verbose = true;
            } else if ("--root".equals(args[i])) {
                root = new File(args[++i]).getAbsoluteFile();

                // } else if (args[i].startsWith("--cors")) {
                // cors = "*";
                // int equalIdx = args[i].indexOf('=');
                // if (equalIdx > 0) {
                // cors = args[i].substring(equalIdx + 1);
                // }
            }
        }

        // Launch the server!
        try {

            WebServer server = new WebServer(host, port);

            // Create a HTTPS socket factory if key store specified.
            SSLServerSocketFactory socketFactory = makeSSLSocketFactory(keyStoreFile, readFirstLine(getFileWithNewExtension(keyStoreFile, ".password")));

            if (socketFactory != null) {
                server.makeSecure(socketFactory, null);
            }

            server.start(root);

            RMIRailroad rmi = RMIRailroadServer.Create();

            if (daemon) {
                System.out.println("Webserver running as daemon!");
                while (true) {
                    try {
                        Thread.sleep(1000 * 10);
                    } catch (InterruptedException ignored) {
                        break;
                    }
                }
            } else {
                System.out.println("Webserver running.  Press Enter to stop.\n");

                try {
                    System.in.read();
                } catch (Throwable ignored) {
                }
            }

            server.stop();

            System.out.println("Webserver stopped.\n");
            System.exit(0);

        } catch (IOException ioe) {

            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);

        }
    }
}

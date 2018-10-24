package com.brickintellect.webserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
//import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.OpCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class WebSocketSession extends WebSocket {

    public static class WebSocketInputStream extends ByteArrayInputStream {

        private final static String utfExceptionMessage = "Improper UTF-8 endpoint encoding.";
        private final static String endpointExceptionMessage = "Improper endpoint prefix (expected \"digit(s):\").";

        private static char readOneCharacter(final InputStream stream) throws IOException {

            char result;

            int theByte = stream.read();

            if ((theByte & 0x80) == 0) {
                result = (char) theByte;
            } else {

                if (theByte == -1) {
                    throw new EOFException(utfExceptionMessage);
                }

                int remain;

                if ((theByte & 0xE0) == 0xC0) {
                    remain = 1;
                    result = (char) (theByte & 0x1F);
                } else if ((theByte & 0xF0) == 0xE0) {
                    remain = 2;
                    result = (char) (theByte & 0x0F);
                } else if ((theByte & 0xF8) == 0xF0) {
                    remain = 3;
                    result = (char) (theByte & 0x07);
                } else {
                    throw new UTFDataFormatException(utfExceptionMessage);
                }

                while (remain-- > 0) {
                    if ((theByte = stream.read()) == -1) {
                        throw new UTFDataFormatException(utfExceptionMessage);
                    }

                    if ((theByte & 0xC0) != 0x80) {
                        throw new UTFDataFormatException(utfExceptionMessage);
                    }

                    result = (char) ((result << 6) | (theByte & 0x3F));
                }
            }

            return result;
        }

        private static int getEndpoint(InputStream stream) throws IOException {
            int result = 0;

            for (char character; (character = readOneCharacter(stream)) != ':';) {

                if (!Character.isDigit(character)) {
                    throw new IOException(endpointExceptionMessage);
                }
                result = result * 10 + character - '0';
            }

            return result;
        }

        private final int endpoint;

        public WebSocketInputStream(byte[] buffer) throws IOException {

            super(buffer);

            endpoint = getEndpoint(this);

            System.out.println("endpoint: " + endpoint);
        }

        public int getEndpoint() {
            return endpoint;
        }
    }

    public static class WebSocketOutputStream extends ByteArrayOutputStream {

        private final WebSocket socket;

        WebSocketOutputStream(WebSocket socket, int endpoint) throws IOException {
            super(2048);
            this.socket = socket;
            write(StandardCharsets.UTF_8.encode(String.valueOf(endpoint) + ':').array());
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            System.out.println(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(toByteArray())).toString());
            socket.send(this.toString(StandardCharsets.UTF_8.name()));
        }

        @Override
        public void close() throws IOException {
            flush();
            super.close();
        }
    }

    private static final Logger LOG = Logger.getLogger(WebSocketSession.class.getName());

    private static final boolean debug = true;
    private final JsonRpcBasicServer rpcServer;

    public WebSocketSession(final IHTTPSession session, final Object service, final Class<?> serviceClass) {
        super(session);
        System.out.println("WebSocketSession()");
        rpcServer = new JsonRpcBasicServer(service, serviceClass);
    }

    @Override
    protected void onOpen() {
        System.out.println("WebSocketSession.OnOpen()");
    }

    @Override
    protected void onClose(final CloseCode code, final String reason, final boolean initiatedByRemote) {
        System.out.println("WebSocketSession.OnClose()");
        if (debug) {
            System.out.println("C [" + (initiatedByRemote ? "Remote" : "Self") + "] "
                    + (code != null ? code : "UnknownCloseCode[" + code + "]")
                    + (reason != null && !reason.isEmpty() ? ": " + reason : ""));
        }
    }

    @Override
    protected void onMessage(final WebSocketFrame requestFrame) {
        System.out.println("WebSocketSession.OnMessage() {");
        try {
            requestFrame.setUnmasked();

            // try {
                System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

                try (WebSocketInputStream inputStream = new WebSocketInputStream(requestFrame.getBinaryPayload())) {
                    try (WebSocketOutputStream outputStream = new WebSocketOutputStream(this, inputStream.getEndpoint())) {
                        rpcServer.handleRequest(inputStream, outputStream);

                        // outputStream.flush();

                        // requestFrame.setBinaryPayload(outputStream.toByteArray());

                        // System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));
                    }
                }
/*
            } catch (Exception exception) {
                System.out.println(exception.toString());
            } finally {
            }
*/
            // sendFrame(requestFrame);
        } catch (IOException exception) {
            System.out.println(exception.toString());
            throw new RuntimeException(exception);
        }
        System.out.println("WebSocketSession.OnMessage() }");
    }

    @Override
    protected void onPong(WebSocketFrame pong) {
        if (debug) {
            System.out.println("P " + pong);
        }
    }

    @Override
    protected void onException(IOException exception) {
        WebSocketSession.LOG.log(Level.SEVERE, "exception occured", exception);
    }
}

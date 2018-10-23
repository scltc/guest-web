package com.brickintellect.webserver;

import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.googlecode.jsonrpc4j.JsonRpcBasicServer;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class WebSocketSession extends WebSocket {

    public static class WebSocketSessionInputStream extends ByteArrayInputStream {

        char readOneCharacter(InputStream stream) throws IOException {

            char result;

            int theByte = stream.read();

            if (theByte == -1) {
                throw new EOFException("Endpoint: Improperly encoded.");
            }

            if ((theByte & 0x80) == 0) {
                result = (char) theByte;
            } else {
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
                    throw new UTFDataFormatException("Endpoint: Improperly encoded.");
                }

                while (remain-- > 0) {
                    if ((theByte = stream.read()) == -1) {
                        throw new UTFDataFormatException("Endpoint: Improperly encoded.");
                    }

                    if ((theByte & 0xC0) != 0x80) {
                        throw new UTFDataFormatException("Endpoint: Improperly encoded.");
                    }

                    result = (char) ((result << 6) | (theByte & 0x3F));
                }
            }

            return result;
        }

        private final char endpoint;

        public WebSocketSessionInputStream(byte[] buffer) throws IOException {

            super(buffer);

            endpoint = readOneCharacter(this);
        }

        public char getEndpoint() {
            return endpoint;
        }
    }

    public static class WebSocketSessionOutputStream extends ByteArrayOutputStream {

        WebSocket socket;

        WebSocketSessionOutputStream(WebSocket socket, char endpoint) throws IOException {
            super(2048);
            this.socket = socket;
            write(StandardCharsets.UTF_8.encode(CharBuffer.wrap(new char[] { endpoint })).array());
        }

        @Override
        public void flush() throws IOException {
            if (socket == null) {
                throw new IOException();
            }
            super.flush();
            socket.send(toByteArray());
        }

        @Override
        public void close() throws IOException {
            if (socket != null) {
                super.close();
                socket = null;
            }
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

            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024);

                System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

                rpcServer.handleRequest(new ByteArrayInputStream(requestFrame.getBinaryPayload()), outputStream);

                outputStream.flush();

                requestFrame.setBinaryPayload(outputStream.toByteArray());

                System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

            } catch (Exception exception) {
                System.out.println(exception.toString());
            } finally {
            }

            sendFrame(requestFrame);
        } catch (IOException e) {
            throw new RuntimeException(e);
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

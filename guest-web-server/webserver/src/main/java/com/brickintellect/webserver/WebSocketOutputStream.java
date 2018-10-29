package com.brickintellect.webserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.nanohttpd.protocols.websockets.WebSocket;

public class WebSocketOutputStream extends ByteArrayOutputStream {

    private final WebSocket socket;

    WebSocketOutputStream(WebSocket socket, int endpointNumber) throws IOException {
        super(2048);
        this.socket = socket;
        write(StandardCharsets.UTF_8.encode(String.valueOf(endpointNumber) + ':').array());
    }

    @Override
    public void flush() throws IOException {
        if (size() > 0) {
            super.flush();
            System.out.println(StandardCharsets.UTF_8.decode(ByteBuffer.wrap(toByteArray())).toString());
            socket.send(this.toString(StandardCharsets.UTF_8.name()));

            reset();
        }
    }

    @Override
    public void close() throws IOException {
        flush();
        super.close();
    }
}

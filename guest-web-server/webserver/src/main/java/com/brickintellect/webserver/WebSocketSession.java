package com.brickintellect.webserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import org.nanohttpd.protocols.http.content.CookieHandler;
import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.websockets.CloseCode;
import org.nanohttpd.protocols.websockets.WebSocket;
import org.nanohttpd.protocols.websockets.WebSocketFrame;

public class WebSocketSession extends WebSocket {

    private static final boolean debug = true;
    private static String clientIdentifier = null;

    private ArrayList<IEndpoint> endpoints = new ArrayList<IEndpoint>();

    public interface IEndpoint {
        int endpointNumber();

        void onMessage(WebSocketInputStream input);
    }

    public WebSocketSession(final IHTTPSession session) {
        super(session);

        // We use a "Client-Identifier" cookie (a UUID) to uniquely identify
        // each client. Retrieve the identifier provided with this WebSocket
        // connection or create a new one if none provided.
        CookieHandler cookies = session.getCookies();
        clientIdentifier = cookies.read("Client-Identifier");
        if (clientIdentifier == null) {
            clientIdentifier = UUID.randomUUID().toString();
            System.out.println("Client identifier missing, created: " + clientIdentifier);
            cookies.set("Client-Identifier", clientIdentifier, 1);
        } else {
            System.out.println("Client identifier provided: " + clientIdentifier);
        }
        System.out.println("WebSocketSession()");
    }

    public void addEndpoint(IEndpoint instance) {
        int endpointNumber = instance.endpointNumber();

        while (endpoints.size() <= endpointNumber) {
            endpoints.add(null);
        }

        endpoints.set(instance.endpointNumber(), instance);
    }

    @Override
    protected void onException(IOException exception) {
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

            System.out.println(new String(requestFrame.getBinaryPayload(), "UTF-8"));

            try (WebSocketInputStream inputStream = new WebSocketInputStream(requestFrame.getBinaryPayload())) {
                int endpointNumber = inputStream.getEndpointNumber();
                IEndpoint endpoint;
                if (endpointNumber > endpoints.size() || (endpoint = endpoints.get(endpointNumber)) == null) {
                    throw new IOException("Not listening on endpoint " + inputStream.getEndpointNumber() + ".");
                }
                endpoint.onMessage(inputStream);
            }
        } catch (IOException exception) {
            System.out.println(exception.toString());
            throw new RuntimeException(exception);
        }
        System.out.println("WebSocketSession.OnMessage() }");
    }

    // Ping/pong support.
    // The WebSocket connection will disconnect (due to a timeout) if
    // not kept active.
    // Issue a ping with a monotonically increasing integer payload and
    // store the pong values.  Throw an exception in ping (because that
    // returns to the "mainline thread" and is easily reported) if more
    // than three pong replies are missed.

    private int pingCount = 0;
    private int pongCount = 0;

    public void ping() throws IOException {

        System.out.println("ping: " + pingCount + "/" + pongCount);

        if (pingCount - pongCount > 3) {
            throw new IOException("pong count, expected " + pingCount);
        }

        pingCount += 1;

        ping(new byte[] { (byte) (pingCount >> 24), (byte) (pingCount >> 16), (byte) (pingCount >> 8),
                (byte) (pingCount >> 0) });
    }

    @Override
    protected void onPong(WebSocketFrame frame) {
        byte[] bytes = frame.getBinaryPayload();
        if (bytes != null && bytes.length == 4) {
            pongCount = ((bytes[0] & 0xFF) << 24) + ((bytes[1] & 0xFF) << 16) + ((bytes[2] & 0xFF) << 8)
                    + ((bytes[3] & 0xFF) << 0);
        }
    }
}

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

        // We use a "Client-Identifier" cookie (UUID) to uniquely identify
        // each client.  Retrieve the identifier provided with this WebSocket
        // connection or create a new one if none provided.
        CookieHandler cookies = session.getCookies();
        clientIdentifier = cookies.read("Client-Identifier");
        if (clientIdentifier == null) {
            clientIdentifier = UUID.randomUUID().toString();
            System.out.println("Client identifier missing, created: " + clientIdentifier);
            cookies.set("Client-Identifier", clientIdentifier, 1);
        }
        else {
            System.out.println("Client identifier provided: " + clientIdentifier);
        }
        System.out.println("WebSocketSession()");
    }

    public void addEndpoint(IEndpoint instance)
    {
        int endpointNumber = instance.endpointNumber();

        while (endpoints.size() <= endpointNumber) {
            endpoints.add(null);
        }

        endpoints.set(instance.endpointNumber(), instance);
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

    @Override
    protected void onPong(WebSocketFrame pong) {
        if (debug) {
            System.out.println("P " + pong);
        }
    }

    @Override
    protected void onException(IOException exception) {
    }
}

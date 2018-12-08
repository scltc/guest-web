package com.brickintellect.exhibit;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.brickintellect.webserver.WebSocketJsonRpcClient;
import com.brickintellect.webserver.WebSocketJsonRpcServer;
import com.brickintellect.webserver.WebSocketSession;
import com.brickintellect.webserver.WebSocketSessionManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.googlecode.jsonrpc4j.JsonRpcParam;

import org.nanohttpd.protocols.http.IHTTPSession;

public class Exhibit {

    private static Exhibit exhibit = new Exhibit();

    private Settings settings = new Settings();

    private final List<HeadTurnerPlayController> heads = new ArrayList<HeadTurnerPlayController>();
    private final List<CatchAndThrow> catchers = new ArrayList<CatchAndThrow>();

    public Exhibit() {

        settings.catchAndThrow.add(new CatchAndThrow.Settings());
        settings.headTurner.add(new HeadTurner.Settings());

        for (CatchAndThrow.Settings setting : settings.catchAndThrow) {
            catchers.add(new CatchAndThrow(setting));
        }

        for (HeadTurner.Settings setting : settings.headTurner) {
            heads.add(new HeadTurnerPlayController(setting, (UUID guest, HeadTurnerState state) -> {
                WebSocketSession session = WebSocketSessionManager.getConnection(guest);
                if (session != null) {
                    ((Exhibit.WebSocketService) session).headsChanged(state);
                }
            }));
        }
    }

    public interface ICatchAndThrowService {
        int runCatchAndThrow(@JsonRpcParam("index") int index);

        public CatchAndThrowState catcherAbandon(@JsonRpcParam("instance") int instance);
        public CatchAndThrowState catcherOperate(@JsonRpcParam("instance") int instance,
                @JsonRpcParam("direction") int direction);
        public CatchAndThrowState catcherReserve(@JsonRpcParam("instance") int instance);
    }

    public interface IHeadTurnerWebSocketService {
        public HeadTurnerState headsAbandon(@JsonRpcParam("instance") int instance);
        public HeadTurnerState headsOperate(@JsonRpcParam("instance") int instance,
                @JsonRpcParam("direction") int direction);
        public HeadTurnerState headsReserve(@JsonRpcParam("instance") int instance);
    }

    public interface IWebSocketService extends ICatchAndThrowService, IHeadTurnerWebSocketService {
        int ping(@JsonRpcParam(value = "value") int value) throws Exception;

        Settings getSettings();

        Settings setSettings(@JsonRpcParam("settings") Settings settings);


    }

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static class WebSocketService extends WebSocketSession implements IWebSocketService {

        private final WebSocketJsonRpcClient client;

        public WebSocketService(IHTTPSession session) {
            super(session);
            client = new WebSocketJsonRpcClient(this, 2, objectMapper);
            addEndpoint(new WebSocketJsonRpcServer(this, 1, objectMapper, this, Exhibit.IWebSocketService.class));
            addEndpoint(client);
        }

        /*
         * public String connect() { System.out.println("connect()"); return
         * getClientIdentifier().toString(); }
         * 
         * public void disconnect() { System.out.println("disconnect()"); }
         */

        public int sendPing(int value) {
            try {
                return client.invoke("ping", value, int.class);
            } catch (Throwable exception) {
                System.out.println(exception.getMessage());
                return -1;
            }
        }

        public int ping(int value) throws Exception {
            /*
             * String result = "ping received: " + value; if (value % 10 == 0) {
             * sendPing(value); throw new Exception("ping # " + value + " failed.");
             * 
             * } System.out.println(result);
             */
            return value;
        }

        public Settings getSettings() {
            return exhibit.settings;
        }

        public Settings setSettings(Settings settings) {
            return exhibit.settings = settings;
        }

        public CatchAndThrowState catcherAbandon(int instance) {
            return new CatchAndThrowState();
        }

        public CatchAndThrowState catcherOperate(int instance, int direction) {
            return new CatchAndThrowState();
        }

        public CatchAndThrowState catcherReserve(int instance) {
            return new CatchAndThrowState();
        }

        // IHeadTurnerWebSocketService implementation.

        public HeadTurnerState headsAbandon(int instance) {
            System.out.println("headsAbandon");
            return (instance >= exhibit.heads.size()) ? null
                    : exhibit.heads.get(instance).headsAbandon(client.getClientIdentifier());
        }

        public HeadTurnerState headsOperate(int instance, int direction) {
            System.out.println("headsOperate");
            return (instance >= exhibit.heads.size()) ? null
                    : exhibit.heads.get(instance).headsOperate(client.getClientIdentifier(), direction);
        }

        public HeadTurnerState headsReserve(int instance) {
            System.out.println("headsReserve");
            return (instance >= exhibit.heads.size()) ? null
                    : exhibit.heads.get(instance).headsReserve(client.getClientIdentifier());
        }

        public void headsChanged(HeadTurnerState state) {
            try {
                client.invoke("headsChanged", state);
            } catch (Throwable exception) {
                System.out.println("headsChanged exception: " + exception.getMessage());
            }
        }

        public int runCatchAndThrow(int index) {
            return (index >= exhibit.catchers.size()) ? 0 : exhibit.catchers.get(index).go();
        }
    }
}
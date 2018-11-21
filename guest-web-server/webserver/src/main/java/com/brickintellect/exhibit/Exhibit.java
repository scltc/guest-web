package com.brickintellect.exhibit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.googlecode.jsonrpc4j.JsonRpcParam;
import com.brickintellect.exhibit.PlaytimeManager.PlaytimeState;
import com.brickintellect.webserver.WebSocketJsonRpcClient;

public class Exhibit {

    public Settings settings = new Settings();

    public final List<HeadTurnerPlayController> heads = new ArrayList<HeadTurnerPlayController>();
    public final List<CatchAndThrow> catchers = new ArrayList<CatchAndThrow>();

    public Exhibit() {

        settings.catchAndThrow.add(new CatchAndThrow.Settings());
        settings.headTurner.add(new HeadTurner.Settings());

        for (CatchAndThrow.Settings setting : settings.catchAndThrow) {
            catchers.add(new CatchAndThrow(setting));
        }

        for (HeadTurner.Settings setting : settings.headTurner) {
            heads.add(new HeadTurnerPlayController(setting, (UUID guest, HeadTurnerState state) -> {
                this.client.invoke("headsChanged", state);
            }));
        }
    }

    public interface IHeadTurnerWebSocketService {
        public HeadTurnerState headsAbandon(@JsonRpcParam(value = "instance") int instance);

        public HeadTurnerState headsOperate(@JsonRpcParam(value = "instance") int instance,
                @JsonRpcParam(value = "direction") int direction);

        public HeadTurnerState headsReserve(@JsonRpcParam(value = "instance") int instance);
    }

    public interface IWebSocketService {
        int ping(@JsonRpcParam(value = "value") int value) throws Exception;

        Settings getSettings();

        Settings setSettings(@JsonRpcParam(value = "settings") Settings settings);

        int runCatchAndThrow(@JsonRpcParam(value = "index") int index);

        public HeadTurnerState headsAbandon(@JsonRpcParam(value = "instance") int instance);

        public HeadTurnerState headsOperate(@JsonRpcParam(value = "instance") int instance,
                @JsonRpcParam(value = "direction") int direction);

        public HeadTurnerState headsReserve(@JsonRpcParam(value = "instance") int instance);

    }

    public static class WebSocketService implements IWebSocketService {

        private final WebSocketJsonRpcClient client;
        private final Exhibit exhibit;

        public WebSocketService(final WebSocketJsonRpcClient client, final Exhibit exhibit) {
            this.client = client;
            this.exhibit = exhibit;
        }

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

        public int runCatchAndThrow(int index) {
            return (index >= exhibit.catchers.size()) ? 0 : exhibit.catchers.get(index).go();
        }
    }
}
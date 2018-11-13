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

    public final List<HeadTurner> heads = new ArrayList<HeadTurner>();
    public final List<CatchAndThrow> catchers = new ArrayList<CatchAndThrow>();

    public Exhibit() {

        settings.catchAndThrow.add(new CatchAndThrow.Settings());
        settings.headTurner.add(new HeadTurner.Settings());

        for (CatchAndThrow.Settings setting : settings.catchAndThrow) {
            catchers.add(new CatchAndThrow(setting));
        }

        for (HeadTurner.Settings setting : settings.headTurner) {
            heads.add(new HeadTurner(setting));
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

    public static class WebSocketService implements IWebSocketService, PlaytimeManager.IPlaytimeStatus {

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

        static final PlaytimeManager headsManager = new PlaytimeManager(new PlaytimeManager.Settings());

        @Override
        public void playtimeStatus(UUID guest, PlaytimeState state, int timeRemaining) {
            HeadTurnerState request = new HeadTurnerState();
            if (state == PlaytimeManager.PlaytimeState.WAITING) {
                request.direction = 0;
                request.status = -1;
            } else if (state == PlaytimeManager.PlaytimeState.CANCELED) {
                request.direction = 0;
                request.status = 0;
            } else {
                request.direction = request.status = +1;
            }
            request.timer = timeRemaining;
            try {
                this.client.invoke("headsChanged", request);
            } catch (Throwable ignored) {

            }
        }

        public HeadTurnerState headsAbandon(int instance) {
            HeadTurnerState result;
            if (instance >= exhibit.heads.size()) {
                result = null;
            } else {
                result = new HeadTurnerState();
                result.status = 0;
                result.direction = exhibit.heads.get(instance).getDirection();
                result.timer = 1000 * 10;
            }
            return result;
        }

        public HeadTurnerState headsOperate(int instance, int direction) {
            HeadTurnerState result;
            if (instance >= exhibit.heads.size()) {
                result = null;
            } else {
                result = new HeadTurnerState();
                result.status = +1;
                result.direction = exhibit.heads.get(instance).setDirection(direction);
                result.timer = 1000 * 10;
            }
            return result;
        }

        public HeadTurnerState headsReserve(int instance) {
            System.out.println("headsReserve");

            headsManager.reserve(client.getClientIdentifier(), this);

            HeadTurnerState result;
            if (instance >= exhibit.heads.size()) {
                result = null;
            } else {
                result = new HeadTurnerState();
                result.status = -1;
                result.direction = exhibit.heads.get(instance).getDirection();
                result.timer = 1000 * 20;
            }
            return result;
        }

        public int runCatchAndThrow(int index) {
            return (index >= exhibit.catchers.size()) ? 0 : exhibit.catchers.get(index).go();
        }
    }
}
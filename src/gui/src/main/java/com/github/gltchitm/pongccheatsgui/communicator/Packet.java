package com.github.gltchitm.pongccheatsgui.communicator;

import org.json.JSONObject;

public class Packet {
    public static class ClientboundPacket {
        public static final byte OK = 0;
        public static final byte LEFT_SCORE = 1;
        public static final byte RIGHT_SCORE = 2;
        public static final byte BUSY = 3;
        public static final byte ALREADY_ATTACHED = 4;
        public static final byte NOT_ATTACHED = 5;
        public static final byte NOT_FOUND = 6;
        public static final byte MALFORMED_PACKET = 7;

        public byte error;
        public Integer score;

        public ClientboundPacket(byte error, Integer score) {
            this.error = error;
            this.score = score;
        }

        public String toString() {
            JSONObject object = new JSONObject();

            object.put("error", error);
            object.put("score", score == null ? JSONObject.NULL : score);

            return object.toString() + "\n";
        }
    }
    public static class ServerboundPacket {
        public static final byte ATTACH = 0;
        public static final byte CHANGE_LEFT_SCORE = 1;
        public static final byte CHANGE_RIGHT_SCORE = 2;
        public static final byte GET_LEFT_SCORE = 3;
        public static final byte GET_RIGHT_SCORE = 4;
        public static final byte DETACH = 5;
        public static final byte DETACH_AND_EXIT = 6;

        public byte id;
        public Integer score;

        public ServerboundPacket(byte id, Integer score) {
            this.id = id;
            this.score = score;
        }

        public String toString() {
            JSONObject object = new JSONObject();

            object.put("id", id);
            object.put("score", score == null ? JSONObject.NULL : score);

            return object.toString() + "\n";
        }
    }
}

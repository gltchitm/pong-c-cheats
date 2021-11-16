package com.github.gltchitm.pongccheatsgui.communicator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.github.gltchitm.pongccheatsgui.communicator.Packet.ClientboundPacket;
import com.github.gltchitm.pongccheatsgui.communicator.Packet.ServerboundPacket;

import org.json.JSONObject;
import org.newsclub.net.unix.AFUNIXSocket;
import org.newsclub.net.unix.AFUNIXSocketAddress;

public class Communicator {
    private AFUNIXSocket socket;
    private BufferedReader reader;
    private OutputStream outputStream;

    public Communicator() throws IOException {
        socket = AFUNIXSocket.newInstance();
        socket.connect(AFUNIXSocketAddress.of(new File("/tmp/pongccheatsd.sock")));
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        outputStream = socket.getOutputStream();
    }
    public ClientboundPacket attach() throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.ATTACH, null));

        return readPacket();
    }
    public ClientboundPacket detach() throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.DETACH, null));

        return readPacket();
    }
    public ClientboundPacket getLeftScore() throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.GET_LEFT_SCORE, null));

        return readPacket();
    }
    public ClientboundPacket getRightScore() throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.GET_RIGHT_SCORE, null));

        return readPacket();
    }
    public ClientboundPacket changeLeftScore(int score) throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.CHANGE_LEFT_SCORE, score));

        return readPacket();
    }
    public ClientboundPacket changeRightScore(int score) throws IOException {
        sendPacket(new ServerboundPacket(ServerboundPacket.CHANGE_RIGHT_SCORE, score));

        return readPacket();
    }
    public boolean isConnected() {
        return this.socket.isConnected();
    }
    private void sendPacket(ServerboundPacket packet) throws IOException {
        outputStream.write(packet.toString().getBytes("UTF-8"));
        outputStream.flush();
    }
    private ClientboundPacket readPacket() throws IOException {
        JSONObject packetData = new JSONObject(reader.readLine().toString());

        byte error = packetData.getNumber("error").byteValue();
        Object score = packetData.get("score");

        return new ClientboundPacket(error, score == JSONObject.NULL ? null : (int) score);
    }
}

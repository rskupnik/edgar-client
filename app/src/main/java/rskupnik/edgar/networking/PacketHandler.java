package rskupnik.edgar.networking;

import android.content.Intent;

import java.io.DataInputStream;
import java.io.IOException;

import rskupnik.edgar.activities.CommandActivity;

final class PacketHandler {

    private ConnectionService connectionService;

    PacketHandler(ConnectionService service) {
        this.connectionService = service;
    }

    void handle(int id, DataInputStream inputStream) {
        switch (id) {
            case PacketId.COMMAND_OUTPUT_PACKET:
                commandOutputPacket(inputStream);
                break;
            case PacketId.HANDSHAKE_PACKET:
                handshakePacket(inputStream);
                break;
            default:
                break;
        }
    }

    private void commandOutputPacket(DataInputStream inputStream) {
        try {
            connectionService.sendCommandOutput(inputStream.readUTF());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handshakePacket(DataInputStream inputStream) {
        connectionService.sendHandshakeSuccessMessage();
    }
}

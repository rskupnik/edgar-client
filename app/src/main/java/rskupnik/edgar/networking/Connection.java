package rskupnik.edgar.networking;

import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

final class Connection extends Thread {

    private ConnectionService connectionService;
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private PacketHandler packetHandler;
    private boolean exit;

    Connection(ConnectionService service, String ip) {
        this.connectionService = service;

        try {
            socket = new SocketConnectTask().execute(ip).get();
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            packetHandler = new PacketHandler(connectionService);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (!exit) {
            try {
                int packetId = inputStream.read();

                if (packetId == -1) {
                    exit = true;
                }

                packetHandler.handle(packetId, inputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void disconnect() {
        new SendPacketTask().execute(new Object[] {0});
        exit = true;
    }

    void sendCommand(String cmd) {
        new SendPacketTask().execute(new Object[]{1, cmd});
    }

    void handshake() {
        new SendPacketTask().execute(new Object[]{2});
    }

    class SocketConnectTask extends AsyncTask<String, Void, Socket> {

        protected Socket doInBackground(String... urls) {
            try {
                socket = new Socket(urls[0], 9432);
                outputStream = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return socket;
        }
    }

    class SendPacketTask extends AsyncTask<Object[], Void, Void> {

        protected Void doInBackground(Object[]... params) {
            for (Object[] paramsUnit : params) {
                int id = (int) paramsUnit[0];
                switch (id) {
                    case 0:
                        sendDisconnectPacket(id);
                        break;
                    case 1:
                        sendCommandPacket(id, (String) paramsUnit[1]);
                        break;
                    case 2:
                        sendHandshakePacket(id);
                        break;
                    default:
                        break;
                }
            }

            return null;
        }

        private void sendHandshakePacket(int id) {
            try {
                outputStream.write(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendDisconnectPacket(int id) {
            try {
                outputStream.write(id);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendCommandPacket(int id, String cmd) {
            try {
                outputStream.write(id);
                outputStream.writeUTF(cmd);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

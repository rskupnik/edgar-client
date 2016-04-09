package rskupnik.edgar.networking;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

import java.util.ArrayList;

public final class ConnectionService extends Service {

    /* BINDER */

    private final IBinder mBinder = new LocalBinder();

    public final class LocalBinder extends Binder {
        public ConnectionService getService() {
            return ConnectionService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (connection != null) {
            connection.disconnect();
            connection = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /* MESSENGER */

    final class IncomingHandler extends Handler { // Handler of incoming messages from clients.
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClients.add(msg.replyTo);
                    break;
                case MSG_CONNECT:
                    String ip = msg.getData().getString("ip");
                    connect(ip);
                    break;
                case MSG_SEND_HANDSHAKE:
                    handshake();
                    break;
                case MSG_SEND_COMMAND:
                    sendCommand(msg.getData().getString("cmd"));
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_HANDSHAKE_SUCCESFUL = 2;
    public static final int MSG_CONNECT = 3;
    public static final int MSG_SEND_HANDSHAKE = 4;
    public static final int MSG_SEND_COMMAND = 5;
    public static final int MSG_COMMAND_OUTPUT = 6;

    final Messenger mMessenger = new Messenger(new IncomingHandler());
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();

    /* CONNECTION */

    private Connection connection;

    private void connect(String ip) {
        connection = new Connection(this, ip);
        connection.start();
    }

    private void handshake() {
        if (connection != null) {
            connection.handshake();
        }
    }

    private void sendCommand(String cmd) {
        connection.sendCommand(cmd);
    }

    public void sendCommandOutput(String output) {
        for (Messenger messenger : mClients) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("output", output);
                Message msg = Message.obtain(null, MSG_COMMAND_OUTPUT);
                msg.setData(bundle);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendHandshakeSuccessMessage() {
        for (Messenger messenger : mClients) {
            try {
                Message msg = Message.obtain(null, MSG_HANDSHAKE_SUCCESFUL);
                messenger.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}

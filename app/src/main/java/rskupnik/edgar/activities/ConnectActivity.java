package rskupnik.edgar.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import rskupnik.edgar.networking.ConnectionService;
import rskupnik.edgar.R;

public class ConnectActivity extends AppCompatActivity {

    private EditText ipInput;

    Messenger connectionService;
    boolean bound = false;
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ConnectionService.MSG_HANDSHAKE_SUCCESFUL:
                    moveToCommandActivity();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            connectionService = new Messenger(iBinder);
            bound = true;

            try {
                Message msg = Message.obtain(null, ConnectionService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                connectionService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        ipInput = (EditText) findViewById(R.id.ipInput);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ConnectionService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(mConnection);
            bound = false;
        }
    }

    public void connectClicked(View view) {
        String ip = ipInput.getText().toString();
        if (bound) {
            try {
                Bundle bundle = new Bundle();
                bundle.putString("ip", ip);
                Message msg = Message.obtain(null, ConnectionService.MSG_CONNECT);
                msg.replyTo = mMessenger;
                msg.setData(bundle);
                connectionService.send(msg);

                msg = Message.obtain(null, ConnectionService.MSG_SEND_HANDSHAKE);
                msg.replyTo = mMessenger;
                connectionService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void moveToCommandActivity() {
        System.out.println("Moving to Command Activity");
        Intent intent = new Intent(this, CommandActivity.class);
        startActivity(intent);
    }
}

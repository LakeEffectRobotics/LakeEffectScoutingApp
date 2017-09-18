package ca.lakeeffect.scoutingapp;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.SharedPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

/**
 * Created by Ajay on 18/09/2017.
 *
 * Class that listens for an incoming connection. This is to reduce strain on the server making it so the server is only connected to 1 client at a time
 *
 */

public class ListenerThread implements Runnable{

    BluetoothSocket bluetoothSocket;

    OutputStream out = null;
    InputStream in = null;

    public ListenerThread(BluetoothSocket bluetoothSocket){
        this.bluetoothSocket = bluetoothSocket;
    }

    @Override
    public void run() {
        while(true) {
            try {
                final BluetoothServerSocket bss = ba.listenUsingRfcommWithServiceRecord("SteamworksScoutingApp", UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                bluetoothSocket = bss.accept();
                System.out.println("accepted");
                out = bluetoothSocket.getOutputStream();
                in = bluetoothSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
            if (bluetoothSocket.isConnected()) {
//                new BluetoothConnection(bluetoothSocket, out, in, MainActivity.this).start();
            }
        }
    }

    public void waitForConformation(){
        Thread thread = new Thread(){
            public void run(){
                while(true) {
                    System.out.println("aaaa");
                    byte[] bytes = new byte[1000];
                    try {
                        if(!connected){
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                            editor.apply();
                            return;
                        }
                        int amount = in.read(bytes);
                        if (new String(bytes, Charset.forName("UTF-8")).equals("done")) {
                            return;
                        }
                        if(!connected){
                            pendingmessages.add(robotNum + ":" + labels.toString() + ":"  + data.toString());
                            SharedPreferences prefs = getSharedPreferences("pendingmessages", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("message"+prefs.getInt("messageAmount",0), robotNum + ":" + labels.toString() + ":"  + data.toString());
                            editor.putInt("messageAmount", prefs.getInt("messageAmount",0)+1);
                            editor.apply();
                            return;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

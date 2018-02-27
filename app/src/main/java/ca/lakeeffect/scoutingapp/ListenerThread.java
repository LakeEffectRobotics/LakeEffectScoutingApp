package ca.lakeeffect.scoutingapp;

import android.bluetooth.BluetoothAdapter;
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

    MainActivity mainActivity;

    BluetoothSocket bluetoothSocket;
    BluetoothAdapter ba;

    OutputStream out = null;
    InputStream in = null;

    ConnectionThread connectionThread;

    Thread connectionThreadThreadClass;

    public ListenerThread(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        ba = BluetoothAdapter.getDefaultAdapter();

        while(true) {
            BluetoothServerSocket bss = null;
            try {
                System.out.println("started search");
//                ba.cancelDiscovery();
//                ba.
                bss = ba.listenUsingRfcommWithServiceRecord("SteamworksScoutingApp", UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                bluetoothSocket = bss.accept();
                System.out.println("accepted");
                out = bluetoothSocket.getOutputStream();
                in = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bluetoothSocket.isConnected()) {
//                new BluetoothConnection(bluetoothSocket, out, in, MainActivity.this).start();

                //call connection thread and break;

                connectionThread = new ConnectionThread(mainActivity, bluetoothSocket, out, in, bss);
                connectionThreadThreadClass = new Thread(connectionThread);
                connectionThreadThreadClass.start();
                break;
            }
        }
    }
}

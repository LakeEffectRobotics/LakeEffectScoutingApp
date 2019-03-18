package ca.lakeeffect.scoutingapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Ajay on 18/09/2017.
 *
 * Class that listens for an incoming connection. This is to reduce strain on the server making it so the server is only connected to 1 client at a time
 *
 */

public class ListenerThread implements Runnable{

    ListeningActitivty listeningActitivty;

    BluetoothSocket bluetoothSocket;
    BluetoothAdapter ba;

    OutputStream out = null;
    InputStream in = null;

    ConnectionThread connectionThread;

    Thread connectionThreadThreadClass;

    //if currently connected to a device
    boolean connected = false;

    boolean running = true;

    BluetoothServerSocket bss = null;

    public ListenerThread(ListeningActitivty listeningActitivty){
        this.listeningActitivty = listeningActitivty;
    }

    @Override
    public void run() {
        //not connected to a device
        connected = false;

        ba = BluetoothAdapter.getDefaultAdapter();

        while (running) {
            try {
                System.out.println("started search");
                bss = ba.listenUsingRfcommWithServiceRecord("LakeEffectScoutingApp", UUID.fromString("6ba6afdc-6a0a-4b1d-a2bf-f71ac108b636"));
                bluetoothSocket = bss.accept();
                System.out.println("accepted");
                out = bluetoothSocket.getOutputStream();
                in = bluetoothSocket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                //now connected to a device
                connected = true;

                //call connection thread and break;
                connectionThread = new ConnectionThread(listeningActitivty, bluetoothSocket, out, in, bss);
                connectionThreadThreadClass = new Thread(connectionThread);
                connectionThreadThreadClass.start();
                break;
            }
        }
    }
}

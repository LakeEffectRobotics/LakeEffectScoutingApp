package ca.lakeeffect.scoutingapp;

import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Ajay on 02/10/2017.
 *
 * This thread runs once the client is connected to the sever, it waits for the server to ask for the data
 */

public class ConnectionThread implements Runnable {

    MainActivity mainActivity;

    BluetoothSocket bluetoothSocket;

    OutputStream out = null;
    InputStream in = null;

    ArrayList<String> sentPendingMessages = new ArrayList<>();

    public ConnectionThread(MainActivity mainActivity, BluetoothSocket bluetoothSocket, OutputStream out, InputStream in){
        this.mainActivity = mainActivity;
        this.bluetoothSocket = bluetoothSocket;
        this.out = out;
        this.in = in;
    }

    @Override
    public void run() {

        String data = "";

        while(out != null && in != null && bluetoothSocket.isConnected()){
            try {
                byte[] bytes = new byte[100000];
                int amount = in.read(bytes);

                //if some bytes were sent, then we received something, then cut out the unused bytes (bytes array is very big because it must be the MAXIMUM amount of data you are willing to receive
                if(amount>0)  bytes = Arrays.copyOfRange(bytes, 0, amount);//puts data into bytes and cuts bytes
                else continue;

                String message = new String(bytes, Charset.forName("UTF-8"));
                if (message.contains("REQUEST")){ //received request
                    mainActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(mainActivity, "Sending Data",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                    sendData(data);
                    data = "";
                }else if (message.contains("RECEIVED")) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    in.close();
                    out.close();
                    bluetoothSocket.close();
                } else {
                    data += message;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        while(true){

            if(!bluetoothSocket.isConnected()){
                break;
            }
        }
    }

    public void sendData(String data){

    }

    public void deleteData(){ //deleted items that are in sent pending messages (because they now have been sent
        for(String message: new ArrayList<>(sentPendingMessages)){
            mainActivity.pendingmessages.remove(message);
            sentPendingMessages.remove(message);
        }
    }
}

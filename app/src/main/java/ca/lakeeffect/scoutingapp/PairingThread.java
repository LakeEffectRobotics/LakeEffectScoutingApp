package ca.lakeeffect.scoutingapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;

import java.util.Set;

/**
 * Created by Ajay on 3/3/2017.
 */
public class PairingThread implements Runnable {

    MainActivity activity;
    boolean firstTime;

    boolean successfullyPaired;

    public PairingThread(MainActivity activity, boolean firstTime){
        this.activity = activity;
        this.firstTime = firstTime;
    }

    @Override
    public void run() {
        //Get all paired devices
        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        boolean contains = false;
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getName().equals("2708 Server")) {
                contains = true;
                break;
            }
        }

        if (contains){//we're already paired, no need to pair
            activity.setupBluetoothConnections(null);
            activity.registerBluetoothListeners();
            return;
        }

        if(!firstTime){//if the second time, do not annoy scout
            startPairing(ba);
            return;
        }

        //Alert user about the pair, and pair
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final AlertDialog dialog = new AlertDialog.Builder(activity)
                        .setTitle("It is your first time")
                        .setMessage("The app is about to try to pair with the server through bluetooth. There might be some dialog popups, make sure to allow everything.\n\nPairing can take up to 2 minutes")
                        .setPositiveButton("Ok, I'm ready to accept the popups", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                startPairing(ba);
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setCancelable(false)
                        .show();
            }
        });
    }

    public void startPairing(final BluetoothAdapter ba){
        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if(device.getName().equals("2708 Server")){
                        successfullyPaired = true;

                        //cancel in pair if it is the server
                        ba.cancelDiscovery();

                        //setup connection and listeners
                        activity.setupBluetoothConnections(device.getAddress());
                        activity.registerBluetoothListeners();

                    }
                }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) && !successfullyPaired) {
                    activity.unregisterReceiver(this);

                    if(firstTime) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Pairing failed, will continue to pair in the background", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    //wait 10 seconds and try again
                    Thread thread = new Thread(){
                        public void run(){
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Thread thread = new Thread(new PairingThread(activity, false));
                            thread.start();
                        }
                    };
                    thread.start();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        activity.registerReceiver(mReceiver, filter);

        //start looking
        ba.startDiscovery();
    }

//    public void pairDevice(BluetoothDevice device) {
//        Intent intent = new Intent();
//        intent.putExtra(Context.EXTRA_DEVICE_ADDRESS, device.getAddress());
////        device.createBond(device);
////        try {
////            Method method = device.getClass().getMethod("createBond", (Class[]) null);
////            method.invoke(device, (Object[]) null);
////        } catch (Exception e) {
////            e.printStackTrace();
////        }
//    }

}

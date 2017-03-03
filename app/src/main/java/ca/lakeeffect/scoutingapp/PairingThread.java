package ca.lakeeffect.scoutingapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Ajay on 3/3/2017.
 */
public class PairingThread implements Runnable {

    MainActivity activity;

    public PairingThread(MainActivity activity){
        this.activity = activity;
    }

    @Override
    public void run() {
        //Get all paired devices
        final BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = ba.getBondedDevices();
        final BluetoothDevice[] devices = pairedDevices.toArray(new BluetoothDevice[0]);
        if (pairedDevices.size() <= 0) {
            System.exit(1);
        }
        boolean contains = false;
        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getName().equals("2708 Server")) {
                contains = true;
                break;
            }
        }

        if (!contains) return;

        //Alert user about the pair
        new AlertDialog.Builder(activity)
            .setTitle("It is your first time")
            .setMessage("The app is about to try to pair with the server through bluetooth. There might be some dialog popups, make sure to allow everything.")
            .setPositiveButton("Ok, I'm ready to accept the popups", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    //start looking
                    ba.startDiscovery();

                    BroadcastReceiver mReceiver = new BroadcastReceiver() {
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                                if(device.getName().equals("2708 Server")){
                                    //cancel in pair if it is the server
                                    ba.cancelDiscovery();

                                    //setup connection and listeners
                                    activity.setupBluetoothConnections(device.getAddress());
                                    activity.registerBluetoothListeners();

//                                    pairDevice(device);
//                                    device.getAddress();
                                }
                            }
                        }
                    };

                    IntentFilter filter = new IntentFilter();
                    filter.addAction(BluetoothDevice.ACTION_FOUND);

                    activity.registerReceiver(mReceiver, filter);
                }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
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

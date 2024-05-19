package com.dotscoket.readercard.acs_card_reader_thailand.src;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.dotscoket.readercard.acs_card_reader_thailand.interfaces.BroadcastUSBEvent;

public class DeviceConnectUSB {
    public  static void ReceivedBroadcastUSB(Context context, BroadcastUSBEvent eventCallback){
       BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UsbManager  mManager =  (UsbManager) context.getSystemService(Context.USB_SERVICE);
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {

                    for (UsbDevice device : mManager.getDeviceList().values()) {
                        eventCallback.onConnected(new Reader(mManager).isSupported(device));
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action) || UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                    eventCallback.onDisconnected();
                }


            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        context.registerReceiver(mUsbReceiver , filter);
        Log.d("cccza007", "mUsbReceiver Registered");
    }
}

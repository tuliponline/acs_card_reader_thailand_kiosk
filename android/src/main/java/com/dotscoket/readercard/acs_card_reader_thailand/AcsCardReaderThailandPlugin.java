package com.dotscoket.readercard.acs_card_reader_thailand;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.acs.smartcard.Reader;
import com.dotscoket.readercard.acs_card_reader_thailand.constant.MessageKey;
import com.dotscoket.readercard.acs_card_reader_thailand.interfaces.BroadcastUSBEvent;
import com.dotscoket.readercard.acs_card_reader_thailand.interfaces.SmartCardDeviceEvent;
import com.dotscoket.readercard.acs_card_reader_thailand.model.PersonalInformation;
import com.dotscoket.readercard.acs_card_reader_thailand.src.DeviceConnectUSB;
import com.dotscoket.readercard.acs_card_reader_thailand.src.SmartCardDevice;
import com.google.gson.Gson;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.EventChannel;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

/**
 * AcsCardReaderThailandPlugin
 */
public class AcsCardReaderThailandPlugin implements FlutterPlugin, MethodCallHandler, EventChannel.StreamHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private MethodChannel channel;
    private EventChannel messageChannel;
    private EventChannel.EventSink eventSink;
    private  Result result_pa;
    BroadcastReceiver mUsbReceiver;

    private Context context;
    private static final String TAG = "AcsCardReaderThailand";

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "acs_card_reader_thailand");
        channel.setMethodCallHandler(this);

        messageChannel = new EventChannel(flutterPluginBinding.getBinaryMessenger(), "eventChannelStream");
        messageChannel.setStreamHandler(this);


        this.context = flutterPluginBinding.getApplicationContext();
        DeviceConnectUSB.ReceivedBroadcastUSB(this.context, new BroadcastUSBEvent() {
            @Override
            public void onConnected(Boolean status) {
                Log.d(TAG, "USB Connected..");
                eventSink.success(status);
            }


            @Override
            public void onDisconnected() {
                Log.d(TAG, "USB Disconnected..");
                eventSink.success(false);
            }
        });
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
    }

    @Override
    public void onListen(Object arguments, EventChannel.EventSink events) {
        this.eventSink = events;
    }


    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        Log.d(TAG, call.method);
   
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("acs_card")) {
            result_pa = result;
            Log.d(TAG, "result_pa : " + result_pa);
            acsReaderCard(result);
        } else {
            result.notImplemented();
        }
    }


    void acsReaderCard(Result result) {
        SmartCardDevice.getSmartCardDevice(context, new SmartCardDeviceEvent() {
            @Override
            public void OnReady(SmartCardDevice device) {
                Toast.makeText(
                        context,
                        "OnReady",
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void OnDetached(SmartCardDevice device) {
                Toast.makeText(
                        context,
                        "Smart Card is removed",
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void OnErrory(String message) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @SuppressLint("LongLogTag")
            @Override
            public void OnSuceess(PersonalInformation personalInformation) {
                Log.d(TAG, "OnSuceess");
                Log.d(TAG, "personalInformation.Status  : " + personalInformation.Status);

                if(!personalInformation.Status && (personalInformation.Message_code == 0 || personalInformation.Message_code == 1) ){

                    acsReaderCard(result_pa);
                }else{

                    Gson gson = new Gson();
                    try {

                        result.success(gson.toJson(personalInformation));
                    } catch (Exception e) {
                        Log.d(TAG, e.getMessage());
                        acsReaderCard(result_pa);

                    }
                }

            }
        });
    }


    @Override
    public void onCancel(Object arguments) {

    }
}

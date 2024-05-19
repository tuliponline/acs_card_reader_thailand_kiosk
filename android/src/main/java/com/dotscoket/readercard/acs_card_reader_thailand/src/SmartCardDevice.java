package com.dotscoket.readercard.acs_card_reader_thailand.src;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.util.Log;
import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.dotscoket.readercard.acs_card_reader_thailand.constant.ApduCommand;
import com.dotscoket.readercard.acs_card_reader_thailand.constant.MessageKey;
import com.dotscoket.readercard.acs_card_reader_thailand.interfaces.SmartCardDeviceEvent;
import com.dotscoket.readercard.acs_card_reader_thailand.model.PersonalInformation;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TaskEvent;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TransmitParams;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TransmitProgress;
import com.google.android.gms.common.util.Base64Utils;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SmartCardDevice {
    private static final String ACTION_USB_PERMISSION = "ninkoman.smartcardreader.USB_PERMISSION";
    private static final String TAG = "SmartCardDevice";
    private static final String[] powerActionStrings = {"Power Down", "Cold Reset", "Warm Reset"};


    private static final int SLOTNUM = 0;

    private UsbManager mManager;
    private Reader mReader;
    private Context context;
    private SmartCardDeviceEvent eventCallback = null;
    private PendingIntent mPermissionIntent;
    PersonalInformation personalInformation = new PersonalInformation();
    private Boolean reading = false;
    private int loopConnect = 0;


    public SmartCardDevice(Context context, UsbManager manager, SmartCardDeviceEvent eventCallback) {

        this.context = context;
        this.mManager = manager;
        this.eventCallback = eventCallback;

        mReader = new Reader(mManager);

        mReader.setOnStateChangeListener(new Reader.OnStateChangeListener() {

            @Override
            public void onStateChange(int slotNum, int prevState, int currState) {

                if (prevState < Reader.CARD_UNKNOWN
                        || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN
                        || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

            }
        });

        mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
            ACTION_USB_PERMISSION),  PendingIntent.FLAG_IMMUTABLE);
        // if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        //     mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
        //         ACTION_USB_PERMISSION),  PendingIntent.FLAG_IMMUTABLE);
        //  }
        //  else
        //  {
        //     mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(
        //         ACTION_USB_PERMISSION),  PendingIntent.FLAG_UPDATE_CURRENT);
        //  }
     
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
      //  filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        context.registerReceiver(mReceiver, filter);
    }


    public void start() {
        // For each device

        if(loopConnect<5000){
            loopConnect++;
            personalInformation.Message_code = 0;
            personalInformation.Status = true;
            // show the list of available terminals

            if (mManager.getDeviceList().size() > 0) {
             
                int index = 0;
                for (UsbDevice device : mManager.getDeviceList().values()) {
                  

                    Log.d(TAG, "Devices Counr: " + mManager.getDeviceList().size());
                    Log.d(TAG, "Device: " + index + " : " + device.getDeviceName());
                   
                 
                    if (mReader.isSupported(device)) {
                        // If device name is found
                        // mManager.requestPermission(device, mPermissionIntent);
                        Log.d(TAG, "Device supported: " + device.getDeviceName());
                        OpenTask(device);
                        break;
                     
                    } else {
                        Log.d(TAG, "Device not supported: " + device.getDeviceName());
                        // personalInformation.Status = false;
                        // personalInformation.Message_code = MessageKey.NotSupport;
                        // eventCallback.OnSuceess(personalInformation);
                    }
                    if (index == mManager.getDeviceList().size() - 1) {
                        break;
                      }
                    index++;
                  

             

                  

                   
                }
            } else {
                personalInformation.Status = false;
                personalInformation.Message_code = MessageKey.NotFoundDevice;
                eventCallback.OnSuceess(personalInformation);
            }

        }else{
            loopConnect = 0;
            personalInformation.Status = false;
            personalInformation.Message_code = MessageKey.TimeOutConnect;
            eventCallback.OnSuceess(personalInformation);
        }
    }



    public static SmartCardDevice getSmartCardDevice(Context context, SmartCardDeviceEvent eventCallback) {

        UsbManager manager;
        HashMap<String, UsbDevice> deviceList;
        UsbDevice device = null;
        SmartCardDevice cardDevice = null;


        manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            Log.w(TAG, "USB manager not found");
            return null;
        }



        cardDevice = new SmartCardDevice(context, manager, eventCallback);


            cardDevice.start();



        return cardDevice;

    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            if(!reading && !mReader.isOpened()){
                reading = true;
                String action = intent.getAction();
                if (ACTION_USB_PERMISSION.equals(action)) {

                    synchronized (this) {
                        Log.d(TAG, "Opening USB:");
                        UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                            if (device != null) {
                                Log.d(TAG, "Opening reader:");
                                // SmartCardDevice.this.eventCallback.OnReady(SmartCardDevice.this);

                                //  OpenTask(device);

                                // show the list of available terminals

                            }

                        } else {

                            Log.d(TAG, "Permission denied for device");
                            personalInformation.Status = false;
                            personalInformation.Message_code = MessageKey.NotSupport;
                            eventCallback.OnSuceess(personalInformation);
                            new CloseTask().execute();

                        }
                    }

                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {

                    synchronized (this) {

                        Log.d(TAG, "Closing reader...");

                        new CloseTask().execute();
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    Log.d(TAG, "ACTION_USB_DEVICE_ATTACHED");

                }
            }

        }
    };


    void  OpenTask(UsbDevice params) {



          try {
            mReader.open(params);

              Log.d(TAG,"card reader name : "+ mReader.getReaderName());

              int numSlots = mReader.getNumSlots();
              Log.d(TAG, "Number of slots: " + numSlots);

              for (int i = 0; i < numSlots; i++) {
                  Log.d(TAG, "Number of slots: " + i);
              }

              PowerParams power = new PowerParams();
              power.slotNum = SLOTNUM; //0
              power.action = Reader.CARD_WARM_RESET;

              // Perform power action
              Log.d(TAG, "Slot " + numSlots + ": "
                      + powerActionStrings[power.action] + "...");
              new PowerTask().execute(power);

          } catch (IllegalArgumentException e) {
            personalInformation.Status = false;
            personalInformation.Message_code = MessageKey.OpenTaskError;

            eventCallback.OnSuceess(personalInformation);

            Log.d(TAG, "OpenTask error : " + e.toString());

            Log.e("TAG", e.getMessage());
        }
    }



    private class CloseTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            reading = false;
            mReader.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {


        }

    }


    private class PowerParams {

        public int slotNum;
        public int action;
    }

    private class PowerResult {

        public byte[] atr;
        public Exception e;
    }

    private class PowerTask extends AsyncTask<PowerParams, Void, PowerResult> {

        @Override
        protected PowerResult doInBackground(PowerParams... params) {

            PowerResult result = new PowerResult();

            try {

                result.atr = mReader.power(params[0].slotNum, params[0].action);

            } catch (Exception e) {

                result.e = e;

            }

            return result;
        }

        @Override
        protected void onPostExecute(PowerResult result) {

            if (result.e != null) {
               if(personalInformation.Status){
//                    personalInformation.Status = false;
//            personalInformation.Message_code = MessageKey.NotInserted;
//
//            eventCallback.OnSuceess(personalInformation);
                   start();
              Log.d(TAG, "PowerTask : " + result.e.toString());
               }

               // mReader.close();

            } else {

                // Show ATR
                if (result.atr != null) {

                    //  atr = result.atr;
                    logBuffer("ATR:", result.atr, result.atr.length);
                    // logBuffer(result.atr, result.atr.length);


                    // Set Parameters
                    SetProtocolParams params = new SetProtocolParams();
                    params.slotNum = SLOTNUM;
                    params.preferredProtocols = Reader.PROTOCOL_T0;

                    // Set protocol
                    Log.d(TAG, "Slot " + SLOTNUM + ":  Setting protocol to T0 ");

                    try { 
                        new SetProtocolTask().execute(params);
                    } catch (Exception e){
                        Log.d(TAG,"SetProtocolTask Error " + e.getMessage());
                    }
                   

                } else {

                    Log.d(TAG, "ATR: None");
                }
            }
        }
    }

    private class SetProtocolResult {

        public int activeProtocol;
        public Exception e;
    }

    private class SetProtocolParams {

        public int slotNum;
        public int preferredProtocols;
    }


    private class SetProtocolTask extends
            AsyncTask<SetProtocolParams, Void, SetProtocolResult> {

        @Override
        protected SetProtocolResult doInBackground(SetProtocolParams... params) {

            SetProtocolResult result = new SetProtocolResult();

            try {

                result.activeProtocol = mReader.setProtocol(params[0].slotNum,
                        params[0].preferredProtocols);

            } catch (Exception e) {

                result.e = e;
            }

            return result;
        }

        @Override
        protected void onPostExecute(SetProtocolResult result) {

            if (result.e != null) {

                Log.d(TAG, result.e.toString());

            } else {

                String activeProtocolString = "Active Protocol: ";

                switch (result.activeProtocol) {

                    case Reader.PROTOCOL_T0:
                        activeProtocolString += "T=0";
                        break;

                    case Reader.PROTOCOL_T1:
                        activeProtocolString += "T=1";
                        break;

                    default:
                        activeProtocolString += "Unknown";
                        break;
                }
                // Show active protocol
                Log.d(TAG, activeProtocolString);


                //  new TransmitTask1().execute(params);

                GetSelect();
            }
        }
    }

    void GetSelect() {
        // Transmit APDU Select
        Log.d(TAG, "Slot " + SLOTNUM + ": Transmitting APDU...");

        new TransmitTask(mReader, new TaskEvent() {

            @Override
            public void onSuccess(TransmitProgress response) {
                logBuffer("Applet:", response.response,response.responseLength);
                logBuffer("Command:", response.command, response.commandLength);

                GetData();
            }

            @Override
            public void onFailure(Exception e) {
                Log.d(TAG,e.getMessage());
            }
        }).execute(new TransmitParams(SLOTNUM, -1, ApduCommand.Select, null));
    }

    void GetData() {

        // Transmit APDU FullnameTH
        personalInformation.NameTH = transmitCommand(ApduCommand.THFullName).replace("#"," ").trim();


        // Transmit APDU FullnameEN
        personalInformation.NameEN = transmitCommand(ApduCommand.ENFullname).replace("#"," ").trim();

        // Transmit APDU CID
        personalInformation.PersonalID = transmitCommand(ApduCommand.CID);

        // Transmit APDU Datebirth
        personalInformation.BirthDate = transmitCommand(ApduCommand.Datebirth);

        // Transmit APDU Gender
        personalInformation.Gender = Integer.parseInt(transmitCommand(ApduCommand.Gender));

        // Transmit APDU Address
        personalInformation.Address =  transmitCommand(ApduCommand.Address).replace("####"," ").replace("#"," ").trim();

        // Transmit APDU CardIssuer
        personalInformation.CardIssuer =  transmitCommand(ApduCommand.CardIssuer).replace("####"," ").replace("#"," ").trim();

        // Transmit APDU IssueDate
        personalInformation.IssueDate =  transmitCommand(ApduCommand.IssueDate).replace("####"," ").replace("#"," ").trim();

        // Transmit APDU ExpireDate
        personalInformation.ExpireDate =  transmitCommand(ApduCommand.ExpireDate).replace("####"," ").replace("#"," ").trim();

        // Transmit APDU Photo Card
        personalInformation.PictureSubFix = transmitCommandPhotoCard().replaceAll("\\s+","");


        eventCallback.OnSuceess(personalInformation);
        mReader.close();
    }


    byte[] convertRequest(byte[] command) {
        byte[] request;
        if (mReader.getAtr(SLOTNUM)[0] == 0x3B && mReader.getAtr(SLOTNUM)[1] == 0x67) {
            request = new byte[]{0x00, (byte) 0xc0, 0x00, 0x01, command[command.length - 1]};
        } else {
            request = new byte[]{0x00, (byte) 0xc0, 0x00, 0x00, command[command.length - 1]};
        }

        return request;
    }



    String transmitCommand(byte[] command) {
        byte[] response = new byte[300];
        int responsLength;
        String  result = "---";
        try {
            mReader.transmit(SLOTNUM, command, command.length, response, response.length);
            responsLength = mReader.transmit(SLOTNUM, convertRequest(command), convertRequest(command).length, response, response.length);
            result = String.valueOf(new String(response, "TIS620").substring(0, responsLength - 2).trim());
         
            Log.d(TAG, result);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }catch (ReaderException e){
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return result;
    }

    String transmitCommandPhotoCard() {
        ByteArrayOutputStream photho_byte = new ByteArrayOutputStream();
        try {
            byte[] response = new byte[500];
            int responsLength;

            for (int i = 0; i < ApduCommand.PictureCard.length; i++) {
                mReader.transmit(SLOTNUM, ApduCommand.PictureCard[i], ApduCommand.PictureCard[i].length, response, response.length);
                responsLength = mReader.transmit(SLOTNUM, ApduCommand.Photo_Data, ApduCommand.Photo_Data.length, response, response.length);
                photho_byte.write(response, 0, responsLength - 2);
            }


        }catch (ReaderException e){
            e.printStackTrace();
            Log.d(TAG, e.getMessage());
        }
        return Base64Utils.encode(photho_byte.toByteArray());
    }





    private void logBuffer(String tag, byte[] buffer, int bufferLength) {

        String bufferString = "";

        for (int i = 0; i < bufferLength; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            if (i % 16 == 0) {

                if (bufferString != "") {

                    Log.d(TAG, bufferString);
                    bufferString = "";
                }
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        if (bufferString != "") {
            Log.d(TAG, tag + " " + bufferString);
        }
    }


    private String toHexString(int i) {

        String hexString = Integer.toHexString(i);
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        return hexString.toUpperCase();
    }

    private String toHexString(byte[] buffer) {

        String bufferString = "";

        for (int i = 0; i < buffer.length; i++) {

            String hexChar = Integer.toHexString(buffer[i] & 0xFF);
            if (hexChar.length() == 1) {
                hexChar = "0" + hexChar;
            }

            bufferString += hexChar.toUpperCase() + " ";
        }

        return bufferString;
    }



}



package com.dotscoket.readercard.acs_card_reader_thailand.src;

import android.os.AsyncTask;
import android.util.Log;

import com.acs.smartcard.Reader;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TaskEvent;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TransmitParams;
import com.dotscoket.readercard.acs_card_reader_thailand.model.TransmitProgress;

public class TransmitTask extends
        AsyncTask<TransmitParams, TransmitProgress, Void> {
    private static final String TAG = "TransmitTask";
    private TaskEvent mCallBack;
    Reader mReader;

    public TransmitTask(Reader mReader,TaskEvent callback) {
        this.mReader = mReader;
        mCallBack = callback;
    }
    @Override
    protected Void doInBackground(TransmitParams... params) {

        TransmitProgress progress = null;

        byte[] response;
        int responseLength = 0;
        byte[] request;
        int requestLength = 0;


        response = new byte[300];
        request = new byte[300];
        progress = new TransmitProgress();
        progress.controlCode = params[0].controlCode;
        try {

            if (params[0].controlCode < 0) {

                if(params[0].request_command!=null){
                    // Transmit APDU request

                    requestLength = mReader.transmit(params[0].slotNum,
                            params[0].request_command, params[0].request_command.length, request,
                            request.length);
                }

                // Transmit APDU
                responseLength = mReader.transmit(params[0].slotNum,
                        params[0].command, params[0].command.length, response,
                        response.length);

            } else {

                // Transmit control command
                responseLength = mReader.control(params[0].slotNum,
                        params[0].controlCode, params[0].command, params[0].command.length,
                        response, response.length);
            }

            progress.command = params[0].command;
            progress.commandLength = params[0].command.length;
            progress.response = response;
            progress.request = request;
            progress.requestLength = requestLength;
            progress.responseLength = responseLength;
            progress.e = null;

        } catch (Exception e) {

            progress.command = null;
            progress.commandLength = 0;
            progress.response = null;
            progress.responseLength = 0;
            progress.e = e;
        }

        publishProgress(progress);


        return null;
    }

    @Override
    protected void onProgressUpdate(TransmitProgress... progress) {

        if (progress[0].e != null) {

            Log.d(TAG, progress[0].e.toString());
            mCallBack.onFailure(progress[0].e);
        } else {


            mCallBack.onSuccess(progress[0]);


        }
    }
}
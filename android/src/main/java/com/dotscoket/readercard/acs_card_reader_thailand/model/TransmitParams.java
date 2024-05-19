package com.dotscoket.readercard.acs_card_reader_thailand.model;

public class TransmitParams {

    public int slotNum;
    public int controlCode;
    public byte[] command;
    public byte[] request_command;


    public TransmitParams(int slotNum, int controlCode, byte[] command, byte[] request_command) {
        this.slotNum = slotNum;
        this.controlCode = controlCode;
        this.command = command;
        this.request_command = request_command;
    }
}
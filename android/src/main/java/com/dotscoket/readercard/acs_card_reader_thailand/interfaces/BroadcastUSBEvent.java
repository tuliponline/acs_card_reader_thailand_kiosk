package com.dotscoket.readercard.acs_card_reader_thailand.interfaces;

public interface BroadcastUSBEvent {
    void onConnected(Boolean status);
    void onDisconnected();
}

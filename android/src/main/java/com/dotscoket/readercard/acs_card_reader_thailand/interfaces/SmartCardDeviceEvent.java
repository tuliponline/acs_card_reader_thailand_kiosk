package com.dotscoket.readercard.acs_card_reader_thailand.interfaces;


import com.dotscoket.readercard.acs_card_reader_thailand.model.PersonalInformation;
import com.dotscoket.readercard.acs_card_reader_thailand.src.SmartCardDevice;

public interface SmartCardDeviceEvent {
    void OnReady(SmartCardDevice device);
    void OnDetached(SmartCardDevice device);
    void OnErrory(String message);
    void OnSuceess(PersonalInformation personalInformation);
}

package com.dotscoket.readercard.acs_card_reader_thailand.model;

public interface TaskEvent {
    void  onSuccess(TransmitProgress response);
    void onFailure(Exception e);
}

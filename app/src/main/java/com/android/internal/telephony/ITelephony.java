package com.android.internal.telephony;

/**
 * Created by User on 6/17/2016.
 */
public interface ITelephony {

    boolean endCall();

    void answerRingingCall();

    void silenceRinger();
}
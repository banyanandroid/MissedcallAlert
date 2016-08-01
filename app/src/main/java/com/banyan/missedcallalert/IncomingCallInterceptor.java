package com.banyan.missedcallalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Sangavi on 6/24/2016.
 */
public class IncomingCallInterceptor extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String callingSIM = "";
        Bundle bundle = intent.getExtras();
        callingSIM =String.valueOf(bundle.getInt("simId", -1));
        if(callingSIM == "0"){

            System.out.println("callingSIM" + callingSIM);
            // Incoming call from SIM1
        }
        else if(callingSIM =="1"){
            // Incoming call from SIM2
            System.out.println("callingSIM" + callingSIM);
        }
    }
}
package com.android.internal.telephony;

import android.telephony.PhoneStateListener;

/**
 * Created by User on 6/24/2016.
 */
public interface MultiSimTelephonyManager {
    public String listen(PhoneStateListener listener, int events);
}

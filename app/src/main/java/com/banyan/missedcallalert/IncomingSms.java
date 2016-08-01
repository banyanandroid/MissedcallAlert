package com.banyan.missedcallalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.banyan.missedcallalert.Global.Config;
import com.banyan.missedcallalert.Global.SessionManager;
import com.banyan.missedcallalert.appcontroller.AppController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class IncomingSms extends BroadcastReceiver {

    SessionManager session;
    String senderNum, message, TAG, messages;
    int duration;

    // Get the object of SmsManager
    final SmsManager sms = SmsManager.getDefault();

    public void onReceive(Context context, Intent intent) {

        // Retrieves a map of extended data from the intent.
        final Bundle bundle = intent.getExtras();

        try {

            if (bundle != null) {

                final Object[] pdusObj = (Object[]) bundle.get("pdus");

                for (int i = 0; i < pdusObj.length; i++) {

                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                    String phoneNumber = currentMessage.getDisplayOriginatingAddress();

                    senderNum = phoneNumber;
                    messages = currentMessage.getDisplayMessageBody().trim().replaceAll(" ", "_");
                    message = messages.replaceAll("\n", "_");

                    Log.i("SmsReceiver", "senderNum: " + senderNum + "; message: " + message);

                    duration = Toast.LENGTH_LONG;

                } // end for loop

                log_action();

                Toast toast = Toast.makeText(context, "senderNum: " + senderNum + ", message: " + message, duration);
                toast.show();

            } // bundle is null

        } catch (Exception e) {
            Log.e("SmsReceiver", "Exception smsReceiver" + e);

        }
    }

    public void log_action() {

        String str_name_msg = MainActivity.str_name;
        String str_id_msg = MainActivity.str_id;
        String str_date_msg = MainActivity.formattedDate;

        System.out.println("NAME MSG : " + str_name_msg);
        System.out.println("ID MSG : " + str_id_msg);
        System.out.println("DATE MSG : " + str_date_msg);

        String str_url = "http://www.crbsms.net/missed_call/?savedata&key=556677&receivedby=" + Config.imei + "&userid=" + str_id_msg + "&username=" + str_name_msg + "&mtype=sms&mobile=" + senderNum + "&text=" + message + "&dat=" + str_date_msg;
        System.out.println("URL  : " + str_url);

        String tag_string_req = "req_login";
        //   PD.show();
        StringRequest strReq = new StringRequest(Request.Method.GET, str_url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "Response:" + response.toString());
                        System.out.println("RESPONSE 1 : " + response.toString());
                        try {
                            System.out.println("RESPONSE : " + response.toString());
                            JSONObject jObj = new JSONObject(response);

                            int success = jObj.getInt("success");


                            if (success == 1) {

                                System.out.println("SUCESS");

                            } else {
                                System.out.println("FAIL");
                                String errorMsg = jObj.getString("error_Msg");

                                Log.v("JSONNNNNNNNNNNN", errorMsg);

                            }
                            // PD.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {

                Log.e(TAG, "Login Error: " + error.getMessage());

            }
        });

        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }


}
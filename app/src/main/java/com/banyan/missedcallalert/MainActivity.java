package com.banyan.missedcallalert;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.MultiSimTelephonyManager;
import com.android.internal.telephony.TelephonyInfo;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.banyan.missedcallalert.Global.Config;
import com.banyan.missedcallalert.Global.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    boolean ring = false;
    SessionManager session;
    static boolean callReceived = false;
    private CountDownTimer countDownTimer;
    private boolean timerHasStarted = false;
    private final long startTime = 1 * 1000;
    private final long interval = 1 * 3000;
    TelephonyManager tel;
    TextView ph_no;
    String imei_num_str, TAG, str_from_number, smsNum, message;
    ProgressDialog pDialog;
    public static RequestQueue queue;
    public static String str_name, str_id, formattedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ph_no = (TextView) findViewById(R.id.ph_no);

        session = new SessionManager(getApplicationContext());
        session.checkLogin();
        getSupportActionBar().setTitle(R.string.app_names);

        // get user data from session
        HashMap<String, String> user = session.getUserDetails();
        // name
        str_name = user.get(SessionManager.KEY_LOGIN_ID);
        // id
        str_id = user.get(SessionManager.KEY_TYPE_ID);
        // Detect Missed Call function
        try {
            TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            TelephonyMgr.listen(new TeleListener(),
                    PhoneStateListener.LISTEN_CALL_STATE);

        } catch (Exception e) {
            // TODO: handle exception
        }

        /*******************************************
         * Current Date and Time
         ******************************************/

        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        // formattedDate have current date/time
        formattedDate = df.format(c.getTime());


        /***********************************
         * IMEI NUMBER FOR DUAL SIM
         * **********************************/

        TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(this);

        String imsiSIM1 = telephonyInfo.getImsiSIM1();
        String imsiSIM2 = telephonyInfo.getImsiSIM2();
        Config.imei = imsiSIM1;

        boolean isSIM1Ready = telephonyInfo.isSIM1Ready();
        boolean isSIM2Ready = telephonyInfo.isSIM2Ready();

        boolean isDualSIM = telephonyInfo.isDualSIM();

        System.out.println(" IME1 : " + imsiSIM1 + "\n" +
                " IME2 : " + imsiSIM2 + "\n" +
                " IS DUAL SIM : " + isDualSIM + "\n" +
                " IS SIM1 READY : " + isSIM1Ready + "\n" +
                " IS SIM2 READY : " + isSIM2Ready + "\n");

    }

    /*********************************************
     * TeleListener Function
     *********************************************/
    class TeleListener extends PhoneStateListener {
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            ph_no.setText(incomingNumber);
            str_from_number = incomingNumber;
            switch (state) {

                case TelephonyManager.CALL_STATE_IDLE:

                    if (ring == true && callReceived == false) {

                        Toast.makeText(getApplicationContext(),
                                "Missed call from : " + incomingNumber,
                                Toast.LENGTH_LONG).show();
                    }
                    // CALL_STATE_IDLE;
                    Toast.makeText(getApplicationContext(), "CALL_STATE_IDLE",
                            Toast.LENGTH_LONG).show();
                    break;

                case TelephonyManager.CALL_STATE_OFFHOOK:
                    // CALL_STATE_OFFHOOK;
                    callReceived = true;
                    Toast.makeText(getApplicationContext(), "CALL_STATE_OFFHOOK",
                            Toast.LENGTH_LONG).show();
                    break;

                case TelephonyManager.CALL_STATE_RINGING:
                    ring = true;
                    countDownTimer = new MyCountDownTimer(startTime, interval);
                    countDownTimer.start();
                    timerHasStarted = true;

                    try {

                        pDialog = new ProgressDialog(MainActivity.this);
                        pDialog.setMessage("Please wait...");
                        pDialog.setCancelable(true);
                        queue = Volley.newRequestQueue(MainActivity.this);
                        missed();

                    } catch (Exception e) {
                        // TODO: handle exception
                    }

                    // CALL_STATE_RINGING
                    Toast.makeText(getApplicationContext(), incomingNumber,
                            Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), "CALL_STATE_RINGING",
                            Toast.LENGTH_LONG).show();
                    break;

                default:
                    break;
            }
        }

    }

    /********************************************
     * TIMER FOR CUT THE CALL
     *******************************************/

    public class MyCountDownTimer extends CountDownTimer {
        public MyCountDownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onFinish() {

            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            Class clazz = null;
            try {
                clazz = Class.forName(telephonyManager.getClass().getName());
                Method method = null;
                method = clazz.getDeclaredMethod("getITelephony");
                method.setAccessible(true);
                ITelephony telephonyService = null;
                telephonyService = (ITelephony) method.invoke(telephonyManager);
                telephonyService.endCall();

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onTick(long millisUntilFinished) {

        }
    }

    /*****************************************************
     * MISSED CALL VOLLEY REQUEST
     ****************************************************/
    public void missed() {

        tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        imei_num_str = tel.getDeviceId().toString();
        String simno = tel.getSimSerialNumber();
        String getSimOperator = tel.getSimOperator();
        String getSimNumber = tel.getLine1Number();
        String getVoiceMailNumber = tel.getVoiceMailNumber();
        int slots = tel.getCallState();
        System.out.println("imei__ " + imei_num_str);
        System.out.println("Simno " + simno);
        System.out.println("Slot" + slots);
        System.out.println("getVoiceMailNumber " + getVoiceMailNumber);
        System.out.println("getSimSerialNumber " + getSimOperator);
        System.out.println("getSimNumber " + getSimNumber);

        /*************************************************************
        DUAL PHONE WORKING STATE VERIFICATION
        *************************************************************/

        try {
            final Class<?> tmClass = Class.forName("android.telephony.MultiSimTelephonyManager");
            // MultiSimTelephonyManager Class found
            // getDefault() gets the manager instances for specific slots
            Method methodDefault = tmClass.getDeclaredMethod("getDefault", int.class);
            methodDefault.setAccessible(true);
            try {
                for (int slot = 0; slot < 2; slot++) {
                    MultiSimTelephonyManager telephonyManagerMultiSim = (MultiSimTelephonyManager)methodDefault.invoke(null, slot);
                    telephonyManagerMultiSim.listen(new MultiSimListener(slot), PhoneStateListener.LISTEN_CALL_STATE);
                    String namne = telephonyManagerMultiSim.listen(new MultiSimListener(slot), PhoneStateListener.LISTEN_CALL_STATE);
                    System.out.println("name" +namne);
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                // (Not tested) the getDefault method might cause the exception if there is only 1 slot
            }
        } catch (ClassNotFoundException e) {
            //
        } catch (NoSuchMethodException e) {
            //
        } catch (IllegalAccessException e) {
            //
        } catch (InvocationTargetException e) {
            //
        } catch (ClassCastException e) {
            //
        }

        Config.MISSED_URL = "http://www.crbsms.net/missed_call/?savedata&key=556677&receivedby="+imei_num_str+"&userid=" + str_id + "&username=" + str_name + "&mtype=call&mobile=" + str_from_number + "&dat=" + formattedDate;
        System.out.println("url" + Config.MISSED_URL);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.MISSED_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //If we are getting success from server
                        try {

                            JSONObject obj = new JSONObject(response);
                            System.out.println("OBJECT" + " : " + obj);
                            JSONArray posts_arr = obj.getJSONArray("posts");
                            JSONObject success_obj = (JSONObject) posts_arr
                                    .get(0);


                            String Success = success_obj.getString("Success");

                            Config.Success = Success;


                            System.out.println("userId" + Success);

                            System.out.println("status" + success_obj);

                        } catch (Exception e) {
// TODO Auto-generated catch block
                            System.out.println("inside catch" + e);
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //You can handle error here if you want
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //Adding parameters to request

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(MainActivity.this);
        requestQueue.add(stringRequest);
    }

    public class MultiSimListener extends PhoneStateListener {

        private Field subscriptionField;
        private int simSlot = -1;

        public MultiSimListener (int simSlot) {
            super();
            try {
                // Get the protected field mSubscription of PhoneStateListener and set it
                subscriptionField = this.getClass().getSuperclass().getDeclaredField("mSubscription");
                subscriptionField.setAccessible(true);
                subscriptionField.set(this, simSlot);
                this.simSlot = simSlot;
            } catch (NoSuchFieldException e) {

            } catch (IllegalAccessException e) {

            } catch (IllegalArgumentException e) {

            }
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            // Handle the event here, with state, incomingNumber and simSlot
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:

                //Logout option usin session manager
                session.logoutUser();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

}



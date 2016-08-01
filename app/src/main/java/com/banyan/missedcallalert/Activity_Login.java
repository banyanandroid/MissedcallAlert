package com.banyan.missedcallalert;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.View;
import android.widget.EditText;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Activity_Login extends AppCompatActivity {

    //Defining views
    private EditText editTextEmail;
    private EditText editTextPassword;
    private AppCompatButton buttonLogin;
    SessionManager session;
    public static RequestQueue queue;

    //boolean variable to check user is logged in or not
    //initially it is false
    private boolean loggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setTitle("callAlert");

        //Initializing views
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);

        //Session
        session = new SessionManager(getApplicationContext());

        /*************************************************************
         * CALENDAR TO GET CURRENT DATE AND TIME
         **********************************************************/
        Calendar c = Calendar.getInstance();
        System.out.println("Current time => " + c.getTime());

        //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        // formattedDate have current date/time

        buttonLogin = (AppCompatButton) findViewById(R.id.buttonLogin);

        //Adding click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    queue = Volley.newRequestQueue(Activity_Login.this);
                    login();

                } catch (Exception e) {
// TODO: handle exception
                }

            }
        });
    }

    /*************************************************************
     * BACK PRESS TO LOGIN
     **********************************************************/

    @Override
    public void onBackPressed() {
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    /*************************************************************
     * JSON FOR LOGIN TO GET DETAILS OF USER NAME AND ID
     **********************************************************/

    private void login() {
        //Getting values from edit texts
        final String user = editTextEmail.getText().toString().trim();
        final String pass = editTextPassword.getText().toString().trim();

        Config.LOGIN_URL = "http://www.crbsms.net/missed_call/?auth&key=556677&user=" + user + "&pass=" + pass;
        System.out.println("url" + Config.LOGIN_URL);

        //Creating a string request
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config.LOGIN_URL,
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
                            JSONObject status = success_obj.getJSONObject("Success");

                            String userId = status.getString("UserId");
                            String userName = status.getString("UserName");

                            Config.UserId = userId;
                            Config.UserName = userName;
                            Config.Pass = pass;

                            System.out.println("userId" + userId);
                            System.out.println("userName" + userName);

                            session.createLoginSession(userName, userId);
                            //Starting profile activity
                            Intent intent = new Intent(Activity_Login.this, MainActivity.class);
                            startActivity(intent);

                            System.out.println("status" + status);

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
                params.put(Config.KEY_USER, user);
                params.put(Config.KEY_PASSWORD, pass);

                //returning parameter
                return params;
            }
        };

        //Adding the string request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


}


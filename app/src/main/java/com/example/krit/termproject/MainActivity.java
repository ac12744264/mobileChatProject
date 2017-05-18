package com.example.krit.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.LocaleDisplayNames;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    EditText editText_username, editText_password;
    Button button_login;
    String text;
    HashMap<String,String> hashMap = new HashMap<>();
    JSONObject jsonObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        begin();
        if(pref.getString("username",null) != null){
            startActivity(new Intent(MainActivity.this,ContactActivity.class));
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setContentView(R.layout.activity_main);
        begin();
    }

    public void begin(){
        pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        editor = pref.edit();
        editText_username = (EditText) findViewById(R.id.editText_username);
        editText_password = (EditText) findViewById(R.id.editText_password);
        button_login = (Button) findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hashMap.put("username", editText_username.getText().toString());
                hashMap.put("password",editText_password.getText().toString());
                Signin signin = new Signin();
                signin.execute();
            }
        });
        editor.clear();
        editor.commit();
    }

    private class Signin extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpHelper httpHelper = new OkHttpHelper();
            try {
                Log.d("test","aaa");
                text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/signIn",hashMap);
                Log.d("test",text);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                jsonObject = new JSONObject(text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            try {
                if(jsonObject.getString("type").equalsIgnoreCase("error")) {
                    Toast.makeText(getBaseContext(),jsonObject.getString("content"), Toast.LENGTH_SHORT).show();
                }
                else if(jsonObject.getString("type").equalsIgnoreCase("sessionid")){
//                    Toast.makeText(getBaseContext(),jsonObject.getString("content"), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this,ContactActivity.class);
                    String session = jsonObject.getString("content");
                    Log.d("session",session);
                    editor.putString("session",session);
                    editor.putString("username",editText_username.getText().toString());
                    editor.commit();

                    startActivity(intent);

                }
                else {
                    Toast.makeText(getBaseContext(),"Error", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

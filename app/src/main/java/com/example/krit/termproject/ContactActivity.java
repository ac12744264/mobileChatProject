package com.example.krit.termproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactActivity extends AppCompatActivity {

    Button addButton;
    ListView listView;
    HashMap<String,String> hashMap;
    String session;
    String username;
    String text;
    JSONObject jsonObject;
    JSONArray jsonArray;
    ArrayList<String> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        begin();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setContentView(R.layout.activity_contact);
        begin();
    }

    private void begin(){
        hashMap = new HashMap<>();
        arrayList = new ArrayList<>();
        addButton = (Button) findViewById(R.id.addButton);
        listView = (ListView) findViewById(R.id.list);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref",MODE_PRIVATE);
        session = pref.getString("session",null);
        username = pref.getString("username",null);

        hashMap.put("sessionid",session);
        GetContact getContact = new GetContact();
        getContact.execute();

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactActivity.this,SearchActivity.class);
                startActivity(intent);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent (ContactActivity.this,ChatActivity.class);
                intent.putExtra("friend",arrayList.get(position));
                startActivity(intent);
            }
        });
    }

    public class GetContact extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpHelper httpHelper = new OkHttpHelper();
            try {
                text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/getContact",hashMap);
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
                jsonArray = jsonObject.getJSONArray("content");
                for(int i = 0; i < jsonArray.length(); i++){
                    arrayList.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ContactActivity.this,android.R.layout.simple_list_item_1,arrayList);
            listView.setAdapter(adapter);
        }
    }


}

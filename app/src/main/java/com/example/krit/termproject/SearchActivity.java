package com.example.krit.termproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {

    String session;
    String username;
    EditText input_searchname;
    Button search_button;
    ListView listView;
    String text;
    JSONObject jsonObject;
    JSONArray jsonArray;
    ArrayList<String> arrayList = new ArrayList<>();
    HashMap<String,String> hashMap_add = new HashMap<>();
    HashMap<String,String> hashMap_search = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    private void init(){
        input_searchname = (EditText) findViewById(R.id.input_searchname);
        search_button = (Button) findViewById(R.id.searchbutton);
        listView = (ListView) findViewById(R.id.searchList);
        Intent intent = getIntent();
        session = intent.getStringExtra("session");
        username = intent.getStringExtra("username");

        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayList = new ArrayList<String>();
                hashMap_search.put("sessionid",session);
                hashMap_search.put("keyword",input_searchname.getText().toString());
                Retreiver_search retreiver_search = new Retreiver_search();
                retreiver_search.execute();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Retreiver_add retreiver_add = new Retreiver_add();
                hashMap_add.put("sessionid",session);
                hashMap_add.put("username",arrayList.get(position).toString());
                retreiver_add.execute();
            }
        });

    }

    private class Retreiver_search extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpHelper httpHelper = new HttpHelper();
            text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/searchUser", hashMap_search);
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
                for (int i = 0; i < jsonArray.length(); i++) {
                    arrayList.add(jsonArray.getString(i));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(SearchActivity.this,android.R.layout.simple_list_item_1,arrayList);
            listView.setAdapter(adapter);
        }
    }

    private class Retreiver_add extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            HttpHelper httpHelper = new HttpHelper();
            text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/addContact", hashMap_add);
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
            Toast.makeText(getBaseContext(),"Added Success", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SearchActivity.this,ContactActivity.class);
            intent.putExtra("session",session);
            intent.putExtra("username",username);
            startActivity(intent);

        }
    }
}

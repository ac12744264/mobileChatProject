package com.example.krit.termproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatActivity extends AppCompatActivity {

    EditText messageEdit;
    ListView listView;
    Button chatSendButton;
    String session;
    String username;
    String friend;
    String text;
    JSONObject jsonObject;
    JSONArray jsonArray;
    JSONArray jsonArrayChatAll;
    ArrayList<String> arrayList = new ArrayList<>();
    HashMap<String,String> hashMap_getMsg;
    HashMap<String,String> hashMap_postMsg;

    ChatArrayAdapter chatArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        init();
    }

    public void init(){
        arrayList = new ArrayList<>();
        hashMap_getMsg = new HashMap<>();
        hashMap_postMsg = new HashMap<>();
        Intent intent = getIntent();
        session = intent.getStringExtra("session");
        username = intent.getStringExtra("username");
        friend = intent.getStringExtra("friend");
        chatSendButton = (Button) findViewById(R.id.chatSendButton);
        messageEdit = (EditText) findViewById(R.id.messageEdit);
        listView = (ListView) findViewById(R.id.messageContainer);
        jsonArray = new JSONArray();

        getSupportActionBar().setTitle(friend);

        hashMap_getMsg.put("sessionid",session);
        hashMap_getMsg.put("seqno","0");
        hashMap_getMsg.put("limit","1000");

        Retriever_getMsg retriever_getMsg = new Retriever_getMsg();
        retriever_getMsg.execute();

        chatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                hashMap_postMsg = new HashMap<>();
                hashMap_postMsg.put("sessionid",session);
                hashMap_postMsg.put("targetname",friend);
                hashMap_postMsg.put("message",messageEdit.getText().toString());
                Retriever_postMsg retriever_postMsg = new Retriever_postMsg();
                retriever_postMsg.execute();
            }
        });
    }

    private class Retriever_postMsg extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpHelper httpHelper = new OkHttpHelper();
            try {
                text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/postMessage",hashMap_postMsg);
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
            init();
            messageEdit.setText(null);
        }
    }


    private class Retriever_getMsg extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... params) {
            OkHttpHelper httpHelper = new OkHttpHelper();
            try {
                text = httpHelper.POST("https://mis.cp.eng.chula.ac.th/mobile/service.php?q=api/getMessage",hashMap_getMsg);
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

            chatArrayAdapter = new ChatArrayAdapter(ChatActivity.this, R.layout.activity_chat_singlemessage);
            listView.setAdapter(chatArrayAdapter);

            try {
                jsonArrayChatAll = jsonObject.getJSONArray("content");
                for(int i = 0; i < jsonArrayChatAll.length(); i++){
                    jsonObject = jsonArrayChatAll.getJSONObject(i);
                    String segno = jsonObject.getString("seqno");
                    String from =jsonObject.getString("from");
                    String to = jsonObject.getString("to");
                    String message = jsonObject.getString("message");
                    String datetime = jsonObject.getString("datetime");
//                    if((from.equals(username) && to.equals(friend))||
//                            (from.equals(friend)&&to.equals(username))){
//                        jsonArray.put(jsonObject);
//                        arrayList.add(from+" : "+message);
//                    }
                    if(from.equals(username) && to.equals(friend)){
                        chatArrayAdapter.add(new ChatMessage(false,message));
                    }
                    else if(from.equals(friend) && to.equals(username)){
                        chatArrayAdapter.add(new ChatMessage(true,message));
                    }
                }
                Log.d("check",jsonArray.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            listView.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            listView.setAdapter(chatArrayAdapter);


//            ArrayAdapter<String> adapter = new ArrayAdapter<String>(ChatActivity.this,android.R.layout.simple_list_item_1,arrayList);
//            listView.setAdapter(adapter);
        }
    }
}

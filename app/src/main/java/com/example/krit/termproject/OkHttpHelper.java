package com.example.krit.termproject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by AC127 on 5/9/2017.
 */

public class OkHttpHelper {
    OkHttpClient client;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public String GET(String url) throws IOException {

        client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public String POST(String url, HashMap<String,String> hashMap) throws IOException {
        client = new OkHttpClient();

        // Initialize Builder (not RequestBody)
        FormBody.Builder builder = new FormBody.Builder();
        // Add Params to Builder
        for ( Map.Entry<String, String> entry : hashMap.entrySet() ) {
            builder.add( entry.getKey(), entry.getValue() );
        }

        // Create RequestBody
        RequestBody formBody = builder.build();

        // Create Request
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}

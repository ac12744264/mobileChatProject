package com.example.krit.termproject;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by AC127 on 5/8/2017.
 */

public class HttpHelper {
    private String connect(String urlstring, String method, String param) {
        HttpURLConnection conn = null;
        InputStream is = null;
        URL url = null;
        try {
            url = new URL(urlstring);
            // Log.v("URLHelper", "connect to URL "+urlstring);
        } catch (MalformedURLException e) {
            // Log.e("URLHelper","connect - MalformedURL" + e.getMessage());
            return null;
        }

        try {
            conn = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if (conn instanceof HttpsURLConnection) {

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs,
                                               String authType) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs,
                                               String authType) throws CertificateException {

                }

            }};

            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts,
                        new java.security.SecureRandom());
                ((HttpsURLConnection) conn).setSSLSocketFactory(sslContext
                        .getSocketFactory());
            } catch (KeyManagementException e) {
                e.printStackTrace();
                return null;
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {

                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            // Install the all-trusting host verifier
            HttpsURLConnection
                    .setDefaultHostnameVerifier(allHostsValid);

        }

        try {
            conn.setRequestMethod(method);
        } catch (ProtocolException e) {
            // Log.e("URLHelper","connect ProtocolException" + e.getMessage());
            return null;
        }

        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setDoInput(true);

        if (method == "POST") {
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            conn.setFixedLengthStreamingMode(param.getBytes().length);
        }

        // Starts the query
        try {
            if (method == "POST") {
                OutputStream out;
                out = conn.getOutputStream();
                out.write(param.getBytes());
                out.close();
            }
            conn.connect();
            int response = conn.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
                // Log.e("URLHelper","connect - HTTP Error " + response);
                return null;
            }
            is = new BufferedInputStream(conn.getInputStream());
        } catch (IOException e) {
            Log.e("URLHelper", "connect - IOException get/post "
                    + e.getClass().getName() + " " + e.getMessage());
            return null;
        }
        if (!url.getHost().equals(conn.getURL().getHost())) {
            // Log.e("URLHelper","connect - redirection");
            return null;
        }

        // Convert the InputStream into a string
        char[] buf = new char[4096];
        Reader reader;
        String dat = new String();
        try {
            reader = new InputStreamReader(is, "UTF-8");
            while (reader.read(buf, 0, 4096) != -1) {
                dat += new String(buf).trim();
            }
            is.close();
        } catch (IOException e) {
            // Log.e("URLHelper","connect - IOException error reading stream");
            return null;
        }

        conn.disconnect();

        return dat;
    }

    public String POST(String urlstring, String param) {
        return connect(urlstring, "POST", param);
    }

    public String GET(String urlstring) {
        return connect(urlstring, "GET", null);
    }

    public JSONObject GETJson(String urlstring) {
        String jsonString = GET(urlstring);
        //jsonString = Html.fromHtml(jsonString).toString();
        //jsonString = jsonString.replaceAll("[\u0000-\u001f]", "");
        if (jsonString == null) {
            return null;
        }
        // Log.v(this.getClass().getSimpleName(),jsonString);

        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            Log.e("JSON Error", jsonString);
            e.printStackTrace();
            obj = null;
        } catch (NumberFormatException ne) {
            Log.e(this.getClass().getSimpleName(), ne.getMessage());
            Log.e("JSON Error", jsonString);
            ne.printStackTrace();
            obj = null;
        }
        // Log.v(this.getClass().getSimpleName(), jsonString);
        return obj;
    }

    public String URLEncode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(this.getClass().getName(), e.getMessage());
            return value;
        }
    }

    public String POST(String urlstring, HashMap<String, String> parms) {
        String param = "";
        Set<String> keys = parms.keySet();
        for (String key : keys) {
            String value = parms.get(key);
            param += key + "=" + URLEncode(value);
            param += "&";
        }

        String str = POST(urlstring, param);
        if (str == null) {
            Log.v(this.getClass().getSimpleName(), "No connection");
        } else {
            Log.v(this.getClass().getSimpleName(), str);
        }
        return str;
    }

    public JSONObject POSTJson(String urlstring, HashMap<String, String> parms) {
        String jsonString = POST(urlstring, parms);
        if (jsonString == null) {
            return null;
        }
        JSONObject obj = null;
        try {
            obj = new JSONObject(jsonString);
        } catch (JSONException e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage());
            Log.e("JSON Error", jsonString);
            e.printStackTrace();
            obj = null;
        } catch (NumberFormatException ne) {
            Log.e(this.getClass().getSimpleName(), ne.getMessage());
            Log.e("JSON Error", jsonString);
            ne.printStackTrace();
            obj = null;
        }
        // Log.v(this.getClass().getSimpleName(), jsonString);
        return obj;
    }

    public boolean downloadToFile(String urlString, File file) {
        try {
            Log.i("downlooad", "start .. " + urlString);
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();

            File path = file.getParentFile();
            if (!path.exists()) {
                path.mkdirs();
            }

            DataInputStream is = new DataInputStream(conn.getInputStream());
            DataOutputStream os = new DataOutputStream(new FileOutputStream(
                    file, false));

            byte[] buffer = new byte[conn.getContentLength()];
            is.readFully(buffer);
            os.write(buffer);
            os.flush();

            is.close();
            os.close();

            Log.i("downlooad", "done .. " + urlString);

        } catch (MalformedURLException e) {

            e.printStackTrace();
        } catch (IOException e) {

            e.printStackTrace();
        }

        return true;
    }
}

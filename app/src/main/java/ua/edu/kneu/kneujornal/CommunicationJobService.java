package ua.edu.kneu.kneujornal;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class CommunicationJobService extends Service {
    private SharedPreferences mSettings;
    private String token;
    dataBaseHelper myDbHelper;
    private WebSocket ws = null;

    private void ServerConnect(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://home-server.ddns.ukrtel.net:1337").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();

        //Log.i("KNEU_TOPCHIK","Start");
    }

    @Override
    public void onCreate() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = mSettings.getString("auth_token","");
        myDbHelper = new dataBaseHelper(this);


        try {
            myDbHelper.createDataBase();
        } catch (IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            myDbHelper.openDataBase();
        }catch(SQLiteException sqle){
            throw sqle;
        }

        ServerConnect();

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
        {
            switch (intent.getStringExtra("action")){
                case "chek_login":
                    if (token.isEmpty())
                        LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(MainActivity.ACTION_MAIN_RECEIVER)
                                .putExtra("action","no_login"));
                    break;
                case "sign_in":
                    JSONObject obj = new JSONObject();
                    try {
                        obj.put("action","login");
                        obj.put("email",intent.getStringExtra("login"));
                        obj.put("pass",intent.getStringExtra("pass"));

                        ws.send(obj.toString());
                    } catch (Exception e) {
                        Log.w("KNEUJornal",e.getLocalizedMessage());
                    }


                    break;
                case "sign_out":
                    obj = new JSONObject();
                    try {
                        obj.put("action","logout");
                        obj.put("token",token);

                        ws.send(obj.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSettings.edit().remove("auth_token").commit();
                    token = "";

                    myDbHelper.delete1("main");
                    myDbHelper.delete1("subMain");

                    LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(MainActivity.ACTION_MAIN_RECEIVER)
                            .putExtra("action","no_login"));
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private final class EchoWebSocketListener extends WebSocketListener{
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JSONObject obj = new JSONObject(text);
                if (obj.has("error"))
                    switch (obj.getString("error")){
                        case "email":
                            LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                                    .putExtra("action","wrong_email"));
                            break;
                        case "pass":
                            LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                                    .putExtra("action","wrong_pass"));
                            break;
                    }
                if (obj.has("token")){
                    token = obj.getString("token");
                    mSettings.edit().putString("auth_token",token).commit();

                    LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                            .putExtra("action","success"));
                }

                if (obj.has("subjects")){
                    int r_count = obj.getJSONArray("subjects").length();
                    ContentValues row1 = new ContentValues();

                    for (int i=0;i<r_count;i++){
                        JSONArray data = obj.getJSONArray("subjects").getJSONArray(i);

                        row1.put("_id", data.getString(0));
                        row1.put("nazva", data.getString(1));
                        row1.put("teacherInfo", data.getString(2));
                        row1.put("mark", 0);
                        row1.put("maxMark", data.getString(3));
                        myDbHelper.inset1("main", row1);
                    }
                    LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(MainActivity.ACTION_MAIN_RECEIVER)
                            .putExtra("action","update"));
                }

                if (obj.has("marks")){
                    int r_count = obj.getJSONArray("marks").length();
                    ContentValues row2 = new ContentValues();
                    for (int i=0;i<r_count;i++){

                        JSONArray data = obj.getJSONArray("marks").getJSONArray(i);
                        row2.put("_id", data.getString(0));
                        row2.put("data", data.getString(1));
                        row2.put("marks", data.getString(2));
                        myDbHelper.inset1("subMain", row2);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onMessage(webSocket, text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(100,null);
            super.onClosing(webSocket, code, reason);
        }


        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            if (token.isEmpty()){
                LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                        .putExtra("action","connection_lost"));
            }
            try {
                Thread.sleep(5000);
                ServerConnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            super.onFailure(webSocket, t, response);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

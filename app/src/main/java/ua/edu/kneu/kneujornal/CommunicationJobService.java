package ua.edu.kneu.kneujornal;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;


public class CommunicationJobService extends Service {
    private SharedPreferences mSettings;
    private String token;

    private WebSocket ws = null;

    @Override
    public void onCreate() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = mSettings.getString("auth_token","");

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://home-server.ddns.ukrtel.net:1337").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();

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
                    mSettings.edit().remove("auth_token").commit();
                    token = "";
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
            //webSocket.send("Hello world!");
            //webSocket.send("Lol kek cheburek");
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
                                    .putExtra("wrong_pass",false));
                            break;
                        case "pass":
                            LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                                    .putExtra("wrong_pass",true));
                            break;
                    }
                if (obj.has("token")){
                    mSettings.edit().putString("auth_token",obj.getString("token")).commit();

                    LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                            .putExtra("success",true));
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
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

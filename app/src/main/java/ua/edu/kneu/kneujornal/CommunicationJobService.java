package ua.edu.kneu.kneujornal;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class CommunicationJobService extends Service {
    private SharedPreferences mSettings;
    private String token;

    @Override
    public void onCreate() {
        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = mSettings.getString("auth_token","");

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
                    mSettings.edit().putString("auth_token",intent.getStringExtra("login")+
                            intent.getStringExtra("pass")+"=cheburek").commit();

                    LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                            .putExtra("success",true));
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

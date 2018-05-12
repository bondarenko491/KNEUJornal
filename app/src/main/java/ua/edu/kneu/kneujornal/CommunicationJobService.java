package ua.edu.kneu.kneujornal;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

public class CommunicationJobService extends Service {
    static final String ACTION_LOGIN = "ACTION_LOGIN";

    LocalBroadcastManager bManager;

    private SharedPreferences mSettings;
    private String token;

    @Override
    public void onCreate() {

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_LOGIN);
        bManager.registerReceiver(bReceiver,intentFilter);

        mSettings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = mSettings.getString("auth_token","");


        if (token.isEmpty())
            LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(MainActivity.ACTION_MAIN_RECEIVER)
                    .putExtra("action","no_login"));

        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("chek_login"))
            if (token.isEmpty())
                LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(MainActivity.ACTION_MAIN_RECEIVER)
                        .putExtra("action","no_login"));
        return super.onStartCommand(intent, flags, startId);
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSettings.edit().putString("auth_token",intent.getStringExtra("login")+
                    intent.getStringExtra("pass")+"=cheburek").commit();

            LocalBroadcastManager.getInstance(CommunicationJobService.this).sendBroadcast(new Intent(LoginActivity.ACTION_LOGIN_RESULT)
                    .putExtra("success",true));
        }
    };

    @Override
    public void onDestroy() {
        bManager.unregisterReceiver(bReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

package ua.edu.kneu.kneujornal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    static final String ACTION_MAIN_RECEIVER = "ACTION_MAIN_RECEIVER";
    LocalBroadcastManager bManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_MAIN_RECEIVER);
        bManager.registerReceiver(bReceiver,intentFilter);

        startService(new Intent(this,CommunicationJobService.class).putExtra("action","chek_login"));

    }

    @Override
    protected void onDestroy() {
        bManager.unregisterReceiver(bReceiver);
        super.onDestroy();
    }

    public void rowClick(View view) {
        startActivity(new Intent(MainActivity.this,SubjInfoActivity.class).putExtra("subj",
                ((TextView)((TableRow)view).getVirtualChildAt(0)).getText()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_settings: {
                startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                break;
            }
            case R.id.action_sign_out: {
                new AlertDialog.Builder(MainActivity.this).setTitle("Выход")
                        .setMessage("Вы уверены, что хотите выйти?").setPositiveButton("Нет", null)
                        .setNegativeButton("Да", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        startService(new Intent(MainActivity.this,CommunicationJobService.class)
                                .putExtra("action","sign_out"));
                    }
                }).show();
                break;
            }
        }
        return false;
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("action")){
                case "no_login":
                    startActivity(new Intent(MainActivity.this,LoginActivity.class));
                    break;
            }
        }
    };
}



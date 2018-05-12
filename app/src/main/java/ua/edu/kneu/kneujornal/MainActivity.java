package ua.edu.kneu.kneujornal;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
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

    private String token;
    AlertDialog.Builder ad;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this,CommunicationJobService.class));





        context = MainActivity.this;

        ad = new AlertDialog.Builder(context);
        ad.setTitle("Выход");
        ad.setMessage("Вы уверены, что хотите выйти?");
        ad.setPositiveButton("Нет", null);
        ad.setNegativeButton("Да", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                Toast.makeText(context, "Деавторизация", Toast.LENGTH_LONG)
                        .show();
                //mSettings.edit().remove("token").commit();
                startActivityForResult(new Intent(MainActivity.this,LoginActivity.class),0);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (resultCode == RESULT_OK){
            token = data.getStringExtra("token");
            //mSettings.edit().putString("token",token).commit();
        }
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
                ad.show();
                break;
            }
        }
        return false;
    }
}

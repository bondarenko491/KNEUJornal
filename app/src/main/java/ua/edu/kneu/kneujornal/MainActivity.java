package ua.edu.kneu.kneujornal;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.IBinder;
import android.renderscript.ScriptGroup;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static final String ACTION_MAIN_RECEIVER = "ACTION_MAIN_RECEIVER";
    LocalBroadcastManager bManager;
    dataBaseHelper myDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
        update_subj_list();

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
                new AlertDialog.Builder(MainActivity.this).setTitle("Вихід  ")
                        .setMessage("Ви впевнені, що хочете вийти?").setPositiveButton("Ні", null)
                        .setNegativeButton("Так", new DialogInterface.OnClickListener() {
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

    private void update_subj_list(){
        Cursor myCursor = myDbHelper.onrawquary("SELECT * FROM main;");
        String name, mark, maxMark, id, info;
        boolean k = true;
        drowHeader();

        while(myCursor.moveToNext()) {
            name = myCursor.getString(2);
            mark = myCursor.getString(3);
            maxMark = myCursor.getString(4);
            id = myCursor.getString(0);
            info = myCursor.getString(1);

            String temp = mark + "|" + maxMark;

            TableLayout table = findViewById(R.id.subj_list);
            TableRow nRow = new TableRow(table.getContext());
            nRow.setPadding(0,10,0,10);
            nRow.setTag(0,id);
            nRow.setTag(1,name);
            nRow.setTag(2,info);

            if(!k) {
                nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));
                k = true;
            }
            else {
                nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.white));
                k = false;
            }
            nRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rowClick(v);
                }
            });

            TextView subjName = new TextView(nRow.getContext());
            subjName.setGravity(Gravity.CENTER_HORIZONTAL);
            subjName.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
            subjName.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            subjName.setEllipsize(TextUtils.TruncateAt.END);
            subjName.setMaxLines(1);
            subjName.setSingleLine(true);

            subjName.setText(name.toCharArray(), 0, name.length());

            TextView mark2 = new TextView(nRow.getContext());
            mark2.setGravity(Gravity.CENTER_HORIZONTAL);
            mark2.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
            mark2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            mark2.setText(temp.toCharArray(), 0, temp.length());

            nRow.addView(subjName);
            nRow.addView(mark2);
            table.addView(nRow);
        }
    }

    private void drowHeader()
    {
        TableLayout table = findViewById(R.id.subj_list);
        TableRow nRow = new TableRow(table.getContext());
        nRow.setPadding(0,15,0,15);
        nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));

        TextView subjName = new TextView(nRow.getContext());
        subjName.setGravity(Gravity.CENTER_HORIZONTAL);
        subjName.setTextAppearance(this,R.style.TextAppearance_AppCompat_Large);
        subjName.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        subjName.setText("Предмет");

        TextView mark2 = new TextView(nRow.getContext());
        mark2.setGravity(Gravity.CENTER_HORIZONTAL);
        mark2.setTextAppearance(this,R.style.TextAppearance_AppCompat_Large);
        mark2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mark2.setText("Підсумок");

        nRow.addView(subjName);
        nRow.addView(mark2);
        table.addView(nRow);
    }

    public void rowClick(View view) {
        startActivity(new Intent(MainActivity.this,SubjInfoActivity.class).putExtra("subj_id", (String)view.getTag(0))
                .putExtra("subj_name",(String)view.getTag(1)).putExtra("subj_info",(String)view.getTag(2)));
    }

    private BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent!=null) {
                switch (intent.getStringExtra("action")) {
                    case "no_login":
                        startActivity(new Intent(MainActivity.this, LoginActivity.class));
                        break;
                    case "update":
                        TableLayout table = findViewById(R.id.subj_list);
                        table.removeAllViews();
                        update_subj_list();
                        break;
                }

            }
        }
    };
}



package ua.edu.kneu.kneujornal;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {
    private String token;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences mSettings;
        mSettings = getPreferences(Context.MODE_PRIVATE);
        token = mSettings.getString("token","");
        if(token.isEmpty()) {
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
        }
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
                SharedPreferences mSettings;
                mSettings = getPreferences(Context.MODE_PRIVATE);
                mSettings.edit().remove("token");
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                break;
            }
        }
        return false;
    }
}

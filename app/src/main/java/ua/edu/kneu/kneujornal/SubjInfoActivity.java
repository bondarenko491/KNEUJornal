package ua.edu.kneu.kneujornal;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.text.SimpleDateFormat;
import java.text.ParseException;


public class SubjInfoActivity extends AppCompatActivity {
    dataBaseHelper myDbHelper;
    String tempId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subj_info);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        myDbHelper = new dataBaseHelper(this);

        try {
            myDbHelper.createDataBase();
        } catch (java.io.IOException ioe) {
            throw new Error("Unable to create database");
        }

        try {
            myDbHelper.openDataBase();
        }catch(android.database.sqlite.SQLiteException sqle){
            throw sqle;
        }

        tempId = getIntent().getStringExtra("subj_id");
        update_subj_list();
    }

    private void update_subj_list(){
        String temptemp = "SELECT * FROM subMain WHERE _id=" + tempId;
        android.database.Cursor myCursor = myDbHelper.onrawquary(temptemp);
        String data, marks, id;

        SimpleDateFormat df = new  SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat output = new  SimpleDateFormat("dd.MM.yyyy");

        boolean k = true;
        drowHeader();

        while(myCursor.moveToNext()) {
            data = myCursor.getString(2);
            marks = myCursor.getString(1);
            String formattedTime = null;
            try {

            java.util.Date res = df.parse(data);
            formattedTime = output.format(res);

            }
            catch (ParseException e1)
             { }
            TableLayout table = findViewById(R.id.subj_large_info);
            TableRow nRow = new TableRow(table.getContext());
            nRow.setPadding(0,10,0,10);

            if(!k) {
                nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));
                k = true;
            }
            else {
                nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.white));
                k = false;
            }

            TextView subjName = new TextView(nRow.getContext());
            subjName.setGravity(Gravity.CENTER_HORIZONTAL);
            subjName.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
            subjName.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            subjName.setText(formattedTime.toCharArray(), 0, formattedTime.length());

            TextView mark2 = new TextView(nRow.getContext());
            mark2.setGravity(Gravity.CENTER_HORIZONTAL);
            mark2.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
            mark2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
            mark2.setText(marks.toCharArray(), 0, marks.length());

            nRow.addView(subjName);
            nRow.addView(mark2);
            table.addView(nRow);
        }
    }

    private void drowHeader()
    {
        TableLayout table = findViewById(R.id.subj_large_info);

        TextView Name = new TextView(table.getContext());
        Name.setGravity(Gravity.CENTER_HORIZONTAL);
        Name.setTextAppearance(this,R.style.TextAppearance_AppCompat_Large);
        Name.setLayoutParams(new TableLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        Name.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));
        Name.setPadding(0,10,0,0);
        Name.setText(getIntent().getStringExtra("subj_name"));

        TextView Info = new TextView(table.getContext());
        Info.setGravity(Gravity.CENTER_HORIZONTAL);
        Info.setTextAppearance(this,R.style.TextAppearance_AppCompat_Large);
        Info.setLayoutParams(new TableLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT));
        Info.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));
        Info.setPadding(0,10,0,0);
        Info.setText(getIntent().getStringExtra("subj_info"));

        TableRow nRow = new TableRow(table.getContext());
        nRow.setPadding(0,0,0,15);
        nRow.setBackgroundColor(table.getContext().getResources().getColor(R.color.gray));

        TextView subjName = new TextView(nRow.getContext());
        subjName.setGravity(Gravity.CENTER_HORIZONTAL);
        subjName.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
        subjName.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        subjName.setText("Дата");

        TextView mark2 = new TextView(nRow.getContext());
        mark2.setGravity(Gravity.CENTER_HORIZONTAL);
        mark2.setTextAppearance(this,R.style.TextAppearance_AppCompat_Medium);
        mark2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mark2.setText("Оцінка");

        table.addView(Name);
        table.addView(Info);
        nRow.addView(subjName);
        nRow.addView(mark2);
        table.addView(nRow);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

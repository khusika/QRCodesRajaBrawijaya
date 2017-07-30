package com.pit.qrcodesrajabrawijaya.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.opencsv.CSVWriter;
import com.pit.qrcodesrajabrawijaya.Absensi;
import com.pit.qrcodesrajabrawijaya.DatabaseHandler;
import com.pit.qrcodesrajabrawijaya.MainActivity;
import com.pit.qrcodesrajabrawijaya.R;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    private ArrayAdapter mAdapter;
    private Toolbar toolbar;
    private Intent i;

    DatabaseHandler db = new DatabaseHandler(this);

    private static final int REQUEST_RUNTIME_PERMISSION = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        ListView lv = (ListView) findViewById(R.id.lvAbsen);
        List<String> initialList = new ArrayList<String>(); //load these
        mAdapter = new ArrayAdapter(this, R.layout.data_listview, initialList);
        lv.setAdapter(mAdapter);

        List<Absensi> absensi = db.getAllAbsensi();

        for (Absensi cn : absensi) {
            String log = "NIM: " + cn.getNim() + " , WAKTU: " + cn.getWaktu();
            mAdapter.add(log);
        }
        toolbar = (Toolbar) findViewById(R.id.data_toolbar);
        setupToolbar();
    }

    private void setupToolbar(){
        toolbar.setTitle(getString(R.string.data_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(DataActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    public void kirimData(){
        File dbFile=getDatabasePath("dbRB2017.db");
        File exportDir = new File(Environment.getExternalStorageDirectory(), "RB2017");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "penugasanrb17.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase dbnya = db.getReadableDatabase();
            Cursor curCSV = dbnya.rawQuery("SELECT nim, waktu FROM absensi",null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to exprort
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            Toast.makeText(getApplicationContext(),"DATA TELAH TERSIMPAN", Toast.LENGTH_SHORT).show();
        }
        catch(Exception sqlEx)
        {
            Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
        }
    }

    public void upload(View view){
        if (CheckPermission(DataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // you have permission go ahead
            kirimData();
        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(DataActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_RUNTIME_PERMISSION);
        }
    }

    public boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context, Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    public void RequestPermission(Activity thisActivity, String Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity, Permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, Permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity, new String[]{Permission}, Code);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case REQUEST_RUNTIME_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    kirimData();
                } else {
                    // you do not have permission show toast.
                }
                return;
            }
        }
    }


}
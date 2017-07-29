package com.pit.qrcodesrajabrawijaya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.pit.qrcodesrajabrawijaya.activities.AboutActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private int counter = 1;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    DatabaseHandler db = new DatabaseHandler(this);

    private static final int REQUEST_RUNTIME_PERMISSION = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                        this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);

        beepManager = new BeepManager(this);


        /**
         * CRUD Operations
         * */

        // Reading all contacts
        Log.d("Reading: ", "Reading all contacts..");
        List<Absensi> absensi = db.getAllAbsensi();

        for (Absensi cn : absensi) {
            String log = "Id: "+cn.getID()+" , NIM: " + cn.getNim() + " , WAKTU: " + cn.getWaktu();
            // Writing Absensis to log
            Log.d("NIM: ", log);
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch(menuItem.getItemId()) {
            case R.id.nav_home:
                if(!menuItem.isChecked()){
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            case R.id.nav_about:
                if(!menuItem.isChecked()) {
                    menuItem.setChecked(false);
                    startActivity(new Intent(this, AboutActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
        }
        return false;
    }

    public void mulaiScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private String getTanggal() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                if(db.cekNim(result.getContents()) == 0) {
                    db.addAbsensi(new Absensi(result.getContents(), getTanggal()));
                }
                mulaiScan();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void scanAbsensi(View view){
        if (CheckPermission(MainActivity.this, Manifest.permission.CAMERA)) {
            // you have permission go ahead
            mulaiScan();
        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(MainActivity.this, Manifest.permission.CAMERA, REQUEST_RUNTIME_PERMISSION);
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
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    mulaiScan();
                } else {
                    Toast.makeText(getApplicationContext(),"Error! Kamu Harus Mengizinkan Aplikasi mengakses kamera!",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();
            barcodeView.setStatusText(result.getText());
            beepManager.playBeepSoundAndVibrate();

            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            TextView textView = (TextView) findViewById(R.id.txtHasil);
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            if (counter % 2 == 0 ) {
                imageView.setBackgroundColor(Color.rgb(85, 255, 0));
            }
            else {
                imageView.setBackgroundColor(Color.rgb(255, 0, 0));
            }
            textView.setText(result.getText());
            counter++;

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

}

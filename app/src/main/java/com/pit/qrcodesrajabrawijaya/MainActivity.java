package com.pit.qrcodesrajabrawijaya;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.pit.qrcodesrajabrawijaya.activities.AboutActivity;
import com.pit.qrcodesrajabrawijaya.activities.DataActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import eu.amirs.JSON;
import io.fabric.sdk.android.Fabric;

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

    TextView lblStatus, lblNamaOP, lblNIMOP, lblDivisiOP, lblStatusOP;
    DatabaseHandler db = new DatabaseHandler(this);

    TelephonyManager telephonyManager;

    private static final int REQUEST_RUNTIME_PERMISSION = 123;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
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

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        lblStatus = (TextView) findViewById(R.id.txtStatus);
        lblNamaOP = (TextView) findViewById(R.id.txtNamaOP);
        lblNIMOP = (TextView) findViewById(R.id.txtNIMOP);
        lblDivisiOP = (TextView) findViewById(R.id.txtDivisiOP);
        lblStatusOP = (TextView) findViewById(R.id.txtStatusOP);

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
            case R.id.nav_scan:
                if(!menuItem.isChecked()) {
                    menuItem.setChecked(false);
                    //startActivity(new Intent(this, ContinuousActivity.class));
                    //untuk panitia
                    if (lblDivisiOP.getText().toString().equalsIgnoreCase("PIT") || lblDivisiOP.getText().toString().equalsIgnoreCase("KESTARI") || lblDivisiOP.getText().toString().equalsIgnoreCase("SPV") || lblDivisiOP.getText().toString().equalsIgnoreCase("KESEHATAN") || lblDivisiOP.getText().toString().equalsIgnoreCase("INTI")  ) {
                        startActivity(new Intent(this, GelangActivity.class));
                        return false;
                    }
                    else {
                        //untuk ukm
                        Log.e("test", lblDivisiOP.getText().toString());
                        Intent intent = new Intent(this, UKMActivity.class);
                        intent.putExtra("divisi", lblDivisiOP.getText().toString());
                        startActivity(intent);
                        return false;
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;

            case R.id.nav_data:
                if(!menuItem.isChecked()) {
                    menuItem.setChecked(false);
                    startActivity(new Intent(this, DataActivity.class));
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return false;
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

    public void keDaftar() {
        Intent intent = new Intent(this, DaftarActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (CheckPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE)) {
            authUser();
        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE, REQUEST_RUNTIME_PERMISSION);
        }


    }

    private void authUser() {
        final String AUTH_URL = "http://rajabrawijaya.ub.ac.id/api/auth/"+getUUID();

        final ProgressDialog loadingDialog = new ProgressDialog(MainActivity.this);
        //set message of the dialog
        loadingDialog.setMessage("Menghubungkan ke server...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);

        //show dialog
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, AUTH_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(MainActivity.this,response,Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();
                        JSON json = new JSON(response);
                        int kodeauth = json.key("kode").intValue();

                        switch (kodeauth) {
                            case 1:
                                lblNamaOP.setText(json.key("user").key("nama").stringValue());
                                lblNIMOP.setText(json.key("user").key("NIM").stringValue());
                                lblDivisiOP.setText(json.key("user").key("divisi").stringValue());
                                lblStatusOP.setText("READY TO SCAN");
                                lblStatus.setVisibility(View.INVISIBLE);
                                return;

                            case 2:
                                lblNamaOP.setText(json.key("user").key("nama").stringValue());
                                lblNIMOP.setText(json.key("user").key("NIM").stringValue());
                                lblDivisiOP.setText(json.key("user").key("divisi").stringValue());
                                lblStatusOP.setVisibility(View.INVISIBLE);
                                lblStatus.setText("USER BELUM AKTIF!");
                                return;

                            case 3:
                                keDaftar();
                                return;

                            default:
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Terjadi Kesalahan!");
                                alertDialog.setMessage("Hubungi anak PIT - ["+response+"]");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Keluar",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                                System.exit(0);
                                            }
                                        });
                                alertDialog.show();
                                return;

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        loadingDialog.dismiss();

                        String message = null;
                        if (error instanceof NetworkError) {
                            message = "Tidak bisa terhubung ke server. Cek koneksi internet kamu! (1)";
                        } else if (error instanceof ServerError) {
                            message = "Terjadi kesalahan server. Hubungi anak PIT & coba lagi nanti!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Tidak bisa terhubung ke server. Cek koneksi internet kamu! (2)";
                        } else if (error instanceof ParseError) {
                            message = "Terjadi kesalahan parsing. Hubungi anak PIT & coba lagi nanti!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Tidak ada koneksi internet. Cek koneksi internet kamu!";
                        } else if (error instanceof TimeoutError) {
                            message = "Koneksi timeout! Cek koneksi internet kamu!";
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("Error!");
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Keluar",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        System.exit(0);
                                    }
                                });
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                    }
                });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
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
                    authUser();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Kamu harus mengizinkan aplikasi ini!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Keluar",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    System.exit(0);
                                }
                            });
                    alertDialog.setCancelable(false);
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.show();
                }
                return;
            }
        }
    }


    public String getUUID() {
        try {
            String data = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + telephonyManager.getDeviceId() + Build.MANUFACTURER + Build.MODEL + Build.VERSION.RELEASE;
            return SHA256(data);
        }
        catch (NoSuchAlgorithmException e){
            Log.e("GAGAL UUID", e.toString());
            return null;
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    public static String SHA256 (String text) throws NoSuchAlgorithmException {

        MessageDigest md = MessageDigest.getInstance("SHA-256");

        md.update(text.getBytes());
        byte[] digest = md.digest();

        return bytesToHexString(digest);
    }

}

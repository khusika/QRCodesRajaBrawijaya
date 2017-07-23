package com.pit.qrcodesrajabrawijaya;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.pit.qrcodesrajabrawijaya.database.Absensi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment {

    private MainActivity main;
    private Toolbar toolbar;
    DatabaseHandler db = new DatabaseHandler(getActivity());

    private static final int REQUEST_RUNTIME_PERMISSION = 123;

    public MainFragment() {
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        main = (MainActivity)activity;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        toolbar = (Toolbar)view.findViewById(R.id.fragment_main_toolbar);
        setupToolbar();

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

        return view;
    }

    @Override
    public  void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        main.setupNavigationDrawer(toolbar);
    }

    private  void setupToolbar(){
        toolbar.setTitle(getString(R.string.app_name));
        main.setSupportActionBar(toolbar);
    }

    public void mulaiScan() {
        IntentIntegrator integrator = new IntentIntegrator(getActivity());
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    private String getTanggal() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
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
        if (CheckPermission(getActivity(), Manifest.permission.CAMERA)) {
            // you have permission go ahead
            mulaiScan();
        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(getActivity(), Manifest.permission.CAMERA, REQUEST_RUNTIME_PERMISSION);
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
                    Toast.makeText(getActivity(),"Error! Kamu Harus Mengizinkan Aplikasi mengakses kamera!",Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

}
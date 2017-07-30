package com.pit.qrcodesrajabrawijaya;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import eu.amirs.JSON;

public class DaftarActivity extends AppCompatActivity {

    TelephonyManager telephonyManager;
    EditText txtNIM, txtNAMA;
    Spinner spnDivisi;

    private static final String REGISTER_URL = "http://rajabrawijaya.ub.ac.id/api/daftar";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        txtNAMA = (EditText) findViewById(R.id.txtNama);
        txtNIM = (EditText) findViewById(R.id.txtNIM);
        spnDivisi = (Spinner) findViewById(R.id.spnDivisi);

        //get the spinner from the xml.
        Spinner dropdown = (Spinner)findViewById(R.id.spnDivisi);
        //create a list of items for the spinner.
        String[] items = new String[]{"SPV","KESTARI","PIT"};
        //create an adapter to describe how the items are displayed, adapters are used in several places in android.
        //There are multiple variations of this, but this is the basic variant.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        //set the spinners adapter to the previously created one.
        dropdown.setAdapter(adapter);
    }

    public void daftarklik(View v){

        daftarUser();
    }

    public void keAwal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void daftarUser(){

        final ProgressDialog loadingDialog = new ProgressDialog(DaftarActivity.this);
        //set message of the dialog
        loadingDialog.setMessage("Mendaftarkan ke server...");
        //show dialog
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(DaftarActivity.this,response,Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();

                        JSON json = new JSON(response);
                        boolean berhasil = json.key("berhasil").booleanValue();

                        if (berhasil){
                            AlertDialog alertDialog = new AlertDialog.Builder(DaftarActivity.this).create();
                            alertDialog.setTitle("Suskes");
                            alertDialog.setMessage(json.key("pesan").stringValue());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            keAwal();
                                        }
                                    });
                            alertDialog.show();
                        }
                        else {
                            loadingDialog.dismiss();

                            AlertDialog alertDialog = new AlertDialog.Builder(DaftarActivity.this).create();
                            alertDialog.setTitle("Pendaftaran Gagal");
                            alertDialog.setMessage(json.key("pesan").stringValue());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ulangi",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            alertDialog.show();
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

                        AlertDialog alertDialog = new AlertDialog.Builder(DaftarActivity.this).create();
                        alertDialog.setTitle("Error!");
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        //System.exit(0);
                                    }
                                });
                        alertDialog.show();
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uuid", getUUID());
                params.put("nama", txtNAMA.getText().toString());
                params.put("NIM", txtNIM.getText().toString());
                params.put("divisi", spnDivisi.getSelectedItem().toString());
                params.put("model", Build.MANUFACTURER + " " +Build.MODEL);
                params.put("versi", Build.VERSION.RELEASE);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    public String getUUID() {
        try {
            String data = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID) + telephonyManager.getDeviceId() + Build.MANUFACTURER + Build.MODEL + Build.VERSION.RELEASE;
            return SHA256(data);
        }
        catch (NoSuchAlgorithmException e){
            Log.e("GAGAL UUID", e.toString());
            return "";
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

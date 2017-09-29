package com.pit.qrcodesrajabrawijaya;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
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
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BeepManager;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.amirs.JSON;
import io.fabric.sdk.android.Fabric;

public class ContinuousActivity extends AppCompatActivity {
    TelephonyManager telephonyManager;
    private static final String TAG = ContinuousActivity.class.getSimpleName();
    private int counter = 1;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private Toolbar toolbar;
    private Intent i;

    private static final int REQUEST_RUNTIME_PERMISSION = 321;
    private static final String KIRIM_URL = "http://rajabrawijaya.ub.ac.id/api/tugas";
    private static final String CIDUK_URL = "http://rajabrawijaya.ub.ac.id/api/ciduk";
    DatabaseHandler db = new DatabaseHandler(this);

    ImageView imageView;
    TextView textView;
    Button btnKesehatan;
    EditText txtKelompok;
    Spinner spnVenue;

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            //Added preview of scanned barcode
            //imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

//            if (counter % 2 == 0 ) {
//                imageView.setBackgroundColor(Color.rgb(0, 255, 0));
//            }
//            else {
//                imageView.setBackgroundColor(Color.rgb(0, 0, 255));
//            }

            if(db.cekNim(result.getText()) == 0) {
                db.addAbsensi(new Absensi(result.getBarcodeFormat().toString(), getTanggal()));
            }

            String kelompok = txtKelompok.getText().toString();
            String venue = spnVenue.getSelectedItem().toString();
            if (kelompok != null && kelompok.equals("")){
                AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                alertDialog.setTitle("Error");
                alertDialog.setMessage("Data absensi belum lengkap!");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //finish();
                                //System.exit(0);
                            }
                        });
                alertDialog.setCancelable(false);
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
            }
            else {
                kirimData(result.getText(), kelompok, venue);
                beepManager.playBeepSoundAndVibrate();
            }

            lastText = result.getText();



            //textView.setText(result.getText());
            //counter++;

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_continuous);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        imageView = (ImageView) findViewById(R.id.barcodePreview);
        textView = (TextView) findViewById(R.id.txtHasil);
        btnKesehatan = (Button) findViewById(R.id.btnKesehatan);

        txtKelompok = (EditText) findViewById(R.id.txtKelompok);
        spnVenue = (Spinner) findViewById(R.id.spnVenue);

        Spinner dropdown = (Spinner)findViewById(R.id.spnVenue);
        String[] items = new String[]{"GOR Pertamina", "Lapangan Rektorat", "Samantha Krida", "Graha Medika (Protestan)", "Aula FTP (Katholik)", "IKA UB (Hindu)", "Rumah Pintar (Buddha & Konghucu)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        if (CheckPermission(ContinuousActivity.this, Manifest.permission.CAMERA)) {

        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(ContinuousActivity.this, Manifest.permission.CAMERA, REQUEST_RUNTIME_PERMISSION);
        }

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
        barcodeView.setStatusText("Arahkan QR Code ke garis merah untuk mulai scan.");

//        btnKesehatan.setVisibility(View.INVISIBLE);
//
//        Bundle bundle = getIntent().getExtras();
//        if (bundle != null) {
//            String divisinya = bundle.getString("divisi");
//            if (divisinya.equalsIgnoreCase("INTI") || divisinya.equalsIgnoreCase("PIT")){
//                btnKesehatan.setVisibility(View.VISIBLE);
//            }
//
//
//        }



        beepManager = new BeepManager(this);
        toolbar = (Toolbar) findViewById(R.id.scan_toolbar);
        setupToolbar();
    }

    private void setupToolbar(){
        toolbar.setTitle(getString(R.string.scan_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(ContinuousActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

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
                    //authUser();
                } else {
                    AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Kamu harus mengizinkan aplikasi ini!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    //System.exit(0);
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

    private void kirimData(final String nimnya, final String kelompok, final String venue){

        final ProgressDialog loadingDialog = new ProgressDialog(ContinuousActivity.this);
        //set message of the dialog
        loadingDialog.setMessage("Mengirimkan ke server...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);
        //show dialog
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, KIRIM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(final String response) {
                        //Toast.makeText(DaftarActivity.this,response,Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();

                        final JSON json = new JSON(response);
                        boolean berhasil = json.key("berhasil").booleanValue();

                        if (berhasil){
                            int kode = json.key("kode").intValue();
                            if (kode == 1) {
                                AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                                alertDialog.setTitle("SCAN BERHASIL");
                                alertDialog.setMessage(json.key("pesan").stringValue()+"\n No Absensi : "+txtKelompok.getText().toString()+"\n Venue : "+spnVenue.getSelectedItem().toString());
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                alertDialog.show();
                            }
                            else if (kode == 2){
                                Intent intent = new Intent(ContinuousActivity.this, DataMahasiswaActivity.class);
                                intent.putExtra("mahasiswa", json.key("mahasiswa").stringValue());
                                intent.putExtra("kesehatan", json.key("kesehatan").stringValue());
                                startActivity(intent);
                            }

                            else if (kode == 3){

                                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which){
                                            case DialogInterface.BUTTON_POSITIVE:
                                                //iya
                                                AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                                                alertDialog.setTitle("SCAN BERHASIL");
                                                alertDialog.setMessage(json.key("pesan").stringValue()+"\n No Absensi : "+txtKelompok.getText().toString()+"\n Venue : "+spnVenue.getSelectedItem().toString());
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        });
                                                alertDialog.show();

                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                //tidak
                                                revisi(nimnya);
                                                break;
                                        }
                                    }
                                };

                                AlertDialog.Builder builder = new AlertDialog.Builder(ContinuousActivity.this);
                                builder.setCancelable(false);
                                builder.setMessage(json.key("pesan").stringValue()+ "\nApakah benar uang ditugas ini Rp. "+ json.key("nominal").stringValue()+"?").setPositiveButton("Iya", dialogClickListener)
                                        .setNegativeButton("Tidak", dialogClickListener).show();

//                                AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
//                                alertDialog.setTitle("SCAN BERHASIL");
//                                alertDialog.setMessage(json.key("pesan").stringValue()+ "Ada Indikasi Kecurangan Untuk Tugas Ini?");
//                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
//                                        new DialogInterface.OnClickListener() {
//                                            public void onClick(DialogInterface dialog, int which) {
//
//                                            }
//                                        });
//                                alertDialog.show();
                            }

                        }
                        else {
                            loadingDialog.dismiss();

                            AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                            alertDialog.setTitle("Scan Gagal!");
                            alertDialog.setMessage(json.key("pesan").stringValue());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            alertDialog.show();
                            //imageView.setBackgroundColor(Color.rgb(255, 0, 0));
                        }

                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Scan Baru")
                                .putContentType("Scanner")
                                .putContentId("1")
                                .putCustomAttribute("NIM QR CODE", nimnya)
                                .putCustomAttribute("BERHASIL", String.valueOf(berhasil))
                                .putCustomAttribute("OPERATOR", getUUID()));

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
                            message = "Format QR Code salah!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Tidak bisa terhubung ke server. Cek koneksi internet kamu! (2)";
                        } else if (error instanceof ParseError) {
                            message = "Terjadi kesalahan parsing. Hubungi anak PIT & coba lagi nanti!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Tidak ada koneksi internet. Cek koneksi internet kamu!";
                        } else if (error instanceof TimeoutError) {
                            message = "Koneksi timeout! Cek koneksi internet kamu!";
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                        alertDialog.setTitle("Error!");
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        //System.exit(0);
                                    }
                                });
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        //imageView.setBackgroundColor(Color.rgb(255, 0, 0));
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uuid", getUUID());
                params.put("NIM", nimnya);
                params.put("venue", venue);
                params.put("kelompok", kelompok);
                return params;
            }

        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void revisi(final String nimnya){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);

        builder.setTitle("Revisi Uang").setMessage("Masukan uang yang benar").setView(text);
        builder.setPositiveButton("Kirim", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int i) {
                final String uangbenar = text.getText().toString();
                ciduk(nimnya, uangbenar);
                //Log.e("DARI DIALOG", uangbenar);
            }
        });

        builder.setCancelable(false);
        builder.create().show();
    }


    private void ciduk(final String nimnya, final String uangbenar){

        final ProgressDialog loadingDialog = new ProgressDialog(ContinuousActivity.this);
        //set message of the dialog
        loadingDialog.setMessage("Mengirimkan ke server...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);
        //show dialog
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, CIDUK_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(DaftarActivity.this,response,Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();

                        JSON json = new JSON(response);
                        boolean berhasil = json.key("berhasil").booleanValue();

                        if (berhasil){
                            int kode = json.key("kode").intValue();
                            if (kode == 1) {
                                AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                                alertDialog.setTitle("BERHASIL");
                                alertDialog.setMessage(json.key("pesan").stringValue()+"\n No Absensi : "+txtKelompok.getText().toString()+"\n Venue : "+spnVenue.getSelectedItem().toString());
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                alertDialog.show();
                            }
                            else if (kode == 2){
                                Intent intent = new Intent(ContinuousActivity.this, DataMahasiswaActivity.class);
                                intent.putExtra("mahasiswa", json.key("mahasiswa").stringValue());
                                intent.putExtra("kesehatan", json.key("kesehatan").stringValue());
                                startActivity(intent);
                            }

                        }
                        else {
                            loadingDialog.dismiss();

                            AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                            alertDialog.setTitle("Gagal!");
                            alertDialog.setMessage(json.key("pesan").stringValue());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            alertDialog.show();
                            //imageView.setBackgroundColor(Color.rgb(255, 0, 0));
                        }

                        Answers.getInstance().logContentView(new ContentViewEvent()
                                .putContentName("Scan Baru")
                                .putContentType("Scanner")
                                .putContentId("1")
                                .putCustomAttribute("NIM QR CODE", nimnya)
                                .putCustomAttribute("BERHASIL", String.valueOf(berhasil))
                                .putCustomAttribute("OPERATOR", getUUID()));

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
                            message = "Format QR Code salah!";
                        } else if (error instanceof AuthFailureError) {
                            message = "Tidak bisa terhubung ke server. Cek koneksi internet kamu! (2)";
                        } else if (error instanceof ParseError) {
                            message = "Terjadi kesalahan parsing. Hubungi anak PIT & coba lagi nanti!";
                        } else if (error instanceof NoConnectionError) {
                            message = "Tidak ada koneksi internet. Cek koneksi internet kamu!";
                        } else if (error instanceof TimeoutError) {
                            message = "Koneksi timeout! Cek koneksi internet kamu!";
                        }

                        AlertDialog alertDialog = new AlertDialog.Builder(ContinuousActivity.this).create();
                        alertDialog.setTitle("Error!");
                        alertDialog.setMessage(message);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                        //System.exit(0);
                                    }
                                });
                        alertDialog.setCancelable(false);
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        //imageView.setBackgroundColor(Color.rgb(255, 0, 0));
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uuid", getUUID());
                params.put("NIM", nimnya);
                params.put("uangbaru", uangbenar);
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

    private String getTanggal() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public void keKesehatan(View v) {
        Intent intent = new Intent(this, KesehatanActivity.class);
        startActivity(intent);
    }

}

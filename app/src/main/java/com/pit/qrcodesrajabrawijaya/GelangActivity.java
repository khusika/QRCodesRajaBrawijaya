package com.pit.qrcodesrajabrawijaya;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import java.io.UnsupportedEncodingException;
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

public class GelangActivity extends AppCompatActivity {
    TelephonyManager telephonyManager;
    private static final String TAG = GelangActivity.class.getSimpleName();
    private int counter = 1;
    private DecoratedBarcodeView barcodeView;
    private BeepManager beepManager;
    private String lastText;
    private Toolbar toolbar;
    private Intent i;

    private static final int REQUEST_RUNTIME_PERMISSION = 321;
    private static final String KIRIM_URL = "http://rajabrawijaya.ub.ac.id/api/gelang";
    DatabaseHandler db = new DatabaseHandler(this);

    ImageView imageView;
    TextView textView, textNametag;
    Button btnManual, btnManual2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_gelang);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        imageView = (ImageView) findViewById(R.id.barcodePreview2);
        textView = (TextView) findViewById(R.id.txtHasil2);
        textNametag = (TextView) findViewById(R.id.txtNametag);
        btnManual = (Button) findViewById(R.id.btnManual);
        btnManual2 = (Button) findViewById(R.id.btnManual2);

        if (CheckPermission(GelangActivity.this, Manifest.permission.CAMERA)) {

        } else {
            // you do not have permission go request runtime permissions
            RequestPermission(GelangActivity.this, Manifest.permission.CAMERA, REQUEST_RUNTIME_PERMISSION);
        }

        barcodeView = (DecoratedBarcodeView) findViewById(R.id.barcode_scanner2);
        barcodeView.decodeContinuous(callback);
        barcodeView.setStatusText("Arahkan barcode nametag & gelang ke garis merah\n untuk mulai scan.");

        beepManager = new BeepManager(this);
        toolbar = (Toolbar) findViewById(R.id.scan_toolbar3);
        setupToolbar();
        imageView.setBackgroundColor(Color.rgb(0, 0, 255));
    }

    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if(result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            //Added preview of scanned barcode
            //imageView.setImategeBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            if (result.getBarcodeFormat().toString().equalsIgnoreCase("QR_CODE")){
                textNametag.setText(decodeBase64(result.getText()));
                textView.setText("SCAN GELANG MILIK \n"+ textNametag.getText());
                btnManual.setVisibility(View.INVISIBLE);
                btnManual2.setVisibility(View.VISIBLE);
                imageView.setBackgroundColor(Color.rgb(255, 0, 0));
            }
            else if (result.getBarcodeFormat().toString().equalsIgnoreCase("CODE_128")){
                if (textNametag.getText().equals("")){
                    //nametag belum discan
                    AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
                    alertDialog.setTitle("ERROR!");
                    alertDialog.setMessage("Nametag belum discan! silahkan scan nametagnya terlebih dahulu!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    textView.setText("SCAN NAMETAG");
                    btnManual.setVisibility(View.VISIBLE);
                    btnManual2.setVisibility(View.INVISIBLE);
                    imageView.setBackgroundColor(Color.rgb(0, 0, 255));
                }
                else {
                    //nametag sudah discan
//                    AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
//                    alertDialog.setTitle("Berhasil!");
//                    alertDialog.setMessage("Gelang "+result.getText()+" berhasil diregistrasi ke NIM "+textNametag.getText().toString());
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.show();

                    kirimData(textNametag.getText().toString(), result.getText());

                    textNametag.setText("");
                    textView.setText("SCAN NAMETAG");
                    btnManual.setVisibility(View.VISIBLE);
                    btnManual2.setVisibility(View.INVISIBLE);
                    imageView.setBackgroundColor(Color.rgb(0, 0, 255));
                }
            }


            if(db.cekNim(result.getText()) == 0) {
                db.addAbsensi(new Absensi(decodeBase64(result.getText()), textNametag.getText().toString()));
            }

            //kirimData(result.getText());
            lastText = result.getText();
            beepManager.playBeepSoundAndVibrate();

            //textView.setText(result.getBarcodeFormat().toString());
            counter++;

        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    public void manual(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);
        text.setInputType(InputType.TYPE_CLASS_NUMBER);
        text.setRawInputType(Configuration.KEYBOARD_12KEY);

        builder.setTitle("Input Manual").setMessage("Masukkan NIM mahasiswa").setView(text);
        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int i) {
                final String nimmanual = text.getText().toString();
                textNametag.setText(nimmanual);
                textView.setText("SCAN GELANG MILIK \n"+ nimmanual);
                imageView.setBackgroundColor(Color.rgb(255, 0, 0));
                btnManual.setVisibility(View.INVISIBLE);
                btnManual2.setVisibility(View.VISIBLE);
            }
        });

        //builder.setCancelable(false);
        builder.create().show();
    }

    public void manual2(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText text = new EditText(this);
        text.setInputType(InputType.TYPE_CLASS_NUMBER);
        text.setRawInputType(Configuration.KEYBOARD_12KEY);

        builder.setTitle("Input Manual").setMessage("Masukkan nomor gelang").setView(text);
        builder.setPositiveButton("Oke", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface di, int i) {
                final String gelangmanual = text.getText().toString();
                if (textNametag.getText().equals("")){
                    //nametag belum discan
                    AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
                    alertDialog.setTitle("ERROR!");
                    alertDialog.setMessage("Nametag belum discan! silahkan scan nametagnya terlebih dahulu!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    textView.setText("SCAN NAMETAG");
                    btnManual.setVisibility(View.VISIBLE);
                    btnManual2.setVisibility(View.INVISIBLE);
                    imageView.setBackgroundColor(Color.rgb(0, 0, 255));
                }
                else {
                    //nametag sudah discan
//                    AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
//                    alertDialog.setTitle("Berhasil!");
//                    alertDialog.setMessage("Gelang "+gelangmanual+" berhasil diregistrasi ke NIM "+textNametag.getText().toString());
//                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                            new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                    alertDialog.show();

                    kirimData(textNametag.getText().toString(),gelangmanual);
                    textNametag.setText("");
                    textView.setText("SCAN NAMETAG");
                    btnManual.setVisibility(View.VISIBLE);
                    btnManual2.setVisibility(View.INVISIBLE);
                    imageView.setBackgroundColor(Color.rgb(0, 0, 255));
                }
            }
        });

        //builder.setCancelable(false);
        builder.create().show();
    }

    private String decodeBase64(String coded){
        byte[] valueDecoded= new byte[0];
        try {
            valueDecoded = Base64.decode(coded.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
        }
        return new String(valueDecoded);
    }

    private void setupToolbar(){
        toolbar.setTitle("Registrasi Gelang");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() !=null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(GelangActivity.this, MainActivity.class);
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
                    AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
                    alertDialog.setTitle("Error");
                    alertDialog.setMessage("Kamu harus mengizinkan aplikasi ini!");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    //System.exit(0);
                                }
                            });
                    alertDialog.show();
                }
                return;
            }
        }
    }

    private void kirimData(final String nimnya, final String gelang){

        final ProgressDialog loadingDialog = new ProgressDialog(GelangActivity.this);
        //set message of the dialog
        loadingDialog.setMessage("Mengirimkan ke server...");
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.setCancelable(false);
        //show dialog
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, KIRIM_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Toast.makeText(DaftarActivity.this,response,Toast.LENGTH_LONG).show();
                        loadingDialog.dismiss();

                        JSON json = new JSON(response);
                        boolean berhasil = json.key("berhasil").booleanValue();

                        if (berhasil){
                            AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
                            alertDialog.setTitle("REGISTRASI BERHASIL");
                            alertDialog.setMessage(json.key("pesan").stringValue());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Oke",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            alertDialog.show();
                        }
                        else {
                            loadingDialog.dismiss();

                            AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
                            alertDialog.setTitle("Registrasi Gagal!");
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

                        AlertDialog alertDialog = new AlertDialog.Builder(GelangActivity.this).create();
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
                params.put("gelang", gelang);
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
        Intent intent = new Intent(this, GelangActivity.class);
        startActivity(intent);
    }

}

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
import android.widget.AdapterView;
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
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import eu.amirs.JSON;
import io.fabric.sdk.android.Fabric;

public class DaftarActivity extends AppCompatActivity {

    TelephonyManager telephonyManager;
    EditText txtNIM, txtNAMA;



    private static final String REGISTER_URL = "http://rajabrawijaya.ub.ac.id/api/daftar";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_daftar);

        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        txtNAMA = (EditText) findViewById(R.id.txtNama);
        txtNIM = (EditText) findViewById(R.id.txtNIM);


        Spinner tipe = (Spinner)findViewById(R.id.spnTipe);
        String[] itemtipe = new String[]{"Panitia", "UKM"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itemtipe);
        tipe.setAdapter(adapter);

        tipe.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if(selectedItem.equals("Panitia")) {
                    // do your stuff
                    populasi(1);
                }
                else if ((selectedItem.equals("UKM"))){
                    populasi(2);
                }
            } // to close the onItemSelected
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });
    }

    public void populasi(int tipe){
        Spinner divisi = (Spinner)findViewById(R.id.spnDivisi);

        String[] itempanitia = new String[]{"PIT", "KESTARI", "SPV", "KESEHATAN", "INTI"};
        String[] itemukm = new String[]{"AIESEC", "BBC (Brawijaya Bridge Community)", "BCC (Brawijaya Chess Club)", "BSB (Baseball – Softball Brawijaya)", "BSC (Basic Shooting Club)", "BUMERANG BRAWIJAYA", "DPM (Dewan Perwakilan Mahasiswa)", "EM (Eksekutif Mahasiswa)", "Fordimapelar (Forum Studi Mahasiswa Pengembang Penalaran)", "FORMAPI (Forum Mahasiswa Peduli Inklusi)", "FORMASI (Forum Mahasiswa Studi Bahasa Inggris)", "IAAS", "IMPALA (Ikatan Mahasiswa Pecinta Alam)", "INKAI", "KSR (Korps Sukarela)", "Marching Band ESB", "Menwa (Resimen Mahasiswa)", "MW (Mahasiswa Wirausaha)", "Nol Derajat Film", "PDUB (Perisai Diri Universitas Brawijaya)", "PPS Betako Merpati Putih", "Pramuka", "PSHT (Persatuan Setia Hati Terate)", "PSM (Paduan Suara Mahasiswa)", "Renang", "RKIM (Riset dan Karya Ilmiah Mahasiswa)", "Seni Religi", "Shorinji Kempo", "Tapak Suci", "Teater Kutub", "TEGAZS (Tim Penanggulangan Penyalahgunaan Napza dan HIV/AIDS)", "TI-UB (Taekwondo Indonesia Universitas Brawijaya)", "UAB (Unit Aktivitas Band)", "UABB (Unit Aktivitas Basket Brawijaya)", "UABT UB (Unit Aktivitas Bulutangkis Universitas Brawijaya)", "UABV (Unit Aktivitas Bola Voli)", "UAKB (Unit Aktivitas Kerohanian Budha)", "UAKI (Unit Aktivitas Kerohanian Islam)", "UAKK (Unit Aktivitas Kerohanian Kristen)", "UAKKat (Unit Aktivitas Kerohanian Katholik)", "UAP Brawijaya (Unit Aktivitas Panahan Brawijaya)", "UAPKM (Unit Aktifitas Pers Kampus Mahasiswa)", "UASB UB (Unit Aktivitas Sepak Bola UB)", "UATL (Unit Aktivitas Tenis Lapangan)", "UATM (Unit Aktivitas Tenis Meja)", "UNIKAHIDHA (Unit Aktivitas Kerohanian Hindu Dharma)", "UNITANTRI (Unit Aktivitas Karawitan dan Tari)", "Lainnya.."};

        if (tipe == 1){
            ArrayAdapter<String> adapterpanitia = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itempanitia);
            divisi.setAdapter(adapterpanitia);
        }
        else {
            ArrayAdapter<String> adapterukm = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, itemukm);
            divisi.setAdapter(adapterukm);
        }
    }

    public void daftarklik(View v){

        daftarUser();
    }

    public void keAwal() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void daftarUser(){
        final Spinner divisi = (Spinner)findViewById(R.id.spnDivisi);

        final ProgressDialog loadingDialog = new ProgressDialog(DaftarActivity.this);
        loadingDialog.setMessage("Mendaftarkan ke server...");
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);
        loadingDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loadingDialog.dismiss();

                        JSON json = new JSON(response);
                        boolean berhasil = json.key("berhasil").booleanValue();

                        if (berhasil){
                            Answers.getInstance().logContentView(new ContentViewEvent()
                                    .putContentName("Pendaftaran Baru")
                                    .putContentType("Registrasi")
                                    .putContentId("2")
                                    .putCustomAttribute("NAMA", txtNAMA.getText().toString())
                                    .putCustomAttribute("NIM", txtNIM.getText().toString())
                                    .putCustomAttribute("DIVISI", divisi.getSelectedItem().toString())
                                    .putCustomAttribute("MODEL", Build.MANUFACTURER + " " +Build.MODEL)
                                    .putCustomAttribute("VERSI", Build.VERSION.RELEASE));


                            AlertDialog alertDialog = new AlertDialog.Builder(DaftarActivity.this).create();
                            alertDialog.setTitle("Sukses");
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

                        String message = error.getMessage();
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
                        alertDialog.setTitle("Error!!");
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
                    }
                }){
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("uuid", getUUID());
                params.put("nama", txtNAMA.getText().toString());
                params.put("NIM", txtNIM.getText().toString());
                params.put("divisi", divisi.getSelectedItem().toString());
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

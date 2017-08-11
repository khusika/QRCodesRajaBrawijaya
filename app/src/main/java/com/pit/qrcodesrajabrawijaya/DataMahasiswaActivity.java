package com.pit.qrcodesrajabrawijaya;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import eu.amirs.JSON;
import io.fabric.sdk.android.Fabric;

/**
 * Created by khusika on 01/08/17.
 */

public class DataMahasiswaActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Intent i;

    TextView lblNama, lblNIM, lblCluster, lblFakultas, lblAgama, lblHP, lblWali, lblTTL, lblPenyakit, lblAlergiObat, lblAlergiMakanan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_data_mahasiswa);

        toolbar = (Toolbar) findViewById(R.id.data_mahasiswa_toolbar);
        setupToolbar();

        lblNama = (TextView) findViewById(R.id.txtNamaMaba);
        lblNIM = (TextView) findViewById(R.id.txtNIMMaba);
        lblCluster = (TextView) findViewById(R.id.txtClusterMaba);
        lblFakultas = (TextView) findViewById(R.id.txtFakultasMaba);
        lblAgama = (TextView) findViewById(R.id.txtAgamaMaba);
        lblHP = (TextView) findViewById(R.id.txtHPMaba);
        lblWali = (TextView) findViewById(R.id.txtWaliMaba);
        lblTTL = (TextView) findViewById(R.id.txtTTLMaba);
        lblPenyakit = (TextView) findViewById(R.id.txtPenyakitMaba);
        lblAlergiObat = (TextView) findViewById(R.id.txtAlergiObatMaba);
        lblAlergiMakanan = (TextView) findViewById(R.id.txtAlergiMakananMaba);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            JSON jsonmahasiswa = new JSON(bundle.getString("mahasiswa"));
            JSON jsonkesehatan = new JSON(bundle.getString("kesehatan"));

            lblNama.setText(jsonmahasiswa.key("nama").stringValue());
            lblNIM.setText(jsonmahasiswa.key("NIM").stringValue() +" / "+ jsonmahasiswa.key("NIM2").stringValue());
            lblCluster.setText("Cluster "+ jsonmahasiswa.key("cluster").stringValue());
            lblFakultas.setText(jsonmahasiswa.key("fakultas").stringValue()+ " - "+ jsonmahasiswa.key("program_studi").stringValue());
            lblAgama.setText(jsonmahasiswa.key("agama").stringValue());
            lblHP.setText("Kontak HP : "+ jsonmahasiswa.key("hp").stringValue());
            lblWali.setText("Kontak Wali : "+ jsonmahasiswa.key("wali").stringValue());
            lblTTL.setText(jsonmahasiswa.key("tempat_lahir").stringValue()+", "+jsonmahasiswa.key("tanggal_lahir").stringValue());
            lblPenyakit.setText(jsonkesehatan.key("penyakit").stringValue());
            lblAlergiObat.setText(jsonkesehatan.key("alergi_obat").stringValue());
            lblAlergiMakanan.setText(jsonkesehatan.key("alergi_makanan").stringValue());
        }

    }

    private void setupToolbar() {
        toolbar.setTitle(getString(R.string.data_mahasiswa_title));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                i = new Intent(DataMahasiswaActivity.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }
}
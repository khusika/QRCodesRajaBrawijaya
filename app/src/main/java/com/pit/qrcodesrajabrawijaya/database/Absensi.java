package com.pit.qrcodesrajabrawijaya.database;

public class Absensi {
    //private variables
    int _id;
    String _nim;
    String _waktu;

    // Empty constructor
    public Absensi(){

    }
    // constructor
    public Absensi(int id, String nim, String waktu){
        this._id = id;
        this._nim = nim;
        this._waktu = waktu;
    }

    // constructor
    public Absensi(String nim, String waktu){
        this._nim = nim;
        this._waktu = waktu;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting nim
    public String getNim(){
        return this._nim;
    }

    // setting nim
    public void setNim(String nim){
        this._nim = nim;
    }

    // getting waktu
    public String getWaktu(){
        return this._waktu;
    }

    // setting waktu
    public void setWaktu(String waktu){
        this._waktu = waktu;
    }
}

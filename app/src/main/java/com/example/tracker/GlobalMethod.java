package com.example.tracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;



public class GlobalMethod {

    //tworzenie zapisu
    //GlobalMethod config=new GlobalMethod(this); jako globalna klasa
    //config.init("global_para"); <-inizjalizacja z nazwa parametrów
    //config.init_log("main_logcat","log");<-inicjalizacja logów main)log dla logcata "log" dla nazwy pliku z logami w htmlu



    public static final String  info = "black";
    public static final String  stop = "red";
    public static final String  network = "blue";
    public static final String  gps = "yellow";
    public static final String  notyfication = "green";
    public static final String  bluetooh = "purple";

    public static final String  DEFAULT_NAME_FILE_LOG = "log";
    public static final String  DEFAULT_NAME_TAG_LOGCAT = "MAIN_LOG";

    private Context context;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String nameTagLogCat=DEFAULT_NAME_TAG_LOGCAT;
    private String nameLogFile=DEFAULT_NAME_FILE_LOG;

    //konstrukt
    public GlobalMethod(Context context) {
        this.context = context;
    }

    public void init(){
        this.pref = this.context.getSharedPreferences("para", MODE_PRIVATE);
        this.editor = pref.edit();
    }
    public void init(String id){
        this.pref = this.context.getSharedPreferences(id, MODE_PRIVATE);
        this.editor = pref.edit();

    }
    public void init_log(String nameTagLogCat,String nameLogFile){
        this.setNameTagLogcat(nameTagLogCat);
        this.setNameLogFile(nameLogFile);
    }



    public void setPara(String namePara, String value) {
        editor.putString(namePara, value);
        editor.apply();
    }
    public void setPara(String namePara, int value) {
        editor.putInt(namePara, value);
        editor.apply();
    }
    public void setPara(String namePara, boolean value) {
        editor.putBoolean(namePara, value);
        editor.apply();
    }
    public void setPara(String namePara, float value) {
        editor.putFloat(namePara, value);
        editor.apply();
    }
    public String getParaString(String namePara) {
        String retString = "null";
        try {
            retString=pref.getString(namePara, "null");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if(retString.equals("null"))
        {
            try {
                retString=String.valueOf(pref.getInt(namePara,0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(retString.equals("null"))
        {
            try {
                retString=String.valueOf(pref.getFloat(namePara,0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return retString;
    }
    public int getParaInt(String namePara) {
        return pref.getInt(namePara, 0);

    }
    public boolean getParaBoolean(String namePara) {
        return pref.getBoolean(namePara, false);
    }
    public float getParaFloat(String namePara) {
        return pref.getFloat(namePara, 0);
    }
    public void delPara(String remove){

        editor.remove(remove);
        editor.apply();
    }
    public void delAllPara(){
        editor.clear();
        editor.apply();
        editor.commit();
    }



    public  void log(String text, String type){

        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        boolean logd=true;


        if(logd){Log.d(this.getNameTagLogcat(),text);}

        String dataTextWithStyle = "<font color='"+type+"'>"+currentTime+" "+text+"</font><br>";

        save_log_to_file(this.getNameLogFile(),dataTextWithStyle+"\n");
    }
    public  void log(String text, String type, boolean isUIisActive){

        if(isUIisActive)
            {
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        boolean logd=true;


        if(logd){Log.d(this.getNameTagLogcat(),text);}

        String dataTextWithStyle = "<font color='"+type+"'>"+currentTime+" "+text+"</font><br>";

        save_log_to_file(this.getNameLogFile(),dataTextWithStyle+"\n");
            }
    }
    public String logLoad(){
    return load_log_from_file(this.getNameLogFile());
    }
    public void logClear(){
        clearLogFile(this.getNameLogFile());
    }
    public String getSizeLogFile(){
        return fileLenght(this.getNameLogFile());
    }
    private void save_log_to_file(String name_file, String data) {
        //write
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.context.openFileOutput(name_file, Context.MODE_APPEND));
            outputStreamWriter.append(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }
    private String load_log_from_file(String name_file) {

        String ret = "";

        try {
            InputStream inputStream = this.context.openFileInput(name_file);

            if (inputStream != null && fileExists(this.context, name_file)) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append("\n").append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            } else {
                ret = "0";
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret.replaceAll("\n", "");
    }
    private void setNameLogFile(String nameLogFile){
        this.nameLogFile=nameLogFile;
    }
    private void setNameTagLogcat(String nameTagLogCat){
        this.nameTagLogCat=nameTagLogCat;
    }
    private String getNameLogFile(){
        return this.nameLogFile;
    }
    private String getNameTagLogcat(){
        return this.nameTagLogCat;
    }
    private boolean fileExists(Context context, String filename) {
        File file = context.getFileStreamPath(filename);
        if (file == null || !file.exists()) {
            return false;
        }
        return true;
    }
    private void clearLogFile(String name_file) {
        //write
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.context.openFileOutput(name_file, Context.MODE_PRIVATE));

            outputStreamWriter.write("");
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    private String fileLenght(String filename) {
        File file = this.context.getFileStreamPath(filename);
        if(file == null || !file.exists()) {
            return "null";
        }
        return String.valueOf(file.length()/1024);
    }

}
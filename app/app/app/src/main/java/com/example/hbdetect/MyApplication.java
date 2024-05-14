package com.example.hbdetect;

import android.app.Application;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MyApplication extends Application {
    //2023年12月29日-当前活动中，上一次确定保存患者的id
    private String pCase_id;
    public void setpCase_id(String case_id){
        pCase_id=case_id;
    }
    public String getpCase_id(){
        return pCase_id;
    }
    //2024年1月3日-为了解决患者可以在患者界面进行添加数据信息
    private String newPatientCaseId;
    public String getnewPatientCaseId(){
        return newPatientCaseId;
    }
    public void setnewPatientCaseId(String newPatientCaseId){
        this.newPatientCaseId=newPatientCaseId;
    }


    public String getDbname() {
        return dbname;
    }
    public boolean ifSaveImages;
    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    private String dir;
    private String dbname;

    public String getOutputname() {
        return outputname;
    }

    public void setOutputname(String outputname) {
        this.outputname = outputname;
    }

    private String outputname;
    private List<String> dblist = new ArrayList<>();
    private static MyApplication instance;

    public void setUri(Uri uri){
        this.uri=uri;
    }

    public Uri getUri() {
        return uri;
    }

    private Uri uri;
    public Bitmap getCurrentSessionBitmap() {
        return currentSessionBitmap;
    }

    public void setCurrentSessionBitmap(Bitmap currentSessionBitmap) {
        this.currentSessionBitmap = currentSessionBitmap;
    }

    private Bitmap currentSessionBitmap;

    public Bitmap getPreprocessedBitmap(){
        return preprocessedBitmap;
    }
    public void setPreprocessedBitmap(Bitmap preprocessedBitmap){
        this.preprocessedBitmap=preprocessedBitmap;
    }

    private Bitmap preprocessedBitmap;
    public Bitmap getCurrentSessionPreprocessedBitmap() {
        return currentSessionPreprocessedBitmap;
    }

    public void setCurrentSessionPreprocessedBitmap(Bitmap currentSessionPreprocessedBitmap) {
        this.currentSessionPreprocessedBitmap = currentSessionPreprocessedBitmap;
    }

    public Bitmap getCurrentSessionSegmentedBitmap() {
        return currentSessionSegmentedBitmap;
    }

    public void setCurrentSessionSegmentedBitmap(Bitmap currentSessionSegmentedBitmap) {
        this.currentSessionSegmentedBitmap = currentSessionSegmentedBitmap;
    }

    private Bitmap currentSessionPreprocessedBitmap;
    private Bitmap currentSessionSegmentedBitmap;
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        try {
            AssetManager assetManager = getAssets(); //获得assets资源管理器（assets中的文件无法直接访问，可以使用AssetManager访问）
            InputStreamReader inputStreamReader = new InputStreamReader(assetManager.open("settings.json"),"UTF-8"); //使用IO流读取json文件内容
            BufferedReader br = new BufferedReader(inputStreamReader);//使用字符高效流
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine())!=null){
                builder.append(line);
            }
            br.close();
            inputStreamReader.close();
            JSONObject testJson = new JSONObject(builder.toString()); // 从builder中读取了json中的数据。
            // 直接传入JSONObject来构造一个实例
            dbname = testJson.getString("default");
            String getImages = testJson.getString("savingImagesEnabled");
            if(getImages.equals("true")){
                ifSaveImages = true;
            }
            dir = testJson.getString("dir");
            outputname = testJson.getString("output_name");
            JSONArray array = testJson.getJSONArray("databases");
            for (int i = 0;i<array.length();i++){
                String db = array.getString(i);
                dblist.add(db);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    public static MyApplication getInstance(){
        // 因为我们程序运行后，Application是首先初始化的，如果在这里不用判断instance是否为空
        return instance;
    }
}
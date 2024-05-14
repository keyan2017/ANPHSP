package com.example.hbdetect.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);

        mContext = context;
        db = this.getReadableDatabase();
    }
    private Context mContext;
    private SQLiteDatabase db=null;
    public static final String CREATE_CASE_TABLE = "create table CaseList ("
            + "id integer primary key autoincrement, "
            + "name text, "
            + "age text, "
            + "gender text, "
            + "departments text,"
            + "bed_id text,"
            + "case_id text)";
    public static final String CREATE_ENTITY_TABLE = "create table EntityList ("
            + "id integer primary key autoincrement, "
            + "mchc text,"
            + "mchc_real text,"
            + "image blob,"
            + "time text,"
            + "take integer ,"
            + "case_id text ,"
            + "eye_side text)";

    //姓名 年龄 性别 科室 床号 ID
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CASE_TABLE);
        sqLiteDatabase.execSQL(CREATE_ENTITY_TABLE);
        db = sqLiteDatabase;
    }
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void updateOnePatient (String name, String age, String gender, String departments, String bed_id, String case_id){
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("name", name);
        values.put("age", age);
        values.put("gender", gender);
        values.put("departments", departments);
        values.put("bed_id", bed_id);
        values.put("case_id", case_id);
        String[] args = {case_id};
        db.update("CaseList",values,"case_id = ?",args);
    }
    public void updateOneEntity (String case_id, String mchc, byte[] bytes,String mchc_real,int take){
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("case_id", case_id);
        values.put("mchc", mchc);
        values.put("image",bytes);
        values.put("mchc_real",mchc_real);
        values.put("take",take);
        values.put("time",getTime());
        String[] args = {case_id, Integer.toString(take)};
        db.update("EntityList", values,"case_id = ? and take = ?",args); // 插入第一条数据
    }
    public void deleteOnePatient(String case_id){
        String[] args = {case_id};
        db.delete("CaseList","case_id = ?", args);
        db.delete("EntityList","case_id = ?", args);
    }
    public void deleteOneEntity(String case_id,int take){
        db.execSQL("Delete from EntityList where case_id = "+case_id+" and take = "+take);
    }
    public void deleteOneEntity(String id){
        String[] args = {id};
        db.delete("EntityList","id = ?", args);
    }
    public static String getTime(){
        Date currentTime = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(currentTime);
        return formattedTime;
    }
    public static Bitmap getResizedBitmap(Bitmap bitmap,float newWidth,float newHeight) {
        if (bitmap.getHeight()>bitmap.getWidth()){
            newWidth= (int) (bitmap.getWidth()*(newHeight/(float) bitmap.getHeight()));
        }else{
            newHeight= (int) (bitmap.getHeight()*(newWidth/(float) bitmap.getWidth()));
        }

        Bitmap resizedBitmap = Bitmap.createBitmap((int)newWidth,(int) newHeight, Bitmap.Config.ARGB_8888);


        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);
        Canvas canvas = new Canvas(resizedBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG |
                Paint.DITHER_FLAG |
                Paint.ANTI_ALIAS_FLAG));
        return resizedBitmap;
    }
    public void insertNewPatient(String name, String age, String gender, String departments, String bed_id, String case_id){
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("name", name);
        values.put("age", age);
        values.put("gender", gender);
        values.put("departments", departments);
        values.put("bed_id", bed_id);
        values.put("case_id", case_id);
        db.insert("CaseList", null, values); // 插入第一条数据
    }
    public void insertNewEntity(String case_id, String mchc, Bitmap image,String mchc_real,int take,String eye_side){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256*256);
        Bitmap imageForSaving = getResizedBitmap(image,256,256);
        imageForSaving.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte [] bytes =byteArrayOutputStream.toByteArray();
        ContentValues values = new ContentValues();
        // 开始组装第一条数据
        values.put("case_id", case_id);
        values.put("mchc", mchc);
        values.put("image",bytes);
        values.put("mchc_real",mchc_real);
        values.put("take",take);
        values.put("time",getTime());
        values.put("eye_side",eye_side);
        db.insert("EntityList", null, values); // 插入第一条数据
    }
    public List<Patient> selectAllPatients(){
        Cursor res = db.rawQuery("select * from CaseList",null);
        ArrayList<Patient> result = new ArrayList<>();
        while(res.moveToNext()){
            result.add(new Patient(res.getString(0),res.getString(1),res.getString(2),res.getString(3),res.getString(4),res.getString(5),res.getString(6)));
        }
        return result;
    }
    public Patient selectOnePatient(String id){
        Cursor res = db.rawQuery("select * from CaseList where case_id = '"+id+"'",null);
        ArrayList<Patient> result = new ArrayList<>();
        while(res.moveToNext()){
            result.add(new Patient(res.getString(0),res.getString(1),res.getString(2),res.getString(3),res.getString(4),res.getString(5),res.getString(6)));
        }
        if(result.size()==0) return null;
        return result.get(0);
    }
//    public static final String CREATE_ENTITY_TABLE = "create table EntityList ("
//            + "id integer primary key autoincrement, "
//            + "mchc text,"
//            + "mhch_real text,"
//            + "image blob,"
//            + "time text,"
//            + "take int,"
//            + "case_id text)";
    public ArrayList selectEntities(String id){
        Cursor res = db.rawQuery("select * from EntityList where case_id =  '"+id+"'",null);
        ArrayList<Entity> result = new ArrayList<>();
        while(res.moveToNext()){
            result.add(new Entity(res.getBlob(3),res.getString(1),res.getString(2),res.getString(6),res.getString(4),res.getInt(5),res.getString(7)));
        }
        return result;
    }
}

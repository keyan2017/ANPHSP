package com.example.hbdetect.Utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AssetLocator {

    public static String assetFilePath(Context context, String assetName) {
        File file = new File(context.getFilesDir(), assetName);

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        } catch (IOException e) {
            Log.e("pytorchandroid", "Error process asset " + assetName + " to file path");
        }
        return null;
    }
    private static String[] getConfig(Context mContext){
        String result[] = null;
        List<String> list = new ArrayList<>();
        try {
            //获取本地的Json文件
            AssetManager assetManager = mContext.getAssets();
            InputStream open = assetManager.open("vendor.json");
            InputStreamReader isr = new InputStreamReader(open, "UTF-8");
            //包装字符流,将字符流放入缓存里
            BufferedReader br = new BufferedReader(isr);
            String line;
            //StringBuilder和StringBuffer功能类似,存储字符串
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                //append 被选元素的结尾(仍然在内部)插入指定内容,缓存的内容依次存放到builder中
                builder.append(line);
            }
            br.close();
            isr.close();
            //builder.toString() 返回表示此序列中数据的字符串
            //使用Json解析
            JSONObject jsonObject = new JSONObject(builder.toString());
            org.json.JSONObject json = new org.json.JSONObject(builder.toString());
            String data = json.getString("vendor");
            org.json.JSONArray array = new org.json.JSONArray(data);
            for (int i=0, j=0; i < array.length(); i++) {
                org.json.JSONObject iter= array.getJSONObject(i);
                Iterator keys = iter.keys();
                while(keys.hasNext()) {
                    String key = (String) keys.next();
                    String value = iter.getString(key);
                    System.out.println("abcdef :" + key + " : " + value);
                    list.add(key);
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        result = new String[list.size()];
        for( int i=0; i<list.size(); ++i){
            result[i] = list.get(i);
            System.out.println("abcdefg : " + result[i]);
        }
        return result;
    }
}
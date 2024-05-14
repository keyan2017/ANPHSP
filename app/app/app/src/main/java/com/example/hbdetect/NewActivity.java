package com.example.hbdetect;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.databinding.ActivityNavigationBinding;
import com.google.android.material.navigation.NavigationView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class NewActivity extends AppCompatActivity {
    ActivityNavigationBinding binding;
    private AppBarConfiguration mAppBarConfiguration;
    Boolean isNewPatient = false;
    String case_id;
    int take;
    private void saveImg(Bitmap bitmap,MyApplication myApplication,String fName,String dir){

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(256 * 256);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();

        String path = NewActivity.this.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + myApplication.getDir() + "/img/" + dir;
        File mImageFile = new File(path);
        Boolean success = true;
        if (!mImageFile.exists()) {
            success = mImageFile.mkdirs();
        }

        String fileName = path + "/" + fName + ".jpg";
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(data, 0, data.length);    //写出图片
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.appBarNavigation.toolbar);
        setContentView(R.layout.activity_new);

        DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
        MyApplication myApplication = (MyApplication)getApplicationContext();
        Intent intent = getIntent();
        String pred = intent.getStringExtra("prediction");
        case_id= intent.getStringExtra("case_id");
        String eye_side=intent.getStringExtra("eye_side");
        Bitmap pic;
        int position = intent.getIntExtra("position",0);
        if(position == 0){
            pic = DatabaseHelper.getResizedBitmap(Bitmap.createBitmap(myApplication.getCurrentSessionBitmap()),200,200);
        }else if(position == 1){
            pic = DatabaseHelper.getResizedBitmap(Bitmap.createBitmap(myApplication.getCurrentSessionPreprocessedBitmap()),200,200);
        }else{
            pic = DatabaseHelper.getResizedBitmap(Bitmap.createBitmap(myApplication.getCurrentSessionSegmentedBitmap()),200,200);
        }
        Toolbar toolbar = this.findViewById(R.id.toolbar_2);
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        ImageView picture = findViewById(R.id.image_heading_new);
        TextView name = findViewById(R.id.name_new);
        TextView age = findViewById(R.id.age_new);
        RadioButton male = findViewById(R.id.rb_male);
        RadioButton female = findViewById(R.id.rb_female);
        TextView departments = findViewById(R.id.departments_new);
        TextView bed = findViewById(R.id.bed_id_new);
        TextView take_text = findViewById(R.id.take);
        TextView serial = findViewById(R.id.serial_number_new);
        serial.setText("病历号："+case_id);

        TextView mchc_pred = findViewById(R.id.mchc_pred_new);
        TextView mchc_real = findViewById(R.id.mchc_real_new);

        picture.setImageBitmap(pic);
        mchc_pred.setText(pred);
        Button save = findViewById(R.id.save_new);

        Patient currentPatient = mDBHelper.selectOnePatient(case_id);

        if(currentPatient == null){
            currentPatient = new Patient("","","","男","","",case_id);
            isNewPatient=true;
        }
        name.setText(currentPatient.getName());
        age.setText(currentPatient.getAge());
        male.setChecked(currentPatient.getGender().equals("男"));
        female.setChecked(currentPatient.getGender().equals("女"));
        departments.setText(currentPatient.getDeparments());
        bed.setText(currentPatient.getBed_id());

        List<Entity> entities = mDBHelper.selectEntities(case_id);
        int nextTake = 0;
        for (Entity entity:entities
             ) {
            if(entity.getTake() > nextTake)nextTake = entity.getTake();
        }
        take = nextTake+1;
        take_text.setText("第"+Integer.toString(take)+"次取样");
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(serial.getText().toString().equals("")){
                    Toast.makeText(MyApplication.getInstance(), "请输入病历号", Toast.LENGTH_SHORT).show();
                }else {
                    String gender = male.isChecked() ? male.getText().toString() : female.getText().toString();
                    if(isNewPatient){
                        mDBHelper.insertNewPatient(name.getText().toString(),age.getText().toString(),gender,departments.getText().toString(),bed.getText().toString(),case_id);
                    }
                    mDBHelper.insertNewEntity(case_id,pred,pic,mchc_real.getText().toString(),take,eye_side);
                    String takeString = Integer.toString(take);
                    saveImg(myApplication.getCurrentSessionBitmap(),myApplication,takeString+"-"+"-原图-"+case_id,case_id);
                    saveImg(myApplication.getCurrentSessionPreprocessedBitmap(),myApplication,takeString+"-"+"-预处理-"+case_id,case_id);
                    saveImg(myApplication.getCurrentSessionSegmentedBitmap(),myApplication,takeString+"-"+"-分割-"+case_id,case_id);
                    Toast.makeText(getApplicationContext(), "创建病例成功", Toast.LENGTH_SHORT).show();
                    //保存成功后，进行保存“上一次”患者的数据为当前的
                    myApplication.setpCase_id(case_id);
                    finish();
                    }
                }
        });
        Button buttonS=findViewById(R.id.buttonS);
        buttonS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 在你的 Activity 或 Fragment 中
                AlertDialog.Builder builder = new AlertDialog.Builder(NewActivity.this);
                builder.setTitle("请重新输入病历号");
                final EditText input = new EditText(NewActivity.this);
                builder.setView(input);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取用户输入的文本
                        String userInput = input.getText().toString();
                        if(userInput.equals("")){
                            return;
                        }
                        case_id=userInput;
                        Patient currentPatient = mDBHelper.selectOnePatient(case_id);
                        if(currentPatient==null){
                            serial.setText("病历号："+userInput);
                            currentPatient = new Patient("","","","男","","",case_id);
                            isNewPatient=true;
                        }else{
                            isNewPatient=false;
                        }
                        name.setText(currentPatient.getName());
                        age.setText(currentPatient.getAge());
                        male.setChecked(currentPatient.getGender().equals("男"));
                        female.setChecked(currentPatient.getGender().equals("女"));
                        departments.setText(currentPatient.getDeparments());
                        bed.setText(currentPatient.getBed_id());
                        serial.setText("病历号："+case_id);
                        List<Entity> entities2 = mDBHelper.selectEntities(case_id);
                        int nextTake2 = 0;
                        for (Entity entity:entities2
                        ) {
                            if(entity.getTake() > nextTake2)nextTake2 = entity.getTake();
                        }
                        take = nextTake2+1;
                        take_text.setText("第"+Integer.toString(take)+"次取样");
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击取消按钮，可以在这里处理取消的逻辑
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();

            }
        });
    }
}
package com.example.hbdetect.ui.home;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.example.hbdetect.CameraActivity;
import com.example.hbdetect.MyApplication;
import com.example.hbdetect.NewActivity;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.DNNUtils;
import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.adapter.PictureAdapter;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.databinding.FragmentHomeBinding;
import com.example.hbdetect.ui.CropImageViewActivity;
import com.example.hbdetect.ui.picture.PictureFragment;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.soundcloud.android.crop.Crop;
public class HomeFragment extends Fragment {
    private MyApplication myApplication;
    private static final int RESULT_OK = 125;
    private Uri uri;

    private static DNNUtils dnn = new DNNUtils();
    static{
        try {
            dnn.init(MyApplication.getInstance());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    private ViewPager2 viewpager;

    private ImageView save_button;
    String result_value;
    private TextView result;
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        ImageView fab = root.findViewById(R.id.fab);
        ImageView open_gallery_button = root.findViewById(R.id.open_gallery);
        viewpager = root.findViewById(R.id.view_pager);
        save_button = root.findViewById(R.id.save_button);
        result = root.findViewById(R.id.result);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(root.getContext(), CameraActivity.class);
                startActivity(intent);
            }
        });
        fab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                String[] options = {"清空", "重拍"};
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                MyApplication myApplication1 = (MyApplication)getActivity().getApplicationContext();
                                myApplication1.setCurrentSessionBitmap(null);
                                myApplication1.setCurrentSessionSegmentedBitmap(null);
                                myApplication1.setCurrentSessionPreprocessedBitmap(null);
                                PictureAdapter adapter = new PictureAdapter(getChildFragmentManager(),getLifecycle());
                                List<Fragment> list = new ArrayList<>();
                                myApplication = (MyApplication)getActivity().getApplicationContext();
                                save_button.setVisibility(View.INVISIBLE);
                                PictureFragment source = PictureFragment.newInstance();
                                source.setTitle("Please click the button below to take a sample");
                                result.setText("");
                                list.add(source);
                                adapter.setFragmentList(list);
                                viewpager.setAdapter(adapter);
                                break;
                            case 1:
                                Intent intent = new Intent(root.getContext(), CameraActivity.class);
                                startActivity(intent);
                                break;
                        }
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                return false;
            }
        });
        open_gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                //调用setDataAndType方法，指定了选择的数据类型为图片
                //设置数据的URI为MediaStore.Images.Media.EXTERNAL_CONTENT_URI，表示选择外部存储中的图片
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                //调用startActivityForResult方法，将Intent发送给系统，并指定一个请求码为2，以便在之后的回调中处理用户选择的图片
                startActivityForResult(intent, 3);
            }
        });
        save_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //首先获取到上一次保存后的患者的id
                String beforCase_id=myApplication.getpCase_id();
                //再次提取application中是否有newPatientCaseId数据，如果有，则代表，用户是从患者信息界面-新建进来
                String newPatientCaseId= myApplication.getnewPatientCaseId();
                if(newPatientCaseId!=null){
                    //进行提示用户选择
                    showEyeSelectionDialog(root,newPatientCaseId,"1");
                    //开始报错当前的数据进行
//                    Intent intent = new Intent(root.getContext(), NewActivity.class);
//                    intent.putExtra("prediction", result_value);
//                    intent.putExtra("position", viewpager.getCurrentItem());
//                    intent.putExtra("case_id", newPatientCaseId);
//                    startActivityForResult(intent,234);
                }else{
                    if (beforCase_id==null||beforCase_id==""){
                        showinputdialog(root);
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                        builder.setTitle("提示");
                        builder.setMessage("是否保存数据到患者\n-- "+beforCase_id+" --\n上？如果点击取消，将新建一个患者。");

                        // 设置确定按钮
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
//                                Intent intent = new Intent(root.getContext(), NewActivity.class);
//                                intent.putExtra("prediction", result_value);
//                                intent.putExtra("position", viewpager.getCurrentItem());
//                                intent.putExtra("case_id", beforCase_id);
//                                startActivity(intent);
                                showEyeSelectionDialog(root,beforCase_id,"2");
                            }
                        });

                        // 设置取消按钮
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 在这里处理取消按钮的点击事件
                                // 可以添加新建一个患者的逻辑
                                myApplication.setpCase_id(null);
                                showinputdialog(root);
                            }
                        });

                        // 创建并显示提示框
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    //提示用户是否进行套用上一次的患者
                }
                }

        });
        return root;
    }
    private void showEyeSelectionDialog(View root,String caseId,String qD) {
        // 布局文件中定义的视图
        final RadioGroup radioGroup = new RadioGroup(getContext());
        RadioButton leftEyeRadioButton = new RadioButton(getContext());
        RadioButton rightEyeRadioButton = new RadioButton(getContext());

        leftEyeRadioButton.setText("左眼");
        rightEyeRadioButton.setText("右眼");

        radioGroup.addView(leftEyeRadioButton);
        radioGroup.addView(rightEyeRadioButton);

        // 创建 AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("选择眼睛");
        builder.setView(radioGroup);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 获取用户选择的眼睛
                int selectedId = radioGroup.getCheckedRadioButtonId();
                String eyesSelectS="";
                if (selectedId == leftEyeRadioButton.getId()) {
                    eyesSelectS="左眼";
                } else if (selectedId == rightEyeRadioButton.getId()) {
                    eyesSelectS="右眼";
                }
                Intent intent = new Intent(root.getContext(), NewActivity.class);
                intent.putExtra("prediction", result_value);
                intent.putExtra("position", viewpager.getCurrentItem());
                intent.putExtra("case_id", caseId);
                intent.putExtra("eye_side",eyesSelectS);
                if(qD.equals("1")){
                    startActivityForResult(intent,234);
                }else{
                    startActivity(intent);
                }
            }
        });

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // 显示 AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void showinputdialog(View root){
        AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
        builder.setTitle("请输入病历号");
        EditText editText = new EditText(root.getContext());
        builder.setView(editText);
        builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                String case_id = editText.getText().toString();
                //判断字符是否含有中文和特殊符号，如果含有不予通过
                //String regex = "^[a-zA-Z0-9\\u4e00-\\u9fa5]+$";
                //只判断不能包含中文和特殊符号
                String regex = "^[\\u4e00-\\u9fa5a-zA-Z0-9]+$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(case_id);
                if(!matcher.matches()){
                    Toast.makeText(MyApplication.getInstance(),"病历号不能特殊符号的病历号，请检查后重试",Toast.LENGTH_SHORT).show();
                    dialog.cancel();
                    showinputdialog(root);
                }
                else if(!case_id.equals("")) {
                    showEyeSelectionDialog(root,case_id,"2");
                }else{
                    dialog.cancel();
                }
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog,int which){
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    public static Bitmap readBitmapFromUri(Context context, Uri uri) {
        Bitmap bitmap = null;
        ParcelFileDescriptor parcelFileDescriptor = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();

            // 打开文件描述符
            parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r");
            if (parcelFileDescriptor != null) {
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                // 通过BitmapFactory解码文件描述符
                bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (parcelFileDescriptor != null) {
                try {
                    parcelFileDescriptor.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmap;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);
        if (requestCode == 123) {
            try{
                Bundle extras = result.getExtras();
                if (extras != null) {

                    Bitmap photo = extras.getParcelable("data");
                    myApplication.setPreprocessedBitmap(photo);
                    // 裁剪后的图片
                }
            }catch (NullPointerException e){
                e.printStackTrace();
            }

        }else if(requestCode==234){
            //监听到此次是已234打开的页面，进行关闭当前activit的界面
            getActivity().onBackPressed();
            //回归赋值的情况
            myApplication.setnewPatientCaseId(null);
            myApplication.setPreprocessedBitmap(null);
            myApplication.setCurrentSessionBitmap(null);
            myApplication.setCurrentSessionPreprocessedBitmap(null);
            myApplication.setCurrentSessionSegmentedBitmap(null);
        }
    }
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            // 使用 ContentResolver 从 Uri 中获取 Bitmap
            return MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        PictureAdapter adapter = new PictureAdapter(getChildFragmentManager(),getLifecycle());
        List<Fragment> list = new ArrayList<>();
        myApplication = (MyApplication)getActivity().getApplicationContext();
        Bitmap currentSeesion = myApplication.getCurrentSessionBitmap();
        Bitmap preprocessedBitmap=myApplication.getPreprocessedBitmap();
        if (currentSeesion == null) {
            save_button.setVisibility(View.INVISIBLE);
            PictureFragment source = PictureFragment.newInstance();
            source.setTitle("Please click the button below to take a sample");
            list.add(source);
            adapter.setFragmentList(list);
            viewpager.setAdapter(adapter);
        } else {
            save_button.setVisibility(View.VISIBLE);
            //做个判断上传图片可能会出现问题
            if(preprocessedBitmap!=null){
                PictureFragment source = PictureFragment.newInstance();
                PictureFragment preprocessed = PictureFragment.newInstance();
                PictureFragment segmented = PictureFragment.newInstance();
                dnn.setInput(preprocessedBitmap);
                source.setPicture(DatabaseHelper.getResizedBitmap(currentSeesion,1000,1500));
                source.setTitle("Original image");
                dnn.process(false);
                preprocessed.setTitle("Preprocessed image");
                //preprocessed.setPicture(dnn.getPreprocessedImage());
                preprocessed.setPicture(preprocessedBitmap);
                //myApplication.setCurrentSessionPreprocessedBitmap(dnn.getPreprocessedImage());
                myApplication.setCurrentSessionPreprocessedBitmap(preprocessedBitmap);

                segmented.setTitle("Segmented image");
                segmented.setPicture(dnn.getSegmentedImage());
                myApplication.setCurrentSessionSegmentedBitmap(dnn.getSegmentedImage());

                result_value = dnn.prediction();

                if(result_value.equals("NaN")){
                    list.add(source);
                    adapter.setFragmentList(list);
                    viewpager.setAdapter(adapter);
                    save_button.setVisibility(View.INVISIBLE);
                    result.setText("采样无效");
                }else {
                    list.add(source);
                    list.add(preprocessed);
                    list.add(segmented);
                    adapter.setFragmentList(list);
                    viewpager.setAdapter(adapter);
                    result.setText("Detection result：" + result_value);
                }
            }else{
                PictureFragment source = PictureFragment.newInstance();
                PictureFragment preprocessed = PictureFragment.newInstance();
                PictureFragment segmented = PictureFragment.newInstance();
                dnn.setInput(currentSeesion);
                source.setPicture(DatabaseHelper.getResizedBitmap(currentSeesion,1000,1500));
                source.setTitle("Original image");
                dnn.process(true);
                preprocessed.setTitle("Preprocessed image");
                //preprocessed.setPicture(dnn.getPreprocessedImage());
                preprocessed.setPicture(dnn.getPreprocessedImage());
                //myApplication.setCurrentSessionPreprocessedBitmap(dnn.getPreprocessedImage());
                myApplication.setCurrentSessionPreprocessedBitmap(dnn.getPreprocessedImage());

                segmented.setTitle("Segmented image");
                segmented.setPicture(dnn.getSegmentedImage());
                myApplication.setCurrentSessionSegmentedBitmap(dnn.getSegmentedImage());

                result_value = dnn.prediction();

                if(result_value.equals("NaN")){
                    //启动图片剪裁
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("选择的图片采样无效是否裁剪上传？");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 获取用户输入的文本
                            // 启动图片剪裁
                            //Crop.of(myApplication.getUri(),null).start(getActivity());
                            Intent intent = new Intent("com.android.camera.action.CROP");
                            intent.setDataAndType(myApplication.getUri(), "image/*");
                            intent.putExtra("crop", "true");
                            intent.putExtra("return-data", true);
                            intent.putExtra("outputX", 256);  // 设置裁剪区域的宽度
                            intent.putExtra("outputY", 256);  // 设置裁剪区域的高度
                            startActivityForResult(intent, 123);
//                            Intent cropIntent = new Intent(getActivity(), CropImageViewActivity.class);
//                            cropIntent.putExtra("currentSessionBitmap", myApplication.getUri());
//                            startActivityForResult(cropIntent, 123);
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

                    list.add(source);
                    adapter.setFragmentList(list);
                    viewpager.setAdapter(adapter);
                    save_button.setVisibility(View.INVISIBLE);
                    result.setText("采样无效");
                }else {
                    list.add(source);
                    list.add(preprocessed);
                    list.add(segmented);
                    adapter.setFragmentList(list);
                    viewpager.setAdapter(adapter);
                    result.setText("Detection result：" + result_value);
                }
            }

        }
    }


}
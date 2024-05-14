package com.example.hbdetect.ui.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hbdetect.CameraActivity;
import com.example.hbdetect.DetailActivity;
import com.example.hbdetect.Mainactivity;
import com.example.hbdetect.MyApplication;
import com.example.hbdetect.NewActivity;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.Utils.RegexUtils;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.ui.home.HomeFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.codehaus.stax2.ri.typed.ValueDecoderFactory;

import java.util.ArrayList;
import java.util.List;

public class DetailFragment extends Fragment {
    public boolean editted = false;
    public Patient getPatient() {
        return patient;
    }
    View v;

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    private Patient patient;
    public DetailFragment() {
        // Required empty public constructor
    }
    public static DetailFragment newInstance() {
        DetailFragment fragment = new DetailFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    private LineData getLineData(Patient patient){
        List<Entry>  chartData = new ArrayList<>();
        List<Entry>  chartData_real = new ArrayList<>();
        DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
        List<Entity> entities = mDBHelper.selectEntities(patient.getCase_id());
        if(entities.size()!=0) {
            for (Entity entity : entities
            ) {
                if(entity.getMchc().equals("")){
                    chartData.add(new Entry(entity.getTake(),0));
                }else {
                    chartData.add(new Entry(entity.getTake(),Float.parseFloat(entity.getMchc())));
                }
                if(entity.getMchc_real().equals("")){
                    chartData_real.add(new Entry(entity.getTake(),0));
                }else {
                    chartData_real.add(new Entry(entity.getTake(),Float.parseFloat(entity.getMchc_real())));
                }
            }
            LineDataSet pred = new LineDataSet(chartData, "预测值");
            pred.setColor(R.color.violet);
            pred.setCircleColor(R.color.violet);
            LineDataSet real = new LineDataSet(chartData_real, "真实值");
            LineData lineData = new LineData();
            lineData.addDataSet(pred);
            lineData.addDataSet(real);
            return lineData;
        }
        return null;
    }
    @Override
    public void onResume() {
        super.onResume();
        LineChart lineChart = v.findViewById(R.id.chart_line);
        lineChart.setData(getLineData(patient));
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v=inflater.inflate(R.layout.fragment_detail, container, false);
        TextView name = v.findViewById(R.id.name);
        TextView age = v.findViewById(R.id.age);
        TextView gender = v.findViewById(R.id.gender);
        TextView departments = v.findViewById(R.id.departments);
        TextView bed = v.findViewById(R.id.bed_id);
        name.setText(patient.getName());
        age.setText(patient.getAge());
        gender.setText(patient.getGender());
        departments.setText(patient.getDeparments());
        bed.setText(patient.getBed_id());



        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请输入姓名");
                EditText editText = new EditText(v.getContext());
                editText.setText(name.getText().toString());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                        if(!editText.getText().toString().equals("")) {
                            name.setText(editText.getText().toString());
                            editted = true;
                        }else{

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
        });
        gender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请输入性别");
                EditText editText = new EditText(v.getContext());
                editText.setText(gender.getText().toString());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                        if(!editText.getText().toString().equals("")) {
                            gender.setText(editText.getText().toString());
                            editted = true;
                        }else{

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
        });
        age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请输入年龄");
                EditText editText = new EditText(v.getContext());
                editText.setText(age.getText().toString());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                        if(!editText.getText().toString().equals("")) {
                            age.setText(editText.getText().toString());
                            editted = true;
                        }else{

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
        });
        departments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请输入科室");
                EditText editText = new EditText(v.getContext());
                editText.setText(departments.getText().toString());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                        if(!editText.getText().toString().equals("")) {
                            departments.setText(editText.getText().toString());
                            editted = true;
                        }else{

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
        });
        bed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("请输入床号");
                EditText editText = new EditText(v.getContext());
                editText.setText(bed.getText().toString());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){

                        if(!editText.getText().toString().equals("")) {
                            bed.setText(editText.getText().toString());
                            editted = true;
                        }else{

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
        });
        Button save = v.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(RegexUtils.isNumber(age.getText().toString())) {
                    editted = false;
                    DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
                    mDBHelper.updateOnePatient(name.getText().toString(),age.getText().toString(),gender.getText().toString(),departments.getText().toString(),bed.getText().toString(),patient.getCase_id());
                    Toast.makeText(MyApplication.getInstance(), "修改成功", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button creatM=v.findViewById(R.id.creatM);
        creatM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //getParentFragmentManager().popBackStack();
                //首先要进行数据处理
                MyApplication myApplication=(MyApplication)getActivity().getApplicationContext();
                myApplication.setnewPatientCaseId(patient.getCase_id());
                //设置数据为null
                myApplication.setPreprocessedBitmap(null);
                myApplication.setCurrentSessionBitmap(null);
                myApplication.setCurrentSessionPreprocessedBitmap(null);
                myApplication.setCurrentSessionSegmentedBitmap(null);
                Intent intent = new Intent(getContext(), Mainactivity.class);
                startActivity(intent);
            }
        });
        return v;
    }
}
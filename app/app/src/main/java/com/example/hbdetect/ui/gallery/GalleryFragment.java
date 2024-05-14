package com.example.hbdetect.ui.gallery;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hbdetect.DetailActivity;
import com.example.hbdetect.MyApplication;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.DatabaseHelper;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.hbdetect.adapter.PatientAdapter;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.databinding.FragmentGalleryBinding;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class GalleryFragment extends Fragment {
    private List<Patient> list;
    //创建一个用于检索的List
    private List<Patient> searchList;
    //创建一个初始适配器，全部的
    private PatientAdapter patientAdapterAll;
    DatabaseHelper mDBHelper;
    ListView caselist;
    private FragmentGalleryBinding binding;
    ViewGroup mContainer = null;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        mContainer = container;
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDBHelper.close();
        binding = null;
    }
    @Override
    public void onResume() {
        super.onResume();
        caselist = mContainer.findViewById(R.id.patient_list);
        mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
        list= mDBHelper.selectAllPatients();
        //初始化searchList
        searchList=list;
        if(list.size()==0){
            caselist.setAdapter(null);
        }else{
            PatientAdapter adapter = new PatientAdapter(MyApplication.getInstance(),list);
            patientAdapterAll=adapter;
            caselist.setAdapter(adapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        ListView caselist = mContainer.findViewById(R.id.patient_list);
        caselist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Patient patient = searchList.get(i);
                Intent intent = new Intent(mContainer.getContext(),DetailActivity.class);
                intent.putExtra("serial",patient.getCase_id());
                //打开单个界面
                startActivity(intent);
            }
        });
        TextView search = mContainer.findViewById(R.id.search_text);
        search.setText("");
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 在文本变化之前的回调
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 在文本变化时的回调
            }
            @Override
            public void afterTextChanged(Editable editable) {
                // 在文本变化之后的回调
                String searchText = editable.toString().trim();

                if (!searchText.isEmpty()) {
                    List<Patient> filteredList = new ArrayList<>();

                    // 根据输入的文本过滤列表数据
                    for (Patient patient : list) {
                        //现在仅支持，患者编号，患者姓名，患者创建时间来检索
                        if (patient.getCase_id().contains(searchText) || patient.getName().contains(searchText)) {
                            filteredList.add(patient);
                        }
                    }

                    // 更新适配器
                    PatientAdapter adapter = new PatientAdapter(MyApplication.getInstance(), filteredList);
                    searchList=filteredList;
                    caselist.setAdapter(adapter);
                } else {
                    // 如果输入框为空，显示原始列表数据
                    PatientAdapter adapter = new PatientAdapter(MyApplication.getInstance(), list);
                    searchList=list;
                    caselist.setAdapter(adapter);
                }
            }
        });

        Button button=mContainer.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(new DatePickerFragment.OnDateSelectedListener() {
                    @Override
                    public void onDateSelected(String selectedDate) {
                        //首先清空输入框
                        search.setText("");
                        // 在这里处理选择的日期
                        //Toast.makeText(getActivity(), "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                        List<Patient> filteredList = new ArrayList<>();
                        for(Patient patient : list){
                            //获取时间
                            List<Entity> entities =mDBHelper.selectEntities(patient.getCase_id());
                            String time=entities.get(entities.size()-1).getTime();
                            if(time.contains(selectedDate)){
                                filteredList.add(patient);
                            }
                        }
                        if(filteredList.size()==0){
                            Toast.makeText(getActivity(), "检索出患者为空，请重新选择日期或者输入患者信息进行查询", Toast.LENGTH_LONG).show();
                            PatientAdapter adapter = new PatientAdapter(MyApplication.getInstance(), list);
                            searchList=list;
                            caselist.setAdapter(adapter);
                            return;
                        }
                        // 更新适配器
                        PatientAdapter adapter = new PatientAdapter(MyApplication.getInstance(), filteredList);
                        searchList=filteredList;
                        caselist.setAdapter(adapter);
                    }
                });
            }
        });
    }
    //创建方法，根据caseid去查创建时间，然后进行查询是否与现在的字符
    private void showDatePickerDialog(DatePickerFragment.OnDateSelectedListener listener) {
        DatePickerFragment newFragment = new DatePickerFragment(listener);
        newFragment.show(getParentFragmentManager(), "datePicker");
    }
    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private String selectedDate;
        private OnDateSelectedListener callback;

        public interface OnDateSelectedListener {
            void onDateSelected(String selectedDate);
        }
        public DatePickerFragment(OnDateSelectedListener callback) {
            this.callback = callback;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // 使用当前日期作为默认日期
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // 创建一个新的 DatePickerDialog 实例并返回
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            // 处理用户选择的日期
            selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day);
            // 通过回调通知选择的日期
            callback.onDateSelected(selectedDate);
        }

        // 添加获取选定日期的方法
        public String getSelectedDate() {
            return selectedDate;
        }
    }
}
package com.example.hbdetect.ui.detail;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.hbdetect.DetailActivity;
import com.example.hbdetect.MyApplication;
import com.example.hbdetect.NewActivity;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.adapter.EntityAdapter;
import com.example.hbdetect.adapter.PatientAdapter;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.databinding.FragmentHomeBinding;

import java.util.List;

public class EntityFragment extends Fragment {


    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    private List<Entity> entities;

    public EntityFragment() {
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
    @Override
    public void onResume() {
        super.onResume();
        EntityAdapter adapter = new EntityAdapter(MyApplication.getInstance(),entities);
        caseList.setAdapter(adapter);
    }
    private ListView caseList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_entity, container, false);
        View root = v.getRootView();
        caseList = v.findViewById(R.id.entity_list);
        if(entities.size()==0){
            caseList.setAdapter(null);
        }else{
            EntityAdapter adapter = new EntityAdapter(MyApplication.getInstance(),entities);
            caseList.setAdapter(adapter);
        }
        caseList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Entity entity = entities.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                builder.setTitle("请输入真实值");
                EditText editText = new EditText(root.getContext());
                builder.setView(editText);
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        String case_id = editText.getText().toString();
                        if(!case_id.equals("")) {
                            entity.setMchc_real(editText.getText().toString());
                            DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
                            mDBHelper.updateOneEntity(entity.getCase_id(),entity.getMchc(),entity.getImage(),editText.getText().toString(),entity.getTake());
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
        caseList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Entity entity = entities.get(i);
                AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                builder.setTitle("是否删除该记录");
                builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
                        mDBHelper.deleteOneEntity(entity.getCase_id(),entity.getTake());
                        entities.remove(entity);
                        EntityAdapter adapter = new EntityAdapter(MyApplication.getInstance(),entities);
                        caseList.setAdapter(adapter);
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
                return true;
            }
        });
        return v;
    }
}
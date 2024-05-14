package com.example.hbdetect.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.hbdetect.MyApplication;
import com.example.hbdetect.R;
import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;

import org.w3c.dom.Text;

import java.util.List;

public class PatientAdapter extends ArrayAdapter<Patient> {
    List<Patient> items;
    public PatientAdapter(@NonNull Context context, List<Patient> items) {
        super(context, R.layout.list,items);
        this.items=items;
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parents){
        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list,parents,false);
        }
        ImageView picture = convertView.findViewById(R.id.picture);
        TextView name = convertView.findViewById(R.id.name);
        TextView data = convertView.findViewById(R.id.data);
        TextView case_id = convertView.findViewById(R.id.case_id);
        TextView mchc = convertView.findViewById(R.id.mchc);
        TextView latestTime = convertView.findViewById(R.id.time_latest);
        Patient patient = items.get(position);
        DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
        List<Entity> entities = mDBHelper.selectEntities(patient.getCase_id());
        byte[] image = null;
        if(entities.size() !=0) {
            image = entities.get(entities.size()-1).getImage();
            picture.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length, null));
            mchc.setText(entities.get(entities.size()-1).getMchc());
            latestTime.setText(entities.get(entities.size()-1).getTime());
        }else{
            mchc.setText("暂无采样");
        }
        name.setText(patient.getName());
        data.setText(patient.getAge()+"岁 "+patient.getGender()+" "+patient.getDeparments()+" "+patient.getBed_id());//"34岁 男 内科 4床"
        case_id.setText("病历号："+patient.getCase_id());



        return convertView;
    }
}

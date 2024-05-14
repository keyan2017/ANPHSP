package com.example.hbdetect.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
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

public class EntityAdapter extends ArrayAdapter<Entity> {
    List<Entity> items;
    public EntityAdapter(@NonNull Context context, List<Entity> items) {
        super(context, R.layout.list,items);
        this.items=items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parents){
        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_2,parents,false);
        }
        TextView take = convertView.findViewById(R.id.detail_take);

        ImageView picture = convertView.findViewById(R.id.detail_picture);

        TextView mchc = convertView.findViewById(R.id.detail_mchc);
        TextView mchc_real = convertView.findViewById(R.id.detail_mchc_real);
        TextView time = convertView.findViewById(R.id.detail_time);
        TextView detail_mchc2=convertView.findViewById(R.id.detail_mchc2);
        Entity entity = items.get(position);
        byte[] image = entity.getImage();

        picture.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length, null));

        take.setText("No."+entity.getTake());
        mchc.setText("预测值："+entity.getMchc());
        mchc_real.setText("真实值："+entity.getMchc_real());
        detail_mchc2.setText("位置："+entity.getEye_side());
        time.setText(entity.getTime());
        return convertView;
    }
}

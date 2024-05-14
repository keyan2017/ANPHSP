package com.example.hbdetect;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hbdetect.Utils.DatabaseHelper;
import com.example.hbdetect.Utils.RegexUtils;
import com.example.hbdetect.adapter.PictureAdapter;
import com.example.hbdetect.bean.Entity;
import com.example.hbdetect.bean.Patient;
import com.example.hbdetect.ui.detail.DetailFragment;
import com.example.hbdetect.ui.detail.EntityFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {
    private ViewPager2 viewpager2;
    private TabLayout tabLayout;
    private EntityFragment entityFragment;
    private DatabaseHelper mDBHelper;
    Patient patient;
    private static final String[] menus = {"患者信息", "采样记录"};
    @Override
    public void onResume() {
        super.onResume();
        entityFragment.setEntities(mDBHelper.selectEntities(patient.getCase_id()));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
        Intent intent = getIntent();
        String extra = intent.getStringExtra("serial");
        patient= mDBHelper.selectOnePatient(intent.getStringExtra("serial"));

        List<Entity> entities = mDBHelper.selectEntities(patient.getCase_id());
        List<String> titleList = new ArrayList<>();
        titleList.add("基本信息");
        titleList.add("采样数据");
        Toolbar toolbar = this.findViewById(R.id.toolbar_3);
        toolbar.setSubtitle(patient.getCase_id());
        toolbar.getOverflowIcon().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        viewpager2 = this.findViewById(R.id.detail_pager);
        tabLayout = this.findViewById(R.id.tablayout);
        ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
        DetailFragment detailFragment = new DetailFragment();
        detailFragment.setPatient(patient);

        entityFragment = new EntityFragment();
        entityFragment.setEntities(entities);

        fragmentList.add(detailFragment);
        fragmentList.add(entityFragment);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(detailFragment.editted){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
                    builder.setTitle("是否放弃修改？");
                    builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog,int which){
                            finish();
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
                }else {
                    finish();
                }
            }
        });
        // 创建ViewPager2所使用的适配器，FragmentStateAdapter抽象类的实现类对象
        FragmentStateAdapter adapter = new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getItemCount() {
                return fragmentList.size();
            }
        };
        viewpager2.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewpager2, false, true, (tab, position) -> { // TabLayout和ViewPager2关联到一起
            tab.setText(menus[position]); // 设置Tab的标题
        }).attach(); // 调用该方法才能真正绑定起来
    }
    /**
     * menu 与 toolbar 绑定
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        return super.onCreateOptionsMenu(menu);
    }
    /**
     *  menu 里 item 点击事件监听
     * @param item menu 里的 菜单
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_delete){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("是否删除该患者");
            builder.setPositiveButton("确定",new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog,int which){
                    DatabaseHelper mDBHelper = new DatabaseHelper(MyApplication.getInstance(),MyApplication.getInstance().getDbname(),null,1);
                    mDBHelper.deleteOnePatient(patient.getCase_id());
                    finish();
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

        }else{
            return super.onOptionsItemSelected(item);
        }
    }
}
package com.example.hbdetect.Utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionUtil {

    //检查某个功能的多个权限，返回true表示已完全启用权限，返回false表示未完全启用权限
    //比如通讯录这个功能，需要获取它有的多个权限[读权限，写权限]
    //act：当前的Activity
    //permissions：某个功能的权限集合，如通讯录的[读，写]
    //requestCode：标识一下“某个功能”
    public static boolean checkPermission(Activity act, String[] permissions, int requestCode){
        //判断android的版本
        //Build.VERSION.SDK_INT：当前android的版本
        //Build.VERSION_CODES.M：android6.0
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){//安卓版本在6.0及以后才需要判断  M就是6.0
            //check的含义：权限是否开启
            //这里给了默认值PackageManager.PERMISSION_GRANTED，意思是开启权限
            int check = PackageManager.PERMISSION_GRANTED;//默认授权的值
            for (String permission:permissions){
                //返回结果为是否授权
                check = ContextCompat.checkSelfPermission(act, permission);
                //如果没有授权，则退出
                if (check!=PackageManager.PERMISSION_GRANTED){
                    break;
                }
            }
            //未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
            if (check!=PackageManager.PERMISSION_GRANTED){
                //弹窗，用户操作
                ActivityCompat.requestPermissions(act,permissions,requestCode);
                return false;
            }
        }

        return true;
    }
    //grantResults：是用户点“授权”完弹窗后的授权结果数组
    //检查权限结果数组，返回true表示都已经获得授权。返回false表示至少有一个未获得授权
    public static boolean checkGrant(int[] grantResults) {

        if (grantResults!=null){
            //遍历权限结果数组中的每条选择结果
            for (int grant:grantResults){
                //未获得授权
                if (grant!= PackageManager.PERMISSION_GRANTED){
                    return false;
                }

            }
            return true;
        }
        return false;
    }
}

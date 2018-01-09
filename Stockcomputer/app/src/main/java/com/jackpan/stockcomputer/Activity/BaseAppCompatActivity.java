package com.jackpan.stockcomputer.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.jackpan.stockcomputer.R;

public class BaseAppCompatActivity extends AppCompatActivity {
    private static Toast toast = null;


    public void startActivity(Class<?> clas) {

        startActivity(new Intent(this, clas));

    }

    public void startActivity(Class<?> clas, Bundle bundle) {

        Intent intent = new Intent();
        intent.setClass(this, clas);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * 基本AlertDilog
     */
    public void showBaseAlertDilog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(getString(R.string.alertbutton_ok), null)
                .show();
    }

    /**
     * 不重複的toast
     */
    public void showToast(String msg) {
        if (toast == null) {
            toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
    /**
     * 檢查網路
     */
    public  void checkNetWork(){
        if (isConnected()) {
            //執行下載任務
        }else{
            //告訴使用者網路無法使用
            showToast("網路無開啟！！");
            return;
        }

    }

    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

}


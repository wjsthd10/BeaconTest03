package com.example.beacontest03;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 12345;

    ArrayList<String> deviceList=new ArrayList<>();
    ArrayAdapter adapter;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> devices;
    ArrayList<String> testAddress=new ArrayList<>();
//    DiscoveryResultReceiver discoveryResultReceiver;
    BluetoothLeScanner scanner;
    public final static int BLUETOOTH_REQUEST = 100123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            finish();
        }
        Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(i, BLUETOOTH_REQUEST);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT != Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);
            }else {
                Intent intent;
                intent=new Intent(this, BeaconDataShow.class);
                startActivity(intent);
                finish();

            }
        }else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P){// 9버전 일때 퍼미션 받기.
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED ){
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            }else {
                Intent intent;
                intent=new Intent(this, BeaconDataShow.class);
                startActivity(intent);
                finish();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 10:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED && grantResults[2]== PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "장치검색 기능이 제한됩니다.", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent=new Intent(this, BeaconDataShow.class);
                    startActivity(intent);
                    finish();
                }
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (requestCode == RESULT_OK) {
                Toast.makeText(this, "블루투스켜짐", Toast.LENGTH_SHORT).show();
            }
        }
        switch (requestCode){
            case BLUETOOTH_REQUEST:
                if (resultCode==RESULT_CANCELED){
                    Toast.makeText(this, "블루투스를 활성화 하지 않으면 사용할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions(){
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            if(permissionCheck!=0){
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1001);
            }
            else{
                Log.d("checkPermission", "No need to check permissions. SDK version < LoLLIPOP");
            }
        }
    }
}
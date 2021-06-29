package com.example.beacontest03;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacAddressDataShow extends AppCompatActivity implements BeaconConsumer {

    BeaconManager beaconManager;

    ArrayList<String> testAddress=new ArrayList<>();
    DeviceDataItem deviceDataItem=new DeviceDataItem();
    TextView tv, major, minor;


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_address_data_show);
        tv=findViewById(R.id.tv01);
        major=findViewById(R.id.tv_major);
        minor=findViewById(R.id.tv_minor);

        beaconManager=BeaconManager.getInstanceForApplication(this);
        beaconManager.bind(this);

    }



    @Override
    protected void onResume() {
        super.onResume();
        deviceDataItem=getIntent().getParcelableExtra("deviceData");
        tv.setText(deviceDataItem.getDeviceUUID());
        major.setText(deviceDataItem.getDeviceMajor());
        minor.setText(deviceDataItem.getDeviceMinor());
        try {
            beaconManager.startRangingBeaconsInRegion(new Region(deviceDataItem.getDeviceUUID(), null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.e("songL", "didEnterRegion = "+"한개이상의 비콘");
            }

            @Override
            public void didExitRegion(Region region) {
                Log.e("songL", "didEnterRegion = "+"비콘을 찾을 수 없음");
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                if (state == 0) {
                    Log.e("songL", "didDetermineStateForRegion = "+"비콘 보이는 상태 : "+state);
                }else {
                    Log.e("songL", "didDetermineStateForRegion = "+"비콘 안보이는 상태 : "+state);
                }
            }
        });

        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                List<Beacon> list = (List<Beacon>) beacons;
                if (beacons.size() > 0) {
                    Log.e("songL", "get Id2() = "+beacons.iterator().next().getId2().toString());
                }
            }
        });

//        try {
//            beaconManager.startRangingBeaconsInRegion(new Region(uuid, null, null, null));
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
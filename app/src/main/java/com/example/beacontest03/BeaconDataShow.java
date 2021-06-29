package com.example.beacontest03;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.JobIntentService;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.formatter.IFillFormatter;
import com.google.android.material.snackbar.Snackbar;
import com.opencsv.CSVWriter;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.crypto.Mac;

import mobi.inthepocket.android.beacons.ibeaconscanner.BluetoothScanBroadcastReceiver;
import mobi.inthepocket.android.beacons.ibeaconscanner.IBeaconScanner;
import mobi.inthepocket.android.beacons.ibeaconscanner.utils.BluetoothUtils;

import static android.content.Context.MODE_APPEND;

public class BeaconDataShow extends AppCompatActivity implements BeaconConsumer {

    Spinner scanTitle;// 타이틀위치에 있는 스피너
    String beaconType="ALL";// 스피너에서 보여지는 비콘 목록

    BluetoothAdapter bluetoothAdapter;

    ArrayList<String> address=new ArrayList<>();
//    DiscoveryResultReceiver discoveryResultReceiver;// 브로드캐스트

    DeviceAdapter deviceAdapter;
    ArrayList<DeviceDataItem> deviceDataItems=new ArrayList<>();// 전체 디바이스 데이터 array

    RecyclerView recyclerView;
    ProgressBar progressBar;

    EditText mac01, mac02, mac03, mac04, mac05, mac06;// 비콘 검색창
    LinearLayout linearLayout;
    Parcelable state=null;// 스크롤 위치 저장
    InputMethodManager imm;

    boolean saveStart=false;// 저장중인지 확인
    boolean scanState=true;// 스캔중인지 확인

    public static Context mContext;// 그래프 화면에서 스캔 제어를 위해 필요

    SimpleDateFormat dateFormat=new SimpleDateFormat("HH:mm:ss");// 비콘 탐지시간 비교를 위해 사용
    SimpleDateFormat dbFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 데이터 저장시에 들어가는 세부 시간
    BluetoothLeScanner scanner;
    ArrayList<String> selectList=new ArrayList<>();// 선택 및 검색된 목록 저장
    ScanSettings settings;//스캔 속도 세팅. 3단계가능

    boolean saveDataToJSON=true;// db데이터 파일로 저장중인지 확인.

    final int START_PROGRESSDIALOG=100;// 프로그래스바 시작
    final int END_PROGRESSDIALOG = 101;// 프로그래스바 종료
    final int OPTION_SET=201;
    ProgressDialog progressDialog=null;
    ProgressDialogHandler progressHandler=null;
    String selectDialogTable="";// 다이얼로그에서 선택한 테이블 이름
    boolean autoScan=true;// 자동스캔 반복 while문 제어
    Toolbar toolbar;// 데이터 저장 및 삭제 메뉴가 있는 툴바
    String saveFileTypeSet;// 파일 저장타입 설정 기본은 .csv로 설정

    final int SCAN_MODE_LOW_POWER=0;
    final int SCAN_MODE_BALANCED=1;
    final int SCAN_MODE_LOW_LATENCY=2;
    final String TYPE_CSV=".csv";
    final String TYPE_JSON=".json";
    int oSpeedVal;// 저장되어있는 스캔속도
    String oSaveTypeVal;// 저장되어있는 파일 타입

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_data_show);
        Log.e("lifeSycle", "onCreate");

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);// 잠금화면 실행 테스트트

       recyclerView=findViewById(R.id.deviceDataList);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle("");
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        mContext=this;



        // onCreate될때 회전값에 맞는 count값 적용
        Resources r=Resources.getSystem();
        Configuration configuration=r.getConfiguration();
        switch (configuration.orientation){
            case Configuration.ORIENTATION_PORTRAIT:recyclerView.setLayoutManager(new GridLayoutManager(this, 2));break;
            case Configuration.ORIENTATION_LANDSCAPE:recyclerView.setLayoutManager(new GridLayoutManager(this, 4));break;
        }

        progressBar=findViewById(R.id.show_progressbar);

        mac01=findViewById(R.id.mac01);
        mac02=findViewById(R.id.mac02);
        mac03=findViewById(R.id.mac03);
        mac04=findViewById(R.id.mac04);
        mac05=findViewById(R.id.mac05);
        mac06=findViewById(R.id.mac06);
        linearLayout=findViewById(R.id.edit_lay);

        progressHandler = new ProgressDialogHandler(BeaconDataShow.this);

        scanTitle=findViewById(R.id.scanTitle);
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.spinner, android.R.layout.simple_spinner_dropdown_item);
        scanTitle.setAdapter(adapter);
        scanTitle.setOnItemSelectedListener(itemSelectedListener);

        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        editSetEvent();
    }

    private void creactSharedPreferences(){// 기본 옵션 세팅.
        SharedPreferences sharedPref=getSharedPreferences(getString(R.string.option_data), MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPref.edit();
        oSpeedVal=sharedPref.getInt(getString(R.string.scan_speed), SCAN_MODE_LOW_LATENCY);
        oSaveTypeVal=sharedPref.getString(getString(R.string.save_type_set), TYPE_CSV);

        editor.putInt(getString(R.string.scan_speed), oSpeedVal);
        editor.putString(getString(R.string.save_type_set), oSaveTypeVal);
        editor.commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.toolbar_save:
                saveDBFile();
                return true;
            case R.id.toolbar_delete:
                deleteData();
                return true;
//            case R.id.testBeaconSelect:// 테스트비콘목록 지정.
//                //신형 + 구형
//                selectList.add("7C:BA:CC:D2:2D:47");
//                selectList.add("7C:BA:CC:C0:02:57");
//                selectList.add("7C:BA:CC:C0:0B:C3");
//                selectList.add("7C:BA:CC:C1:1D:6B");
//                selectList.add("3C:A3:08:D3:0D:34");
//                selectList.add("3C:A3:08:D1:02:86");
//                //나이스 천사
//                selectList.add("F8:5B:9C:6E:C6:76");
//                selectList.add("F8:5B:9C:6E:C6:53");
//                selectList.add("F8:5B:9C:6E:E2:32");
//                selectList.add("F8:5B:9C:6E:D1:C9");
//                selectList.add("F8:5B:9C:6E:D5:3C");
//                selectList.add("F8:5B:9C:6E:D0:A0");
//                selectList.add("F8:5B:9C:6E:D1:E8");
//                selectList.add("F8:5B:9C:6E:D0:F7");
//                selectList.add("F8:5B:9C:6E:D7:24");
//                selectList.add("F8:5B:9C:6E:CC:8F");
//                selectList.add("F8:5B:9C:6E:D6:6F");
//                for (int i = 0; i < deviceDataItems.size(); i++) {
//                    if (selectList.contains(deviceDataItems.get(i).getDeviceAddress())){
//                        deviceDataItems.get(i).setSelect(true);
//                        deviceAdapter.notifyItemChanged(i);
//                        deviceAdapter.setData();
//                    }
//                }
//                beaconType="SELECT";
//                scanTitle.setSelection(1);
//                deviceAdapter.setData();
//                deviceAdapter.notifyDataSetChanged();
//                return true;
            case R.id.option_set:
                Intent optionPage=new Intent(this, OptionList.class);
                startActivityForResult(optionPage, OPTION_SET);
                return true;


//            case R.id.onBtn:
//                Intent intent=new Intent(getApplicationContext(), ScreenService.class);
//                startService(intent);
//                Log.e("startService", "startService");
//                return true;
//            case R.id.offBtn:
//                Intent intent1=new Intent(getApplicationContext(), ScreenService.class);
//                stopService(intent1);
//                Log.e("startService", "stopService");
//                return true;

            default:return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        autoScan=true;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {// 필터 없이 스캔 결과
            new Thread(new Runnable() {
                @Override
                public void run() {
                    processResult(result);
                }
            }).start();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {// 필터 사용 스캔 결과
            for (ScanResult result : results) {
                processResult(result);
            }
            Log.e("endScanResults", "end");
        }

        @Override
        public void onScanFailed(int errorCode) {
//            Log.e("errorCode", "errorCode = "+errorCode);
//            Vibrator vibrator= (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//                vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
//            }else {
//                vibrator.vibrate(100);
//            }

            switch (errorCode) {
                case SCAN_FAILED_ALREADY_STARTED:
//                    scanner.stopScan(this);
                    break;
                case SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                    Log.e("SCANFAILED", "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED (" + errorCode);

                    if (BluetoothAdapter.getDefaultAdapter() == null) {
                        BeaconDataShow.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BeaconDataShow.this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {// 블루투스 꺼져있을때
                            BluetoothAdapter.getDefaultAdapter().enable();// 블루투스 활성화
                            scanner = null;
                            scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
                            scanner.stopScan(this);
                            if (scanState) {
                                scanner.startScan(null, settings, this);
                            }
                        }else {// 블루투스 켜져있을때
                            BluetoothAdapter.getDefaultAdapter().disable();// 블루투스 비활성화
                            BluetoothAdapter.getDefaultAdapter().enable();// 블루투스 활성화
                            scanner = null;
                            scanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
                            scanner.stopScan(this);
                            if (scanState) {
                                scanner.startScan(null, settings, this);
                            }
                        }
                    }
                    break;
                case SCAN_FAILED_INTERNAL_ERROR:
                    Log.e("SCANFAILED", "SCAN_FAILED_INTERNAL_ERROR ("+errorCode);
                    break;
                case SCAN_FAILED_FEATURE_UNSUPPORTED:
                    Log.e("SCANFAILED", "SCAN_FAILED_FEATURE_UNSUPPORTED ("+errorCode);
                    break;
            }
        }

        private void processResult(ScanResult result) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                    try {
//                        Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
//                        ParcelUuid[] uuids= (ParcelUuid[]) getUuidsMethod.invoke(bluetoothAdapter, null);
//
//                        if (uuids != null) {
//                            for (ParcelUuid uuidf : uuids){
//                                Log.e("showUUIDFor", uuidf.getUuid().toString());
//                            }
//                        }
//                    } catch (NoSuchMethodException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    } catch (InvocationTargetException e) {
//                        e.printStackTrace();
//                    }
//                    ByteBuffer bb = ByteBuffer.wrap(record.getBytes());
//                    long firstLong = bb.getLong();
//                    long secondLong = bb.getLong();

                            UUID uuid = UUID.nameUUIDFromBytes(result.getScanRecord().getBytes());// 스캔결과로 uuid값 받아옴.
                            ScanRecord record = result.getScanRecord();
//                    Log.e("showDeviceName", "deviceNM = "+record.getDeviceName()+", MAC = "+result.getDevice().getAddress());
                            String major = String.valueOf((record.getBytes()[25] & 0xff) * 0x100 + (record.getBytes()[26] & 0xff));
                            String minor = String.valueOf((record.getBytes()[27] & 0xff) * 0x100 + (record.getBytes()[28] & 0xff));
//                    Log.e("showDataResult", "magorVal = "+String.valueOf( (record.getBytes()[25] & 0xff) * 0x100 + (record.getBytes()[26] & 0xff)));
//                    Log.e("showDataResult", "minorVal = "+String.valueOf( (record.getBytes()[27] & 0xff) * 0x100 + (record.getBytes()[28] & 0xff)));
//                            Log.e("showDataResult", "uuid = "+uuid.toString()+", major = "+major+", minor = "+minor);
//                    Log.e("showDataResult", "   \n");

//                            Collections.reverse(deviceDataItems);

                            if (!address.contains(result.getDevice().getAddress())) {// 처음 스캔결과 저장.
//                                Log.e("addressAddData", "new = " + result.getDevice().getAddress());
                                address.add(result.getDevice().getAddress());
                                DeviceDataItem data = new DeviceDataItem();
                                data.setDeviceName(record.getDeviceName());
                                data.setScanTime(dateFormat.format(new Date(System.currentTimeMillis())));
                                data.setNoSignal(false);
                                data.setTimeOutDB(dbFormat.format(new Date()));
                                data.setDeviceUUID(uuid.toString());
                                data.setDeviceMajor(major);
                                data.setDeviceMinor(minor);
                                if (record.getDeviceName()!=null){
        //                            if ( data.getDeviceName().contains("pBeacon") || data.getDeviceName().contains("pBeacon_n")  || data.getDeviceName().contains("PB100M") || data.getDeviceName().contains("PB100") ){// data.getDeviceName()==null ||
        //                                data.setDeviceAddress(result.getDevice().getAddress()+"");
        //                                data.setDeviceRssi(result.getRssi()+"");
        //                                data.setRunCount(runCount);
        //                                data.setBeaconType(record.getDeviceName());
        //                                deviceDataItems.add(data);
        //                            }
                                    data.setDeviceAddress(result.getDevice().getAddress() + "");
                                    data.setDeviceRssi(result.getRssi() + "");
                                    data.setBeaconType(record.getDeviceName());
                                    deviceDataItems.add(data);
                                    if (saveStart) {
                                        if (selectList.contains(result.getDevice().getAddress())) {// 선택한 비콘 SQL에 저장.
                                            Log.e("saveBeaconDataFirst", "MAC = "+result.getDevice().getAddress()+", RSSI = "+result.getRssi()+", time = "+dateFormat.format(new Date())+", s2 = "+0+"초");
                                            selectBeaconDB(result.getDevice().getAddress(), result.getRssi() + "", dbFormat.format(new Date()), 0+"");// 선택 데이터 저장.
                                        }
                                    }
                                    beaconTestData_yun02(result.getDevice().getAddress(), result.getRssi() + "", dbFormat.format(new Date()));// 느려짐..
    //                                deviceAdapter.notifyDataSetChanged();// 데이터 변경 알림.
                                    if (!deviceDataItems.contains(result.getDevice().getAddress())){
                                        if (deviceDataItems.size()>0){
                                            deviceAdapter.notifyItemInserted(deviceDataItems.size()-1);
                                        }
                                    }
                                    deviceAdapter.setData();// 새로 데이터 추가될 때만 데이터 분류
                                }
                            } else {// 이후의 스켄 저장.
//                                Log.e("addressAddData", "else = " + result.getDevice().getAddress());
                                for (int j = 0; j < deviceDataItems.size(); j++) {
                                    if (result.getDevice().getAddress().equals(deviceDataItems.get(j).getDeviceAddress())) {
                                        beaconTestData_yun02(result.getDevice().getAddress(), result.getRssi() + "", dbFormat.format(new Date()));// 느려짐..
                                        DeviceDataItem data = new DeviceDataItem();
                                        data.setDeviceUUID(uuid.toString());
                                        if (!major.equals("0") && !minor.equals("0")) {
                                            data.setDeviceMajor(major);
                                            data.setDeviceMinor(minor);
                                        } else {
                                            data.setDeviceMinor(deviceDataItems.get(j).getDeviceMinor());
                                            data.setDeviceMajor(deviceDataItems.get(j).getDeviceMajor());
                                        }
                                        data.setTimeOutDB(dbFormat.format(new Date()));
                                        data.setSelect(deviceDataItems.get(j).isSelect());
                                        data.setDeviceName(deviceDataItems.get(j).getDeviceName());
                                        data.setDeviceRssi(result.getRssi() + "");
                                        data.setDeviceAddress(result.getDevice().getAddress());
                                        data.setScanTime(dateFormat.format(new Date(System.currentTimeMillis())));
                                        data.setNoSignal(false);
                                        data.setBeaconType(record.getDeviceName());

                                        // sqlite에 데이터 저장
                                        long now=System.currentTimeMillis();// 지금 시간 밀리초로 받기
                                        try {
                                            Date dateCreated=dateFormat.parse(deviceDataItems.get(j).getScanTime());// 비콘 마지막 확인 시간
                                            Date dateNow=dateFormat.parse(dateFormat.format(new Date(now)));// 현재 시간
                                            long duration=dateNow.getTime()-dateCreated.getTime();// 현재 시간에서 마지막 스캔시간을 뺀값
                                            long min = duration/60000;// 분으로 변경.
                                            long s = duration/1000;// 초로 변경

                                            if (selectList.contains(deviceDataItems.get(j).getDeviceAddress()) && saveStart) {// 선택한 비콘만 저장
//                                                Log.e("selectListShow", Arrays.toString(selectList.toArray())+", this mac = "+deviceDataItems.get(j).getDeviceAddress());
                                                if (!dateFormat.format(new Date(now)).equals(deviceDataItems.get(j).getScanTime())){// 저장 버튼 클릭 하였고 scanTime이 같지 않을때만 저장.
//                                                    Log.e("dateCheack", "dateCreated = "+dateFormat.format(new Date(now))+", dateNow = "+deviceDataItems.get(j).getScanTime());
                                                    selectBeaconDB(deviceDataItems.get(j).getDeviceAddress(), deviceDataItems.get(j).getDeviceRssi() + "", dbFormat.format(new Date()), s+"");// 선택 데이터 저장. 저장버튼 눌렀을때 저장.
//                                        deviceDataItems.get(j).setScanTime(dateFormat.format(now));
                                                }
                                                if (min>=2){// 2분 이상 초과
                                                    deviceDataItems.get(j).setRunCount(deviceDataItems.get(j).getRunCount()+1);// 비콘 데이터 저장할때만 카운트 값 올라감
                                                    timeOutData(deviceDataItems.get(j).getDeviceAddress(), dbFormat.format(new Date()), min, deviceDataItems.get(j).getRunCount());// 저장 버튼 눌렀을때 시간 초과시 저장.
                                                }
                                            }
                                            if (min >= 2) {
                                                deviceDataItems.get(j).setNoSignal(true);
                                                deviceDataItems.get(j).setTimeOut((int) min);
                                            }else {
                                                deviceDataItems.get(j).setNoSignal(false);
                                                deviceDataItems.get(j).setTimeOut(0);
                                            }
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        deviceDataItems.set(j, data);
                                        deviceAdapter.notifyItemChanged(j);// 추가되지 않았을때는 데이터 변경 알림만
                                        deviceAdapter.setData();// 개별로 setData 해줘서 속도 향상함
                                    }
                                }// fori..
                            }// else..
//                            Collections.reverse(deviceDataItems);
//                            deviceAdapter.setData();// 데이터 변경되지 않아도 뷰 갱신하는 부분 주석 처리
//                            state=recyclerView.getLayoutManager().onSaveInstanceState();
//                            recyclerView.getLayoutManager().onRestoreInstanceState(state);// 스크롤 위치 지정 주석 처리

                        }
                    });
                }
            }).start();

        }
    };



    AdapterView.OnItemSelectedListener itemSelectedListener=new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//            Log.e("selectItemaDataShow", "view = "+view+", parent = "+parent+", position = "+position+", id = "+id);
            switch (position){
                case 0:beaconType="ALL"; break;
                case 1:beaconType="SELECT"; break;
                case 2:beaconType="PB100"; break;
                case 3:beaconType="PB100M"; break;
                case 4:beaconType="pBeacon"; break;
                case 5:beaconType="pBeacon_n"; break;
            }
//            Log.e("selectItemaDataShow", "BeaconType = "+beaconType);
            if (deviceDataItems!=null && deviceDataItems.size()>0){
                deviceAdapter.setData();
                deviceAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    void editSetEvent(){

        mac02.setOnEditorActionListener(actionListener);
        mac03.setOnEditorActionListener(actionListener);
        mac04.setOnEditorActionListener(actionListener);
        mac05.setOnEditorActionListener(actionListener);
        mac06.setOnEditorActionListener(actionListener);
        mac01.setOnEditorActionListener(actionListener);

        mac01.addTextChangedListener(textWatcher);
        mac02.addTextChangedListener(textWatcher);
        mac03.addTextChangedListener(textWatcher);
        mac04.addTextChangedListener(textWatcher);
        mac05.addTextChangedListener(textWatcher);
        mac06.addTextChangedListener(textWatcher);

        mac02.setOnKeyListener(keyListener);
        mac03.setOnKeyListener(keyListener);
        mac04.setOnKeyListener(keyListener);
        mac05.setOnKeyListener(keyListener);
        mac06.setOnKeyListener(keyListener);

    }

    TextWatcher textWatcher=new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            TextView text= (TextView) getCurrentFocus();
            if (text != null && text.length() == 2) {
                View next=text.focusSearch(View.FOCUS_RIGHT);
                if (next != null) {
                    next.requestFocus();
                }
            }
        }
        @Override
        public void afterTextChanged(Editable s) { }
    };
    EditText.OnEditorActionListener actionListener=new EditText.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            switch (v.getId()){
                case R.id.mac01: if (v.getText().length() == 2) { mac02.requestFocus(); }break;
                case R.id.mac02: if (v.getText().length() == 2) { mac03.requestFocus(); }break;
                case R.id.mac03: if (v.getText().length() == 2) { mac04.requestFocus(); }break;
                case R.id.mac04: if (v.getText().length() == 2) { mac05.requestFocus(); }break;
                case R.id.mac05: if (v.getText().length() == 2) { mac06.requestFocus(); }break;
            }
            switch (actionId){
                case EditorInfo.IME_ACTION_NEXT:
                    if (v.getText().length() < 2){
                        Toast.makeText(BeaconDataShow.this, "2자리를 모두 입력해 주세요.", Toast.LENGTH_SHORT).show();
                        return true;
                    }else {
                        return false;
                    }
                case EditorInfo.IME_ACTION_SEARCH:
                    searchData();
                    break;
            }

            return false;
        }
    };

    View.OnKeyListener keyListener=new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                TextView textView=findViewById(v.getId());
                switch (textView.getId()){
                    case R.id.mac02: if (textView.getText().length() == 0) { mac01.requestFocus(); }break;
                    case R.id.mac03: if (textView.getText().length() == 0) { mac02.requestFocus(); }break;
                    case R.id.mac04: if (textView.getText().length() == 0) { mac03.requestFocus(); }break;
                    case R.id.mac05: if (textView.getText().length() == 0) { mac04.requestFocus(); }break;
                    case R.id.mac06: if (textView.getText().length() == 0) { mac05.requestFocus(); }break;
                }
            }
            return false;
        }
    };


    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.e("showConfigOrientation", "orientation = "+newConfig.orientation);
        switch (newConfig.orientation){
            case Configuration.ORIENTATION_PORTRAIT:recyclerView.setLayoutManager(new GridLayoutManager(this, 2));break;
            case Configuration.ORIENTATION_LANDSCAPE:recyclerView.setLayoutManager(new GridLayoutManager(this, 4));break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter.getDefaultAdapter().enable();
        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

        creactSharedPreferences();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings=new ScanSettings.Builder().setScanMode(oSpeedVal).build();// 저장되어있는 속도록 스캔
        }
        startScan();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(300000);// 5분마다 스캔 재시작.300000
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("AutoScan", "thread");
                                stopScan();
                                startScan();
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    state=recyclerView.getLayoutManager().onSaveInstanceState();
                    recyclerView.getLayoutManager().onRestoreInstanceState(state);// 뷰 갱신 및 스크롤 위치 설정.
                }
            });
        }
        Log.e("lifeSycle", "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
//        BluetoothAdapter.getDefaultAdapter().disable();
    }

    public void scanBeacon_BTN(View view) {// 중지버튼
        stopScan();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (scanner==null){
                scanner=bluetoothAdapter.getBluetoothLeScanner();
                scanner.stopScan(mScanCallback);
            }else {
                scanner.stopScan(mScanCallback);
            }
        }
    }

    public void RestartScan(View view) {
        scanState=true;
        startScan();
    }

    public void startScan(){
//        IntentFilter filter=new IntentFilter();
//        filter.addAction(BluetoothDevice.ACTION_FOUND);
//        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        filter.addAction(BluetoothDevice.ACTION_UUID);
//        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        registerReceiver(discoveryResultReceiver, filter);//Manifest.xml말고 JAVA에서 리시버 등록
//        Intent intent=new Intent(this, DiscoveryResultReceiver.class);
//        sendBroadcast(intent);
        //탐색 시작!!
        deviceAdapter=new DeviceAdapter();
        recyclerView.setAdapter(deviceAdapter);
//        bluetoothAdapter.startDiscovery();
        scanner=null;

        progressBar.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner=bluetoothAdapter.getBluetoothLeScanner();
            scanner.startScan(null, settings, mScanCallback);
        }
        scanState=true;
        if (saveStart){
            Toast.makeText(this, "비콘 탐색을 시작합니다.\n데이터 저장을 시작합니다.", Toast.LENGTH_SHORT).show();
            Button button=findViewById(R.id.select_data_save_button);
            button.setText("데이터 저장중");
            button.setTextColor(getResources().getColor(R.color.red));
        }else {
            Toast.makeText(this, "비콘 탐색을 시작합니다.", Toast.LENGTH_SHORT).show();
        }
//        deviceAdapter.notifyDataSetChanged();
    }

    public void stopScan(){
        scanner=null;
        progressBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanner=bluetoothAdapter.getBluetoothLeScanner();
            scanner.stopScan(mScanCallback);
            scanState=false;
        }
        if (saveStart){
            Toast.makeText(this, "비콘 탐색을 중지했습니다. 데이터 저장이 중지됩니다.\n탐색 시작시에 이어서 저장 됩니다.", Toast.LENGTH_SHORT).show();
            Button button=findViewById(R.id.select_data_save_button);
            button.setText("저장 중지");
        }else {
            Toast.makeText(this, "비콘 탐색을 중지했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void searchBtn(View view) {
        searchData();
    }

    private void searchData(){// 검색기능

        if (linearLayout.getVisibility() == View.VISIBLE){// 검색 시작
            ArrayList<String> scanStrArr=new ArrayList<>();// 검색할 데이터
            scanStrArr.add(mac01.getText().toString());
            scanStrArr.add(mac02.getText().toString());
            scanStrArr.add(mac03.getText().toString());
            scanStrArr.add(mac04.getText().toString());
            scanStrArr.add(mac05.getText().toString());
            scanStrArr.add(mac06.getText().toString());

            scanTitle.setVisibility(View.VISIBLE);
            String searchStr="";
            if (!scanStrArr.contains("")){// 전부 사용하여서 검색 했을때..
                searchStr+=mac01.getText()+":"+mac02.getText()+":"+mac03.getText()+":"+mac04.getText()+":"+mac05.getText()+":"+mac06.getText();
                for (int i = 0; i < deviceDataItems.size(); i++) {
                    if (searchStr.equals(deviceDataItems.get(i).getDeviceAddress())){
                        DeviceDataItem data=new DeviceDataItem();
                        data.setSelect(true);
                        data.setDeviceName(deviceDataItems.get(i).getDeviceName());
                        data.setDeviceRssi(deviceDataItems.get(i).getDeviceRssi());
                        data.setDeviceAddress(deviceDataItems.get(i).getDeviceAddress());
                        data.setScanTime(dateFormat.format(new Date()));
                        data.setNoSignal(deviceDataItems.get(i).isNoSignal());
                        deviceDataItems.remove(i);
                        Collections.reverse(deviceDataItems);
                        deviceDataItems.add(data);
                        Collections.reverse(deviceDataItems);
                        break;
                    }
//                    else {
//                        DeviceDataItem data=new DeviceDataItem();
//                        data.setSelect(false);
//                        data.setDeviceName(deviceDataItems.get(i).getDeviceName());
//                        data.setDeviceRssi(deviceDataItems.get(i).getDeviceRssi());
//                        data.setDeviceAddress(deviceDataItems.get(i).getDeviceAddress());
//                        data.setScanTime(dateFormat.format(new Date()));
//                        data.setNoSignal(deviceDataItems.get(i).isNoSignal());
//                        deviceDataItems.remove(i);
//                        Collections.reverse(deviceDataItems);
//                        deviceDataItems.add(data);
//                        Collections.reverse(deviceDataItems);
//                    }
                }
            }else{// 포함된 숫자 모두 검색
                for (int j = 0; j < deviceDataItems.size(); j++) {
                    String[] split=deviceDataItems.get(j).getDeviceAddress().split(":");//2개씩 나눈 데이터
                    for (int i = 0; i < scanStrArr.size(); i++) {
                        if (scanStrArr.get(i).equals(split[i]) && deviceDataItems.get(j).isSearchData()){
                            deviceDataItems.get(j).setSelect(true);// 검색한 데이터가 맞을 경우 선택으로 변경
                        }else if (!scanStrArr.get(i).equals("") && !scanStrArr.get(i).equals(split[i])){
                            deviceDataItems.get(j).setSelect(false);
                            deviceDataItems.get(j).setSearchData(false);
                            if (selectList!=null && selectList.size()>0 && selectList.contains(deviceDataItems.get(j).getDeviceAddress())){
                                deviceDataItems.get(j).setSelect(true);
                                deviceDataItems.get(j).setSearchData(true);
                            }
                        }
                    }
                    deviceAdapter.notifyItemChanged(j);

                }
                if (selectList!=null){
                    selectList.clear();
                    for (int i = 0; i < deviceDataItems.size(); i++) {
                        if (deviceDataItems.get(i).isSelect()){
                            selectList.add(deviceDataItems.get(i).getDeviceAddress());
                        }
                    }
                }// 선택목록에 추가.


            }
            Log.e("showStrArr", Arrays.toString(scanStrArr.toArray()));
            if (!Arrays.toString(scanStrArr.toArray()).equals("[, , , , , ]")){
                beaconType="SELECT";
                scanTitle.setSelection(1);
            }
            deviceAdapter.setData();// 데이터 재배치
            scanStrArr.clear();
            scanEditorClear();
//            deviceAdapter.notifyDataSetChanged();

        }else {// 검색창 열기
            scanTitle.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            mac01.requestFocus();
            imm.showSoftInput(mac01, 0);
//            for (int i = 0; i < deviceDataItems.size(); i++) {
//                deviceDataItems.get(i).setSearchData(true);
//            }// 검색 후 선택 내용 초기화 코드
        }
    }

    private void scanEditorClear(){
        mac01.setText("");
        mac02.setText("");
        mac03.setText("");
        mac04.setText("");
        mac05.setText("");
        mac06.setText("");
        linearLayout.setVisibility(View.GONE);
        deviceAdapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(0);

        imm.hideSoftInputFromWindow(mac06.getWindowToken(), 0);
    }

    @Override
    public void onBeaconServiceConnect() {

    }

    public void openBeaconChart(View view) {
        ArrayList<String> selectBeaconArr=new ArrayList<>();// 선택한 비콘목록 그레프

        for (int i = 0; i < deviceDataItems.size(); i++) {
            if (deviceDataItems.get(i).isSelect()){
                selectBeaconArr.add(deviceDataItems.get(i).getDeviceAddress());
            }
        }

        Intent intent=new Intent(BeaconDataShow.this, DataListShow.class);
        intent.putStringArrayListExtra("MAC", selectBeaconArr);
        intent.putExtra("scanState", scanState);
        startActivity(intent);

    }

    // db저장 시작
    public void startSave(View view) {
        selectSaveData();
    }

    public void selectSaveData(){

        if (saveStart==false){
            if (deviceDataItems.size()>0){
                saveStart=true;
                selectList.clear();
                for (int i = 0; i < deviceDataItems.size(); i++) {
                    if (deviceDataItems.get(i).isSelect()){
                        selectList.add(deviceDataItems.get(i).getDeviceAddress());
                    }
                }
            }

            if (scanState){
                if (selectList.size()>0){
                    Button button=findViewById(R.id.select_data_save_button);
                    button.setText("데이터 저장중");
                    Toast.makeText(this, "데이터 저장을 시작합니다.", Toast.LENGTH_SHORT).show();
                    button.setTextColor(getResources().getColor(R.color.red));
                }else {
                    Toast.makeText(this, "선택한 비콘이 없습니다. 비콘을 선택하고 실행해 주세요.", Toast.LENGTH_SHORT).show();
                    saveStart=false;
                }
            }else {
                saveStart=false;
                Toast.makeText(this, "스캔 중지 상태입니다. 스캔을 재시작 하셔야 저장 가능합니다.", Toast.LENGTH_SHORT).show();
            }
        }else {
            saveStart=false;
            Button button=findViewById(R.id.select_data_save_button);
            button.setText("데이터 저장");
            button.setTextColor(getResources().getColor(R.color.white));
            Toast.makeText(this, "데이터 저장을 중지합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 65498:
                if (grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "파일을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }else {
                    saveDBFile();
                }
                break;
        }

    }

    public void saveTextFile(View view) {// 데이터 textFile로 저장.
        saveDBFile();
    }

    public void saveDBFile(){// 프로그래스바 추가하기.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){// 권한 확인
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 65498);
            }else {// 저장할 파일 선택하는 코드 생성하기.
                if (saveDataToJSON){
                    saveDialogShow();
                }else {
                    Toast.makeText(BeaconDataShow.this, "데이터 저장 중입니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveDialogShow(){
        Dialog dialog=new Dialog(BeaconDataShow.this);
        View view=View.inflate(this, R.layout.table_select_dialog, null);
        RadioGroup radioGroup=view.findViewById(R.id.dialogRadioGroup);
        TextView title=view.findViewById(R.id.dialogTitle);
        TextView cancel=view.findViewById(R.id.cancelDialog);
        TextView select=view.findViewById(R.id.selectDialog);
        Switch fileType=view.findViewById(R.id.switch_file_type);
        fileType.setText(oSaveTypeVal);
        fileType.setVisibility(View.VISIBLE);
        saveFileTypeSet=oSaveTypeVal;
        fileType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (fileType.getText().toString().equals(".csv")){
                    fileType.setText(".json");
                    saveFileTypeSet=".json";
                }else {
                    fileType.setText(".csv");
                    saveFileTypeSet=".csv";
                }
            }
        });


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.allBeaconData:
                        selectDialogTable="beaconTestData02";
                        break;
                    case R.id.selectBeaconData:
                        selectDialogTable="selectBeaconDB";
                        break;
                    case R.id.timeOutBeaconData:
                        selectDialogTable="timeOutData";
                        break;
                }
            }
        });
        dialog.setContentView(view);
        dialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectDialogTable.equals("")){
                    Toast.makeText(BeaconDataShow.this, "저장할 데이터를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }else {
                    saveDataToJSON=false;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            SQLiteDatabase readDB=null;
                            readDB=openOrCreateDatabase(selectDialogTable, MODE_PRIVATE, null);

                            Cursor cursor=readDB.rawQuery("select tbl_name from sqlite_master where tbl_name = '"+selectDialogTable+"'", null);
                            if (cursor==null){
                                BeaconDataShow.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(BeaconDataShow.this, "저장할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                saveDataToJSON=true;
                            }else {
                                Cursor c=readDB.rawQuery("SELECT * FROM "+selectDialogTable.toString(), null);
                                if (c.moveToNext()){
                                    progressHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressHandler.sendEmptyMessage(START_PROGRESSDIALOG);
                                        }
                                    });
                                    saveFileToJSON(selectDialogTable);// 선택파일, 전체파일 저장
                                    saveFinishedShow();
                                }else {
                                    saveDataToJSON=true;
                                    BeaconDataShow.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(BeaconDataShow.this, "저장할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                                c.close();
                            }
                            cursor.close();
                        }
                    }).start();
                }
            }
        });
        selectDialogTable="";
    }

    public void sqlSaveDataDeleteAll(View view) {// 저장 데이터 지우기버튼
        deleteData();
    }

    private void deleteData(){

        Dialog dialog=new Dialog(BeaconDataShow.this);
        View lay=View.inflate(this, R.layout.table_select_dialog, null);
        RadioGroup radioGroup=lay.findViewById(R.id.dialogRadioGroup);
        TextView title=lay.findViewById(R.id.dialogTitle);
        TextView cancel=lay.findViewById(R.id.cancelDialog);
        TextView select=lay.findViewById(R.id.selectDialog);
        title.setText("삭제할 데이터를 선택해 주세요.");
        Switch s=lay.findViewById(R.id.switch_file_type);
        s.setVisibility(View.GONE);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.allBeaconData:
                        selectDialogTable="beaconTestData02";
                        break;
                    case R.id.selectBeaconData:
                        selectDialogTable="selectBeaconDB";
                        break;
                    case R.id.timeOutBeaconData:
                        selectDialogTable="timeOutData";
                        break;
                }
            }
        });
        dialog.setContentView(lay);
        dialog.show();
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectDialogTable.equals("")){
                    Toast.makeText(BeaconDataShow.this, "삭제할 데이터를 선택해 주세요.", Toast.LENGTH_SHORT).show();
                }else {

                }

                SQLiteDatabase readDB=null;
                readDB=openOrCreateDatabase(selectDialogTable, MODE_PRIVATE, null);
                Cursor cursor=readDB.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+selectDialogTable+"'", null);
                if (cursor==null) {
                    Toast.makeText(BeaconDataShow.this, "삭제할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }else {
                    Cursor c=readDB.rawQuery("SELECT * FROM "+selectDialogTable.toString(), null);
                    if (c.moveToNext()){
                        beaconDataDelete(selectDialogTable);// 삭제 코드
                        Toast.makeText(BeaconDataShow.this, "저장된 비콘 데이터가 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                        stopScan();
                    }else {
                        Toast.makeText(BeaconDataShow.this, "삭제할 데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                    c.close();
                    dialog.dismiss();
                }
                cursor.close();

            }
        });
    }

    public void saveFileToJSON(String tableName){// text파일로 저장하는 코드
        SQLiteDatabase readDB=null;
        DeviceDataItem deviceDataItem=new DeviceDataItem();
        ArrayList<DeviceDataItem> sendList=new ArrayList<>();
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        SimpleDateFormat dateFormat=new SimpleDateFormat("yy.MM.dd");
        String date=dateFormat.format(new Date());
        int count=0;
        File file = new File(path, tableName+"_"+date+"("+count+")"+saveFileTypeSet);
        while (file.exists()){
            count++;
            file = new File(path, tableName+"_"+date+"("+count+")"+saveFileTypeSet);
        }
        FileWriter fw=null;
        try {
            fw=new FileWriter(file);
            CSVWriter csvWriter=new CSVWriter(fw);
            readDB=openOrCreateDatabase(tableName, MODE_PRIVATE, null);
            Cursor c=readDB.rawQuery("SELECT * FROM "+tableName, null);
            if (saveFileTypeSet.equals(".json")){
                fw.write("[");
            }

            while (c.moveToNext()) {
                Log.e("saveTest", String.valueOf(c.getInt(c.getColumnIndex("index_num"))));
                Log.e("CursorGetCount", "count = "+c.getCount()+", position = "+c.getPosition());
                if (tableName.equals("timeOutData")){// 시간 초과 데이터 저장.
                    //beaconMac, outTime, min, count
                    int index=c.getInt(c.getColumnIndex("index_num"));
                    Log.e("totalDataStr", index+"");
                    String mac=c.getString(c.getColumnIndex("beaconMac"));
                    String time=c.getString(c.getColumnIndex("outTime"));
                    int countNum=c.getInt(c.getColumnIndex("count"));
                    int min=c.getInt(c.getColumnIndex("min"));
                    if (saveFileTypeSet.equals(".csv")){// .csv 저장
                        String arrStr[] ={index+"",mac, time, countNum+"",min+""};
                        csvWriter.writeNext(arrStr);
                    }else {// .json 저장
                        if (c.getCount()-1==c.getPosition()){
                            String saveData="{\"index_num\" : \""+index+"\", \"beaconMac\" : \""+mac+"\", \"outTime\" : \""+time+"\", \"min\" : \""+min+"\", \"count\" : \""+countNum+"\"}";
                            fw.write(saveData);
                        }else {
                            String saveData="{\"index_num\" : \""+index+"\", \"beaconMac\" : \""+mac+"\", \"outTime\" : \""+time+"\", \"min\" : \""+min+"\", \"count\" : \""+countNum+"\"},";
                            fw.write(saveData);
                        }
                    }
                }else {// 선택 비콘 및 전체 비콘 데이터 저장
                    int index=c.getInt(c.getColumnIndex("index_num"));
                    deviceDataItem.setDeviceAddress(c.getString(c.getColumnIndex("beaconMac")));
                    deviceDataItem.setDeviceRssi(c.getString(c.getColumnIndex("beaconRssi")));
                    deviceDataItem.setScanTime(c.getString(c.getColumnIndex("beaconTime")));
                    sendList.add(deviceDataItem);
                    Log.e("saveDataStr", index+"");
//                String saveData="MAC : "+deviceDataItem.getDeviceAddress()+", RSSI : "+deviceDataItem.getDeviceRssi()+", TIME : "+deviceDataItem.getScanTime()+"\n";
                    if (saveFileTypeSet.equals(".csv")){
                        String arrStr[]={index+"", deviceDataItem.getDeviceAddress(), deviceDataItem.getDeviceRssi(), deviceDataItem.getScanTime()};
                        csvWriter.writeNext(arrStr);
                    }else {
                        if (c.getCount()-1==c.getPosition()){
                            Log.e("lostCursor", "not next");
                            String saveData="{\"index_num\" : \""+index+"\", \"beaconMac\" : \""+deviceDataItem.getDeviceAddress()+"\", \"beaconRssi\" : \""+deviceDataItem.getDeviceRssi()+"\", \"beaconTime\" : \""+deviceDataItem.getScanTime()+"\"}";
                            fw.write(saveData);
                        }else {
                            Log.e("haveCursor", "have next");
                            String saveData="{\"index_num\" : \""+index+"\", \"beaconMac\" : \""+deviceDataItem.getDeviceAddress()+"\", \"beaconRssi\" : \""+deviceDataItem.getDeviceRssi()+"\", \"beaconTime\" : \""+deviceDataItem.getScanTime()+"\"},";
                            fw.write(saveData);
                        }
                    }
                }
            }
            if (saveFileTypeSet.equals(".json")){
                fw.write("]");
            }
            csvWriter.close();
            if (fw != null) {
                fw.close();
                Log.e("fileWriterF", "finish");
            }
            saveDataToJSON=true;
            progressHandler.sendEmptyMessage(END_PROGRESSDIALOG);

        }catch (Exception e){
            Log.e("fileWriterError", "error = "+e.getMessage());
            saveDataToJSON=true;
        }
    }

    public void selectCancelAll(View view) {// 비콘 선택 취소 버튼
        if (deviceDataItems!=null && deviceDataItems.size()>0){
            for (int i = 0; i < deviceDataItems.size(); i++) {
                if (deviceDataItems.get(i).isSelect()){
                    deviceDataItems.get(i).setSelect(false);
                }
            }
            deviceAdapter.notifyDataSetChanged();
            deviceAdapter.setData();
        }
    }

    public class ProgressDialogHandler extends Handler{
        Context context=null;

        public ProgressDialogHandler(Context context) {
            this.context = context;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_PROGRESSDIALOG:
                    if (progressDialog == null) {
                        progressDialog=new ProgressDialog(context);
                        progressDialog.setMessage("데이터 저장중입니다.");
                        progressDialog.setCanceledOnTouchOutside(false);
                    }else {
                        progressDialog=null;
                        progressDialog=new ProgressDialog(context);
                        progressDialog.setMessage("데이터 저장중입니다.");
                        progressDialog.setCanceledOnTouchOutside(false);
                    }
                    progressDialog.show();
                    break;
                case END_PROGRESSDIALOG:
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }
                    break;
            }
        }
    }


//    class DiscoveryResultReceiver extends BroadcastReceiver {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action=intent.getAction();
//
//            if (action.equals(BluetoothDevice.ACTION_FOUND)){
//                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                Log.e("receivedData", device.getAddress());
//
//                deviceAdapter=new DeviceAdapter();
//                deviceAdapter.notifyDataSetChanged();
//                recyclerView.setAdapter(deviceAdapter);
//                recyclerView.getLayoutManager().onRestoreInstanceState(state);
//            }else if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){// 수신 완료 부분
////                if (runCount <= 3){// 찾기 완료하면 runCount값 올리고 다시 수신 시작
////                    runCount++;
////                }else {
////                    for (int i = 0; i < deviceDataItems.size(); i++) {
////                        if (deviceDataItems.get(i).getRunCount() < runCount){
////                            deviceDataItems.get(i).setNoSignal(true);// 신호끊기면 true로 변경하여 끊김 이미지 보여줌.
////                            // 신호 끊겼을때 데이터 지우는 코드
//////                            for (int j = 0; j < address.size(); j++) {
//////                                if (address.get(j).equals(deviceDataItems.get(i).getDeviceAddress())){
//////                                    Log.e("removeAddress", "address = "+address.get(j));
//////                                    address.remove(j);
//////                                    break;
//////                                }
//////                            }
//////                            Log.e("removeAddress", "deviceItemas = "+deviceDataItems.get(i).getDeviceAddress());
//////                            deviceDataItems.remove(i);
////                        }
////                    }
////                    runCount=0;
////                }
////                if (scanState){
////
////                    if (scanner==null){
////                        scanner=bluetoothAdapter.getBluetoothLeScanner();
////                        scanner.startScan(null, settings,mScanCallback);
////                    }else {
////                        scanner.startScan(null, settings, mScanCallback);
////                    }
////                }
////                if (scanState){
////                    Log.e("restart", "restart");
////                    bluetoothAdapter.startDiscovery();
////                }
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void beaconDataDelete(String tableName){
        SQLiteDatabase sampleDB=null;
        try {
            sampleDB=openOrCreateDatabase(tableName, MODE_PRIVATE, null);// 선택한 비콘데이터만 삭제하기
            sampleDB.execSQL("DELETE FROM "+tableName+";");
            sampleDB.execSQL("DROP TABLE "+tableName+";");

            switch (tableName){
                case "timeOutData":sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"timeOutData"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), outTime VARCHAR(200), min VARCHAR(200), count VARCHAR(200));");break;
                case "selectBeaconDB": sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"selectBeaconDB"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), beaconRssi VARCHAR(200), beaconTime VARCHAR(200), second VARCHAR(200));");break;
                case "beaconTestData02" : sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"beaconTestData02"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), beaconRssi VARCHAR(200), beaconTime VARCHAR(200));");break;
            }

            sampleDB.close();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.w("sqliteException", e.getMessage());
        }
    }
    // mac, time 2개 , count, min
    public void timeOutData(String beaconMac, String outTime, long min, int count){// 비콘데이터 저장.
        SQLiteDatabase sampleDB=null;
        try {
            sampleDB=openOrCreateDatabase("timeOutData", MODE_PRIVATE, null);
            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"timeOutData"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), outTime VARCHAR(200), min VARCHAR(200), count VARCHAR(200));");
            sampleDB.execSQL("INSERT INTO "+"timeOutData"+" (beaconMac, outTime, min, count) Values('" +beaconMac+"', '"+outTime+"', '"+min+"', '"+count+"');");
            Log.e("timeOutDataInput", "saveData : mac = "+beaconMac+", outTime = "+outTime+", min = "+min+", count = "+count);
            sampleDB.close();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.w("sqliteException", e.getMessage());
        }
    }

    public void beaconTestData_yun02(String beaconMac, String beaconRssi, String beaconTime){// 비콘데이터 저장.
        SQLiteDatabase sampleDB=null;
        try {
            sampleDB=openOrCreateDatabase("beaconTestData02", MODE_PRIVATE, null);
            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"beaconTestData02"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), beaconRssi VARCHAR(200), beaconTime VARCHAR(200));");
            sampleDB.execSQL("INSERT INTO "+"beaconTestData02"+" (beaconMac, beaconRssi, beaconTime) Values('" +beaconMac+"', '"+beaconRssi+"', '"+beaconTime+"');");
            sampleDB.close();
        }catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.w("sqliteException", e.getMessage());
        }
    }

    public void selectBeaconDB(String beaconMac, String beaconRssi, String beaconTime, String second){// 선택한 비콘 데이터 저장.. second추가
        SQLiteDatabase sampleDB=null;
        try {
            sampleDB=openOrCreateDatabase("selectBeaconDB", MODE_PRIVATE, null);
            sampleDB.execSQL("CREATE TABLE IF NOT EXISTS "+"selectBeaconDB"+" ( index_num INTEGER PRIMARY KEY AUTOINCREMENT, beaconMac VARCHAR(200), beaconRssi VARCHAR(200), beaconTime VARCHAR(200), second VARCHAR(200));");
            sampleDB.execSQL("INSERT INTO "+"selectBeaconDB"+" (beaconMac, beaconRssi, beaconTime, second) Values('" +beaconMac+"', '"+beaconRssi+"', '"+beaconTime+"', '"+second+"');");
            sampleDB.close();
            Log.e("saveBeaconDataElseData", "mac = "+beaconMac+", rssi = "+beaconRssi+", time = "+beaconTime+", second = "+second);
        }catch (Exception e){
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.w("sqliteException", e.getMessage());
        }
    }

    class DeviceAdapter extends RecyclerView.Adapter {
        ArrayList<DeviceDataItem> aDeviceDataItems=new ArrayList<>();
        public DeviceAdapter() {
            setData();
        }
        public void setData(){
            aDeviceDataItems.clear();
            if (!beaconType.equals("ALL") && !beaconType.equals("SELECT")){
                for (int i = 0; i < deviceDataItems.size(); i++) {
                    if (beaconType.equals("pBeacon")){
                        if (deviceDataItems.get(i).getDeviceName()!=null && deviceDataItems.get(i).getDeviceName().contains(beaconType)){
                            aDeviceDataItems.add(deviceDataItems.get(i));
                        }
                    }else {
                        if (deviceDataItems.get(i).getDeviceName()!=null && deviceDataItems.get(i).getDeviceName().equals(beaconType)){
                            aDeviceDataItems.add(deviceDataItems.get(i));
                        }
                    }
                }
            }else if (beaconType.equals("SELECT")){
                for (int i = 0; i < deviceDataItems.size(); i++) {
                    if (deviceDataItems.get(i).isSelect()){
                        aDeviceDataItems.add(deviceDataItems.get(i));
                    }
                }
            }else if (beaconType.equals("ALL")){
                aDeviceDataItems.addAll(deviceDataItems);
//                updateDeviceListItems(deviceDataItems);
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(BeaconDataShow.this).inflate(R.layout.device_data_item, parent, false);
            VH holder=new VH(itemView);
            return holder;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("ResourceAsColor")
        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            VH vh= (VH) holder;
            if (aDeviceDataItems != null && aDeviceDataItems.size() > 0){
//                ColorStateList clickY=ColorStateList.valueOf(getResources().getColor(R.color.cardColorN));
//                ColorStateList clickN=ColorStateList.valueOf(getResources().getColor(R.color.cardColorY));

                if (aDeviceDataItems.get(position).isSelect()){
                    vh.selecte.setBackground(getDrawable(R.color.cardColorN));
                }else {
                    vh.selecte.setBackground(getDrawable(R.color.cardColorY));
                }
                vh.name.setText(aDeviceDataItems.get(position).getDeviceName());
                vh.address.setText(aDeviceDataItems.get(position).getDeviceAddress());

                vh.rssi.setText(aDeviceDataItems.get(position).getDeviceRssi());
                if (aDeviceDataItems.get(position).isNoSignal()) {// 신호 있을때...이미지 보여줌
                    vh.signal.setVisibility(View.VISIBLE);
                }else {// 신호 없을때... 이미지 보여주지 않음
                    vh.signal.setVisibility(View.GONE);
                }
                vh.scanTime.setText(aDeviceDataItems.get(position).getScanTime());

                vh.selecte.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (aDeviceDataItems.get(position).isSelect()){
                            vh.selecte.setBackground(getDrawable(R.color.cardColorY));
                            for (int i = 0; i < deviceDataItems.size(); i++) {
                                if (deviceDataItems.get(i).getDeviceAddress().equals(aDeviceDataItems.get(position).getDeviceAddress())){
                                    deviceDataItems.get(i).setSelect(false);
                                }
                            }
                            aDeviceDataItems.get(position).setSelect(false);// 선택 취소
                        }else {
                            vh.selecte.setBackground(getDrawable(R.color.cardColorN));
                            for (int i = 0; i < deviceDataItems.size(); i++) {
                                if (deviceDataItems.get(i).getDeviceAddress().equals(aDeviceDataItems.get(position).getDeviceAddress())){
                                    deviceDataItems.get(i).setSelect(true);
                                }
                            }
                            aDeviceDataItems.get(position).setSelect(true);// 선택
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return aDeviceDataItems.size();
        }

        class VH extends RecyclerView.ViewHolder{

            TextView name, address, rssi, scanTime, timeOut;
            ConstraintLayout selecte;
            ImageView signal;

            public VH(@NonNull View itemView) {
                super(itemView);
                name=itemView.findViewById(R.id.device_name);
                address=itemView.findViewById(R.id.device_address);
                rssi=itemView.findViewById(R.id.device_rssi);
                selecte=itemView.findViewById(R.id.select_beacon);
                scanTime=itemView.findViewById(R.id.scanTime);
                signal=itemView.findViewById(R.id.signal);
                timeOut=itemView.findViewById(R.id.timeOut_tv);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void saveFinishedShow(){
        Snackbar finishSnackbar=Snackbar.make(findViewById(R.id.deviceDataList), "저장이 완료되었습니다.\n파일을 확인 하시겠습니까?", Snackbar.LENGTH_SHORT);
        finishSnackbar.setAction("이동", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/*");
                startActivity(intent);
            }
        });
        finishSnackbar.show();
    }
}
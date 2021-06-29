package com.example.beacontest03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class DataListShow extends AppCompatActivity {

    public LineChart chart;
    ArrayList<String> macAddress=new ArrayList<>();
//    List<Entry> entries=new ArrayList<>();
    Random random=new Random();
    boolean endVal=true;
    ArrayList<Integer> colorArr=new ArrayList<>();

    Thread thread;

    LineData lineData=new LineData();
    ArrayList<LegendEntry> legs=new ArrayList<>();

    boolean scanState;
    ImageView stopIcon;
    TextView macTitle;
    View lineColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_list_show);
        Log.e("ShowLifeCycle", "OnCreate");
        stopIcon=findViewById(R.id.renewBtn);
        macTitle=findViewById(R.id.mac_title);
        lineColor=findViewById(R.id.line_color);

        macTitle.setText("전체");
        macAddress=getIntent().getStringArrayListExtra("MAC");
        scanState=getIntent().getBooleanExtra("scanState",false);

        if (scanState){
            Glide.with(this).load(R.drawable.ic_baseline_motion_photos_paused_24).into(stopIcon);
        }else {
            Glide.with(this).load(R.drawable.ic_baseline_not_started_24).into(stopIcon);
        }

        chartOptionSetting();
        for (int i = 0; i < macAddress.size(); i++) {// 그래프 색상 지정
            int color=Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
            if (colorArr.contains(color)){
                Log.e("setColorReturn", "return = "+color);
                colorArr.add(color);
//                return;
            }else {
                colorArr.add(color);
            }
        }
        Log.e("setColorReturn", "return = "+ Arrays.toString(colorArr.toArray()));
        entriesDataSet();
        thread=new Thread(new Runnable() {// 자동갱신코드
            @Override
            public void run() {
                while (true){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            autoSetData();
                        }
                    });
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    public void setLineData(){// 그래프 선 데이터 저장.
        lineData.clearValues();
        legs.clear();

        if (macTitle.getText().toString().equals("전체")){
            for (int i = 0; i < macAddress.size(); i++) {
                lineData.addDataSet(lineDataSet(getBeaconDataList_yun(macAddress.get(i)), macAddress.get(i), colorArr.get(i)));
                legs.add(new LegendEntry(macAddress.get(i),Legend.LegendForm.LINE, 10f, 2f, null, colorArr.get(i)));
            }
        }else {
            for (int i = 0; i < macAddress.size(); i++) {
                if (macTitle.getText().toString().equals(macAddress.get(i))){
                    lineData.addDataSet(lineDataSet(getBeaconDataList_yun(macAddress.get(i)), macAddress.get(i), colorArr.get(i)));
                    legs.add(new LegendEntry(macAddress.get(i),Legend.LegendForm.LINE, 10f, 2f, null, colorArr.get(i)));
                }
            }
        }
    }

    public void entriesDataSet(){
        Legend l=chart.getLegend();
        l.setWordWrapEnabled(true);

        setLineData();

        chart.setData(lineData);
        chart.setScaleEnabled(true);

        l.setCustom(legs);
        l.setTextColor(Color.WHITE);
        l.setEnabled(true);// 줌기능 활성화

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.enableGridDashedLine(8, 24, 0);

        YAxis yLAxis = chart.getAxisLeft();
        yLAxis.setTextColor(Color.WHITE);

        YAxis yRAxis = chart.getAxisRight();
        yRAxis.setDrawLabels(false);
        yRAxis.setDrawAxisLine(false);
        yRAxis.setDrawGridLines(false);

        Description description = new Description();
        description.setText("");

        chart.setDoubleTapToZoomEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.animateY(1000, Easing.EasingOption.EaseInCubic);
//        chart.setVisibleXRangeMaximum(25);
        chart.setVisibleXRangeMinimum(7);// 최대 확대했을때 보여줄 수 있는 데이터 개수
        chart.zoom(50f, 0, 50f, 0, YAxis.AxisDependency.LEFT);
        chart.moveViewTo(lineData.getXMax(), 50f, YAxis.AxisDependency.LEFT);
        chart.setScaleYEnabled(false);
        chart.invalidate();
    }

    @Override
    protected void onPause() {
        endVal=false;
        if (thread!=null)thread.interrupt();
        super.onPause();
        Log.e("ShowLifeCycle", "OnPause");
    }

    private void chartOptionSetting(){
        chart=findViewById(R.id.beaconDataChart);
        chart.setMarker(null);
        MyMarkerView myMarkerView=new MyMarkerView(this, R.layout.marker_view);
        myMarkerView.setChartView(chart);
        chart.setMarker(myMarkerView);
    }

    private LineDataSet lineDataSet(List<Entry> entries, String label, int color){
        LineDataSet lineDataSet=new LineDataSet(entries, label);
        lineDataSet.setLineWidth(2);
        lineDataSet.setCircleRadius(6);
        lineDataSet.setCircleColor(color);
        lineDataSet.setCircleColorHole(Color.BLUE);
        lineDataSet.setColor(color);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);

        return lineDataSet;
    }

    public List<Entry> getBeaconDataList_yun(String beaconMac){
        SQLiteDatabase readDB=null;
        DeviceDataItem deviceDataItem=new DeviceDataItem();
        ArrayList<DeviceDataItem> rData=new ArrayList<>();
        List<Entry> entries=new ArrayList<>();
        try {
            readDB=openOrCreateDatabase("beaconTestData02", MODE_PRIVATE, null);
            Cursor c=readDB.rawQuery("SELECT * FROM "+"beaconTestData02"+" where beaconMac='"+beaconMac+"'", null);


            if (c==null || !c.moveToNext()) {
                entries.add(new Entry(0, 0, deviceDataItem));
            }

            while (c.moveToNext()) {
//                Log.e("SQLiteDataShow", "rssi = "+c.getString(c.getColumnIndex("beaconRssi"))+", time = "+c.getString(c.getColumnIndex("beaconTime")));
                deviceDataItem.setDeviceRssi(c.getString(c.getColumnIndex("beaconRssi")));
                deviceDataItem.setScanTime(c.getString(c.getColumnIndex("beaconTime")));
                deviceDataItem.setDeviceAddress(beaconMac);
                rData.add(deviceDataItem);
                entries.add(new Entry(c.getPosition(), c.getInt(c.getColumnIndex("beaconRssi")), deviceDataItem));
            }
        }catch (Exception e){e.getMessage();}

        return entries;
    }

    public void reSetData(View view) {
        scanState=scanState ? false : true;
        Log.e("stopSetData", scanState+"");
        if (scanState){
            Glide.with(this).load(R.drawable.ic_baseline_motion_photos_paused_24).into(stopIcon);
            ((BeaconDataShow)BeaconDataShow.mContext).startScan();
        }else {
            Glide.with(this).load(R.drawable.ic_baseline_not_started_24).into(stopIcon);
            ((BeaconDataShow)BeaconDataShow.mContext).stopScan();
        }
    }

    public void autoSetData() {
        if (scanState){
            setLineData();// 그래프 데이터 갱신
            lineData.notifyDataChanged();// 데이터 변화 알림
            chart.notifyDataSetChanged();// 데이터 변화 알림
            chart.moveViewToX(lineData.getEntryCount());// x축으로 그래프 자동이동 코드 => 가장 긴 그래프로 이동함. 분기 필요.
            Log.e("autoSetData", "setData"+", "+scanState);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {

        chart.moveViewToX(chart.getLowestVisibleX()+(chart.getVisibleXRange()/2));
        super.onConfigurationChanged(newConfig);
    }

    public void nextMacBtn(View view) {
        if (macTitle.getText().toString().equals("전체")){// 첫번째 라인만 보여주도록 변경.
            macTitle.setText(macAddress.get(0));
            lineColor.setBackgroundColor(colorArr.get(0));
            chart.setMarker(null);
        }else {
            for (int i = 0; i < macAddress.size(); i++) {
                if (macTitle.getText().toString().equals(macAddress.get(i))){
                    if ((i+1)!=macAddress.size()){// 다시 전체 라인 보여주도록 변경
                        macTitle.setText(macAddress.get(i+1));
                        lineColor.setBackgroundColor(colorArr.get(i+1));
                    }else {//다음 라인 보여주도록 변경
                        macTitle.setText("전체");
                        lineColor.setBackgroundColor(0);
                    }
                    break;
                }
            }
        }



        setLineData();// 그래프 데이터 갱신
        chartOptionSetting();
        lineData.notifyDataChanged();// 데이터 변화 알림
        chart.notifyDataSetChanged();// 데이터 변화 알림
        chart.zoom(0, 0, 0, 0, YAxis.AxisDependency.LEFT);
        chart.moveViewToX(lineData.getEntryCount());// x축으로 그래프 자동이동 코드 => 가장 긴 그래프로 이동함. 분기 필요.

    }

    public void selectSaveBtn(View view) {
        ((BeaconDataShow)BeaconDataShow.mContext).selectSaveData();
    }
}
package com.example.beacontest03;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

public class OptionList extends AppCompatActivity {// 옵션설정

    Toolbar toolbar;

    LinearLayout speed0, speed1,speed2;
    LinearLayout sType0, sType1;
    LinearLayout sVisibleLay, tVisibleLay;
    ImageView rScanBtn0, rScanBtn1, rScanBtn2;
    ImageView rSaveBtn0, rSaveBtn1;
    ImageView sVisibleBtn, tVisibleBtn;

    final int SCAN_MODE_LOW_POWER=0;
    final int SCAN_MODE_BALANCED=1;
    final int SCAN_MODE_LOW_LATENCY=2;
    final String TYPE_CSV=".csv";
    final String TYPE_JSON=".json";
    int oSpeedVal;// 스캔속도
    String oSaveTypeVal;// 파일 타입

    SharedPreferences.Editor editor;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_list);

        setToolbar();
        setLayoutId();
        getSharedPreferences();
        setScanBtnChecked();
    }

    private void getSharedPreferences(){// 기본 옵션 세팅.
        sharedPref=getSharedPreferences(getString(R.string.option_data), MODE_PRIVATE);
        editor=sharedPref.edit();
        oSpeedVal=sharedPref.getInt(getString(R.string.scan_speed), SCAN_MODE_LOW_LATENCY);
        oSaveTypeVal=sharedPref.getString(getString(R.string.save_type_set), TYPE_CSV);
    }

    private void setScanBtnChecked(){
        switch (oSpeedVal){
            case SCAN_MODE_LOW_POWER:// 느림
                rScanBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                break;
            case SCAN_MODE_BALANCED:// 중간
                rScanBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                break;
            case SCAN_MODE_LOW_LATENCY:// 빠름
                rScanBtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                break;
        }

        switch (oSaveTypeVal){
            case TYPE_CSV:
                rSaveBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                break;
            case TYPE_JSON:
                rSaveBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                break;
        }

    }

    private void setToolbar(){
        toolbar=findViewById(R.id.option_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_backpressed_w_50p));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {// 저장 메뉴버튼 생성
        MenuInflater menuInflater=new MenuInflater(this);
        menuInflater.inflate(R.menu.option_save_button_meun, menu);
        MenuItem menuItem=toolbar.getMenu().getItem(0);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        SpannableString s=new SpannableString(menuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(Color.WHITE),0, s.length(), 0);//getResources().getColor(R.color.white)
        s.setSpan(new RelativeSizeSpan(1.3f),0, s.length(), 0);
        menuItem.setTitle(s);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
//        return super.onOptionsItemSelected(item);
        switch (item.getItemId()){
            case R.id.option_save:
                saveOption();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setLayoutId(){// 레이아웃 아이디 설정
        speed0=findViewById(R.id.speed_00);
        speed1=findViewById(R.id.speed_01);
        speed2=findViewById(R.id.speed_02);
        sType0=findViewById(R.id.type_00);
        sType1=findViewById(R.id.type_01);
        rScanBtn0=findViewById(R.id.btn_speed_00);
        rScanBtn1=findViewById(R.id.btn_speed_01);
        rScanBtn2=findViewById(R.id.btn_speed_02);
        rSaveBtn0=findViewById(R.id.btn_type_00);
        rSaveBtn1=findViewById(R.id.btn_type_01);
        sVisibleLay=findViewById(R.id.sOption_visible_lay);
        tVisibleLay=findViewById(R.id.tOption_visible_lay);
        sVisibleBtn=findViewById(R.id.option_0_visible);
        tVisibleBtn=findViewById(R.id.option_1_visible);
    }

    private void setOptionVisible(LinearLayout layout, ImageView imageButton){
        if (layout.getVisibility()==View.VISIBLE){
            layout.setVisibility(View.GONE);
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_down_b));
        }else {
            layout.setVisibility(View.VISIBLE);
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_up_b));
        }
    }


    public void itemClicked(View view) {// 아이템 선택시 동작.
        switch (view.getId()){
            case R.id.speed_00:
                rScanBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                rScanBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                rScanBtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                oSpeedVal=SCAN_MODE_LOW_POWER;
                break;
            case R.id.speed_01:
                rScanBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                rScanBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                rScanBtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                oSpeedVal=SCAN_MODE_BALANCED;
                break;
            case R.id.speed_02:
                rScanBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                rScanBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                rScanBtn2.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                oSpeedVal=SCAN_MODE_LOW_LATENCY;
                break;
            case R.id.type_00:
                rSaveBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                rSaveBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                oSaveTypeVal=TYPE_CSV;
                break;
            case R.id.type_01:
                rSaveBtn0.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_unchecked));
                rSaveBtn1.setImageDrawable(getResources().getDrawable(R.drawable.ic_radio_checked));
                oSaveTypeVal=TYPE_JSON;
                break;
        }
    }
    
    private void saveOption(){
        int sVal=sharedPref.getInt(getString(R.string.scan_speed), SCAN_MODE_LOW_LATENCY);
        String tVal=sharedPref.getString(getString(R.string.save_type_set), TYPE_CSV);
        
        if (sVal!=oSpeedVal || !tVal.equals(oSaveTypeVal)){
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("옵션 저장");
            builder.setMessage("변경사항이 있습니다. 저장하시겠습니까?");
            builder.setPositiveButton("저장", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.putInt(getString(R.string.scan_speed), oSpeedVal);
                    editor.putString(getString(R.string.save_type_set), oSaveTypeVal);
                    editor.commit();
                    OptionList.super.onBackPressed();
                }
            });
            builder.setNegativeButton("취소", null);
            AlertDialog alertDialog=builder.create();
            alertDialog.show();
        }else {
            OptionList.super.onBackPressed();
        }
        
    }

    public void listVisibleBtn(View view) {
        switch (view.getId()){
            case R.id.option_0_visible:
                setOptionVisible(sVisibleLay, sVisibleBtn);
                break;
            case R.id.option_1_visible:
                setOptionVisible(tVisibleLay, tVisibleBtn);
                break;
        }
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        saveOption();
    }
}
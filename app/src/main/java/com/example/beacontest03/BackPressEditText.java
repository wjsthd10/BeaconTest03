package com.example.beacontest03;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BackPressEditText extends androidx.appcompat.widget.AppCompatEditText {

    private OnBackPressedCallback backPressedCallback;

    public BackPressEditText(Context context) {
        super(context);
    }
    public BackPressEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
    public BackPressEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && backPressedCallback != null){
            backPressedCallback.handleOnBackPressed();
        }
        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnBackPressCallback(OnBackPressedCallback backPressCallback){
        backPressedCallback=backPressCallback;
    }
    public interface OnBackPressCallback{
        public void onBackPress();
    }
}

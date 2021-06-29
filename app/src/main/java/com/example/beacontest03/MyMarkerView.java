package com.example.beacontest03;

import android.content.Context;
import android.widget.TextView;

import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Utils;

public class MyMarkerView extends MarkerView {
    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context
     * @param layoutResource the layout resource to use for the MarkerView
     */

    TextView tv, time;

    public MyMarkerView(Context context, int layoutResource) {
        super(context, layoutResource);
        tv=findViewById(R.id.tv_marker);
        time=findViewById(R.id.tv_marker_time);
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        if (e instanceof CandleEntry) {
            CandleEntry ce= (CandleEntry) e;
            DeviceDataItem deviceDataItem= (DeviceDataItem) ce.getData();
            tv.setText("RSSI : "+ Utils.formatNumber(ce.getHigh(), 0, true));
            time.setText("TIME : "+deviceDataItem.getScanTime());
        }else {
            DeviceDataItem deviceDataItem= (DeviceDataItem) e.getData();
            tv.setText("RSSI : "+Utils.formatNumber(e.getY(), 0, true));
            time.setText("TIME : "+deviceDataItem.getScanTime());
        }
        super.refreshContent(e, highlight);
    }

    @Override
    public MPPointF getOffset() {
        return new MPPointF(-(getWidth()/2), -getHeight());
    }
}

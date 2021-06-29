package com.example.beacontest03;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceDataItem implements Parcelable {

    String deviceName;
    String deviceAddress;
    String deviceRssi;
    String deviceUUID;
    String deviceMajor;
    String deviceMinor;
    String deviceTxPower;
    String scanTime;// 스캔된 시간
    String rssiSum;// RSSI값의 평균
    String beaconType;

    boolean select;// 선택되어있는 상태 표시 선택되면 true로 표시
    boolean noSignal;// 2분이상 비콘이 탐지되지 않을 경우 true로 표시
    boolean searchData;// 검색과정에서 탈락하면 false로 변경

    int runCount;// timeOut횟수 저장.
    int timeOut;// 초과시간(분)

    String timeOutDB;

    public DeviceDataItem() {
        deviceName="";
        deviceAddress="";
        deviceRssi="";
        deviceUUID="";
        deviceMajor="";
        deviceMinor="";
        deviceTxPower="";
        select=false;
        runCount=0;
        scanTime="";
        rssiSum="";
        beaconType="";
        noSignal=false;
        searchData=true;
        timeOut=-1;
        timeOutDB="";
    }


    protected DeviceDataItem(Parcel in) {
        deviceName = in.readString();
        deviceAddress = in.readString();
        deviceRssi = in.readString();
        deviceUUID = in.readString();
        deviceMajor = in.readString();
        deviceMinor = in.readString();
        deviceTxPower = in.readString();
        scanTime = in.readString();
        rssiSum = in.readString();
        beaconType = in.readString();
        select = in.readByte() != 0;
        noSignal = in.readByte() != 0;
        searchData = in.readByte() != 0;
        runCount = in.readInt();
        timeOut = in.readInt();
        timeOutDB = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(deviceName);
        dest.writeString(deviceAddress);
        dest.writeString(deviceRssi);
        dest.writeString(deviceUUID);
        dest.writeString(deviceMajor);
        dest.writeString(deviceMinor);
        dest.writeString(deviceTxPower);
        dest.writeString(scanTime);
        dest.writeString(rssiSum);
        dest.writeString(beaconType);
        dest.writeByte((byte) (select ? 1 : 0));
        dest.writeByte((byte) (noSignal ? 1 : 0));
        dest.writeByte((byte) (searchData ? 1 : 0));
        dest.writeInt(runCount);
        dest.writeInt(timeOut);
        dest.writeString(timeOutDB);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DeviceDataItem> CREATOR = new Creator<DeviceDataItem>() {
        @Override
        public DeviceDataItem createFromParcel(Parcel in) {
            return new DeviceDataItem(in);
        }

        @Override
        public DeviceDataItem[] newArray(int size) {
            return new DeviceDataItem[size];
        }
    };

    public String getTimeOutDB() {
        return timeOutDB;
    }

    public void setTimeOutDB(String timeOutDB) {
        this.timeOutDB = timeOutDB;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceRssi() {
        return deviceRssi;
    }

    public void setDeviceRssi(String deviceRssi) {
        this.deviceRssi = deviceRssi;
    }

    public String getDeviceUUID() {
        return deviceUUID;
    }

    public void setDeviceUUID(String deviceUUID) {
        this.deviceUUID = deviceUUID;
    }

    public String getDeviceMajor() {
        return deviceMajor;
    }

    public void setDeviceMajor(String deviceMajor) {
        this.deviceMajor = deviceMajor;
    }

    public String getDeviceMinor() {
        return deviceMinor;
    }

    public void setDeviceMinor(String deviceMinor) {
        this.deviceMinor = deviceMinor;
    }

    public String getDeviceTxPower() {
        return deviceTxPower;
    }

    public void setDeviceTxPower(String deviceTxPower) {
        this.deviceTxPower = deviceTxPower;
    }

    public String getScanTime() {
        return scanTime;
    }

    public void setScanTime(String scanTime) {
        this.scanTime = scanTime;
    }

    public String getRssiSum() {
        return rssiSum;
    }

    public void setRssiSum(String rssiSum) {
        this.rssiSum = rssiSum;
    }

    public String getBeaconType() {
        return beaconType;
    }

    public void setBeaconType(String beaconType) {
        this.beaconType = beaconType;
    }

    public boolean isSelect() {
        return select;
    }

    public void setSelect(boolean select) {
        this.select = select;
    }

    public boolean isNoSignal() {
        return noSignal;
    }

    public void setNoSignal(boolean noSignal) {
        this.noSignal = noSignal;
    }

    public boolean isSearchData() {
        return searchData;
    }

    public void setSearchData(boolean searchData) {
        this.searchData = searchData;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public String toString(){
        return "UUID : "+deviceUUID+", Major : "+deviceMajor+", Minor : "+deviceMinor;
    }

}

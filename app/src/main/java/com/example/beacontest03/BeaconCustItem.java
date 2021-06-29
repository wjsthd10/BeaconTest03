package com.example.beacontest03;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.Region;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BeaconCustItem implements Parcelable {

    private String sGroupCd;
    private String sCustCd;
    private String sCustNm;
    private String sUUID;
    private String sMajor;
    private String sMinor;
    private boolean bNameModify;

    private String sMacAddress;

    private int nConnState;

    private int nScanCnt;
    private String dtWrite;

    private int mBeaconTypeCode;
    protected int mServiceUuid;
    private List<Identifier> mIdentifiers;
    private Double mDistance = 0.00;
    private int mRssi;
    private int mTxPower;

    public BeaconCustItem() {

    }

    public static final Creator<BeaconCustItem> CREATOR = new Creator<BeaconCustItem>() {
        @Override
        public BeaconCustItem createFromParcel(Parcel in) {
            return new BeaconCustItem(in);
        }

        @Override
        public BeaconCustItem[] newArray(int size) {
            return new BeaconCustItem[size];
        }
    };

    public Region getRegion() {
        /*List<Identifier> mIdentifiers = new ArrayList(3);
        mIdentifiers.add(Identifier.fromUuid(UUID.fromString(sUUID)));
        mIdentifiers.add(Identifier.fromInt(nMajor));
        mIdentifiers.add(Identifier.fromInt(nMinor));
        return new Region(sCustCd, mIdentifiers, sMacAddress);*/

        String sMacAddressTemp = sMacAddress.replaceAll(":", "");
        return new Region(sMacAddressTemp, sMacAddress);
    }

    @Override
    public int hashCode() {
        return sMacAddress.hashCode();
    }

    public String getsGroupCd() {
        return sGroupCd;
    }
    public void setsGroupCd(String sGroupCd) {
        this.sGroupCd = sGroupCd;
    }
    public String getsCustCd() {
        return sCustCd;
    }
    public void setsCustCd(String sCustCd) {
        this.sCustCd = sCustCd;
    }

    public String getsCustNm() {
        return sCustNm;
    }
    public void setsCustNm(String sCustNm) {
        this.sCustNm = sCustNm;
    }

    public String getsMacAddress() {
        return sMacAddress;
    }
    public void setsMacAddress(String sMacAddress) {
        this.sMacAddress = sMacAddress;
    }

    public String getsUUID() {
        return sUUID;
    }
    public void setsUUID(String sUUID) {
        this.sUUID = sUUID;
    }

    public String getsMajor() {
        return sMajor;
    }
    public void setsMajor(String sMajor) {
        this.sMajor = sMajor;
    }

    public String getsMinor() {
        return sMinor;
    }
    public void setsMinor(String sMinor) {
        this.sMinor = sMinor;
    }

    public boolean bNameModify() {
        return bNameModify;
    }
    public void setbNameModify(boolean bNameModify) {
        this.bNameModify = bNameModify;
    }

    public int getnConnState() {
        return nConnState;
    }
    public void setnConnState(int nConnState) {
        this.nConnState = nConnState;
    }

    public int getnScanCnt() {
        return nScanCnt;
    }
    public void setnScanCnt(int nScanCnt) {
        this.nScanCnt = nScanCnt;
    }

    public String getDtWrite() {
        return TextUtils.isEmpty(dtWrite) ? "" : dtWrite;
    }
    public void setDtWrite(String dtWrite) {
        this.dtWrite = dtWrite;
    }


    public int getmBeaconTypeCode() {
        return mBeaconTypeCode;
    }
    public int getmServiceUuid() {
        return mServiceUuid;
    }
    public List<Identifier> getmIdentifiers() {
        return mIdentifiers;
    }
    public Double getmDistance() {
        return mDistance;
    }
    public int getmRssi() {
        return mRssi;
    }
    public int getmTxPower() {
        return mTxPower;
    }

    public void setBeacon(Beacon beacon) {
        this.mBeaconTypeCode = beacon.getBeaconTypeCode();
        this.mServiceUuid = beacon.getServiceUuid();
        this.mIdentifiers = beacon.getIdentifiers();
        this.mDistance = beacon.getDistance();
        this.mRssi = beacon.getRssi();
        this.mTxPower = beacon.getTxPower();
        //setDtWrite(System.currentTimeMillis());
        setDtWrite(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.sss").format(new Date(System.currentTimeMillis())));
    }

    public Identifier getId1() {
        return (Identifier)this.mIdentifiers.get(0);
    }

    public Identifier getId2() {
        return (Identifier)this.mIdentifiers.get(1);
    }

    public Identifier getId3() {
        return (Identifier)this.mIdentifiers.get(2);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.sGroupCd);
        dest.writeString(this.sCustCd);
        dest.writeString(this.sCustNm);
        dest.writeString(this.sUUID);
        dest.writeString(this.sMajor);
        dest.writeString(this.sMinor);
        dest.writeByte(this.bNameModify ? (byte) 1 : (byte) 0);
        dest.writeString(this.sMacAddress);
        dest.writeInt(this.nConnState);
        dest.writeInt(this.nScanCnt);
        dest.writeString(this.dtWrite);
        dest.writeInt(this.mBeaconTypeCode);
        dest.writeInt(this.mServiceUuid);
        dest.writeList(this.mIdentifiers);
        dest.writeValue(this.mDistance);
        dest.writeInt(this.mRssi);
        dest.writeInt(this.mTxPower);
    }

    protected BeaconCustItem(Parcel in) {
        this.sGroupCd = in.readString();
        this.sCustCd = in.readString();
        this.sCustNm = in.readString();
        this.sUUID = in.readString();
        this.sMajor = in.readString();
        this.sMinor = in.readString();
        this.bNameModify = in.readByte() != 0;
        this.sMacAddress = in.readString();
        this.nConnState = in.readInt();
        this.nScanCnt = in.readInt();
        this.dtWrite = in.readString();
        this.mBeaconTypeCode = in.readInt();
        this.mServiceUuid = in.readInt();
        this.mIdentifiers = new ArrayList<Identifier>();
        in.readList(this.mIdentifiers, Identifier.class.getClassLoader());
        this.mDistance = (Double) in.readValue(Double.class.getClassLoader());
        this.mRssi = in.readInt();
        this.mTxPower = in.readInt();
    }


}

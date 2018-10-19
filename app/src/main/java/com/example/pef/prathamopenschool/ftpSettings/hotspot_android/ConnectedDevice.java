package com.example.pef.prathamopenschool.ftpSettings.hotspot_android;
import android.os.Parcel;
import android.os.Parcelable;

public class ConnectedDevice implements Parcelable {
    private String ipAddr;
    private String macAddr;
    private String device;
    private boolean isReachable;

    public ConnectedDevice(String ipAddr, String hWAddr, String device, boolean isReachable) {
        this.ipAddr = ipAddr;
        this.macAddr = hWAddr;
        this.device = device;
        this.isReachable = isReachable;
    }

    protected ConnectedDevice(Parcel in) {
        ipAddr = in.readString();
        macAddr = in.readString();
        device = in.readString();
        isReachable = in.readByte() != 0;
    }

    public static final Creator<ConnectedDevice> CREATOR = new Creator<ConnectedDevice>() {
        @Override
        public ConnectedDevice createFromParcel(Parcel in) {
            return new ConnectedDevice(in);
        }

        @Override
        public ConnectedDevice[] newArray(int size) {
            return new ConnectedDevice[size];
        }
    };

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String hWAddr) {
        macAddr = hWAddr;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public boolean isReachable() {
        return isReachable;
    }

    public void setReachable(boolean isReachable) {
        this.isReachable = isReachable;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ipAddr);
        dest.writeString(macAddr);
        dest.writeString(device);
        dest.writeByte((byte) (isReachable ? 1 : 0));
    }
}
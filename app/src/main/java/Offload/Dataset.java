package Offload;

import java.util.Observable;

/**
 * Created by pedrocandido on 10/5/17.
 */

public class Dataset extends Observable {

    private static Dataset instance = null;
    protected Dataset(){}
    public static Dataset getInstance(){
        if(instance == null){
            instance = new Dataset();
        }
        return instance;
    }

    private boolean isCharging = false;
    private String charginMethod = "";
    private float batteryPct = 0;

    private boolean isConnected = false;
    private String connectionType = "";

    private long heapSize, heapUsed, heapFree;

    private float cpuUser,cpuSystem,cpuIdle,cpuWait;

    private long localTime, remoteTime;

    private long fileSize;

    private long batteryNanoWatt = 0;

    private boolean shouldOffload;


    public long getLocalTime() {
        return localTime;
    }

    public void setLocalTime(long localTime) {
        this.localTime = localTime;
        setChanged();
        notifyObservers();
    }

    public long getRemoteTime() {
        return remoteTime;
    }

    public void setRemoteTime(long remoteTime) {
        this.remoteTime = remoteTime;
        setChanged();
        notifyObservers();
    }

    public float getCpuUser() {
        return cpuUser;
    }

    public void setCpuUser(float cpuUser) {
        this.cpuUser = cpuUser;
        setChanged();
        notifyObservers();
    }

    public float getCpuSystem() {
        return cpuSystem;
    }

    public void setCpuSystem(float cpuSystem) {
        this.cpuSystem = cpuSystem;
        setChanged();
        notifyObservers();
    }

    public float getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(float cpuIdle) {
        this.cpuIdle = cpuIdle;
        setChanged();
        notifyObservers();
    }

    public float getCpuWait() {
        return cpuWait;
    }

    public void setCpuWait(float cpuWait) {
        this.cpuWait = cpuWait;
        setChanged();
        notifyObservers();
    }

    public long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(long heapSize) {
        this.heapSize = heapSize;
        setChanged();
        notifyObservers();
    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
        setChanged();
        notifyObservers();
    }

    public long getHeapFree() {
        return heapFree;
    }

    public void setHeapFree(long headFree) {
        this.heapFree = headFree;
        setChanged();
        notifyObservers();
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
        setChanged();
        notifyObservers();
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
        setChanged();
        notifyObservers();
    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;
        setChanged();
        notifyObservers();
    }

    public String getCharginMethod() {
        return charginMethod;
    }

    public void setCharginMethod(String charginMethod) {
        this.charginMethod = charginMethod;
        setChanged();
        notifyObservers();
    }

    public float getBatteryPct() {
        return batteryPct;
    }

    public void setBatteryPct(float batteryPct) {
        this.batteryPct = batteryPct;
        setChanged();
        notifyObservers();
    }

    public long getBatteryNanoWatt() {
        return batteryNanoWatt;
    }

    public void setBatteryNanoWatt(long batteryNanoWatt) {
        this.batteryNanoWatt = batteryNanoWatt;
        setChanged();
        notifyObservers();
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize= fileSize;
        setChanged();
        notifyObservers();
    }

    public boolean getShouldOffload() {
        return shouldOffload;
    }

    public void setShouldOffload(boolean shouldOffload) {
        this.shouldOffload= shouldOffload;
        setChanged();
        notifyObservers();
    }
}
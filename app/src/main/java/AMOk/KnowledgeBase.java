package AMOk;

import Offload.Dataset;

public class KnowledgeBase {

    private static KnowledgeBase instance = null;
    protected KnowledgeBase (){}
    public static KnowledgeBase getInstance(){
        if(instance == null){
            instance = new KnowledgeBase();
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

    private double coefficient;

    private boolean shouldOffload;

    public long getLocalTime() {
        return localTime;
    }

    public void setLocalTime(long localTime) {
        this.localTime = localTime;
    }

    public long getRemoteTime() {
        return remoteTime;
    }

    public void setRemoteTime(long remoteTime) {
        this.remoteTime = remoteTime;

    }

    public float getCpuUser() {
        return cpuUser;
    }

    public void setCpuUser(float cpuUser) {
        this.cpuUser = cpuUser;

    }

    public float getCpuSystem() {
        return cpuSystem;
    }

    public void setCpuSystem(float cpuSystem) {
        this.cpuSystem = cpuSystem;

    }

    public float getCpuIdle() {
        return cpuIdle;
    }

    public void setCpuIdle(float cpuIdle) {
        this.cpuIdle = cpuIdle;

    }

    public float getCpuWait() {
        return cpuWait;
    }

    public void setCpuWait(float cpuWait) {
        this.cpuWait = cpuWait;

    }

    public long getHeapSize() {
        return heapSize;
    }

    public void setHeapSize(long heapSize) {
        this.heapSize = heapSize;

    }

    public long getHeapUsed() {
        return heapUsed;
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;

    }

    public long getHeapFree() {
        return heapFree;
    }

    public void setHeapFree(long headFree) {
        this.heapFree = headFree;

    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;

    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;

    }

    public boolean isCharging() {
        return isCharging;
    }

    public void setCharging(boolean charging) {
        isCharging = charging;

    }

    public String getCharginMethod() {
        return charginMethod;
    }

    public void setCharginMethod(String charginMethod) {
        this.charginMethod = charginMethod;

    }

    public float getBatteryPct() {
        return batteryPct;
    }

    public void setBatteryPct(float batteryPct) {
        this.batteryPct = batteryPct;

    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize= fileSize;
    }

    public double getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(double coefficient) {
        this.coefficient= coefficient;
    }

    public boolean getShouldOffload() {
        return shouldOffload;
    }

    public void setShouldOffload(boolean shouldOffload) {
        this.shouldOffload = shouldOffload;
    }
}
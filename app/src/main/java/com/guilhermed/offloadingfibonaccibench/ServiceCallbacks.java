package com.guilhermed.offloadingfibonaccibench;

public interface ServiceCallbacks {

    public String getResult();
    public void setResult(String response);

    public String getOutputPos();
    public void setOutputPos(String response);

    public void setBenchmarkValues();
    public void setBenchmarkStart();
}

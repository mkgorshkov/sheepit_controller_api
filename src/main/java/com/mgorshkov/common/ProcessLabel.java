package com.mgorshkov.common;

public enum ProcessLabel {
    PID("PID"),
    UPTIME("UPTIME"),
    CMD("CMD");

    private String header;

    ProcessLabel(String header){
        this.header = header;
    }

    public String getHeader(){
        return header;
    }

    public String toString(){
        return header;
    }
}

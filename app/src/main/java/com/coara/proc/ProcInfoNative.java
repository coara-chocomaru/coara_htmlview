package com.coara.proc;

public class ProcInfoNative {
    static {
        System.loadLibrary("proc");
    }
    public static native String getProcVersion();
    public static native String getProcCPUInfo();
    public static native String getProcMemInfo();

    public static native String getProcSelfStatus();
    public static native String getProcSelfMaps();
    public static native String getProcSelfMountinfo();
    public static native String getProcSelfMounts();
    public static native String getProcSelfMountstats();
    public static native String getProcSelfIO();
    public static native String getProcSelfLimits();
    public static native String getProcSelfOomScore();
    public static native String getProcSelfOomAdj();
    public static native String getProcSelfOomScoreAdj();
    public static native String getProcSelfSched();
    public static native String getProcSelfSchedBoost();
    public static native String getProcSelfSchedBoostPeriodMs();
    public static native String getProcSelfSchedGroupId();
    public static native String getProcSelfSchedInitTaskLoad();
    public static native String getProcSelfSchedWakeUpIdle();
    public static native String getProcSelfSchedstat();
    public static native String getProcSelfSmap();
}
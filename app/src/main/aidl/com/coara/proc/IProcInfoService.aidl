package com.coara.proc;

interface IProcInfoService {
    String getProcVersion();
    String getProcCPUInfo();
    String getProcMemInfo();
    String getProcSelfStatus();
    String getProcSelfMaps();
    String getProcSelfMountinfo();
    String getProcSelfMounts();
    String getProcSelfMountstats();
    String getProcSelfIO();
    String getProcSelfLimits();
    String getProcSelfOomScore();
    String getProcSelfOomAdj();
    String getProcSelfOomScoreAdj();
    String getProcSelfSched();
    String getProcSelfSchedBoost();
    String getProcSelfSchedBoostPeriodMs();
    String getProcSelfSchedGroupId();
    String getProcSelfSchedInitTaskLoad();
    String getProcSelfSchedWakeUpIdle();
    String getProcSelfSchedstat();
    String getProcSelfSmap();
}

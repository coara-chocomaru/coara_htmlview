package com.coara.proc;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

public class ProcInfoService extends Service {
    private final IProcInfoService.Stub binder = new IProcInfoService.Stub() {
        
        @Override
        public String getProcVersion() throws RemoteException {
            return ProcInfoNative.getProcVersion();
        }
        
        @Override
        public String getProcCPUInfo() throws RemoteException {
            return ProcInfoNative.getProcCPUInfo();
        }
        
        @Override
        public String getProcMemInfo() throws RemoteException {
            return ProcInfoNative.getProcMemInfo();
        }
        
        @Override
        public String getProcSelfStatus() throws RemoteException {
            return ProcInfoNative.getProcSelfStatus();
        }
        
        @Override
        public String getProcSelfMaps() throws RemoteException {
            return ProcInfoNative.getProcSelfMaps();
        }
        
        @Override
        public String getProcSelfMountinfo() throws RemoteException {
            return ProcInfoNative.getProcSelfMountinfo();
        }
        
        @Override
        public String getProcSelfMounts() throws RemoteException {
            return ProcInfoNative.getProcSelfMounts();
        }
        
        @Override
        public String getProcSelfMountstats() throws RemoteException {
            return ProcInfoNative.getProcSelfMountstats();
        }
        
        @Override
        public String getProcSelfIO() throws RemoteException {
            return ProcInfoNative.getProcSelfIO();
        }
        
        @Override
        public String getProcSelfLimits() throws RemoteException {
            return ProcInfoNative.getProcSelfLimits();
        }
        
        @Override
        public String getProcSelfOomScore() throws RemoteException {
            return ProcInfoNative.getProcSelfOomScore();
        }
        
        @Override
        public String getProcSelfOomAdj() throws RemoteException {
            return ProcInfoNative.getProcSelfOomAdj();
        }
        
        @Override
        public String getProcSelfOomScoreAdj() throws RemoteException {
            return ProcInfoNative.getProcSelfOomScoreAdj();
        }
        
        @Override
        public String getProcSelfSched() throws RemoteException {
            return ProcInfoNative.getProcSelfSched();
        }
        
        @Override
        public String getProcSelfSchedBoost() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedBoost();
        }
        
        @Override
        public String getProcSelfSchedBoostPeriodMs() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedBoostPeriodMs();
        }
        
        @Override
        public String getProcSelfSchedGroupId() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedGroupId();
        }
        
        @Override
        public String getProcSelfSchedInitTaskLoad() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedInitTaskLoad();
        }
        
        @Override
        public String getProcSelfSchedWakeUpIdle() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedWakeUpIdle();
        }
        
        @Override
        public String getProcSelfSchedstat() throws RemoteException {
            return ProcInfoNative.getProcSelfSchedstat();
        }
        
        @Override
        public String getProcSelfSmap() throws RemoteException {
            return ProcInfoNative.getProcSelfSmap();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}

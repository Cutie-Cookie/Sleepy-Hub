package top.niunaijun.blackbox.core.system;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.core.IBActivityThread;
import top.niunaijun.blackbox.core.env.BEnvironment;
import top.niunaijun.blackbox.core.system.notification.BNotificationManagerService;
import top.niunaijun.blackbox.core.system.pm.BPackageManagerService;
import top.niunaijun.blackbox.core.system.user.BUserHandle;
import top.niunaijun.blackbox.entity.AppConfig;
import top.niunaijun.blackbox.proxy.ProxyManifest;
import top.niunaijun.blackbox.utils.FileUtils;
import top.niunaijun.blackbox.utils.Slog;
import top.niunaijun.blackbox.utils.LaunchDiagnostics;
import top.niunaijun.blackbox.utils.compat.ApplicationThreadCompat;
import top.niunaijun.blackbox.utils.compat.BundleCompat;
import top.niunaijun.blackbox.utils.provider.ProviderCall;


public class BProcessManagerService implements ISystemService {
    public static final String TAG = "BProcessManager";

    public static BProcessManagerService sBProcessManagerService = new BProcessManagerService();
    private final Map<Integer, Map<String, ProcessRecord>> mProcessMap = new HashMap<>();
    private final List<ProcessRecord> mPidsSelfLocked = new ArrayList<>();
    private final Object mProcessLock = new Object();

    public static BProcessManagerService get() {
        return sBProcessManagerService;
    }

    public ProcessRecord startProcessLocked(String packageName, String processName, int userId, int bpid, int callingPid) {
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.start.request", packageName, userId,
                "process=" + processName + "; requestedBpid=" + bpid);
        ApplicationInfo info = BPackageManagerService.get().getApplicationInfo(packageName, 0, userId);
        if (info == null) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.app_info_missing", packageName, userId,
                    "virtual package manager returned no ApplicationInfo");
            return null;
        }
        ProcessRecord app;
        int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
        synchronized (mProcessLock) {
            Map<String, ProcessRecord> bProcess = mProcessMap.get(buid);

            if (bProcess == null) {
                bProcess = new HashMap<>();
            }
            if (bpid == -1) {
                app = bProcess.get(processName);
                if (app != null) {
                    if (app.initLock != null) {
                        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.wait_existing", packageName, userId,
                                "waiting for existing process initialization");
                        app.initLock.block();
                    }
                    if (app.bActivityThread != null) {
                        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.existing_ready", packageName, userId,
                                "process=" + app.processName + "; bpid=" + app.bpid + "; pid=" + app.pid);
                        return app;
                    }
                    LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.existing_not_ready", packageName, userId,
                            "existing process record has no activity-thread binder");
                }
                bpid = getUsingBPidL();
                Slog.d(TAG, "init bUid = " + buid + ", bPid = " + bpid);
            }
            if (bpid == -1) {
                LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.no_slot", packageName, userId,
                        "no free virtual process slot is available");
                throw new RuntimeException("No processes available");
            }
            app = new ProcessRecord(info, processName);
            app.uid = Process.myUid();
            app.bpid = bpid;
            app.buid = BPackageManagerService.get().getAppId(packageName);
            app.callingBUid = getBUidByPidOrPackageName(callingPid, packageName);
            app.userId = userId;

            bProcess.put(processName, app);
            mPidsSelfLocked.add(app);

            synchronized (mProcessMap) {
                mProcessMap.put(buid, bProcess);
            }
            boolean initialized;
            try {
                initialized = initAppProcessL(app);
            } catch (RuntimeException error) {
                LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.init.error", packageName, userId,
                        LaunchDiagnostics.describeError(error));
                throw error;
            }
            if (!initialized) {
                LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.init.failed", packageName, userId,
                        "provider initialization did not return a live virtual activity-thread binder");
                bProcess.remove(processName);
                mPidsSelfLocked.remove(app);
                app = null;
            } else {
                app.pid = getPid(BlackBoxCore.getContext(), ProxyManifest.getProcessName(app.bpid));
                LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.ready", packageName, userId,
                        "process=" + app.processName + "; bpid=" + app.bpid + "; pid=" + app.pid);
            }
        }
        return app;
    }

    private int getUsingBPidL() {
        ActivityManager manager = (ActivityManager) BlackBoxCore.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        Set<Integer> usingPs = new HashSet<>();
        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
            int i = parseBPid(runningAppProcess.processName);
            usingPs.add(i);
        }
        for (int i = 0; i < ProxyManifest.FREE_COUNT; i++) {
            if (usingPs.contains(i)) {
                continue;
            }
            return i;
        }
        return -1;
    }

    public void restartAppProcess(String packageName, String processName, int userId) {
        synchronized (mProcessLock) {
            int callingUid = Binder.getCallingUid();
            int callingPid = Binder.getCallingPid();
            ProcessRecord app = findProcessByPid(callingPid);;
            if (app == null) {
                String stubProcessName = getProcessName(BlackBoxCore.getContext(), callingPid);
                int bpid = parseBPid(stubProcessName);
                startProcessLocked(packageName, processName, userId, bpid, callingPid);
            }
        }
    }

    private int parseBPid(String stubProcessName) {
        String prefix;
        if (stubProcessName == null) {
            return -1;
        } else {
            prefix = BlackBoxCore.getHostPkg() + ":p";
        }
        if (stubProcessName.startsWith(prefix)) {
            try {
                return Integer.parseInt(stubProcessName.substring(prefix.length()));
            } catch (NumberFormatException e) {
                
            }
        }
        return -1;
    }

    private boolean initAppProcessL(ProcessRecord record) {
        Log.d(TAG, "initProcess: " + record.processName);
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.provider_init.begin", record.info.packageName,
                record.userId, "authority=" + record.getProviderAuthority() + "; process=" + record.processName);
        AppConfig appConfig = record.getClientConfig();
        Bundle bundle = new Bundle();
        bundle.putParcelable(AppConfig.KEY, appConfig);
        Bundle init = ProviderCall.callSafely(record.getProviderAuthority(), "_Black_|_init_process_", null, bundle);
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.provider_init.returned", record.info.packageName,
                record.userId, "resultBundle=" + (init == null ? "null" : "present"));
        IBinder appThread = BundleCompat.getBinder(init, "_Black_|_client_");
        if (appThread == null || !appThread.isBinderAlive()) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.provider_binder_missing", record.info.packageName,
                    record.userId, "virtual process provider returned no live activity-thread binder");
            return false;
        }
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.provider_binder_ready", record.info.packageName,
                record.userId, "activity-thread binder is alive");
        attachClientL(record, appThread);

        createProc(record);
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.provider_init.complete", record.info.packageName,
                record.userId, "virtual process attached");
        return true;
    }

    private void attachClientL(final ProcessRecord app, final IBinder appThread) {
        IBActivityThread activityThread = IBActivityThread.Stub.asInterface(appThread);
        if (activityThread == null) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.activity_thread_null", app.info.packageName,
                    app.userId, "IBActivityThread.Stub.asInterface returned null");
            app.kill();
            return;
        }
        try {
            appThread.linkToDeath(new IBinder.DeathRecipient() {
                @Override
                public void binderDied() {
                    Log.d(TAG, "App Died: " + app.processName);
                    LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.binder_died", app.info.packageName,
                            app.userId, "process=" + app.processName + "; bpid=" + app.bpid);
                    appThread.unlinkToDeath(this, 0);
                    onProcessDie(app);
                }
            }, 0);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        app.bActivityThread = activityThread;
        try {
            app.appThread = ApplicationThreadCompat.asInterface(activityThread.getActivityThread());
        } catch (RemoteException e) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.app_thread_binder_error", app.info.packageName,
                    app.userId, LaunchDiagnostics.describeError(e));
            e.printStackTrace();
        }
        app.initLock.open();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.activity_thread_attached", app.info.packageName,
                app.userId, "process=" + app.processName + "; bpid=" + app.bpid);
    }

    public void onProcessDie(ProcessRecord record) {
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "process.died", record.info.packageName, record.userId,
                "process=" + record.processName + "; bpid=" + record.bpid + "; pid=" + record.pid);
        synchronized (mProcessLock) {
            record.kill();
            Map<String, ProcessRecord> process = mProcessMap.get(record.buid);
            if (process != null) {
                process.remove(record.processName);
                if (process.isEmpty()) {
                    mProcessMap.remove(record.buid);
                }
            }
            mPidsSelfLocked.remove(record);

            removeProc(record);
            BNotificationManagerService.get().deletePackageNotification(record.getPackageName(), record.userId);
        }
    }

    public ProcessRecord findProcessRecord(String packageName, String processName, int userId) {
        synchronized (mProcessMap) {
            int appId = BPackageManagerService.get().getAppId(packageName);
            int buid = BUserHandle.getUid(userId, appId);
            Map<String, ProcessRecord> processRecordMap = mProcessMap.get(buid);
            if (processRecordMap == null)
                return null;
            return processRecordMap.get(processName);
        }
    }

    public void killAllByPackageName(String packageName) {
        synchronized (mProcessLock) {
            synchronized (mPidsSelfLocked) {
                List<ProcessRecord> tmp = new ArrayList<>(mPidsSelfLocked);
                int appId = BPackageManagerService.get().getAppId(packageName);
                for (ProcessRecord processRecord : mPidsSelfLocked) {
                    int appId1 = BUserHandle.getAppId(processRecord.buid);
                    if (appId == appId1) {
                        mProcessMap.remove(processRecord.buid);
                        tmp.remove(processRecord);
                        processRecord.kill();
                    }
                }
                mPidsSelfLocked.clear();
                mPidsSelfLocked.addAll(tmp);
            }
        }
    }

    public void killPackageAsUser(String packageName, int userId) {
        synchronized (mProcessLock) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null)
                return;
            for (ProcessRecord value : process.values()) {
                value.kill();
                mPidsSelfLocked.remove(value);
            }
            mProcessMap.remove(buid);
        }
    }

    public List<ProcessRecord> getPackageProcessAsUser(String packageName, int userId) {
        synchronized (mProcessMap) {
            int buid = BUserHandle.getUid(userId, BPackageManagerService.get().getAppId(packageName));
            Map<String, ProcessRecord> process = mProcessMap.get(buid);
            if (process == null)
                return new ArrayList<>();
            return new ArrayList<>(process.values());
        }
    }

    public int getBUidByPidOrPackageName(int pid, String packageName) {
        ProcessRecord callingProcess = findProcessByPid(pid);
        if (callingProcess == null) {
            return BPackageManagerService.get().getAppId(packageName);
        }
        return BUserHandle.getAppId(callingProcess.buid);
    }

    public int getUserIdByCallingPid(int callingPid) {
        ProcessRecord callingProcess = findProcessByPid(callingPid);
        if (callingProcess == null) {
            return 0;
        }
        return callingProcess.userId;
    }

    public ProcessRecord findProcessByPid(int pid) {
        synchronized (mPidsSelfLocked) {
            for (ProcessRecord processRecord : mPidsSelfLocked) {
                if (processRecord.pid == pid)
                    return processRecord;
            }
            return null;
        }
    }

    private static String getProcessName(Context context, int pid) {
        String processName = null;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo info : am.getRunningAppProcesses()) {
            if (info.pid == pid) {
                processName = info.processName;
                break;
            }
        }
        if (processName == null) {
            throw new RuntimeException("processName = null");
        }
        return processName;
    }

    public static int getPid(Context context, String processName) {
        try {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                if (runningAppProcess.processName.equals(processName)) {
                    return runningAppProcess.pid;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static void createProc(ProcessRecord record) {
        File cmdline = new File(BEnvironment.getProcDir(record.bpid), "cmdline");
        try {
            FileUtils.writeToFile(record.processName.getBytes(), cmdline);
        } catch (IOException ignored) {
        }
    }

    private static void removeProc(ProcessRecord record) {
        FileUtils.deleteDir(BEnvironment.getProcDir(record.bpid));
    }

    @Override
    public void systemReady() {
        FileUtils.deleteDir(BEnvironment.getProcDir());
    }
}

package top.niunaijun.blackbox.fake.delegate;

import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.lang.reflect.Field;

import black.android.app.BRActivity;
import black.android.app.BRActivityThread;
import top.niunaijun.blackbox.BlackBoxCore;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.fake.hook.HookManager;
import top.niunaijun.blackbox.fake.hook.IInjectHook;
import top.niunaijun.blackbox.fake.service.HCallbackProxy;
import top.niunaijun.blackbox.fake.service.IActivityClientProxy;
import top.niunaijun.blackbox.utils.HackAppUtils;
import top.niunaijun.blackbox.utils.LaunchDiagnostics;
import top.niunaijun.blackbox.utils.compat.ActivityCompat;
import top.niunaijun.blackbox.utils.compat.ActivityManagerCompat;
import top.niunaijun.blackbox.utils.compat.ContextCompat;

public final class AppInstrumentation extends BaseInstrumentationDelegate implements IInjectHook {

    private static final String TAG = AppInstrumentation.class.getSimpleName();

    private static AppInstrumentation sAppInstrumentation;

    public static AppInstrumentation get() {
        if (sAppInstrumentation == null) {
            synchronized (AppInstrumentation.class) {
                if (sAppInstrumentation == null) {
                    sAppInstrumentation = new AppInstrumentation();
                }
            }
        }
        return sAppInstrumentation;
    }

    public AppInstrumentation() {
    }

    @Override
    public void injectHook() {
        try {
            Instrumentation mInstrumentation = getCurrInstrumentation();
            if (mInstrumentation == this || checkInstrumentation(mInstrumentation))
                return;
            mBaseInstrumentation = (Instrumentation) mInstrumentation;
            BRActivityThread.get(BlackBoxCore.mainThread())._set_mInstrumentation(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Instrumentation getCurrInstrumentation() {
        Object currentActivityThread = BlackBoxCore.mainThread();
        return BRActivityThread.get(currentActivityThread).mInstrumentation();
    }

    @Override
    public boolean isBadEnv() {
        return !checkInstrumentation(getCurrInstrumentation());
    }

    private boolean checkInstrumentation(Instrumentation instrumentation) {
        if (instrumentation instanceof AppInstrumentation) {
            return true;
        }
        Class<?> clazz = instrumentation.getClass();
        if (Instrumentation.class.equals(clazz)) {
            return false;
        }
        do {
            assert clazz != null;
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (Instrumentation.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        Object obj = field.get(instrumentation);
                        if ((obj instanceof AppInstrumentation)) {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Instrumentation.class.equals(clazz));
        return false;
    }

    private void checkHCallback() {
        HookManager.get().checkEnv(HCallbackProxy.class);
    }

    private void checkActivity(Activity activity) {
        String packageName = activity.getPackageName();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "instrumentation.check_activity.begin", packageName,
                BActivityThread.getUserId(), "class=" + activity.getClass().getName());
        Log.d(TAG, "callActivityOnCreate: " + activity.getClass().getName());
        HackAppUtils.enableQQLogOutput(packageName, activity.getClassLoader());
        checkHCallback();
        HookManager.get().checkEnv(IActivityClientProxy.class);
        ActivityInfo info = BRActivity.get(activity).mActivityInfo();
        if (info == null) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "instrumentation.activity_info_missing", packageName,
                    BActivityThread.getUserId(), "ActivityThread returned null ActivityInfo");
        } else {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "instrumentation.activity_info_ready", packageName,
                    BActivityThread.getUserId(), "activity=" + info.name + "; theme=" + info.theme
                            + "; orientation=" + info.screenOrientation);
        }
        ContextCompat.fix(activity);
        ActivityCompat.fix(activity);
        if (info.theme != 0) {
            activity.getTheme().applyStyle(info.theme, true);
        }
        ActivityManagerCompat.setActivityOrientation(activity, info.screenOrientation);
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "instrumentation.check_activity.complete", packageName,
                BActivityThread.getUserId(), "activity preparation completed");
    }

    @Override
    public Application newApplication(ClassLoader cl, String className, Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        ContextCompat.fix(context);

        return super.newApplication(cl, className, context);
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle, PersistableBundle persistentState) {
        String packageName = activity.getPackageName();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.begin", packageName,
                BActivityThread.getUserId(), "class=" + activity.getClass().getName() + "; persistable=true");
        try {
            checkActivity(activity);
            super.callActivityOnCreate(activity, icicle, persistentState);
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.returned", packageName,
                    BActivityThread.getUserId(), "target Activity onCreate returned");
        } catch (RuntimeException error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        } catch (Error error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        }
    }

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        String packageName = activity.getPackageName();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.begin", packageName,
                BActivityThread.getUserId(), "class=" + activity.getClass().getName() + "; persistable=false");
        try {
            checkActivity(activity);
            super.callActivityOnCreate(activity, icicle);
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.returned", packageName,
                    BActivityThread.getUserId(), "target Activity onCreate returned");
        } catch (RuntimeException error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        } catch (Error error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        }
    }

    @Override
    public void callApplicationOnCreate(Application app) {
        String packageName = app.getPackageName();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.application.on_create.begin", packageName,
                BActivityThread.getUserId(), "application=" + app.getClass().getName());
        try {
            checkHCallback();
            super.callApplicationOnCreate(app);
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.application.on_create.returned", packageName,
                    BActivityThread.getUserId(), "target Application onCreate returned");
        } catch (RuntimeException error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.application.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        } catch (Error error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.application.on_create.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        }
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        String packageName = activity.getPackageName();
        LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_resume.begin", packageName,
                BActivityThread.getUserId(), "class=" + activity.getClass().getName());
        try {
            super.callActivityOnResume(activity);
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_resume.returned", packageName,
                    BActivityThread.getUserId(), "target Activity onResume returned");
        } catch (RuntimeException error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_resume.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        } catch (Error error) {
            LaunchDiagnostics.record(BlackBoxCore.getContext(), "app.activity.on_resume.error", packageName,
                    BActivityThread.getUserId(), LaunchDiagnostics.describeError(error));
            throw error;
        }
    }

    public Activity newActivity(ClassLoader cl, String className, Intent intent) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        try {
            return super.newActivity(cl, className, intent);
        } catch (ClassNotFoundException e) {
            return mBaseInstrumentation.newActivity(cl, className, intent);
        }
    }
}

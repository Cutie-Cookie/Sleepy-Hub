package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import black.android.telephony.BRTelephonyManager;
import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.env.BUserIdentifiers;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;
import top.niunaijun.blackbox.utils.MethodParameterUtils;

public class IPhoneSubInfoProxy extends ClassInvocationStub {
    public static final String TAG = "IPhoneSubInfoProxy";

    public IPhoneSubInfoProxy() {
        if (BRTelephonyManager.get()._check_sServiceHandleCacheEnabled() != null) {
            BRTelephonyManager.get()._set_sServiceHandleCacheEnabled(true);
        }
        if (BRTelephonyManager.get()._check_getSubscriberInfoService() != null) {
            BRTelephonyManager.get().getSubscriberInfoService();
        }
    }

    @Override protected Object getWho() { return BRTelephonyManager.get().sIPhoneSubInfo(); }
    @Override protected void inject(Object baseInvocation, Object proxyInvocation) {
        BRTelephonyManager.get()._set_sIPhoneSubInfo(proxyInvocation);
    }
    @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        MethodParameterUtils.replaceFirstAppPkg(args);
        return super.invoke(proxy, method, args);
    }
    @Override public boolean isBadEnv() { return false; }

    @ProxyMethod("getDeviceId")
    public static class GetDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getDeviceIdForPhone")
    public static class GetDeviceIdForPhone extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getImeiForSubscriber")
    public static class GetImeiForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getMeidForSubscriber")
    public static class GetMeidForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getMeid(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getSubscriberIdForSubscriber")
    public static class GetSubscriberIdForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getSubscriberId(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getIccSerialNumberForSubscriber")
    public static class GetIccSerialNumberForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getSimSerial(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getSimSerialNumberForSubscriber")
    public static class GetSimSerialNumberForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getSimSerial(BActivityThread.getUserId());
        }
    }
    @ProxyMethod("getLine1NumberForSubscriber")
    public static class GetLine1NumberForSubscriber extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) { return null; }
    }
}

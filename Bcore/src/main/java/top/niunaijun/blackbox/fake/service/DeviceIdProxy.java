package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.env.BUserIdentifiers;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

public class DeviceIdProxy extends ClassInvocationStub {
    public DeviceIdProxy() { super(); }
    @Override protected Object getWho() { return null; }
    @Override protected void inject(Object baseInvocation, Object proxyInvocation) {}
    @Override public boolean isBadEnv() { return false; }

    @ProxyMethod("getDeviceId")
    public static class GetDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }

    @ProxyMethod("setDeviceId")
    public static class SetDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) { return null; }
    }

    @ProxyMethod("isValidDeviceId")
    public static class IsValidDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) { return true; }
    }

    @ProxyMethod("generateDeviceId")
    public static class GenerateDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }

    @ProxyMethod("storeDeviceId")
    public static class StoreDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) { return null; }
    }

    @ProxyMethod("retrieveDeviceId")
    public static class RetrieveDeviceId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getImei(BActivityThread.getUserId());
        }
    }
}

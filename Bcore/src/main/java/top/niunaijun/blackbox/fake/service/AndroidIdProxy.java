package top.niunaijun.blackbox.fake.service;

import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.Locale;

import top.niunaijun.blackbox.app.BActivityThread;
import top.niunaijun.blackbox.core.env.BUserIdentifiers;
import top.niunaijun.blackbox.fake.hook.ClassInvocationStub;
import top.niunaijun.blackbox.fake.hook.MethodHook;
import top.niunaijun.blackbox.fake.hook.ProxyMethod;

public class AndroidIdProxy extends ClassInvocationStub {
    public AndroidIdProxy() { super(); }
    @Override protected Object getWho() { return null; }
    @Override protected void inject(Object baseInvocation, Object proxyInvocation) {}
    @Override public boolean isBadEnv() { return false; }

    private static String identifierForKey(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof String)) return null;
        String key = ((String) args[0]).toLowerCase(Locale.US);
        int userId = BActivityThread.getUserId();
        if (key.contains("android_id") || key.contains("secure_id")) return BUserIdentifiers.getAndroidId(userId);
        if (key.contains("device_id") || key.contains("imei")) return BUserIdentifiers.getImei(userId);
        return null;
    }

    private static Long identifierAsLong(Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof String)) return null;
        String key = ((String) args[0]).toLowerCase(Locale.US);
        int userId = BActivityThread.getUserId();
        if (key.contains("android_id") || key.contains("secure_id")) {
            return new BigInteger(BUserIdentifiers.getAndroidId(userId), 16).longValue();
        }
        if (key.contains("device_id") || key.contains("imei")) {
            return new BigInteger(BUserIdentifiers.getImei(userId), 10).longValue();
        }
        return null;
    }

    @ProxyMethod("getAndroidId")
    public static class GetAndroidId extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) {
            return BUserIdentifiers.getAndroidId(BActivityThread.getUserId());
        }
    }

    @ProxyMethod("getString")
    public static class GetString extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String value = identifierForKey(args);
            return value != null ? value : method.invoke(who, args);
        }
    }

    @ProxyMethod("getLong")
    public static class GetLong extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            Long value = identifierAsLong(args);
            return value != null ? value : method.invoke(who, args);
        }
    }

    @ProxyMethod("get")
    public static class Get extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String value = identifierForKey(args);
            return value != null ? value : method.invoke(who, args);
        }
    }

    @ProxyMethod("read")
    public static class Read extends MethodHook {
        @Override protected Object hook(Object who, Method method, Object[] args) throws Throwable {
            String value = identifierForKey(args);
            return value != null ? value : method.invoke(who, args);
        }
    }
}

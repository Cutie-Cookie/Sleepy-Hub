package top.niunaijun.blackbox.core.env;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public final class BUserIdentifiers {
    private static final Object LOCK = new Object();
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Map<Integer, Properties> CACHE = new ConcurrentHashMap<>();

    private BUserIdentifiers() {}

    public static String getAndroidId(int userId) {
        return getOrCreate(userId, "android_id", () -> randomHex(8));
    }

    public static String getImei(int userId) {
        return getOrCreate(userId, "imei", BUserIdentifiers::randomImei);
    }

    public static String getMeid(int userId) {
        return getOrCreate(userId, "meid", () -> randomHex(7).toUpperCase(Locale.US));
    }

    public static String getSubscriberId(int userId) {
        return getOrCreate(userId, "subscriber_id", () -> "310260" + randomDigits(9));
    }

    public static String getSimSerial(int userId) {
        return getOrCreate(userId, "sim_serial", () -> "89" + randomDigits(18));
    }

    public static String getSerial(int userId) {
        return getOrCreate(userId, "serial", () -> randomHex(8).toUpperCase(Locale.US));
    }

    private interface ValueFactory { String create(); }

    private static String getOrCreate(int userId, String key, ValueFactory factory) {
        synchronized (LOCK) {
            Properties cached = CACHE.get(userId);
            if (cached != null) {
                String value = cached.getProperty(key);
                if (value != null && !value.isEmpty()) return value;
            }

            File userDir = BEnvironment.getUserDir(userId);
            if (!userDir.exists() && !userDir.mkdirs() && !userDir.isDirectory()) {
                return stableFallback(userId, key);
            }

            File valuesFile = new File(userDir, ".sleepyhub-identifiers");
            File lockFile = new File(userDir, ".sleepyhub-identifiers.lock");
            try (RandomAccessFile lockHandle = new RandomAccessFile(lockFile, "rw");
                 FileChannel channel = lockHandle.getChannel();
                 FileLock ignored = channel.lock()) {
                Properties properties = load(valuesFile);
                if (cached != null) {
                    for (String cachedKey : cached.stringPropertyNames()) {
                        if (!properties.containsKey(cachedKey)) {
                            properties.setProperty(cachedKey, cached.getProperty(cachedKey));
                        }
                    }
                }
                String value = properties.getProperty(key);
                if (value == null || value.isEmpty()) {
                    value = factory.create();
                    properties.setProperty(key, value);
                    storeAtomically(valuesFile, properties);
                }
                CACHE.put(userId, properties);
                return value;
            } catch (Exception error) {
                return stableFallback(userId, key);
            }
        }
    }

    private static Properties load(File file) {
        Properties properties = new Properties();
        if (!file.isFile()) return properties;
        try (FileInputStream input = new FileInputStream(file)) {
            properties.load(input);
        } catch (Exception ignored) {}
        return properties;
    }

    private static void storeAtomically(File target, Properties properties) throws Exception {
        File temporary = new File(target.getParentFile(), target.getName() + ".tmp");
        try (FileOutputStream output = new FileOutputStream(temporary)) {
            properties.store(output, "Sleepy Hub virtual profile identifiers");
            output.getFD().sync();
        }
        if (!temporary.renameTo(target)) {
            try (FileOutputStream output = new FileOutputStream(target)) {
                properties.store(output, "Sleepy Hub virtual profile identifiers");
                output.getFD().sync();
            }
            temporary.delete();
        }
    }

    private static String stableFallback(int userId, String key) {
        String seed = userId + ":" + key + ":SleepyHub";
        String compact = java.util.UUID.nameUUIDFromBytes(
                seed.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        ).toString().replace("-", "");
        if ("android_id".equals(key) || "serial".equals(key)) return compact.substring(0, 16);
        if ("meid".equals(key)) return compact.substring(0, 14).toUpperCase(Locale.US);
        if ("subscriber_id".equals(key)) return "310260" + compact.replaceAll("[^0-9]", "0").substring(0, 9);
        if ("sim_serial".equals(key)) return "89" + compact.replaceAll("[^0-9]", "0").substring(0, 18);
        return luhn(compact.replaceAll("[^0-9]", "0").substring(0, 14));
    }

    private static String randomHex(int byteCount) {
        byte[] bytes = new byte[byteCount];
        RANDOM.nextBytes(bytes);
        StringBuilder result = new StringBuilder(byteCount * 2);
        for (byte value : bytes) result.append(String.format(Locale.US, "%02x", value & 0xff));
        return result.toString();
    }

    private static String randomDigits(int count) {
        StringBuilder result = new StringBuilder(count);
        for (int index = 0; index < count; index++) result.append(RANDOM.nextInt(10));
        return result.toString();
    }

    private static String randomImei() {
        return luhn(randomDigits(14));
    }

    private static String luhn(String firstFourteenDigits) {
        int sum = 0;
        for (int index = firstFourteenDigits.length() - 1; index >= 0; index--) {
            int digit = firstFourteenDigits.charAt(index) - '0';
            if (((firstFourteenDigits.length() - 1 - index) & 1) == 0) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return firstFourteenDigits + ((10 - (sum % 10)) % 10);
    }
}

package top.niunaijun.blackbox.utils;

import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;

/**
 * A small, local-only trace for diagnosing virtual app launch failures.
 *
 * The trace deliberately records only launch stages, package/component names,
 * profile IDs, process IDs, and sanitized exception summaries. It never reads
 * target-app files, accounts, messages, or other app data.
 */
public final class LaunchDiagnostics {
    private static final String FILE_NAME = "sleepyhub-launch-diagnostics.txt";
    private static final int MAX_FILE_BYTES = 64 * 1024;
    private static final Object LOCK = new Object();

    private LaunchDiagnostics() {
    }

    public static void beginAttempt(Context context, String packageName, int userId) {
        clear(context);
        record(context, "launch.requested", packageName, userId, "diagnostic trace started");
    }

    public static void record(Context context, String stage, String packageName, int userId, String detail) {
        if (context == null) {
            return;
        }
        String line = System.currentTimeMillis()
                + "\t" + Process.myPid()
                + "\t" + clean(Thread.currentThread().getName(), 48)
                + "\t" + userId
                + "\t" + clean(stage, 72)
                + "\t" + clean(packageName, 160)
                + "\t" + clean(detail, 220)
                + "\n";

        synchronized (LOCK) {
            File file = getFile(context);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return;
            }
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 FileChannel channel = raf.getChannel();
                 FileLock ignored = channel.lock()) {
                if (raf.length() > MAX_FILE_BYTES) {
                    int keepBytes = MAX_FILE_BYTES / 2;
                    byte[] tail = new byte[keepBytes];
                    raf.seek(Math.max(0, raf.length() - keepBytes));
                    raf.readFully(tail);
                    String text = new String(tail, StandardCharsets.UTF_8);
                    int firstLine = text.indexOf('\n');
                    String kept = firstLine >= 0 ? text.substring(firstLine + 1) : text;
                    raf.setLength(0);
                    raf.seek(0);
                    raf.write(kept.getBytes(StandardCharsets.UTF_8));
                }
                raf.seek(raf.length());
                raf.write(line.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable ignored) {
                // Diagnostics must never interfere with launching an app.
            }
        }
    }

    public static String describeError(Throwable error) {
        if (error == null) {
            return "unknown error";
        }
        String message = error.getMessage();
        if (message == null || message.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }
        return error.getClass().getSimpleName() + ": " + clean(message, 160);
    }

    public static String getReport(Context context) {
        StringBuilder report = new StringBuilder();
        report.append("Sleepy Hub — app launch diagnostic\n");
        report.append("Android: ").append(clean(Build.VERSION.RELEASE, 32))
                .append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
        report.append("This report contains launch-stage events only. It does not read app contents,")
                .append(" accounts, passwords, messages, device IDs, or personal files.\n\n");

        if (context == null) {
            return report.append("No context available.").toString();
        }

        File file = getFile(context);
        if (!file.isFile()) {
            return report.append("No launch attempt has been recorded yet.").toString();
        }

        try (FileInputStream input = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) Math.min(file.length(), MAX_FILE_BYTES)];
            int count = input.read(bytes);
            if (count <= 0) {
                return report.append("The diagnostic trace is empty.").toString();
            }
            report.append("Columns: time | pid | thread | profile | stage | package | detail\n");
            report.append(new String(bytes, 0, count, StandardCharsets.UTF_8));
        } catch (Exception error) {
            report.append("Could not read the diagnostic trace: ")
                    .append(error.getClass().getSimpleName());
        }
        return report.toString();
    }

    public static boolean hasReport(Context context) {
        return context != null && getFile(context).isFile() && getFile(context).length() > 0;
    }

    public static void clear(Context context) {
        if (context == null) {
            return;
        }
        synchronized (LOCK) {
            try (FileOutputStream ignored = new FileOutputStream(getFile(context), false)) {
                // Truncate the previous attempt so the report stays focused.
            } catch (Exception ignored) {
            }
        }
    }

    private static File getFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    private static String clean(String value, int maxLength) {
        if (value == null) {
            return "-";
        }
        String safe = value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        safe = safe.replaceAll("(?i)https?://\\S+", "[url]");
        safe = safe.replaceAll("(?i)content://\\S+", "[content-uri]");
        safe = safe.replaceAll("(?i)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", "[email]");
        safe = safe.replaceAll("(?i)/(data|storage|sdcard)/\\S+", "[path]");
        if (safe.length() > maxLength) {
            safe = safe.substring(0, maxLength) + "…";
        }
        return safe;
    }
}

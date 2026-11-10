package com.dev.ministudio;

import android.content.Context;
import android.os.Build;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public CrashHandler(Context context) {
        this.context = context.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void install(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        try {
            saveCrash(throwable, thread);
        } catch (Exception ignored) {
        }

        // ส่งต่อให้ระบบปกติ (ให้แอปปิดตามปกติ)
        if (defaultHandler != null) {
            defaultHandler.uncaughtException(thread, throwable);
        } else {
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    private void saveCrash(Throwable throwable, Thread thread) {
        try {
            File dir = new File(context.getFilesDir(), "crash_logs");
            if (!dir.exists()) dir.mkdirs();

            String time = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(new Date());
            File file = new File(dir, "crash-" + time + ".txt");

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            throwable.printStackTrace(pw);
            pw.close();

            StringBuilder sb = new StringBuilder();
            sb.append("===== CRASH REPORT =====\n");
            sb.append("Time: ").append(new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.US).format(new Date())).append("\n");
            sb.append("Thread: ").append(thread.getName()).append("\n\n");

            sb.append("===== DEVICE INFO =====\n");
            sb.append("Android: ").append(Build.VERSION.RELEASE).append(" (API ").append(Build.VERSION.SDK_INT).append(")\n");
            sb.append("Manufacturer: ").append(Build.MANUFACTURER).append("\n");
            sb.append("Model: ").append(Build.MODEL).append("\n");
            sb.append("Device: ").append(Build.DEVICE).append("\n");
            sb.append("Product: ").append(Build.PRODUCT).append("\n\n");

            sb.append("===== STACK TRACE =====\n");
            sb.append(sw.toString());

            FileWriter fw = new FileWriter(file);
            fw.write(sb.toString());
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getCrashDir(Context context) {
        return new File(context.getFilesDir(), "crash_logs");
    }
}
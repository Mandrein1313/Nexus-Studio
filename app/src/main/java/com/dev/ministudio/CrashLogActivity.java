package com.dev.ministudio;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class CrashLogActivity extends AppCompatActivity {

    private LinearLayout listContainer;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Layout แบบ programmatic (ไม่ต้องสร้าง XML เพิ่ม)
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#0D0E14"));
        root.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Header
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(16), dp(16), dp(16), dp(12));
        header.setBackgroundColor(Color.parseColor("#1A1B26"));

        TextView btnClose = new TextView(this);
        btnClose.setText("✕");
        btnClose.setTextColor(Color.WHITE);
        btnClose.setTextSize(22);
        btnClose.setPadding(dp(8), dp(4), dp(16), dp(4));
        btnClose.setOnClickListener(v -> finish());
        header.addView(btnClose);

        TextView title = new TextView(this);
        title.setText("Crash Log");
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        header.addView(title);

        root.addView(header);

        // ปุ่ม คัดลอก / ล้างทั้งหมด
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(dp(16), dp(12), dp(16), dp(8));
        btnRow.setGravity(Gravity.CENTER);

        Button btnCopy = makeButton("คัดลอก");
        Button btnClear = makeButton("ล้างทั้งหมด");

        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(40), 1f);
        bp.setMargins(dp(4), 0, dp(4), 0);
        btnCopy.setLayoutParams(bp);
        btnClear.setLayoutParams(bp);

        btnRow.addView(btnCopy);
        btnRow.addView(btnClear);
        root.addView(btnRow);

        // รายการ
        ScrollView scroll = new ScrollView(this);
        listContainer = new LinearLayout(this);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setPadding(dp(12), dp(8), dp(12), dp(24));
        scroll.addView(listContainer);

        emptyView = new TextView(this);
        emptyView.setText("ไม่มี Crash Log 🎉");
        emptyView.setTextColor(Color.parseColor("#A1A1AA"));
        emptyView.setTextSize(16);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(0, dp(80), 0, 0);
        emptyView.setVisibility(View.GONE);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.addView(emptyView);
        content.addView(scroll, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        setContentView(root);

        btnCopy.setOnClickListener(v -> copyAllLogs());
        btnClear.setOnClickListener(v -> clearAllLogs());

        loadLogs();
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setBackgroundColor(Color.parseColor("#3F3F46"));
        return b;
    }

    private void loadLogs() {
        listContainer.removeAllViews();
        File dir = CrashHandler.getCrashDir(this);

        File[] files = dir.exists() ? dir.listFiles((d, name) -> name.endsWith(".txt")) : null;

        if (files == null || files.length == 0) {
            emptyView.setVisibility(View.VISIBLE);
            return;
        }

        emptyView.setVisibility(View.GONE);

        // เรียงใหม่สุดอยู่บน
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        for (File f : files) {
            listContainer.addView(createItemView(f));
        }
    }

    private View createItemView(File file) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(14), dp(12), dp(14), dp(12));
        item.setBackgroundColor(Color.parseColor("#1A1B26"));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        item.setLayoutParams(lp);

        String dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                .format(new Date(file.lastModified()));

        TextView tvDate = new TextView(this);
        tvDate.setText(dateStr);
        tvDate.setTextColor(Color.parseColor("#A1A1AA"));
        tvDate.setTextSize(12);
        item.addView(tvDate);

        TextView tvName = new TextView(this);
        tvName.setText(file.getName());
        tvName.setTextColor(Color.WHITE);
        tvName.setTextSize(14);
        tvName.setTypeface(Typeface.DEFAULT_BOLD);
        tvName.setPadding(0, dp(2), 0, 0);
        item.addView(tvName);

        item.setOnClickListener(v -> showDetail(file));

        return item;
    }

    private void showDetail(File file) {
        String content = readFile(file);

        ScrollView sv = new ScrollView(this);
        TextView tv = new TextView(this);
        tv.setText(content);
        tv.setTextColor(Color.parseColor("#E4E4E7"));
        tv.setTextSize(12);
        tv.setTypeface(Typeface.MONOSPACE);
        tv.setPadding(dp(16), dp(16), dp(16), dp(16));
        sv.addView(tv);

        new AlertDialog.Builder(this)
                .setTitle(file.getName())
                .setView(sv)
                .setPositiveButton("คัดลอก", (d, w) -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText("crash", content));
                    Toast.makeText(this, "คัดลอกแล้ว", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("ปิด", null)
                .setNeutralButton("ลบ", (d, w) -> {
                    file.delete();
                    loadLogs();
                })
                .show();
    }

    private void copyAllLogs() {
        File dir = CrashHandler.getCrashDir(this);
        File[] files = dir.exists() ? dir.listFiles((d, n) -> n.endsWith(".txt")) : null;
        if (files == null || files.length == 0) {
            Toast.makeText(this, "ไม่มี log", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (File f : files) {
            sb.append("===== ").append(f.getName()).append(" =====\n");
            sb.append(readFile(f)).append("\n\n");
        }

        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText("crash_all", sb.toString()));
        Toast.makeText(this, "คัดลอกทั้งหมดแล้ว", Toast.LENGTH_SHORT).show();
    }

    private void clearAllLogs() {
        new AlertDialog.Builder(this)
                .setTitle("ล้างทั้งหมด?")
                .setMessage("ต้องการลบ Crash Log ทั้งหมดหรือไม่")
                .setPositiveButton("ลบ", (d, w) -> {
                    File dir = CrashHandler.getCrashDir(this);
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File f : files) f.delete();
                    }
                    loadLogs();
                    Toast.makeText(this, "ล้างแล้ว", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("ยกเลิก", null)
                .show();
    }

    private String readFile(File file) {
        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            return "อ่านไฟล์ไม่ได้: " + e.getMessage();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}
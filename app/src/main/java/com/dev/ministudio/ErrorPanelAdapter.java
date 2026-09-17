package com.dev.ministudio;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ErrorPanelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnErrorClickListener {
        void onErrorClick(ParsedError error);
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Row> rows = new ArrayList<>();
    private final OnErrorClickListener listener;
    private final float density;

    public ErrorPanelAdapter(android.content.Context context, OnErrorClickListener listener) {
        this.listener = listener;
        this.density = context.getResources().getDisplayMetrics().density;
    }

    /** จัดกลุ่มตามชื่อไฟล์ แล้วสร้างแถว header + item */
    public void setErrors(List<ParsedError> errors) {
        rows.clear();
        if (errors == null || errors.isEmpty()) {
            notifyDataSetChanged();
            return;
        }

        Map<String, List<ParsedError>> grouped = new LinkedHashMap<>();
        for (ParsedError e : errors) {
            String key = (e.file != null && !e.file.isEmpty()) ? e.file : "(unknown)";
            if (!grouped.containsKey(key)) {
                grouped.put(key, new ArrayList<>());
            }
            grouped.get(key).add(e);
        }

        for (Map.Entry<String, List<ParsedError>> entry : grouped.entrySet()) {
            rows.add(Row.header(entry.getKey(), entry.getValue().size()));
            for (ParsedError e : entry.getValue()) {
                rows.add(Row.item(e));
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            LinearLayout box = new LinearLayout(parent.getContext());
            box.setOrientation(LinearLayout.HORIZONTAL);
            box.setGravity(Gravity.CENTER_VERTICAL);
            box.setPadding(dp(14), dp(10), dp(14), dp(6));
            box.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView icon = new TextView(parent.getContext());
            icon.setText("📄");
            icon.setTextSize(13);
            icon.setPadding(0, 0, dp(8), 0);
            box.addView(icon);

            TextView title = new TextView(parent.getContext());
            title.setId(View.generateViewId());
            title.setTextColor(Color.parseColor("#7AA2F7"));
            title.setTextSize(13);
            title.setTypeface(Typeface.DEFAULT_BOLD);
            title.setLayoutParams(new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            box.addView(title);

            TextView count = new TextView(parent.getContext());
            count.setId(View.generateViewId());
            count.setTextColor(Color.parseColor("#F7768E"));
            count.setTextSize(12);
            box.addView(count);

            return new HeaderVH(box, title, count);
        }

        LinearLayout box = new LinearLayout(parent.getContext());
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(28), dp(8), dp(14), dp(10));
        box.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        box.setBackgroundColor(Color.parseColor("#1A1B26"));

        TextView msg = new TextView(parent.getContext());
        msg.setId(View.generateViewId());
        msg.setTextColor(Color.parseColor("#C0CAF5"));
        msg.setTextSize(12);
        msg.setLineSpacing(0, 1.15f);
        box.addView(msg);

        TextView meta = new TextView(parent.getContext());
        meta.setId(View.generateViewId());
        meta.setTextColor(Color.parseColor("#565F89"));
        meta.setTextSize(11);
        meta.setPadding(0, dp(4), 0, 0);
        box.addView(meta);

        return new ItemVH(box, msg, meta);
    }

    @Override
public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Row row = rows.get(position);

    if (row.isHeader) {
        HeaderVH h = (HeaderVH) holder;
        h.title.setText(row.fileName);
        h.count.setText(String.valueOf(row.fileCount));
        // header ไม่ต้องคลิกวาร์ป
        h.itemView.setOnClickListener(null);
        return;
    }

    // ===== ตรงนี้: แถว error แต่ละข้อ =====
    ItemVH h = (ItemVH) holder;
    final ParsedError e = row.error;

    h.msg.setText("✕  " + (e.message != null ? e.message : ""));
    h.meta.setText("Line " + e.line
            + (e.column > 0 ? "  ·  Col " + e.column : "")
            + "  ·  " + shortType(e.type));

    h.itemView.setOnClickListener(v -> {
        if (listener != null && e != null) {
            listener.onErrorClick(e);   // → ไปเรียก executeJumpToError ใน MainActivity
        }
    });
}

    @Override
    public int getItemCount() {
        return rows.size();
    }

    private String shortType(String type) {
        if (type == null) return "";
        switch (type) {
            case "JAVA_ERROR": return "Java";
            case "XML_AAPT2_ERROR": return "XML / AAPT";
            case "KOTLIN_ERROR": return "Kotlin";
            default: return type;
        }
    }

    private int dp(int v) {
        return (int) (v * density + 0.5f);
    }

    private static class Row {
        final boolean isHeader;
        final String fileName;
        final int fileCount;
        final ParsedError error;

        private Row(boolean isHeader, String fileName, int fileCount, ParsedError error) {
            this.isHeader = isHeader;
            this.fileName = fileName;
            this.fileCount = fileCount;
            this.error = error;
        }

        static Row header(String file, int count) {
            return new Row(true, file, count, null);
        }

        static Row item(ParsedError e) {
            return new Row(false, null, 0, e);
        }
    }

    private static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView title, count;
        HeaderVH(View itemView, TextView title, TextView count) {
            super(itemView);
            this.title = title;
            this.count = count;
        }
    }

    private static class ItemVH extends RecyclerView.ViewHolder {
        final TextView msg, meta;
        ItemVH(View itemView, TextView msg, TextView meta) {
            super(itemView);
            this.msg = msg;
            this.meta = meta;
        }
    }
}

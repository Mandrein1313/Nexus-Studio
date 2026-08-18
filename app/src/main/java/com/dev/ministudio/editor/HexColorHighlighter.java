package com.dev.ministudio.editor;

import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.rosemoe.sora.lang.styling.MappedSpans;
import io.github.rosemoe.sora.lang.styling.Styles;
import io.github.rosemoe.sora.lang.styling.TextStyle;
import io.github.rosemoe.sora.text.Content;
import io.github.rosemoe.sora.widget.CodeEditor;
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * ไฮไลต์รหัสสี #RGB / #RRGGBB / #AARRGGBB ใน editor ให้เป็นสีจริง
 * ใช้เฉพาะไฟล์ .xml / .css — ไม่ควรใช้กับ .java
 */
public class HexColorHighlighter {

    private final CodeEditor editor;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    private int nextColorId = 200;
    private final HashMap<String, Integer> colorIdMap = new HashMap<>();

    private static final Pattern HEX_PATTERN = Pattern.compile(
            "#(?:[0-9a-fA-F]{8}|[0-9a-fA-F]{6}|[0-9a-fA-F]{3})"
    );

    public HexColorHighlighter(CodeEditor editor) {
        this.editor = editor;
    }

    /** ไฟล์ที่ควรไฮไลต์สีหรือไม่ */
    public static boolean isColorFile(File file) {
        if (file == null) return false;
        String n = file.getName().toLowerCase();
        return n.endsWith(".xml") || n.endsWith(".css");
    }

    /** เรียกหลังพิมพ์ / เปิดไฟล์ — จะหน่วงนิดหน่อยกันกระตุก */
    public void schedule(File currentFile) {
        if (!isColorFile(currentFile) || editor == null) return;

        if (pending != null) {
            handler.removeCallbacks(pending);
        }
        pending = this::apply;
        handler.postDelayed(pending, 300);
    }

    /** ไฮไลต์ทันที */
    public void apply() {
        if (editor == null) return;

        try {
            Content content = editor.getText();
            int lineCount = content.getLineCount();
            EditorColorScheme scheme = editor.getColorScheme();

            MappedSpans.Builder builder = new MappedSpans.Builder(lineCount);

            for (int line = 0; line < lineCount; line++) {
                String lineText = content.getLineString(line);
                builder.addIfNeeded(line, 0, EditorColorScheme.TEXT_NORMAL);

                Matcher matcher = HEX_PATTERN.matcher(lineText);
                while (matcher.find()) {
                    String hex = matcher.group();
                    int color = parseHex(hex);
                    if (color == 0) continue;

                    // บังคับทึบ ให้อ่านง่าย
                    int solid = 0xFF000000 | (color & 0x00FFFFFF);
                    int colorId = getOrRegister(scheme, hex, solid);

                    builder.addIfNeeded(line, matcher.start(), TextStyle.makeStyle(colorId));
                    builder.addIfNeeded(line, matcher.end(), EditorColorScheme.TEXT_NORMAL);
                }
            }

            Styles styles = new Styles(builder.build());
            editor.setStyles(styles);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getOrRegister(EditorColorScheme scheme, String hex, int color) {
        String key = hex.toLowerCase();
        Integer existing = colorIdMap.get(key);
        if (existing != null) {
            scheme.setColor(existing, color);
            return existing;
        }
        int id = nextColorId++;
        if (nextColorId > 2000) nextColorId = 200;
        colorIdMap.put(key, id);
        scheme.setColor(id, color);
        return id;
    }

    public static int parseHex(String hex) {
        try {
            String h = hex.startsWith("#") ? hex.substring(1) : hex;
            if (h.length() == 3) {
                h = "" + h.charAt(0) + h.charAt(0)
                        + h.charAt(1) + h.charAt(1)
                        + h.charAt(2) + h.charAt(2);
                return Color.parseColor("#" + h);
            } else if (h.length() == 6) {
                return Color.parseColor("#" + h);
            } else if (h.length() == 8) {
                return (int) Long.parseLong(h, 16);
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    public void destroy() {
        if (pending != null) {
            handler.removeCallbacks(pending);
            pending = null;
        }
    }
}
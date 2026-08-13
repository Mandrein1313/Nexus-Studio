package com.dev.ministudio.editor;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * ธีม Tokyo Night / One Dark โทนม่วง สำหรับ Nexus Studio
 */
public class NexusColorScheme extends EditorColorScheme {

    @Override
    public void applyDefault() {
        super.applyDefault();

        // ===== พื้นหลัง =====
        setColor(WHOLE_BACKGROUND, 0xFF1A1B26);      // 深蓝ม่วงเข้ม
        setColor(TEXT_NORMAL, 0xFFA9B1D6);           // เทาฟ้าอ่อน อ่านง่าย
        setColor(CURRENT_LINE, 0xFF24283B);          // ไฮไลต์บรรทัดปัจจุบัน
        setColor(LINE_NUMBER, 0xFF3B4261);           // เลขบรรทัด
        setColor(LINE_NUMBER_BACKGROUND, 0xFF1A1B26);
        setColor(LINE_DIVIDER, 0xFF292E42);

        // ===== การเลือกข้อความ =====
        setColor(SELECTED_TEXT_BACKGROUND, 0xFF364A82);
        setColor(SELECTION_INSERT, 0xFFC0CAF5);
        setColor(SELECTION_HANDLE, 0xFF7AA2F7);

        // ===== ไวยากรณ์ (จุดสำคัญที่ทำให้โค้ดดูสวย) =====
        setColor(KEYWORD, 0xFFBB9AF7);               // ม่วง — public, class, if, return
        setColor(IDENTIFIER_NAME, 0xFF7DCFFF);       // ฟ้าสว่าง — ชื่อเมธอด / ฟังก์ชัน
        setColor(IDENTIFIER_VAR, 0xFFC0CAF5);        // ขาวฟ้า — ตัวแปร
        setColor(LITERAL, 0xFFFF9E64);               // ส้ม — ตัวเลข
        setColor(OPERATOR, 0xFF89DDFF);              // ฟ้าอมเขียว — { } ( ) = ;
        setColor(COMMENT, 0xFF565F89);               // เทาม่วง — // comment
        setColor(ANNOTATION, 0xFFE0AF68);            // ทอง — @Override

        // string (บางเวอร์ชันใช้ TEXT_INJECTED / PROBLEM ฯลฯ)
        try {
            setColor(TEXT_INJECTED, 0xFF9ECE6A);     // เขียว — "string"
        } catch (Throwable ignored) {}

        // ===== วงเล็บ / block =====
        setColor(BLOCK_LINE, 0xFF3B4261);
        setColor(BLOCK_LINE_CURRENT, 0xFF7AA2F7);
        setColor(MATCHED_TEXT_BACKGROUND, 0xFF3D59A1);
        setColor(UNDERLINE, 0xFF7AA2F7);

        // ===== scrollbar =====
        setColor(SCROLL_BAR_THUMB, 0xFF3B4261);
        setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF7AA2F7);
        setColor(SCROLL_BAR_TRACK, 0xFF1A1B26);

        // ===== completion popup =====
        try {
            setColor(COMPLETION_WND_BACKGROUND, 0xFF1F2335);
            setColor(COMPLETION_WND_TEXT_PRIMARY, 0xFFC0CAF5);
            setColor(COMPLETION_WND_TEXT_SECONDARY, 0xFF565F89);
            setColor(COMPLETION_WND_ITEM_CURRENT, 0xFF2A2F41);
        } catch (Throwable ignored) {}

        // ===== error / warning =====
        try {
            setColor(DIAGNOSTIC_ERROR, 0xFFF7768E);
            setColor(DIAGNOSTIC_WARNING, 0xFFE0AF68);
            setColor(DIAGNOSTIC_TIP, 0xFF7DCFFF);
        } catch (Throwable ignored) {}
    }
}
package com.dev.ministudio.editor;

import io.github.rosemoe.sora.widget.schemes.EditorColorScheme;

/**
 * ธีมสีโค้ดแบบ VS Code Dark+ สำหรับ Nexus Studio
 */
public class NexusColorScheme extends EditorColorScheme {

    // กำหนด Constant ID สำหรับสีกำหนดเองที่ไม่มีในคลาสแม่ (EditorColorScheme)
    public static final int TEXT_INJECTED = 1001;
    public static final int DIAGNOSTIC_ERROR = 1002;
    public static final int DIAGNOSTIC_WARNING = 1003;
    public static final int DIAGNOSTIC_TIP = 1004;

    @Override
    public void applyDefault() {
        super.applyDefault();

        // พื้นหลัง / ข้อความทั่วไป
        setColor(WHOLE_BACKGROUND, 0xFF1E1E1E);
        setColor(TEXT_NORMAL, 0xFFD4D4D4);
        setColor(LINE_NUMBER, 0xFF858585);
        setColor(LINE_NUMBER_BACKGROUND, 0xFF1E1E1E);
        setColor(LINE_NUMBER_CURRENT, 0xFFC6C6C6);
        setColor(CURRENT_LINE, 0xFF2A2A2A);
        setColor(SELECTION_INSERT, 0xFFAEAFAD);
        setColor(SELECTION_HANDLE, 0xFF007ACC);
        setColor(SELECTED_TEXT_BACKGROUND, 0xFF264F78);
        setColor(TEXT_SELECTED, 0xFFFFFFFF);

        // ไฮไลต์ไวยากรณ์ (ใกล้ VS Code Dark+)
        setColor(KEYWORD, 0xFF569CD6);          // ฟ้า — if, class, public
        setColor(IDENTIFIER_NAME, 0xFFDCDCAA);  // เหลืองอ่อน — ชื่อเมธอด
        setColor(IDENTIFIER_VAR, 0xFF9CDCFE);   // ฟ้าอ่อน — ตัวแปร
        setColor(LITERAL, 0xFFB5CEA8);          // เขียวอ่อน — ตัวเลข
        setColor(COMMENT, 0xFF6A9955);          // เขียว — คอมเมนต์
        setColor(OPERATOR, 0xFFD4D4D4);         // เทาขาว — + - = ;
        setColor(ANNOTATION, 0xFFDCDCAA);       // เหลือง — @Override
        setColor(TEXT_INJECTED, 0xFFCE9178);    // ส้ม — string

        // วงเล็บคู่ / block
        setColor(BLOCK_LINE, 0xFF404040);
        setColor(BLOCK_LINE_CURRENT, 0xFF707070);
        setColor(MATCHED_TEXT_BACKGROUND, 0xFF613214);

        // สครอลบาร์ / เส้นแบ่ง
        setColor(SCROLL_BAR_THUMB, 0xFF424242);
        setColor(SCROLL_BAR_THUMB_PRESSED, 0xFF4F4F4F);
        setColor(SCROLL_BAR_TRACK, 0xFF1E1E1E);
        setColor(LINE_DIVIDER, 0xFF2B2B2B);

        // completion / diagnostic (ถ้ามี)
        setColor(COMPLETION_WND_BACKGROUND, 0xFF252526);
        setColor(COMPLETION_WND_TEXT_PRIMARY, 0xFFCCCCCC);
        setColor(COMPLETION_WND_TEXT_SECONDARY, 0xFF808080);
        setColor(COMPLETION_WND_ITEM_CURRENT, 0xFF04395E);

        setColor(DIAGNOSTIC_ERROR, 0xFFF44747);
        setColor(DIAGNOSTIC_WARNING, 0xFFCCA700);
        setColor(DIAGNOSTIC_TIP, 0xFF75BEFF);

        // sticky scroll / underline
        setColor(UNDERLINE, 0xFF569CD6);
        setColor(SIDE_BLOCK_LINE, 0xFF404040);
    }
}

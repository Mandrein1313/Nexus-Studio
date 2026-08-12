package com.dev.ministudio;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;

// เปลี่ยนจาก AISettingsActivity เป็น AiSettingsActivity
public class AiSettingsActivity extends AppCompatActivity { 

    private EditText etApiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_settings);

        etApiKey = findViewById(R.id.etApiKey);
        Button btnSave = findViewById(R.id.btnSaveApi);

        // 🌟 แก้ไขจุดสำคัญ: เปลี่ยนชื่อไฟล์จาก "AISettings" เป็น "ai_settings" ตัวเล็ก 
        // เพื่อให้ตรงกับตัวดึงค่าในคลาส GeminiAssistant ครับน้า
        SharedPreferences prefs =
                getSharedPreferences("ai_settings", MODE_PRIVATE);

        etApiKey.setText(
                prefs.getString("groq_api_key", "")
        );

        btnSave.setOnClickListener(v -> {

            String key = etApiKey.getText().toString().trim();

            prefs.edit()
                    .putString("groq_api_key", key)
                    .apply();

            Toast.makeText(
                    this,
                    "บันทึก API Key แล้ว",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        });
    }
}

package com.dev.ministudio;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.dev.ministudio.model.ProjectModel; 
import java.io.File;

public class TabAdapter extends RecyclerView.Adapter<TabAdapter.TabViewHolder> {

    private ProjectModel projectModel;
    private OnTabInterface listener; 

    // ยุบรวม Interface ให้จัดการได้ทั้งการคลิกดูและการกดปิดแท็บ
    public interface OnTabInterface {
        void onTabClick(File file);
        void onTabClose(File file, int position);
    }

    public TabAdapter(ProjectModel projectModel, OnTabInterface listener) {
        this.projectModel = projectModel;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab, parent, false);
        return new TabViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        File file = projectModel.getOpenedFiles().get(position);
        holder.tvTabName.setText(file.getName());

        // ตรวจสอบสถานะว่าใช่ไฟล์ที่กำลังเปิดอ่านอยู่หรือไม่
        if (file.equals(projectModel.getCurrentOpenFile())) {
            // ดึง XML drawable ขีดเส้นใต้สีม่วงที่เราสร้างไว้มาใช้
            holder.itemView.setBackgroundResource(R.drawable.tab_active_bg);
            holder.tvTabName.setTextColor(Color.parseColor("#BB86FC")); // ชื่อไฟล์สีม่วงเด่นชัด
            holder.btnCloseTab.setColorFilter(Color.parseColor("#BB86FC")); // ปุ่ม X สีม่วงเข้าคู่กัน
        } else {
            // แท็บปกติที่ไม่ได้เลือกใช้สีมืดเรียบๆ
            holder.itemView.setBackgroundColor(Color.parseColor("#1D1D1D")); 
            holder.tvTabName.setTextColor(Color.parseColor("#888888")); // สีตัวอักษรจางลง
            holder.btnCloseTab.setColorFilter(Color.parseColor("#555555"));
        }

        // กดที่ตัวแท็บเพื่อสลับหน้าจอไปอ่านไฟล์นั้นๆ
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTabClick(file);
        });

        // 🟢 ระบบกดปุ่ม X เพื่อปิดแท็บแบบสมบูรณ์!
        holder.btnCloseTab.setOnClickListener(v -> {
            if (listener != null) {
                // ส่งตำแหน่งและไฟล์กลับไปให้ MainActivity สั่งทำลายแท็บ
                listener.onTabClose(file, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return projectModel.getOpenedFiles() != null ? projectModel.getOpenedFiles().size() : 0;
    }

    public static class TabViewHolder extends RecyclerView.ViewHolder {
        TextView tvTabName;
        ImageButton btnCloseTab;

        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTabName = itemView.findViewById(R.id.tvTabName);
            btnCloseTab = itemView.findViewById(R.id.btnCloseTab);
        }
    }
}

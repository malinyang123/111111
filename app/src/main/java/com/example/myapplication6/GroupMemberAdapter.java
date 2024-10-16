package com.example.myapplication6;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<User> groupMembers;
    private Consumer<User> editClickListener;
    private Consumer<User> deleteClickListener;

    public GroupMemberAdapter(List<User> groupMembers, Consumer<User> editClickListener, Consumer<User> deleteClickListener) {
        this.groupMembers = groupMembers;
        this.editClickListener = editClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public GroupMemberAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    // 合并后的 onBindViewHolder 方法
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = groupMembers.get(position);
        String remark = user.getGroups().get(user.username);
        holder.contactPeople.setText(user.username + " - " + remark);

        // 拨打电话按钮点击事件
        holder.btnCall.setOnClickListener(v -> {
            // 拨打电话
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + user.username));
            v.getContext().startActivity(intent);
        });

        // 编辑按钮点击事件
        holder.btnEdit.setOnClickListener(v -> {
            if (editClickListener != null) {
                editClickListener.accept(user);
            }
        });

        // 删除按钮点击事件
        holder.btnDelete.setOnClickListener(v -> {
            if (deleteClickListener != null) {
                deleteClickListener.accept(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView contactPeople;
        ImageButton btnCall;
        Button btnEdit;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            contactPeople = itemView.findViewById(R.id.contact_people);
            btnCall = itemView.findViewById(R.id.btn_call);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
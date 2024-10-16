package com.example.myapplication6;

import java.util.HashMap;
import java.util.Map;

public class User {
    public String username;
    public String password;
    public double latitude = -1;
    public double longitude = -1;
    public Map<String, String> groups = new HashMap<>();

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void setLocation(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUsername() {
        return username;
    }

    // 新增方法：添加关联用户及备注
    public void addGroupMember(String phoneNumber, String remark) {
        groups.put(phoneNumber, remark);
    }

    // 新增方法：获取关联用户列表
    public Map<String, String> getGroups() {
        return groups;
    }

    // 新增方法：更新备注
    public void updateRemark(String phoneNumber, String newRemark) {
        if (groups.containsKey(phoneNumber)) {
            groups.put(phoneNumber, newRemark);
        }
    }

    // 新增方法：删除关联用户
    public void removeGroupMember(String phoneNumber) {
        groups.remove(phoneNumber);
    }
}
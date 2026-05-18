package com.example.demo.dto;

import java.util.List;

public class EscapeRequest {
    private String userId;
    private List<Integer> itemIds; // 持ち帰ったアイテムIDのリスト（例: [1, 1, 2]）

    // ゲッターとセッター
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public List<Integer> getItemIds() { return itemIds; }
    public void setItemIds(List<Integer> itemIds) { this.itemIds = itemIds; }
}

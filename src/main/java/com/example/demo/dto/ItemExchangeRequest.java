package com.example.demo.dto;

public class ItemExchangeRequest {
    private Integer itemId; // UnityのItemMasterのId (1, 2, 3)
    private Integer count;  // プレイヤーが獲得した個数

    // ゲッターとセッター
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}

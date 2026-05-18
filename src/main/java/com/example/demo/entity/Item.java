package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "items") // 💡 必ずSupabaseの実テーブル名「items」を指定してください
public class Item {
    
    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "rate")
    private Integer rate;

    // 以下、Getter / Setter
}


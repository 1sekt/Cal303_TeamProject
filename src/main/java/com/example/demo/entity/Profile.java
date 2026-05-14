package com.example.demo.entity; // 自分のパッケージ名に合わせてください

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "profiles")
@Getter @Setter
@NoArgsConstructor  // これを追加：引数なしのコンストラクタを自動作成
@AllArgsConstructor // これを追加：全フィールドを引数に持つコンストラクタを自動作成
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    private Integer level = 1;

    @Column(columnDefinition = "text") // H2と互換性のため一旦text
    private String inventory = "{}";
}

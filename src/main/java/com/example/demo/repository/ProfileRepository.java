package com.example.demo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {
    // ユーザー名で検索するためのメソッドを定義（これだけで自動実装されます！）
    Optional<Profile> findByUsername(String username);
}

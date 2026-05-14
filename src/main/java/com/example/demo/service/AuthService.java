package com.example.demo.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Profile;
import com.example.demo.repository.ProfileRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final ProfileRepository profileRepository;
    // パスワードを暗号化・照合するためのツール
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // サインアップ処理
    public String signUp(String username, String password) {
        // 同じ名前のユーザーがいないかチェック
        if (profileRepository.findByUsername(username).isPresent()) {
            return "Error: User ID already exists.";
        }

        Profile profile = new Profile();
        profile.setUsername(username);
        // パスワードをそのまま保存せず、暗号化（ハッシュ化）して保存！
        profile.setPasswordHash(passwordEncoder.encode(password));
        
        profileRepository.save(profile);
        return "Success: User registered!";
    }

    // ログイン処理
    public Optional<Profile> login(String username, String password) {
        return profileRepository.findByUsername(username)
            .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }
}

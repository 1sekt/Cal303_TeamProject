package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Profile;
import com.example.demo.service.AuthService;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    // application.properties から Supabase の JWT Secret を読み込む
    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @PostMapping("/signup")
    public Map<String, String> signUp(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String result = authService.signUp(username, password);
        return Map.of("message", result);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Optional<Profile> user = authService.login(username, password);
        
        if (user.isPresent()) {
            // ログインしたユーザーの固有ID（UUID）を取得
            String userUuid = user.get().getId().toString();

            // 🔐 Supabaseの秘密鍵を使って本物の暗号化JWTトークンを生成（有効期限24時間）
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            String realToken = Jwts.builder()
                    .setSubject(userUuid) // トークンの持ち主にUUIDを刻印
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();

            // Unity側へ本物の暗号化JWTを返却
            return Map.of(
                "message", "ログイン成功！", 
                "token", realToken
            );
        } else {
            return Map.of("message", "IDまたはパスワードが違います");
        }
    }
}

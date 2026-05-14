package com.example.demo.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Profile;
import com.example.demo.service.AuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

@CrossOrigin(origins = "*", allowedHeaders = "*")
public class AuthController {

    private final AuthService authService;

    // サインアップ受付窓口
    // Unityから https://your-app/api/auth/signup にPOSTされると動く
    @PostMapping("/signup")
    public Map<String, String> signUp(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        String result = authService.signUp(username, password);
        return Map.of("message", result);
    }

    // ログイン受付窓口
    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Optional<Profile> user = authService.login(username, password);
        
        if (user.isPresent()) {
            // 本来はここでJWT（トークン）を発行して返しますが、
            // まずはログイン成功のメッセージを返します
            return Map.of("message", "ログイン成功！", "token", "dummy-token-for-now");
        } else {
            return Map.of("message", "IDまたはパスワードが違います");
        }
    }
}

package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Score;
import com.example.demo.repository.ScoreRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*", allowedHeaders = "*") // WebGLからのアクセス許可
public class ScoreController {

    @Autowired
    private ScoreRepository scoreRepository;

    // 🔐 application.properties から Supabase の JWT Secret を読み込みます
    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    // 1. スコア保存API (POST /api/score/submit)
    @PostMapping("/submit")
    public ResponseEntity<?> submitScore(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, Integer> body) {
        try {
            // JWTから安全にUUID（ユーザーID）を取り出す
            UUID userId = extractUserIdFromToken(token);

            Score scoreEntity = new Score();
            scoreEntity.setUserId(userId);
            scoreEntity.setScore(body.get("score"));
            
            scoreRepository.save(scoreEntity);

            return ResponseEntity.ok("{\"message\":\"Score saved successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid token: " + e.getMessage() + "\"}");
        }
    }

    // 2. ハイスコアと履歴取得API (GET /api/score/history)
    @GetMapping("/history")
    public ResponseEntity<?> getScoreHistory(@RequestHeader("Authorization") String token) {
        try {
            // JWTから安全にUUID（ユーザーID）を取り出す
            UUID userId = extractUserIdFromToken(token);

            Integer highScore = scoreRepository.findHighScoreByUserId(userId);
            if (highScore == null) highScore = 0;

            // 最新10件の履歴を取得
            List<Score> historyEntities = scoreRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
            List<Integer> scoreHistory = historyEntities.stream()
                    .map(Score::getScore)
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("highScore", highScore);
            response.put("history", scoreHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid token: " + e.getMessage() + "\"}");
        }
    }

    // 💡 届いた暗号化JWTが本物か検証し、中のUUIDを安全にトリミングして取り出すロジック
    private UUID extractUserIdFromToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            throw new IllegalArgumentException("Authorizationヘッダーが空です");
        }

        // 送信形式のズレに対応するため、大文字小文字を無視して「bearer」を削り、前後の空白を除去
        String token = authorizationHeader.toLowerCase().replace("bearer", "").trim();
        
        // 元の文字列から、正確な大文字小文字のトークン部分（eY...）だけを切り出す
        if (authorizationHeader.length() > 7 && authorizationHeader.substring(0, 7).equalsIgnoreCase("bearer ")) {
            token = authorizationHeader.substring(7).trim();
        }

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        
        // 署名が偽装されていないか検証しながらパース（暗号解除）
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        // JWTの「sub（Subject）」に刻印されている本物のユーザーUUID文字列を取得
        String userIdStr = claims.getSubject();
        return UUID.fromString(userIdStr);
    }
}

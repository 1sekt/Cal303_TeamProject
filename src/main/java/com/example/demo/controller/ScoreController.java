package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*", allowedHeaders = "*") // WebGLからのアクセス許可
public class ScoreController {

    @Autowired
    private ScoreRepository scoreRepository;

    // 1. スコア保存API (POST /api/score/submit)
    @PostMapping("/submit")
    public ResponseEntity<?> submitScore(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, Integer> body) {
        try {
            // Unityから「Bearer <UUID>」の形で送られてくるため、文字を整形してUUIDに変換
            String uuidStr = token.replace("Bearer ", "").trim();
            UUID userId = UUID.fromString(uuidStr);

            Score scoreEntity = new Score();
            scoreEntity.setUserId(userId);
            scoreEntity.setScore(body.get("score"));
            
            scoreRepository.save(scoreEntity);

            return ResponseEntity.ok("{\"message\":\"Score saved successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid user ID format: " + e.getMessage() + "\"}");
        }
    }

    // 2. ハイスコアと履歴取得API (GET /api/score/history)
    @GetMapping("/history")
    public ResponseEntity<?> getScoreHistory(@RequestHeader("Authorization") String token) {
        try {
            String uuidStr = token.replace("Bearer ", "").trim();
            UUID userId = UUID.fromString(uuidStr);

            Integer highScore = scoreRepository.findHighScoreByUserId(userId);
            if (highScore == null) highScore = 0; // まだデータがない場合

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
                    .body("{\"error\":\"Invalid user ID format: " + e.getMessage() + "\"}");
        }
    }
}

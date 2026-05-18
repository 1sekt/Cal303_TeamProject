package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.EscapeRequest;
import com.example.demo.dto.ScoreResultResponse;
import com.example.demo.service.ScoreService;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*") // Unity WebGL(ブラウザ)のCORSエラーを防ぐために必須
public class GameController {

    @Autowired
    private ScoreService scoreService;

    @PostMapping("/escape")
    public ResponseEntity<?> processEscape(@RequestBody EscapeRequest request) {
        try {
            // 例外チェック（ユーザーIDが空の場合は弾く）
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                return ResponseEntity.badRequest().body("ユーザーIDが空です");
            }

            // スコアの計算、保存、取得処理を実行
            ScoreResultResponse result = scoreService.processEscapeAndGetResults(request.getUserId(), request.getItemIds());
            
            // UnityへHTTP 200 (OK) とJSONデータを返却
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            // 万が一サーバー側でエラーが起きた場合は500エラーを返す
            e.printStackTrace();
            return ResponseEntity.status(500).body("サーバーエラーが発生しました: " + e.getMessage());
        }
    }
}

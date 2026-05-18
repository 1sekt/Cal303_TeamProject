package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
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
    
    @Autowired
    private JdbcTemplate jdbcTemplate; // 🎯 追加：SQLを直接叩くための設定

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

    
    // 2. ハイスコアと履歴取得API (GET /api/score/history) の【完全修正版】
    @GetMapping("/history")
    public ResponseEntity<?> getScoreHistory(@RequestHeader("Authorization") String token) {
        try {
            // JWTから安全にUUID（ユーザーID）を取り出す
            UUID userId = extractUserIdFromToken(token);

            // 🎯【ここを修正】リポジトリ(JPA)ではなく、jdbcTemplateを使ってSupabaseから生の最新ハイスコアを直撃クエリする
            String selectScoreSql = "SELECT score FROM scores WHERE user_id = CAST(? AS UUID)";
            Integer highScore = 0;
            try {
                highScore = jdbcTemplate.queryForObject(selectScoreSql, Integer.class, userId.toString());
            } catch (Exception e) {
                highScore = 0; // まだデータがない新規ユーザーの場合は0
            }

            // 履歴リストの取得（1回目、2回目と並べるため、古い順 ASC で取得）
            String selectHistorySql = "SELECT score_earned FROM score_histories WHERE user_id = CAST(? AS UUID) ORDER BY cleared_at ASC";
            List<Integer> scoreHistory = jdbcTemplate.queryForList(selectHistorySql, Integer.class, userId.toString());

            Map<String, Object> response = new HashMap<>();
            response.put("highScore", highScore);
            response.put("history", scoreHistory);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid token: " + e.getMessage() + "\"}");
        }
    }


    
    // 3. 🎯 新設：アイテムIDリストを受け取り換金・保存して最新データを返すAPI
    @PostMapping("/escape")
    @Transactional // 処理の途中でエラーが起きたら自動でデータベースを巻き戻す
    public ResponseEntity<?> processEscape(
            @RequestHeader("Authorization") String token, 
            @RequestBody Map<String, List<Integer>> body) {
        try {
            // 🔒 既存の最強セキュアロジックをそのまま流用してUUIDを取得
            UUID userId = extractUserIdFromToken(token);

            // Unityから送られてきた {"itemIds": [1, 2, 3]} のリストを取得
            List<Integer> itemIds = body.get("itemIds");
            int currentEarned = 0;

            // 1. 持ち帰ったアイテムIDから、今回の獲得スコア（金額）を合算
            if (itemIds != null && !itemIds.isEmpty()) {
                for (Integer itemId : itemIds) {
                    String sql = "SELECT rate FROM items WHERE id = ?";
                    try {
                        // itemsテーブルから1件ずつ換金レートを取得
                        Integer rate = jdbcTemplate.queryForObject(sql, Integer.class, itemId);
                        if (rate != null) {
                            currentEarned += rate;
                        }
                    } catch (Exception e) {
                        // DBにない不正なアイテムID（チート等）が送られてきた場合は安全にスルー
                        System.out.println("警告: 存在しないアイテムIDをスキップしました: " + itemId);
                    }
                }
            }

            // 2. 既存の scores テーブルを累積加算（UPSERT処理）
            // user_idが一意（UNIQUE制約）なので、既存なら加算、新規なら作成される
            String upsertSql = 
                "INSERT INTO scores (user_id, score) VALUES (CAST(? AS UUID), ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET score = scores.score + EXCLUDED.score";
            jdbcTemplate.update(upsertSql, userId.toString(), currentEarned);

            // 3. 新設した score_histories テーブルへ今回のクリア履歴を挿入
            String insertHistorySql = "INSERT INTO score_histories (user_id, score_earned) VALUES (CAST(? AS UUID), ?)";
            jdbcTemplate.update(insertHistorySql, userId.toString(), currentEarned);

            // 4. Unity側の「ScoreHistoryResponse」のデータ構造に合わせて、最新の情報をDBから再取得
            // 最新のハイスコア（累積スコア）を既存のリポジトリから取得
            Integer highScore = scoreRepository.findHighScoreByUserId(userId);
            if (highScore == null) highScore = 0;

            // 直近5件の履歴を新設テーブルから新しい順で取得
            String selectHistorySql = "SELECT score_earned FROM score_histories WHERE user_id = CAST(? AS UUID) ORDER BY cleared_at DESC LIMIT 5";
            List<Integer> history = jdbcTemplate.queryForList(selectHistorySql, Integer.class, userId.toString());

            // UnityのScoreHistoryResponseクラス（JSON）と100%一致するMapを作成
            Map<String, Object> response = new HashMap<>();
            response.put("currentEarned", currentEarned); // 今回のスコア
            response.put("highScore", highScore);        // 累積ハイスコア
            response.put("history", history);            // 履歴5件のリスト

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"Invalid processing: " + e.getMessage() + "\"}");
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

package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ScoreResultResponse;

@Service
public class ScoreService {

    @Autowired
    private JdbcTemplate jdbcTemplate; // Supabaseへの接続に使用

    @Transactional // 処理の途中でエラーが起きたら自動でロールバック（巻き戻し）する設定
    public ScoreResultResponse processEscapeAndGetResults(String userId, List<Integer> itemIds) {
        int currentEarned = 0;

        // 1. 持ち帰ったアイテムIDから今回の獲得スコアを合算
        if (itemIds != null && !itemIds.isEmpty()) {
            for (Integer itemId : itemIds) {
                String sql = "SELECT rate FROM items WHERE id = ?";
                try {
                    // itemsテーブルから1件ずつレートを引く
                    Integer rate = jdbcTemplate.queryForObject(sql, Integer.class, itemId);
                    if (rate != null) {
                        currentEarned += rate;
                    }
                } catch (Exception e) {
                    // 一致するアイテムIDがない（不正データなど）の場合はログを出してスルー
                    System.out.println("警告: 存在しないアイテムIDです: " + itemId);
                }
            }
        }

        // 2. 既存の scores テーブルに対して累積加算（UPSERT処理）
        // 🎯 修正：EXCLUDED に頼るのをやめ、3番目の「?」をSQL文に新しく明示的に作成します。
        // これにより、Javaの currentEarned が確実にダイレクトに天秤（GREATEST）にかけられます。
        String upsertSql = 
            "INSERT INTO scores (user_id, score) VALUES (CAST(? AS UUID), ?) " +
            "ON CONFLICT (user_id) DO UPDATE SET score = GREATEST(scores.score, ?)";

        // 💡 引数の順番に注目：1番目がuserId、2番目が今回のスコア、3番目にも今回のスコアをもう一度渡します
        jdbcTemplate.update(upsertSql, userId, currentEarned, currentEarned);


        // 3. 新しく作った score_histories テーブルへ今回のクリア履歴を挿入
        String insertHistorySql = "INSERT INTO score_histories (user_id, score_earned) VALUES (CAST(? AS UUID), ?)";
        jdbcTemplate.update(insertHistorySql, userId, currentEarned);

        // 4. Unityに返すために、最新の「累積ハイスコア」と「履歴5件」をSupabaseから再取得
        // 累積スコアの取得
        String selectScoreSql = "SELECT score FROM scores WHERE user_id = CAST(? AS UUID)";
        int totalHighScore = jdbcTemplate.queryForObject(selectScoreSql, Integer.class, userId);

        // 過去履歴5件を、新しい順（cleared_at DESC）で取得
        String selectHistorySql = "SELECT score_earned FROM score_histories WHERE user_id = CAST(? AS UUID) ORDER BY cleared_at DESC LIMIT 5";
        List<Integer> history = jdbcTemplate.queryForList(selectHistorySql, Integer.class, userId);

        // まとめてDTOに詰めて返却
        return new ScoreResultResponse(currentEarned, totalHighScore, history);
    }
}

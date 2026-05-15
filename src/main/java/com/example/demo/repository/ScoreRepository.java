package com.example.demo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Score;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    
    // 💡 特定のユーザーの最新スコア履歴を日付順に最大10件取得する（JPAがメソッド名から自動生成）
    List<Score> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    // 💡 特定のユーザーの最高スコア（ハイスコア）を取得するカスタムクエリ
    @Query("SELECT MAX(s.score) FROM Score s WHERE s.userId = :userId")
    Integer findHighScoreByUserId(@Param("userId") UUID userId);
}

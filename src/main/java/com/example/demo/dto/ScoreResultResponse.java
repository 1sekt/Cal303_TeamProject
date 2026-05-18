package com.example.demo.dto;

import java.util.List;

public class ScoreResultResponse {
    private int currentEarned;    // 今回のクリアで稼いだスコア
    private int totalHighScore;   // 最新の累積（ハイ）スコア
    private List<Integer> history; // 直近5件の履歴

    // コンストラクタ
    public ScoreResultResponse(int currentEarned, int totalHighScore, List<Integer> history) {
        this.currentEarned = currentEarned;
        this.totalHighScore = totalHighScore;
        this.history = history;
    }

    // ゲッターとセッター
    public int getCurrentEarned() { return currentEarned; }
    public void setCurrentEarned(int currentEarned) { this.currentEarned = currentEarned; }
    public int getTotalHighScore() { return totalHighScore; }
    public void setTotalHighScore(int totalHighScore) { this.totalHighScore = totalHighScore; }
    public List<Integer> getHistory() { return history; }
    public void setHistory(List<Integer> history) { this.history = history; }
}

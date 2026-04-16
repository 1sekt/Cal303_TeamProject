using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class Enemy : MonoBehaviour
{
    [Header("HP")]
    public int maxHP = 100;
    private int currentHP;

    private void Start()
    {
        currentHP = maxHP;
    }

    // ★ ダメージを受ける関数
    public void TakeDamage(int damage)
    {
        currentHP -= damage;

        Debug.Log("敵HP: " + currentHP);

        if (currentHP <= 0)
        {
            Die();
        }
    }

    void Die()
    {
        Debug.Log("敵を撃破！");
        Destroy(gameObject);
    }
}
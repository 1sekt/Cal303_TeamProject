using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class EnemyAI : MonoBehaviour
{
    [Header("設定")]
    public Transform player;          // プレイヤーのTransform
    public float moveSpeed = 3f;      // 移動速度
    public float stopDistance = 5f;   // プレイヤーから離れて止まる距離
    public float attackRange = 7f;    // 攻撃を開始する射程

    [Header("攻撃設定")]
    public GameObject bulletPrefab;   // 弾のプレハブ
    public Transform firePoint;       // 弾の発射口
    public float fireRate = 1.0f;     // 攻撃頻度（秒）
    private float nextFireTime;

    void Start()
    {
        // プレイヤーがセットされていない場合、タグで探す
        if (player == null)
            player = GameObject.FindGameObjectWithTag("Player").transform;
    }

    void Update()
    {
        if (player == null) return;

        // プレイヤーとの距離を計算
        float distance = Vector3.Distance(transform.position, player.position);

        // 1. プレイヤーの方向を向く
        LookAtPlayer();

        // 2. 移動：停止距離より遠ければ近づく
        if (distance > stopDistance)
        {
            MoveToPlayer();
        }

        // 3. 攻撃：射程内かつクールタイムが終わっていれば発射
        if (distance <= attackRange && Time.time >= nextFireTime)
        {
            Attack();
            nextFireTime = Time.time + fireRate;
        }
    }

    void LookAtPlayer()
    {
        Vector3 direction = (player.position - transform.position).normalized;
        // 見下ろし型なのでY軸（上下）の回転は固定する
        direction.y = 0;
        if (direction != Vector3.zero)
        {
            Quaternion lookRotation = Quaternion.LookRotation(direction);
            transform.rotation = Quaternion.Slerp(transform.rotation, lookRotation, Time.deltaTime * 10f);
        }
    }

    void MoveToPlayer()
    {
        // ターゲットの座標を取得し、高さ(Y)だけ自分の高さに上書きする
        Vector3 targetPos = player.position;
        targetPos.y = transform.position.y;

        transform.position = Vector3.MoveTowards(transform.position, targetPos, moveSpeed * Time.deltaTime);
    }

    void Attack()
    {
        if (bulletPrefab != null && firePoint != null)
        {
            // 弾を生成して発射
            Instantiate(bulletPrefab, firePoint.position, firePoint.rotation);
        }
    }
}
using UnityEngine;

public class EnemyBullet : MonoBehaviour
{
    public float damage = 10f; // 1発のダメージ量
    public float speed = 10f;

    void Start()
    {
        // 弾を前方に飛ばす
        GetComponent<Rigidbody>().velocity = transform.forward * speed;
        // 3秒後に消滅
        Destroy(gameObject, 3f);
    }

    private void OnTriggerEnter(Collider other)
    {
        // ぶつかった相手のタグが "Player" だったら
        if (other.CompareTag("Player"))
        {
            // PlayerSimpleスクリプトを探してダメージ関数を呼ぶ
            PlayerSimple player = other.GetComponent<PlayerSimple>();
            if (player != null)
            {
                player.TakeDamage(damage);
            }

            // 弾を消す
            Destroy(gameObject);
        }
    }
}

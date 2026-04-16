using UnityEngine;

public class Bullet : MonoBehaviour
{
    public float speed = 20f;

    void Update()
    {
        transform.Translate(Vector3.forward * speed * Time.deltaTime);
    }

    private void OnTriggerEnter(Collider other)
    {
        // PlayerSimpleを探す
        PlayerSimple player = FindObjectOfType<PlayerSimple>();

        if (player != null)
        {
            player.DealDamage(other.gameObject);
        }

        Destroy(gameObject);
    }
}
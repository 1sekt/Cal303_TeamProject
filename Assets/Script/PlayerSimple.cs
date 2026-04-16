using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.InputSystem;
using TMPro;
using UnityEngine.UI; // ★HPバー(Slider)を使うために追加

public class PlayerSimple : MonoBehaviour
{
    public GameObject bulletPrefab;
    public Transform muzzle;
    public UICrosshair crosshair;
    [Header("Sound")]
    public AudioSource audioSource;
    public AudioClip shootSound;
    public AudioClip reloadSound;
    [Header("Hit Sound")]
    public AudioClip hitSound;
    [Header("Move")]
    public float walkSpeed = 5.0f;
    public float dashSpeed = 20.0f;
    public TextMeshProUGUI ammoText;

    [Header("HP")] // ★HP関連の変数を追加
    public float maxHp = 100f;
    public float currentHp;
    public Slider hpSlider; // ★Unity上のHPバー（スライダー）を紐付ける
    public AudioClip damageSound; // ★ダメージを受けた時の音

    private Vector2 moveInput;
    private bool isDashing = false;

    [Header("Ammo")]
    public int currentAmmo = 10;
    public int maxAmmo = 10;
    public int reserveAmmo = 50;
    public float reloadTime = 1.5f;

    [Header("Shooting Settings")]
    public float fireRate = 0.2f;
    private float nextFireTime = 0f;
    private bool isFiring = false;

    [Header("Rolling")]
    public float rollSpeed = 15f;
    public float rollDuration = 0.5f;
    private float rollTimer;
    private bool isRolling = false;
    private Vector3 rollDir;

    private bool isReloading = false;

    void Start()
    {
        currentHp = maxHp; // ★HPを全快で開始
        UpdateAmmoUI();
        UpdateHpUI(); // ★HPバーを更新
    }

    // ★ダメージを受ける処理
    public void TakeDamage(float damage)
    {
        if (isRolling) return; // 回避中は無敵（ルートシューターの定番！）

        currentHp -= damage;
        UpdateHpUI();

        if (audioSource != null && damageSound != null)
        {
            audioSource.PlayOneShot(damageSound);
        }

        if (currentHp <= 0)
        {
            Die();
        }
    }

    void Die()
    {
        Debug.Log("ゲームオーバー");
        // ここにリスタート処理やJavaへのデータ送信（敗北ログ）などを後で書きます
    }

    // 敵にダメージを与える処理
    public void DealDamage(GameObject target)
    {
        Enemy enemy = target.GetComponent<Enemy>();
        if (enemy != null)
        {
            enemy.TakeDamage(10);
            if (audioSource != null && hitSound != null)
            {
                audioSource.PlayOneShot(hitSound);
            }
            if (crosshair != null)
            {
                crosshair.ShowHit();
            }
        }
    }

    void Update()
    {
        if (isRolling)
        {
            transform.position += rollDir * rollSpeed * Time.deltaTime;
            rollTimer -= Time.deltaTime;
            if (rollTimer <= 0) isRolling = false;
            return;
        }

        float currentSpeed = isDashing ? dashSpeed : walkSpeed;
        Vector3 dir = new Vector3(moveInput.x, 0f, moveInput.y).normalized;
        transform.position += dir * currentSpeed * Time.deltaTime;

        Vector3 mouseScreenPos = Input.mousePosition;
        mouseScreenPos.z = Camera.main.transform.position.y;
        Vector3 mouseWorldPos = Camera.main.ScreenToWorldPoint(mouseScreenPos);
        Vector3 targetDir = mouseWorldPos - transform.position;
        targetDir.y = 0;

        if (targetDir != Vector3.zero)
        {
            float angle = Mathf.Atan2(targetDir.x, targetDir.z) * Mathf.Rad2Deg;
            transform.rotation = Quaternion.Euler(0, angle, 0);
        }

        if (isFiring && !isReloading && currentAmmo > 0 && Time.time >= nextFireTime)
        {
            Shoot();
            nextFireTime = Time.time + fireRate;
        }

        if (Keyboard.current.rKey.wasPressedThisFrame)
        {
            TryReload();
        }

        if (Keyboard.current.spaceKey.wasPressedThisFrame && moveInput != Vector2.zero)
        {
            StartRoll();
        }
    }

    void StartRoll()
    {
        isRolling = true;
        rollTimer = rollDuration;
        rollDir = new Vector3(moveInput.x, 0f, moveInput.y).normalized;
    }

    void OnMove(InputValue value)
    {
        moveInput = value.Get<Vector2>();
    }

    void OnDash(InputValue value)
    {
        isDashing = value.isPressed;
    }

    void OnFire(InputValue value)
    {
        isFiring = value.isPressed;
    }

    void Shoot()
    {
        currentAmmo--;
        UpdateAmmoUI();
        if (muzzle != null && bulletPrefab != null)
        {
            Instantiate(bulletPrefab, muzzle.position, muzzle.rotation);
        }
        if (audioSource != null && shootSound != null)
        {
            audioSource.PlayOneShot(shootSound);
        }
    }

    void TryReload()
    {
        if (isReloading || isRolling) return;
        if (currentAmmo >= maxAmmo) return;
        if (reserveAmmo <= 0) return;
        StartCoroutine(ReloadCoroutine());
    }

    IEnumerator ReloadCoroutine()
    {
        isReloading = true;
        if (audioSource != null && reloadSound != null)
        {
            audioSource.PlayOneShot(reloadSound);
        }
        yield return new WaitForSeconds(reloadTime);
        int need = maxAmmo - currentAmmo;
        int loadAmount = Mathf.Min(need, reserveAmmo);
        currentAmmo += loadAmount;
        reserveAmmo -= loadAmount;
        isReloading = false;
        UpdateAmmoUI();
    }

    void UpdateAmmoUI()
    {
        if (ammoText != null)
        {
            ammoText.text = currentAmmo + " / " + reserveAmmo;
        }
    }

    // ★HPバーの更新
    void UpdateHpUI()
    {
        if (hpSlider != null)
        {
            hpSlider.maxValue = maxHp;
            hpSlider.value = currentHp;
        }
    }
}
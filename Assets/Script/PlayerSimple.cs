using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.InputSystem;
using TMPro;

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
    [Header("Mone")]
    public float walkSpeed = 5.0f;
    public float dashSpeed = 20.0f;
    public TextMeshProUGUI ammoText;
    private Vector2 moveInput;

    private bool isDashing = false;

    [Header("Ammo")]
    public int currentAmmo = 10;
    public int maxAmmo = 10;
    public int reserveAmmo = 50;
    public float reloadTime = 1.5f;

    [Header("Shooting Settings")]
    public float fireRate = 0.2f; // 連射速度 (0.2秒に1発)
    private float nextFireTime = 0f; // 次に撃てる時刻
    private bool isFiring = false; // 今マウスを押しているか

    private bool isReloading = false;

    void Start()
    {
        UpdateAmmoUI();
    }

    // 敵にダメージを与える処理（中央管理）
    public void DealDamage(GameObject target)
{
    Enemy enemy = target.GetComponent<Enemy>();

    if (enemy != null)
    {
        enemy.TakeDamage(10);
        // 🔊 ヒット音
    if (audioSource != null && hitSound != null)
    {
    audioSource.PlayOneShot(hitSound);
    }

        // 🔥 ヒット時クロスヘア変更
        if (crosshair != null)
        {
            crosshair.ShowHit();
        }
    }
}

    void Update()
    {
        // 1. 移動
        float currentSpeed = isDashing ? dashSpeed : walkSpeed;

        Vector3 dir = new Vector3(moveInput.x, 0f, moveInput.y).normalized;
        transform.position += dir * currentSpeed * Time.deltaTime;

        // 2. マウス方向
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

        // 3. 連射の判定
        if (isFiring && !isReloading && currentAmmo > 0 && Time.time >= nextFireTime)
        {
            Shoot();
            nextFireTime = Time.time + fireRate;
        }

        // Rキーでリロード
        if (Keyboard.current.rKey.wasPressedThisFrame)
        {
            TryReload();
        }
    }

    void OnMove(InputValue value)
    {
        moveInput = value.Get<Vector2>();
    }

    void OnDash(InputValue value)
    {
        isDashing = value.isPressed;
        Debug.Log("Dash入力:" + isDashing);
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

        Debug.Log("残弾: " + currentAmmo);
    }

    void TryReload()
    {
        if (isReloading) return;
        if (currentAmmo >= maxAmmo) return;
        if (reserveAmmo <= 0) return;

        StartCoroutine(ReloadCoroutine());
    }

    IEnumerator ReloadCoroutine()
    {
        isReloading = true;
        Debug.Log("リロード開始");

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

        Debug.Log("リロード完了");
    }

    void UpdateAmmoUI()
    {
        if (ammoText != null)
        {
            ammoText.text = currentAmmo + " / " + reserveAmmo;
        }
    }
}
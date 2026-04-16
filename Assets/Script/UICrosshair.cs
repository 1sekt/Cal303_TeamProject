using UnityEngine;
using UnityEngine.UI;

public class UICrosshair : MonoBehaviour
{
    public Image crosshairImage;

    public Sprite normalSprite;
    public Sprite hitSprite;

    private float hitTimer = 0f;
    public float hitDuration = 0.05f;

    void Start()
    {
        SetNormal();
    }

    void Update()
    {
        if (hitTimer > 0f)
        {
            hitTimer -= Time.deltaTime;

            if (hitTimer <= 0f)
            {
                SetNormal();
            }
        }
    }

    public void ShowHit()
    {
        if (crosshairImage == null || hitSprite == null) return;

        crosshairImage.sprite = hitSprite;
        hitTimer = hitDuration;
    }

    void SetNormal()
    {
        if (crosshairImage == null || normalSprite == null) return;

        crosshairImage.sprite = normalSprite;
    }
}
using UnityEngine;
using UnityEngine.InputSystem;

public class UICrosshairFollow : MonoBehaviour
{
    [SerializeField] private RectTransform crosshairRect;

    private void Start()
    {
        Cursor.visible = false;
    }

    private void Update()
    {
        if (crosshairRect == null) return;
        if (Mouse.current == null) return;

        crosshairRect.position = Mouse.current.position.ReadValue();
    }
}
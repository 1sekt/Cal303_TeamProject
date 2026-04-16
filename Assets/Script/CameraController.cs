using System.Collections;
using System.Collections.Generic;
using UnityEngine;

public class CameraController : MonoBehaviour
{
    public Transform target; // 追いかける対象（Player）

    // Updateだとガタつくことがあるので、移動後に処理するLateUpdateを使います
    void LateUpdate()
    {
        if (target != null)
        {
            // XYはPlayerに合わせるが、Zはカメラの定位置(-10)に固定
            // これでPlayerが回転してもカメラは回りません
            transform.position = new Vector3(target.position.x,10f, target.position.z);
        }
    }
}
package com.lumi.fpsaddons

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private lateinit var tvLog: TextView

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        updateStatus()
        log(if (grantResult == 0) "Đã được cấp quyền Shizuku ✅" else "Bị từ chối quyền ❌")
    }

    private val pickAddonLauncher =
        registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val name = queryFileName(uri) ?: "addon_${System.currentTimeMillis()}.mcpack"
            log("Đang cài đặt: $name ...")
            val result = AddonInstaller.installAddon(this, uri, name)
            log(result)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatus = findViewById(R.id.tvStatus)
        tvLog = findViewById(R.id.tvLog)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        findViewById<Button>(R.id.btnRequestPermission).setOnClickListener {
            if (!ShizukuHelper.isShizukuAvailable()) {
                log("Shizuku chưa chạy — mở app Shizuku, bật service trước.")
            } else {
                ShizukuHelper.requestPermission()
            }
        }

        findViewById<Button>(R.id.btnImportAddon).setOnClickListener {
            if (!ShizukuHelper.hasPermission()) {
                log("Cần xin quyền Shizuku trước khi nhập addon.")
            } else {
                pickAddonLauncher.launch("*/*")
            }
        }

        findViewById<Button>(R.id.btnClearCache).setOnClickListener {
            log(OptimizeTasks.clearMinecraftCache())
        }

        findViewById<Button>(R.id.btnOptimize).setOnClickListener {
            log(OptimizeTasks.disableSystemAnimations())
        }

        updateStatus()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }

    private fun updateStatus() {
        tvStatus.text = when {
            !ShizukuHelper.isShizukuAvailable() -> "Trạng thái: Shizuku chưa chạy"
            ShizukuHelper.hasPermission() -> "Trạng thái: Đã có quyền ✅"
            else -> "Trạng thái: Chưa cấp quyền"
        }
    }

    private fun log(msg: String) {
        tvLog.append("\n$msg")
    }

    private fun queryFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) return cursor.getString(idx)
        }
        return null
    }
}

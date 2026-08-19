package com.lumi.fpsaddons

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
        LogManager.log(if (grantResult == 0) "Đã được cấp quyền Shizuku ✅" else "Bị từ chối quyền ❌")
        (supportFragmentManager.findFragmentById(R.id.fragmentContainer) as? Refreshable)?.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Shizuku.addRequestPermissionResultListener(permissionListener)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            switchFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_fps -> FpsFragment()
                R.id.nav_device -> DeviceFragment()
                R.id.nav_ram -> RamFragment()
                R.id.nav_log -> LogFragment()
                else -> HomeFragment()
            }
            switchFragment(fragment)
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        super.onDestroy()
    }
}

/** Fragment nào cần tự cập nhật lại UI khi quyền Shizuku thay đổi thì implement cái này */
interface Refreshable {
    fun refresh()
}

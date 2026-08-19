package com.lumi.fpsaddons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class RamFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_ram, container, false)

        view.findViewById<Button>(R.id.btnCleanRam).setOnClickListener {
            if (!ShizukuHelper.hasPermission()) {
                LogManager.log("Chưa có quyền Shizuku — vào tab Tối ưu để xin quyền.")
                return@setOnClickListener
            }
            showCleanRamDialog()
        }

        view.findViewById<Button>(R.id.btnClearCache).setOnClickListener {
            if (!ShizukuHelper.hasPermission()) {
                LogManager.log("Chưa có quyền Shizuku — vào tab Tối ưu để xin quyền.")
                return@setOnClickListener
            }
            showCacheClearDialog()
        }

        return view
    }

    private fun showCleanRamDialog() {
        val names = RamCleaner.COMMON_APPS.keys.toTypedArray()
        val packages = RamCleaner.COMMON_APPS.values.toTypedArray()
        val checked = BooleanArray(names.size) { true }

        AlertDialog.Builder(requireContext(), R.style.LumiDialogTheme)
            .setTitle("Chọn app muốn tắt để giải phóng RAM")
            .setMultiChoiceItems(names, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Dọn ngay") { _, _ ->
                val selected = packages.filterIndexed { i, _ -> checked[i] }
                LogManager.log("Đang dọn RAM...")
                LogManager.log(RamCleaner.clean(selected))
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }

    private fun showCacheClearDialog() {
        val allApps = RamCleaner.COMMON_APPS + mapOf("Free Fire" to "com.dts.freefireth")
        val names = allApps.keys.toList()
        val packages = allApps.values.toList()

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 8)
        }

        val selectAllBox = CheckBox(requireContext()).apply {
            text = "Chọn tất cả"
            setTextColor(requireContext().getColor(R.color.neon_pink))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        container.addView(selectAllBox)

        val checkboxes = names.map { name ->
            CheckBox(requireContext()).apply { text = name; setTextColor(requireContext().getColor(R.color.text_light)) }
        }
        checkboxes.forEach { container.addView(it) }

        selectAllBox.setOnCheckedChangeListener { _, isChecked -> checkboxes.forEach { it.isChecked = isChecked } }

        val scroll = ScrollView(requireContext()).apply { addView(container) }

        AlertDialog.Builder(requireContext(), R.style.LumiDialogTheme)
            .setTitle("Chọn app muốn xoá cache")
            .setView(scroll)
            .setPositiveButton("Xoá cache") { _, _ ->
                val selected = packages.filterIndexed { i, _ -> checkboxes[i].isChecked }
                if (selected.isEmpty()) {
                    LogManager.log("Chưa chọn app nào.")
                } else {
                    LogManager.log("Đang xoá cache...")
                    LogManager.log(OptimizeTasks.clearCacheForApps(selected))
                }
            }
            .setNegativeButton("Huỷ", null)
            .show()
    }
}

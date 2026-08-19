package com.lumi.fpsaddons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class DeviceFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_device, container, false)
        val tvInfo = view.findViewById<TextView>(R.id.tvDeviceInfo)

        fun load() {
            val sb = StringBuilder()
            sb.appendLine(DeviceInfoHelper.getCpuInfo())
            sb.appendLine()
            sb.appendLine(DeviceInfoHelper.getRamInfo(requireContext()))
            sb.appendLine()
            sb.appendLine(DeviceInfoHelper.getStorageInfo())
            sb.appendLine()
            sb.appendLine(DeviceInfoHelper.getBatteryInfo(requireContext()))
            if (ShizukuHelper.hasPermission()) {
                sb.appendLine()
                sb.append("Nhiệt độ chip: ${DeviceInfoHelper.getTemperature()}")
            }
            tvInfo.text = sb.toString()
        }

        load()
        view.findViewById<Button>(R.id.btnRefreshDevice).setOnClickListener { load() }

        return view
    }
}

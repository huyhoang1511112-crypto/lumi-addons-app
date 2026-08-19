package com.lumi.fpsaddons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class LogFragment : Fragment() {

    private lateinit var tvLog: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_log, container, false)
        tvLog = view.findViewById(R.id.tvLog)

        tvLog.text = if (LogManager.getAll().isEmpty()) "Chưa có nhật ký." else LogManager.getAll().joinToString("\n")

        LogManager.setListener { line ->
            activity?.runOnUiThread { tvLog.append("\n$line") }
        }

        view.findViewById<Button>(R.id.btnClearLog).setOnClickListener {
            LogManager.clear()
            tvLog.text = "Chưa có nhật ký."
        }

        return view
    }

    override fun onDestroyView() {
        LogManager.setListener(null)
        super.onDestroyView()
    }
}

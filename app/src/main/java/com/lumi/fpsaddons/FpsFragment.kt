package com.lumi.fpsaddons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class FpsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_fps, container, false)
        val tvResult = view.findViewById<TextView>(R.id.tvFpsResult)
        val tvNote = view.findViewById<TextView>(R.id.tvFpsNote)

        view.findViewById<Button>(R.id.btnMeasureFps).setOnClickListener {
            if (!ShizukuHelper.hasPermission()) {
                tvNote.text = "Chưa có quyền Shizuku — vào tab Tối ưu để xin quyền."
                return@setOnClickListener
            }
            tvNote.text = "Đang đo..."
            val result = FpsMeasurer.measureFps()
            tvResult.text = "%.0f FPS".format(result.fps)
            tvNote.text = result.note
            LogManager.log("Đo FPS: %.0f FPS — ${result.note}".format(result.fps))
        }

        return view
    }
}

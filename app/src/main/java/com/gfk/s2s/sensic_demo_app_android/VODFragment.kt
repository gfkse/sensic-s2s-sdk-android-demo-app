package com.gfk.s2s.sensic_demo_app_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gfk.s2s.s2sagent.S2SAgent

class VODFragment : Fragment() {

    val videoURL = "https://demo-config-preproduction.sensic.net/video/video3.mp4"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.vod_fragment, container, false)
    }
}
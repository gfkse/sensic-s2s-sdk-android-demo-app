package com.gfk.s2s.sensic_demo_app_android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class VODFragment : BaseVideoFragment() {

    override val videoURL = "https://demo-config-preproduction.sensic.net/video/video3.mp4"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.supportActionBar?.title =
            getString(R.string.fragment_title_vod)
        return inflater.inflate(R.layout.vod_fragment, container, false)
    }

}
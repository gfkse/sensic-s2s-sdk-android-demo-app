package com.gfk.s2s.sensic_demo_app_android.video

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gfk.s2s.S2SExtension.S2SExtension
import com.gfk.s2s.s2sagent.S2SAgent
import com.gfk.s2s.sensic_demo_app_android.MainActivity
import com.gfk.s2s.sensic_demo_app_android.R


class LiveNoSeekFragment : BaseVideoFragment() {

    override val videoURL = "https://d2e1asnsl7br7b.cloudfront.net/7782e205e72f43aeb4a48ec97f66ebbe/index_1.m3u8"
    override val configUrl = "https://demo-config-preproduction.sensic.net/s2s-android.json"
    override val mediaId = "s2sdemomediaid_ssa_android_new"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as? MainActivity)?.supportActionBar?.title =
            getString(R.string.fragment_title_live_no_seekbar)
        return inflater.inflate(R.layout.video_fragment_no_seekbar, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.prepareLiveVideoPlayer()

        super.disableSeekFromExoPlayer()

        agent = S2SAgent(configUrl, mediaId, context)
        val s2sExtension = S2SExtension(mediaId, videoURL, null)
        s2sExtension.bindLivePlayer(agent!!, exoPlayer!!)
    }

}
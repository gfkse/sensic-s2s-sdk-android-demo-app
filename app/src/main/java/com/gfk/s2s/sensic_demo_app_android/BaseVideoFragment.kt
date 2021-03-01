package com.gfk.s2s.sensic_demo_app_android

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.gfk.s2s.s2sagent.S2SAgent
//import com.gfk.s2s.S2SExtension.S2SExtension
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

open class BaseVideoFragment : Fragment() {
    private var exoPlayer: ExoPlayer? = null
    private var agent: S2SAgent? = null
    open val videoURL = ""
    private val configUrl = "https://demo-config-preproduction.sensic.net/s2s-android.json"
    private val mediaId = "s2sdemomediaid_ssa_android_new"
    private var playerPosition = 0L


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        (activity as? MainActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as? MainActivity)?.supportActionBar?.setDisplayShowHomeEnabled(true)

        prepareVideoPlayer()
        setupS2sAgent()
    }

    private fun setupS2sAgent() {
        agent = S2SAgent(configUrl, mediaId, context)
       // val s2sExtension = S2SExtension(mediaId, videoURL, null)
     //   s2sExtension.bindPlayer(agent!!, exoPlayer!!)
    }

    private fun prepareVideoPlayer() {
        val trackSelector = DefaultTrackSelector(requireContext())
        val loadControl = DefaultLoadControl()

        exoPlayer = SimpleExoPlayer.Builder(requireContext()).setTrackSelector(trackSelector)
                .setLoadControl(loadControl).build()
        val userAgent = Util.getUserAgent(
                requireContext(), context?.getString(R.string.app_name)
                ?: ""
        )
        val mediaSource =
                ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
                        .createMediaSource(Uri.parse(videoURL))

        view?.findViewById<PlayerView>(R.id.videoView)?.player = exoPlayer
        exoPlayer?.prepare(mediaSource)
        exoPlayer?.seekTo(playerPosition)
        exoPlayer?.playWhenReady = true
    }

    override fun onResume() {
        super.onResume()
        (activity as? MainActivity)?.usePictureInPictureByHomeButtonPress = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true
        ) {
            if (activity?.isInPictureInPictureMode == false) {
                exoPlayer?.playWhenReady = true
            }
        } else {
            exoPlayer?.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N &&
                activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE) == true
        ) {
            if (activity?.isInPictureInPictureMode == false) {
                playerPosition = exoPlayer?.currentPosition ?: 0
                exoPlayer?.playWhenReady = false
            }
        } else {
            playerPosition = exoPlayer?.currentPosition ?: 0
            exoPlayer?.playWhenReady = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer?.release()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playerPosition", playerPosition);
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        playerPosition = savedInstanceState?.getLong("playerPosition") ?: 0
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val mediaFrame = view?.findViewById<FrameLayout>(R.id.main_media_frame)
        val params = mediaFrame?.layoutParams as? ConstraintLayout.LayoutParams

        if (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT) {
            params?.height = FrameLayout.LayoutParams.MATCH_PARENT
            params?.dimensionRatio = null
        } else {
            params?.dimensionRatio = "1:0.666"
            params?.height = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> findNavController().popBackStack()
        }
        return true

    }
}
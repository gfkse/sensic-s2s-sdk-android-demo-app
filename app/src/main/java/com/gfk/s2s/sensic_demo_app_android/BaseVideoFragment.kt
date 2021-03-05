package com.gfk.s2s.sensic_demo_app_android

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.gfk.s2s.s2sagent.S2SAgent
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 * class BaseVideoFragment has the code to show the exoplayer.
 *  The values for videoUrl, configUrl and mediaId are overridden in the
 *  fragments extending from this class.
 */

open class BaseVideoFragment : BaseFragment() {
    var exoPlayer: ExoPlayer? = null
    var agent: S2SAgent? = null
    open val videoURL = ""
    open val configUrl = ""
    open val mediaId = ""
    private var playerPosition = 0L

    open fun prepareVideoPlayer() {
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



}
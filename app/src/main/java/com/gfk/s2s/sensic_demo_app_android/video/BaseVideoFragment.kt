package com.gfk.s2s.sensic_demo_app_android.video

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.gfk.s2s.s2sagent.S2SAgent
import com.gfk.s2s.sensic_demo_app_android.BaseFragment
import com.gfk.s2s.sensic_demo_app_android.MainActivity
import com.gfk.s2s.sensic_demo_app_android.R
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.DefaultTimeBar
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory
import com.google.android.exoplayer2.util.Log
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
    var playerPosition = 0L


    fun prepareVideoPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        view?.findViewById<PlayerView>(R.id.videoView)?.player = exoPlayer

        val userAgent = Util.getUserAgent(
            requireContext(), context?.getString(R.string.app_name)
                ?: ""
        )
        val mediaSource =
            ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, userAgent))
                .createMediaSource(Uri.parse(videoURL))

        exoPlayer?.prepare(mediaSource)
        exoPlayer?.seekTo(playerPosition)
        exoPlayer?.playWhenReady = true
    }

    fun prepareLiveVideoPlayer() {
        exoPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        view?.findViewById<PlayerView>(R.id.videoView)?.player = exoPlayer

        val dataSourceFactory: DataSource.Factory = DefaultHttpDataSourceFactory(
            Util.getUserAgent(requireContext(), getString(R.string.app_name))
        )

        val hlsMediaSource: HlsMediaSource =
            HlsMediaSource.Factory(dataSourceFactory).createMediaSource(
                Uri.parse(videoURL)
            )

        exoPlayer?.prepare(hlsMediaSource)
        exoPlayer?.seekTo(playerPosition)
        exoPlayer?.playWhenReady = true
    }

    fun disableSeekFromExoPlayer() {
        val playerView = view?.findViewById<PlayerView>(R.id.videoView)
        try {
            val timeBar = playerView?.findViewById<DefaultTimeBar>(R.id.exo_progress)
            timeBar?.hideScrubber()
            timeBar?.isEnabled = false // clicks on timeBar still seek
            playerView?.setControlDispatcher(object : ControlDispatcher {
                override fun dispatchSetPlayWhenReady(
                    player: Player,
                    playWhenReady: Boolean
                ): Boolean {
                    player.playWhenReady = playWhenReady
                    return true
                }

                override fun dispatchSeekTo(
                    player: Player,
                    windowIndex: Int,
                    positionMs: Long
                ): Boolean {
                    return false
                }

                override fun dispatchSetRepeatMode(player: Player, repeatMode: Int): Boolean {
                    player.repeatMode = repeatMode
                    return true
                }

                override fun dispatchSetShuffleModeEnabled(
                    player: Player,
                    shuffleModeEnabled: Boolean
                ): Boolean {
                    player.shuffleModeEnabled = shuffleModeEnabled
                    return true
                }

                override fun dispatchStop(player: Player, reset: Boolean): Boolean {
                    player.stop(reset)
                    return true
                }
            })


        } catch (e: Exception) {
            Log.d("GFK_SENSIC_APP", "Exception: " + e.localizedMessage, e)
        }
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
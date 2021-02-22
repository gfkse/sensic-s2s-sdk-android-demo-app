package com.gfk.s2s.ui.pixel

import android.content.Context
import android.content.Intent
import android.graphics.Color.green
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.gfk.s2s.demo.s2s.R
import com.gfk.s2s.ui.details.VideoPlayerActivity
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.util.Date

class PixelRequestActivity: AppCompatActivity() {

    private var pixelTextView: TextView? = null
    private var checkImage: ImageView? = null

    companion object {
        private var PIXEL_URL = "https://demo-config.sensic.net/tp?ty=IM&gdpr={GDPR}&gdpr_consent={GDPR_CONSENT_758}&optin=false&m=s2sdemomediaid_ssa_android&c=pixel_native_android&pr=[timestamp]"

        fun newIntent(context: Context?): Intent? {
            return Intent(context, PixelRequestActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pixel)
        pixelTextView = findViewById(R.id.pixelUrl)

        checkImage = findViewById(R.id.checkImage)
        checkImage?.setColorFilter(ContextCompat.getColor(applicationContext, android.R.color.darker_gray))

        PIXEL_URL = PIXEL_URL.replace("[timestamp]", Date().time.toString())
        pixelTextView?.text = PIXEL_URL

        triggerPixelRequest()
    }

    private fun triggerPixelRequest() {
        PIXEL_URL.httpGet().responseString { _, _, result ->
            when (result) {
                is Result.Failure -> {
                    Log.e("GfKlog", result.getException().message)
                }
                is Result.Success -> {
                    Log.d("GfKlog", "Successfully fired Pixel Request")
                    checkImage?.setColorFilter(ContextCompat.getColor(applicationContext, android.R.color.holo_green_light))
                }
            }
        }
    }
}
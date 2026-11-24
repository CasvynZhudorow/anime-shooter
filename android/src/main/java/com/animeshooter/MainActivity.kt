package com.animeshooter

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.animeshooter.screens.MainMenuScreen
import com.animeshooter.GameMain

class MainActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            numSamples = 2
            r = 8
            g = 8
            b = 8
            a = 8
            depth = 16
            stencil = 8
        }
        initialize(GameMain(), config)
    }
}
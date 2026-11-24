package com.animeshooter

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import com.animeshooter.GameMain

object DesktopLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = Lwjgl3ApplicationConfiguration().apply {
            title = "Anime Shooter"
            setWindowedMode(540, 960)
            setResizable(false)
            foregroundFPS = 60
            backgroundFPS = 30
        }
        Lwjgl3Application(GameMain(), config)
    }
}
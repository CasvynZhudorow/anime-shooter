package com.animeshooter

import com.animeshooter.screens.MainMenuScreen
import com.animeshooter.systems.GameWorld
import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.ModelBatch

class GameMain : Game() {
    
    val gameWorld = GameWorld()
    lateinit var batch: SpriteBatch
    lateinit var modelBatch: ModelBatch
    
    override fun create() {
        batch = SpriteBatch()
        modelBatch = ModelBatch()
        setScreen(MainMenuScreen(this))
    }
    
    override fun dispose() {
        batch.dispose()
        modelBatch.dispose()
        super.dispose()
    }
}
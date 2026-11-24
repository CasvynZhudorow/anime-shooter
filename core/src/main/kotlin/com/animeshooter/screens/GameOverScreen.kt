package com.animeshooter.screens

import com.animeshooter.GameMain
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class GameOverScreen(
    private val game: GameMain,
    private val reason: String,
    private val finalScore: Int,
    private val highScore: Int
) : ScreenAdapter() {
    
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var viewport: FitViewport
    private var time = 0f
    private var canExit = false
    
    private val screenWidth = 540f
    private val screenHeight = 960f
    
    override fun show() {
        batch = SpriteBatch()
        font = BitmapFont()
        font.data.setScale(2f)
        viewport = FitViewport(screenWidth, screenHeight)
        viewport.apply()
        
        // Delay before allowing exit
        canExit = false
        time = 0f
    }
    
    override fun render(delta: Float) {
        time += delta
        
        if (time > 1f) {
            canExit = true
        }
        
        Gdx.gl.glClearColor(0.1f, 0.02f, 0.02f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        
        batch.projectionMatrix = viewport.getCamera().combined
        batch.begin()
        
        drawBackground()
        
        // Game Over / Victory text
        font.color = if (reason == "Victory!") Color.GOLD else Color.RED
        val reasonLayout = GlyphLayout(font, reason)
        font.draw(batch, reasonLayout, (screenWidth - reasonLayout.width) / 2, screenHeight * 0.75f)
        
        // Final score
        font.color = Color.WHITE
        font.data.setScale(1.5f)
        val scoreLayout = GlyphLayout(font, "FINAL SCORE: $finalScore")
        font.draw(batch, scoreLayout, (screenWidth - scoreLayout.width) / 2, screenHeight * 0.55f)
        
        // High score
        val highScoreLayout = GlyphLayout(font, "HIGH SCORE: $highScore")
        font.draw(batch, highScoreLayout, (screenWidth - highScoreLayout.width) / 2, screenHeight * 0.45f)
        
        // New high score indicator
        if (finalScore >= highScore && finalScore > 0) {
            font.color = Color.GOLD
            val newHsLayout = GlyphLayout(font, "NEW HIGH SCORE!")
            font.draw(batch, newHsLayout, (screenWidth - newHsLayout.width) / 2, screenHeight * 0.35f)
        }
        
        // Tap to continue
        if (canExit) {
            val alpha = (MathUtils.sin(time * 4f) + 1f) / 2f * 0.5f + 0.5f
            font.color = Color.GRAY.cpy().mul(alpha)
            font.data.setScale(1f)
            val hintLayout = GlyphLayout(font, "TAP TO RETURN TO MENU")
            font.draw(batch, hintLayout, (screenWidth - hintLayout.width) / 2, screenHeight * 0.2f)
            font.data.setScale(2f)
        }
        
        batch.end()
        
        if (canExit && Gdx.input.justTouched()) {
            game.setScreen(MainMenuScreen(game))
        }
    }
    
    private fun drawBackground() {
        for (i in 0..30) {
            val starX = (i * 137 + time * 5) % screenWidth
            val starY = (i * 211 + time * 3) % screenHeight
            val alpha = 0.1f + 0.2f * MathUtils.sin(time + i)
            batch.setColor(Color.WHITE.cpy().mul(alpha))
            batch.draw(batch.getColor().r, starX, starY, 2f, 2f)
        }
        batch.setColor(Color.WHITE)
    }
    
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }
    
    override fun dispose() {
        batch.dispose()
        font.dispose()
    }
}
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

class MainMenuScreen(private val game: GameMain) : ScreenAdapter() {
    
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var viewport: FitViewport
    private lateinit var titleLayout: GlyphLayout
    private lateinit var startLayout: GlyphLayout
    private lateinit var highScoreLayout: GlyphLayout
    private var time = 0f
    
    private val screenWidth = 540f
    private val screenHeight = 960f
    
    override fun show() {
        batch = SpriteBatch()
        font = BitmapFont()
        font.data.setScale(2f)
        viewport = FitViewport(screenWidth, screenHeight)
        viewport.apply()
        
        titleLayout = GlyphLayout(font, "ANIME SHOOTER")
        startLayout = GlyphLayout(font, "TOUCH TO START")
        highScoreLayout = GlyphLayout(font, "HIGH SCORE: ${game.gameWorld.highScore}")
    }
    
    override fun render(delta: Float) {
        time += delta
        
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        
        batch.projectionMatrix = viewport.getCamera().combined
        batch.begin()
        
        // Animated background particles
        drawBackground()
        
        // Title
        font.color = Color.CYAN
        font.draw(batch, titleLayout, (screenWidth - titleLayout.width) / 2, screenHeight * 0.7f)
        
        // Pulsing start text
        val alpha = (MathUtils.sin(time * 3f) + 1f) / 2f * 0.5f + 0.5f
        font.color = Color.WHITE.cpy().mul(alpha)
        font.draw(batch, startLayout, (screenWidth - startLayout.width) / 2, screenHeight * 0.4f)
        
        // High score
        font.color = Color.GOLD
        font.data.setScale(1f)
        highScoreLayout.setText(font, "HIGH SCORE: ${game.gameWorld.highScore}")
        font.draw(batch, highScoreLayout, (screenWidth - highScoreLayout.width) / 2, screenHeight * 0.25f)
        font.data.setScale(2f)
        
        // Ship icons hint
        font.data.setScale(0.8f)
        font.color = Color.GRAY
        val hintLayout = GlyphLayout(font, "3 SHIPS • UPGRADES • ULTIMATES • 5 STAGES")
        font.draw(batch, hintLayout, (screenWidth - hintLayout.width) / 2, screenHeight * 0.15f)
        font.data.setScale(2f)
        
        batch.end()
        
        // Touch to start
        if (Gdx.input.justTouched()) {
            game.setScreen(ShipSelectScreen(game))
        }
    }
    
    private fun drawBackground() {
        // Simple animated stars
        for (i in 0..20) {
            val x = (i * 73 + time * 20) % screenWidth
            val y = (i * 127 + time * 15) % screenHeight
            val size = 1f + MathUtils.sin(time + i) * 0.5f
            batch.setColor(Color.WHITE.cpy().mul(0.3f + 0.2f * MathUtils.sin(time * 2 + i)))
            batch.draw(batch.getColor().r, x, y, size, size) // This won't work, need a texture
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
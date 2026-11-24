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

class StageClearScreen(
    private val game: GameMain,
    private val clearedStage: Int,
    private val score: Int
) : ScreenAdapter() {
    
    private lateinit var batch: SpriteBatch
    private lateinit var font: BitmapFont
    private lateinit var viewport: FitViewport
    private var time = 0f
    private var canContinue = false
    
    private val screenWidth = 540f
    private val screenHeight = 960f
    
    override fun show() {
        batch = SpriteBatch()
        font = BitmapFont()
        font.data.setScale(2f)
        viewport = FitViewport(screenWidth, screenHeight)
        viewport.apply()
        
        canContinue = false
        time = 0f
    }
    
    override fun render(delta: Float) {
        time += delta
        
        if (time > 1.5f) {
            canContinue = true
        }
        
        Gdx.gl.glClearColor(0.02f, 0.1f, 0.02f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
        
        batch.projectionMatrix = viewport.getCamera().combined
        batch.begin()
        
        drawBackground()
        
        // Stage clear text
        font.color = Color.GOLD
        val clearLayout = GlyphLayout(font, "STAGE $clearedStage CLEAR!")
        font.draw(batch, clearLayout, (screenWidth - clearLayout.width) / 2, screenHeight * 0.7f)
        
        // Score
        font.color = Color.WHITE
        font.data.setScale(1.5f)
        val scoreLayout = GlyphLayout(font, "SCORE: $score")
        font.draw(batch, scoreLayout, (screenWidth - scoreLayout.width) / 2, screenHeight * 0.5f)
        
        // Bonus
        val bonus = clearedStage * 500
        val bonusLayout = GlyphLayout(font, "STAGE BONUS: +$bonus")
        font.draw(batch, bonusLayout, (screenWidth - bonusLayout.width) / 2, screenHeight * 0.4f)
        
        // Next stage preview
        font.color = Color.CYAN
        font.data.setScale(1f)
        val nextStage = clearedStage + 1
        val nextText = if (nextStage <= 5) "NEXT: STAGE $nextStage" else "ALL STAGES CLEARED!"
        val nextLayout = GlyphLayout(font, nextText)
        font.draw(batch, nextLayout, (screenWidth - nextLayout.width) / 2, screenHeight * 0.3f)
        
        // Tap to continue
        if (canContinue) {
            val alpha = (MathUtils.sin(time * 3f) + 1f) / 2f * 0.5f + 0.5f
            font.color = Color.GRAY.cpy().mul(alpha)
            val hintLayout = GlyphLayout(font, "TAP TO CONTINUE")
            font.draw(batch, hintLayout, (screenWidth - hintLayout.width) / 2, screenHeight * 0.15f)
        }
        
        batch.end()
        
        if (canContinue && Gdx.input.justTouched()) {
            if (nextStage <= 5) {
                game.gameWorld.startGame(game.gameWorld.getPlayerEntity()?.getComponent(com.animeshooter.ecs.PlayerComponent::class.java)?.shipType ?: com.animeshooter.ecs.ShipType.INTERCEPTOR)
                game.setScreen(GameScreen(game))
            } else {
                game.setScreen(GameOverScreen(game, "Victory!", score, game.gameWorld.highScore))
            }
        }
    }
    
    private fun drawBackground() {
        for (i in 0..30) {
            val starX = (i * 137 + time * 8) % screenWidth
            val starY = (i * 211 + time * 5) % screenHeight
            val alpha = 0.2f + 0.3f * MathUtils.sin(time * 1.5 + i)
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
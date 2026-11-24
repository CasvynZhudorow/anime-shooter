package com.animeshooter.screens

import com.animeshooter.GameMain
import com.animeshooter.ecs.ShipType
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class ShipSelectScreen(private val game: GameMain) : ScreenAdapter() {
    
    private lateinit var batch: SpriteBatch
    private lateinit var modelBatch: ModelBatch
    private lateinit var font: BitmapFont
    private lateinit var viewport: FitViewport
    private lateinit var whiteTexture: Texture
    private var selectedIndex = 0
    private var time = 0f
    private var touchDragged = false
    private var lastTouchX = 0f
    
    private val ships = arrayOf(
        ShipData(ShipType.INTERCEPTOR, "INTERCEPTOR", "FAST & PIERCING", "RAPID BURST", Color.CYAN),
        ShipData(ShipType.BALANCED, "BALANCED", "HOMING SHOTS", "HOMING MISSILES", Color.GREEN),
        ShipData(ShipType.HEAVY, "HEAVY", "WIDE SPREAD", "NOVA RING", Color.ORANGE)
    )
    
    private val screenWidth = 540f
    private val screenHeight = 960f
    
    override fun show() {
        batch = SpriteBatch()
        modelBatch = ModelBatch()
        font = BitmapFont()
        font.data.setScale(1.5f)
        viewport = FitViewport(screenWidth, screenHeight)
        viewport.apply()
        selectedIndex = 0
        
        // Create 1x1 white texture for drawing
        val pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888)
        pixmap.setColor(Color.WHITE)
        pixmap.fill()
        whiteTexture = Texture(pixmap)
        pixmap.dispose()
    }
    
    override fun render(delta: Float) {
        time += delta
        
        handleInput()
        
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.15f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT)
        
        batch.projectionMatrix = viewport.getCamera().combined
        modelBatch.begin(viewport.getCamera())
        batch.begin()
        
        drawBackground()
        
        // Title
        font.color = Color.WHITE
        val titleLayout = GlyphLayout(font, "SELECT YOUR SHIP")
        font.draw(batch, titleLayout, (screenWidth - titleLayout.width) / 2f, screenHeight * 0.9f)
        
        // Ship cards
        for (i in ships.indices) {
            drawShipCard(i, ships[i], i == selectedIndex)
        }
        
        // Start hint
        font.data.setScale(1f)
        font.color = Color.GRAY
        val hintLayout = GlyphLayout(font, "SWIPE LEFT/RIGHT • TAP TO CONFIRM")
        font.draw(batch, hintLayout, (screenWidth - hintLayout.width) / 2f, screenHeight * 0.1f)
        font.data.setScale(1.5f)
        
        batch.end()
        modelBatch.end()
    }
    
    private fun handleInput() {
        if (Gdx.input.isTouched()) {
            val x = Gdx.input.getX()
            if (!touchDragged) {
                lastTouchX = x.toFloat()
                touchDragged = true
            } else {
                val delta = lastTouchX - x
                if (Math.abs(delta) > 50) {
                    if (delta > 0) {
                        selectedIndex = (selectedIndex + 1) % ships.size
                    } else {
                        selectedIndex = (selectedIndex - 1 + ships.size) % ships.size
                    }
                    touchDragged = false
                }
            }
        } else if (touchDragged) {
            // Tap to confirm
            game.gameWorld.startGame(ships[selectedIndex].shipType)
            game.setScreen(GameScreen(game))
            touchDragged = false
        }
    }
    
    private fun drawShipCard(index: Int, data: ShipData, isSelected: Boolean) {
        val cardWidth = screenWidth * 0.8f
        val cardHeight = screenHeight * 0.25f
        val x = (screenWidth - cardWidth) / 2f
        val y = screenHeight * 0.6f - index * (cardHeight + 20f)
        
        // Card background
        batch.setColor(if (isSelected) Color.CYAN.cpy().mul(0.3f) else Color.DARK_GRAY.cpy().mul(0.7f))
        batch.draw(whiteTexture, x, y, cardWidth, cardHeight)
        batch.setColor(Color.WHITE)
        
        // Border if selected
        if (isSelected) {
            batch.setColor(Color.CYAN)
            drawRect(x, y, cardWidth, cardHeight, 3f)
            batch.setColor(Color.WHITE)
        }
        
        // Ship name
        font.color = data.color
        val nameLayout = GlyphLayout(font, data.name)
        font.draw(batch, nameLayout, x + (cardWidth - nameLayout.width) / 2f, y + cardHeight - 30f)
        
        // Description
        font.color = Color.LIGHT_GRAY
        font.data.setScale(0.8f)
        val descLayout = GlyphLayout(font, data.description)
        font.draw(batch, descLayout, x + (cardWidth - descLayout.width) / 2f, y + cardHeight - 70f)
        
        // Ultimate
        font.color = Color.GOLD
        val ultLayout = GlyphLayout(font, "ULTIMATE: ${data.ultimate}")
        font.draw(batch, ultLayout, x + (cardWidth - ultLayout.width) / 2f, y + cardHeight - 100f)
        font.data.setScale(1.5f)
        
        // Draw ship model
        drawShipModel(data.shipType, x + cardWidth / 2f, y + cardHeight / 2f, time)
    }
    
    private fun drawShipModel(shipType: ShipType, x: Float, y: Float, time: Float) {
        // We'll draw a simple representation since we don't have the model instance here easily
        // In a real implementation, we'd create ModelInstances for each ship
    }
    
    private fun drawRect(x: Float, y: Float, w: Float, h: Float, thickness: Float) {
        batch.draw(whiteTexture, x, y, w, thickness)
        batch.draw(whiteTexture, x, y + h - thickness, w, thickness)
        batch.draw(whiteTexture, x, y, thickness, h)
        batch.draw(whiteTexture, x + w - thickness, y, thickness, h)
    }
    
    private fun drawBackground() {
        // Stars
        for (i in 0..30) {
            val starX = (i * 137 + time * 10) % screenWidth
            val starY = (i * 211 + time * 8) % screenHeight
            val alpha = 0.2f + 0.3f * MathUtils.sin(time * 2 + i * 0.5f)
            batch.setColor(Color.WHITE.cpy().mul(alpha))
            batch.draw(whiteTexture, starX, starY, 2f, 2f)
        }
        batch.setColor(Color.WHITE)
    }
    
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }
    
    override fun dispose() {
        batch.dispose()
        modelBatch.dispose()
        font.dispose()
        whiteTexture.dispose()
    }
    
    private data class ShipData(
        val shipType: ShipType,
        val name: String,
        val description: String,
        val ultimate: String,
        val color: Color
    )
}
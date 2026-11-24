package com.animeshooter.screens

import com.animeshooter.GameMain
import com.animeshooter.ecs.*
import com.animeshooter.systems.GameWorld
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g3d.ModelBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.viewport.FitViewport

class GameScreen(private val game: GameMain) : ScreenAdapter() {
    
    private lateinit var batch: SpriteBatch
    private lateinit var modelBatch: ModelBatch
    private lateinit var font: BitmapFont
    private lateinit var viewport: FitViewport
    private lateinit var uiLayout: GlyphLayout
    
    private val screenWidth = 540f
    private val screenHeight = 960f
    private var screenShakeX = 0f
    private var screenShakeY = 0f
    private var screenShakeTimer = 0f
    private var screenShakeIntensity = 0f
    
    override fun show() {
        batch = SpriteBatch()
        modelBatch = ModelBatch()
        font = BitmapFont()
        font.data.setScale(1f)
        viewport = FitViewport(screenWidth, screenHeight)
        viewport.apply()
        uiLayout = GlyphLayout()
        
        // Setup callbacks
        val world = game.gameWorld
        world.onScoreChange = { score -> }
        world.onStageChange = { stage, wave -> }
        world.onHealthChange = { current, max -> }
        world.onUltimateChange = { current, max -> }
        world.onWeaponLevelChange = { level -> }
        world.onGameOver = { reason -> 
            game.setScreen(GameOverScreen(game, reason, world.score, world.highScore))
        }
        world.onStageClear = { reason ->
            if (world.currentStage >= 5) {
                game.setScreen(GameOverScreen(game, "Victory!", world.score, world.highScore))
            } else {
                game.setScreen(StageClearScreen(game, world.currentStage, world.score))
            }
        }
    }
    
    override fun render(delta: Float) {
        handleInput()
        
        // Update game world
        game.gameWorld.update(delta)
        
        // Screen shake
        updateScreenShake(delta)
        
        // Render
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.08f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT)
        
        // Apply screen shake to camera
        val camera = viewport.getCamera()
        camera.position.x = screenWidth / 2 + screenShakeX
        camera.position.y = screenHeight / 2 + screenShakeY
        camera.update()
        
        modelBatch.begin(camera)
        renderModels()
        modelBatch.end()
        
        // Reset camera for UI
        camera.position.x = screenWidth / 2
        camera.position.y = screenHeight / 2
        camera.update()
        
        batch.projectionMatrix = camera.combined
        batch.begin()
        renderUI()
        batch.end()
    }
    
    private fun handleInput() {
        val world = game.gameWorld
        if (world.isPaused || world.isGameOver || world.isStageClear) return
        
        if (Gdx.input.isTouched()) {
            val x = Gdx.input.getX()
            val y = Gdx.graphics.height - Gdx.input.getY() // Flip Y
            
            // Convert to viewport coordinates
            val touchX = viewport.unproject(com.badlogic.gdx.math.Vector3(x.toFloat(), y.toFloat(), 0f)).x
            val touchY = viewport.unproject(com.badlogic.gdx.math.Vector3(x.toFloat(), y.toFloat(), 0f)).y
            
            world.setPlayerInput(touchX, touchY, true, false)
        } else {
            world.setPlayerInput(0f, 0f, false, false)
        }
        
        // Ultimate key (Space on desktop, or double-tap on mobile - simplified to touch)
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.SPACE)) {
                    world.setPlayerInput(0f, 0f, false, true)
                }
        
                // Pause
                if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ESCAPE) || 
                    Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.P)) {
                    world.togglePause()
                }
    }
    
    private fun updateScreenShake(delta: Float) {
        if (screenShakeTimer > 0f) {
            screenShakeTimer -= delta
            val progress = 1f - screenShakeTimer / 0.3f
            val currentIntensity = screenShakeIntensity * (1f - progress)
            screenShakeX = MathUtils.random(-currentIntensity, currentIntensity)
            screenShakeY = MathUtils.random(-currentIntensity, currentIntensity)
        } else {
            screenShakeX = 0f
            screenShakeY = 0f
        }
    }
    
    fun addScreenShake(intensity: Float, duration: Float) {
        screenShakeIntensity = intensity
        screenShakeTimer = duration
    }
    
    private fun renderModels() {
        val engine = game.gameWorld.getEngine()
        val entities = engine.getEntitiesFor(
            com.badlogic.ashley.core.Family.all(ModelComponent::class.java, TransformComponent::class.java).get()
        )
        
        for (entity in entities) {
            val modelComp = entity.getComponent(ModelComponent::class.java)
            val transformComp = entity.getComponent(TransformComponent::class.java)
            
            if (modelComp.modelInstance != null && modelComp.isVisible) {
                val instance = modelComp.modelInstance!!
                instance.transform.setToTranslation(transformComp.position)
                instance.transform.rotate(com.badlogic.gdx.math.Vector3(0, 0, 1), transformComp.rotation)
                instance.transform.scale(transformComp.scale)
                
                // Apply color
                instance.materials.forEach { material ->
                    material.set(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse(modelComp.color))
                }
                
                modelBatch.render(instance)
            }
        }
        
        // Render particles
        val particles = engine.getEntitiesFor(
            com.badlogic.ashley.core.Family.all(ParticleComponent::class.java, TransformComponent::class.java, ModelComponent::class.java).get()
        )
        
        for (entity in particles) {
            val particleComp = entity.getComponent(ParticleComponent::class.java)
            val transformComp = entity.getComponent(TransformComponent::class.java)
            val modelComp = entity.getComponent(ModelComponent::class.java)
            
            if (modelComp.modelInstance != null) {
                val instance = modelComp.modelInstance!!
                instance.transform.setToTranslation(transformComp.position)
                instance.transform.scale(particleComp.scale)
                
                val alpha = 1f - particleComp.lifetime / particleComp.maxLifetime
                val color = particleComp.color.cpy().mul(alpha)
                instance.materials.forEach { material ->
                    material.set(com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute.createDiffuse(color))
                }
                
                modelBatch.render(instance)
            }
        }
    }
    
    private fun renderUI() {
        val world = game.gameWorld
        val player = world.getPlayerEntity()
        
        if (player == null) return
        
        val playerComp = player.getComponent(PlayerComponent::class.java)
        val healthComp = player.getComponent(HealthComponent::class.java)
        val ultimateComp = player.getComponent(UltimateComponent::class.java)
        
        // Top UI - Score and Stage
        font.color = Color.WHITE
        uiLayout.setText(font, "STAGE ${world.currentStage}-${world.currentWave}  SCORE: ${world.score}")
        font.draw(batch, uiLayout, 20, screenHeight - 20)
        
        // High score
        uiLayout.setText(font, "BEST: ${world.highScore}")
        font.draw(batch, uiLayout, screenWidth - uiLayout.width - 20, screenHeight - 20)
        
        // Health bar (left)
        drawHealthBar(20, 60, 200, 20, healthComp.currentHealth, healthComp.maxHealth, healthComp.isInvulnerable)
        
        // Ultimate meter (right)
        drawUltimateMeter(screenWidth - 220, 60, 200, 20, playerComp.ultimateMeter, playerComp.maxUltimateMeter, ultimateComp.isActive, ultimateComp.cooldown)
        
        // Weapon level
        font.color = Color.CYAN
        uiLayout.setText(font, "WEAPON LV. ${playerComp.weaponLevel}/${playerComp.maxWeaponLevel}")
        font.draw(batch, uiLayout, 20, 100)
        
        // Ship type
        font.color = when (playerComp.shipType) {
            ShipType.INTERCEPTOR -> Color.CYAN
            ShipType.BALANCED -> Color.GREEN
            ShipType.HEAVY -> Color.ORANGE
        }
        uiLayout.setText(font, playerComp.shipType.name)
        font.draw(batch, uiLayout, 20, 130)
        
        // Pause indicator
        if (world.isPaused) {
            font.color = Color.WHITE
            font.data.setScale(3f)
            uiLayout.setText(font, "PAUSED")
            font.draw(batch, uiLayout, (screenWidth - uiLayout.width) / 2, screenHeight / 2)
            font.data.setScale(1f)
        }
        
        // Boss health bar
        if (world.isBossWave) {
            val bosses = world.getEngine().getEntitiesFor(
                com.badlogic.ashley.core.Family.all(EnemyComponent::class.java).get()
            ).filter { it.getComponent(EnemyComponent::class.java)?.isBoss == true }
            
            if (bosses.isNotEmpty()) {
                val boss = bosses.first()
                val bossComp = boss.getComponent(EnemyComponent::class.java)
                val bossHealth = boss.getComponent(HealthComponent::class.java)
                
                drawBossHealthBar(screenWidth / 2 - 150, screenHeight - 60, 300, 25, bossHealth.currentHealth, bossHealth.maxHealth, bossComp.bossPhase, bossComp.maxBossPhases)
            }
        }
    }
    
    private fun drawHealthBar(x: Float, y: Float, w: Float, h: Float, current: Int, max: Int, isInvulnerable: Boolean) {
        // Background
        batch.setColor(Color.DARK_GRAY)
        batch.draw(batch.getColor().r, x, y, w, h)
        
        // Health
        val healthPercent = current.toFloat() / max
        batch.setColor(if (healthPercent > 0.5f) Color.GREEN else if (healthPercent > 0.25f) Color.YELLOW else Color.RED)
        batch.draw(batch.getColor().r, x, y, w * healthPercent, h)
        
        // Border
        batch.setColor(Color.WHITE)
        drawRect(x, y, w, h, 2f)
        
        // Invulnerable flash
        if (isInvulnerable && (System.currentTimeMillis() / 100) % 2 == 0) {
            batch.setColor(Color.WHITE.cpy().mul(0.5f))
            batch.draw(batch.getColor().r, x, y, w, h)
        }
        batch.setColor(Color.WHITE)
    }
    
    private fun drawUltimateMeter(x: Float, y: Float, w: Float, h: Float, current: Float, max: Float, isActive: Boolean, cooldown: Float) {
        // Background
        batch.setColor(Color.DARK_GRAY)
        batch.draw(batch.getColor().r, x, y, w, h)
        
        // Meter
        val percent = if (isActive) 1f else if (cooldown > 0f) 0f else current / max
        
        batch.setColor(if (isActive) Color.CYAN else if (cooldown > 0f) Color.GRAY else Color.GOLD)
        batch.draw(batch.getColor().r, x, y, w * percent, h)
        
        // Border
        batch.setColor(Color.WHITE)
        drawRect(x, y, w, h, 2f)
        
        // Label
        font.color = Color.GOLD
        val label = if (isActive) "ULTIMATE ACTIVE" else if (cooldown > 0f) "COOLDOWN ${cooldown.toInt()}s" else "ULTIMATE READY"
        uiLayout.setText(font, label)
        font.draw(batch, uiLayout, x + (w - uiLayout.width) / 2, y - 5)
        
        batch.setColor(Color.WHITE)
    }
    
    private fun drawBossHealthBar(x: Float, y: Float, w: Float, h: Float, current: Int, max: Int, phase: Int, maxPhases: Int) {
        // Background
        batch.setColor(Color.DARK_GRAY)
        batch.draw(batch.getColor().r, x, y, w, h)
        
        // Health segments per phase
        val phaseWidth = w / maxPhases
        val healthPercent = current.toFloat() / max
        
        for (i in 0 until maxPhases) {
            val segmentX = x + i * phaseWidth
            val segmentHealth = MathUtils.clamp(healthPercent * maxPhases - i, 0f, 1f)
            
            batch.setColor(if (segmentHealth > 0f) Color.RED else Color.DARK_GRAY)
            batch.draw(batch.getColor().r, segmentX, y, phaseWidth * segmentHealth, h)
            
            // Phase separator
            if (i < maxPhases - 1) {
                batch.setColor(Color.WHITE)
                batch.draw(batch.getColor().r, segmentX + phaseWidth - 1, y, 2, h)
            }
        }
        
        // Border
        batch.setColor(Color.WHITE)
        drawRect(x, y, w, h, 2f)
        
        // Phase indicator
        font.color = Color.WHITE
        uiLayout.setText(font, "BOSS PHASE $phase/$maxPhases")
        font.draw(batch, uiLayout, x + (w - uiLayout.width) / 2, y - 5)
        
        batch.setColor(Color.WHITE)
    }
    
    private fun drawRect(x: Float, y: Float, w: Float, h: Float, thickness: Float) {
        batch.draw(batch.getColor().r, x, y, w, thickness)
        batch.draw(batch.getColor().r, x, y + h - thickness, w, thickness)
        batch.draw(batch.getColor().r, x, y, thickness, h)
        batch.draw(batch.getColor().r, x + w - thickness, y, thickness, h)
    }
    
    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }
    
    override fun dispose() {
        batch.dispose()
        modelBatch.dispose()
        font.dispose()
    }
}
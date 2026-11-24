package com.animeshooter.systems

import com.animeshooter.ecs.*
import com.animeshooter.entities.EntityFactory
import com.badlogic.ashley.core.Engine
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

typealias GameWorldCallback = (String) -> Unit

class GameWorld {
    private val engine = PooledEngine()
    private val entityFactory = EntityFactory(engine)
    
    // Systems
    private val movementSystem = MovementSystem(0)
    private val playerMovementSystem = PlayerMovementSystem(1)
    private val weaponSystem = WeaponSystem(2)
    private val enemyAISystem = EnemyAISystem(3)
    private val bossAISystem = BossAISystem(4)
    private val projectileSystem = ProjectileSystem(5)
    private val pickupSystem = PickupSystem(6)
    private val ultimateSystem = UltimateSystem(7)
    private val collisionSystem = CollisionSystem(10)
    private val cleanupSystem = CleanupSystem(100)
    
    private var playerEntity: Entity? = null
    private var selectedShipType: ShipType = ShipType.INTERCEPTOR
    
    // Game state
    var score = 0
    var highScore = 0
    var currentStage = 1
    var currentWave = 1
    var enemiesInWave = 0
    var isBossWave = false
    var bossSpawned = false
    var waveTimer = 0f
    var stageClearDelay = 0f
    var gameOverDelay = 0f
    var isGameOver = false
    var isStageClear = false
    var isPaused = false
    
    // Callbacks
    var onGameOver: GameWorldCallback? = null
    var onStageClear: GameWorldCallback? = null
    var onScoreChange: ((Int) -> Unit)? = null
    var onStageChange: ((Int, Int) -> Unit)? = null
    var onHealthChange: ((Int, Int) -> Unit)? = null
    var onUltimateChange: ((Float, Float) -> Unit)? = null
    var onWeaponLevelChange: ((Int) -> Unit)? = null
    
    init {
        setupSystems()
    }
    
    private fun setupSystems() {
        engine.addSystem(movementSystem)
        engine.addSystem(playerMovementSystem)
        engine.addSystem(weaponSystem)
        engine.addSystem(enemyAISystem)
        engine.addSystem(bossAISystem)
        engine.addSystem(projectileSystem)
        engine.addSystem(pickupSystem)
        engine.addSystem(ultimateSystem)
        engine.addSystem(collisionSystem)
        engine.addSystem(cleanupSystem)
        
        // Set references
        weaponSystem.setEntityFactory(entityFactory)
        weaponSystem.setEngine(engine)
        
        enemyAISystem.setEntityFactory(entityFactory)
        enemyAISystem.setEngine(engine)
        
        bossAISystem.setEntityFactory(entityFactory)
        bossAISystem.setEngine(engine)
        
        projectileSystem.setEngine(engine)
        
        pickupSystem.setEngine(engine)
        
        ultimateSystem.setEntityFactory(entityFactory)
        ultimateSystem.setEngine(engine)
        
        collisionSystem.setEngine(engine)
        collisionSystem.setEntityFactory(entityFactory)
        collisionSystem.setGameWorld(this)
        
        cleanupSystem.setEngine(engine)
        cleanupSystem.setGameWorld(this)
    }
    
    fun startGame(shipType: ShipType) {
        selectedShipType = shipType
        resetGameState()
        spawnPlayer()
        startStage(1)
    }
    
    private fun resetGameState() {
        score = 0
        currentStage = 1
        currentWave = 1
        isGameOver = false
        isStageClear = false
        isPaused = false
        onScoreChange?.invoke(score)
        onStageChange?.invoke(currentStage, currentWave)
    }
    
    private fun spawnPlayer() {
        // Remove old player if exists
        playerEntity?.let { engine.removeEntity(it) }
        
        playerEntity = entityFactory.createPlayer(selectedShipType)
        engine.addEntity(playerEntity!!)
        
        // Set player references
        playerMovementSystem.setPlayerEntity(playerEntity)
        weaponSystem.setPlayerEntity(playerEntity)
        enemyAISystem.setPlayerEntity(playerEntity)
        bossAISystem.setPlayerEntity(playerEntity)
        projectileSystem.setPlayerEntity(playerEntity)
        ultimateSystem.setPlayerEntity(playerEntity)
        
        val playerComp = playerEntity!!.getComponent(PlayerComponent::class.java)
        val healthComp = playerEntity!!.getComponent(HealthComponent::class.java)
        val ultimateComp = playerEntity!!.getComponent(UltimateComponent::class.java)
        
        onHealthChange?.invoke(healthComp.currentHealth, healthComp.maxHealth)
        onUltimateChange?.invoke(playerComp.ultimateMeter, playerComp.maxUltimateMeter)
        onWeaponLevelChange?.invoke(playerComp.weaponLevel)
    }
    
    fun setPlayerInput(touchX: Float, touchY: Float, isTouching: Boolean, ultimatePressed: Boolean) {
        val input = playerEntity?.getComponent(InputComponent::class.java) ?: return
        input.touchX = touchX
        input.touchY = touchY
        input.isTouching = isTouching
        input.ultimatePressed = ultimatePressed
        
        if (ultimatePressed) {
            ultimateSystem.tryActivateUltimate()
        }
    }
    
    fun update(deltaTime: Float) {
        if (isPaused || isGameOver) return
        
        engine.update(deltaTime)
        collisionSystem.checkCollisions()
        
        updateStage(deltaTime)
        updateUI()
    }
    
    private fun updateUI() {
        val player = playerEntity?.getComponent(PlayerComponent::class.java) ?: return
        val health = playerEntity?.getComponent(HealthComponent::class.java) ?: return
        val ultimate = playerEntity?.getComponent(UltimateComponent::class.java) ?: return
        
        onHealthChange?.invoke(health.currentHealth, health.maxHealth)
        onUltimateChange?.invoke(player.ultimateMeter, player.maxUltimateMeter)
        onWeaponLevelChange?.invoke(player.weaponLevel)
    }
    
    private fun updateStage(deltaTime: Float) {
        if (isBossWave) {
            if (!bossSpawned) {
                spawnBoss()
                bossSpawned = true
            }
            
            // Check if boss is defeated
            val bosses = engine.getEntitiesFor(
                com.badlogic.ashley.core.Family.all(EnemyComponent::class.java).get()
            ).filter { it.getComponent(EnemyComponent::class.java)?.isBoss == true }
            
            if (bosses.isEmpty() && bossSpawned) {
                onBossDefeated()
            }
        } else {
            waveTimer += deltaTime
            
            // Check if wave cleared
            val enemies = engine.getEntitiesFor(
                com.badlogic.ashley.core.Family.all(EnemyComponent::class.java).get()
            ).filter { !(it.getComponent(EnemyComponent::class.java)?.isBoss ?: false) }
            
            if (enemies.isEmpty() && enemiesInWave > 0) {
                onWaveCleared()
            }
            
            // Spawn enemies periodically
            if (waveTimer > 1.5f && enemiesInWave > 0 && enemies.size < 8) {
                spawnWaveEnemy()
                waveTimer = 0f
            }
        }
        
        if (isStageClear) {
            stageClearDelay += deltaTime
            if (stageClearDelay > 2f) {
                nextStage()
            }
        }
        
        if (isGameOver) {
            gameOverDelay += deltaTime
            if (gameOverDelay > 2f) {
                onGameOver?.invoke("Game Over")
            }
        }
    }
    
    private fun startStage(stage: Int) {
        currentStage = stage
        currentWave = 1
        isBossWave = false
        bossSpawned = false
        enemiesInWave = 0
        waveTimer = 0f
        onStageChange?.invoke(currentStage, currentWave)
        startWave()
    }
    
    private fun startWave() {
        enemiesInWave = 3 + currentStage + (currentWave - 1) * 2
        waveTimer = 0f
        onStageChange?.invoke(currentStage, currentWave)
    }
    
    private fun spawnWaveEnemy() {
        if (enemiesInWave <= 0) return
        
        val screenWidth = Gdx.graphics.width.toFloat()
        val x = MathUtils.random(50f, screenWidth - 50f)
        val y = Gdx.graphics.height.toFloat() + 50f
        
        val enemy = if (MathUtils.random() < 0.6f) {
            entityFactory.createStraightShooter(x, y, currentStage)
        } else {
            entityFactory.createSpreadShooter(x, y, currentStage)
        }
        
        engine.addEntity(enemy)
        enemiesInWave--
    }
    
    private fun spawnBoss() {
        val boss = entityFactory.createBoss(currentStage)
        engine.addEntity(boss)
    }
    
    private fun onWaveCleared() {
        currentWave++
        if (currentWave > 3) {
            isBossWave = true
            bossSpawned = false
        } else {
            startWave()
        }
    }
    
    fun onEnemyDestroyed() {
        // Called when regular enemy destroyed
    }
    
    fun onBossDefeated() {
        val bonus = 1000 * currentStage
        addScore(bonus)
        isStageClear = true
        stageClearDelay = 0f
    }
    
    fun onPlayerDeath() {
        isGameOver = true
        gameOverDelay = 0f
        
        if (score > highScore) {
            highScore = score
        }
    }
    
    private fun nextStage() {
        if (currentStage >= 5) {
            // Game completed all stages
            isGameOver = true
            gameOverDelay = 0f
            onGameOver?.invoke("Victory!")
        } else {
            isStageClear = false
            startStage(currentStage + 1)
        }
    }
    
    fun addScore(points: Int) {
        score += points
        onScoreChange?.invoke(score)
    }
    
    fun getEngine(): Engine = engine
    
    fun getPlayerEntity(): Entity? = playerEntity
    
    fun pause() { isPaused = true }
    fun resume() { isPaused = false }
    fun togglePause() { isPaused = !isPaused }
    
    fun isPaused(): Boolean = isPaused
    fun isGameOver(): Boolean = isGameOver
    fun isStageClear(): Boolean = isStageClear
    fun getScore(): Int = score
    fun getHighScore(): Int = highScore
    fun getCurrentStage(): Int = currentStage
    fun getCurrentWave(): Int = currentWave
    fun isBossWave(): Boolean = isBossWave
}
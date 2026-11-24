package com.animeshooter

import com.animeshooter.ecs.*
import com.animeshooter.entities.EntityFactory
import com.animeshooter.systems.GameWorld
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.backends.headless.HeadlessApplication
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

// Initialize headless LibGDX for testing
@BeforeAll
fun initLibGDX() {
    HeadlessApplicationConfiguration().also { config ->
        HeadlessApplication(object : com.badlogic.gdx.ApplicationListener {
            override fun create() {}
            override fun resize(width: Int, height: Int) {}
            override fun render() {}
            override fun pause() {}
            override fun resume() {}
            override fun dispose() {}
        }, config)
    }
}

class GameWorldTest {
    
    @Test
    fun testGameWorldCreation() {
        val world = GameWorld()
        assertNotNull(world)
        assertEquals(0, world.score)
        assertEquals(1, world.currentStage)
        assertEquals(1, world.currentWave)
        assertEquals(false, world.isGameOver)
        assertEquals(false, world.isPaused)
    }
    
    @Test
    fun testEntityFactoryCreatesPlayer() {
        val engine = PooledEngine()
        val factory = EntityFactory(engine)
        
        val player = factory.createPlayer(ShipType.INTERCEPTOR)
        
        assertNotNull(player)
        assertNotNull(player.getComponent(TransformComponent::class.java))
        assertNotNull(player.getComponent(PlayerComponent::class.java))
        assertNotNull(player.getComponent(WeaponComponent::class.java))
        assertNotNull(player.getComponent(UltimateComponent::class.java))
        assertNotNull(player.getComponent(ModelComponent::class.java))
        assertNotNull(player.getComponent(CollisionComponent::class.java))
        assertNotNull(player.getComponent(HealthComponent::class.java))
        assertNotNull(player.getComponent(InputComponent::class.java))
        
        val playerComp = player.getComponent(PlayerComponent::class.java)
        assertEquals(ShipType.INTERCEPTOR, playerComp.shipType)
        assertEquals(1, playerComp.weaponLevel)
        assertEquals(3, playerComp.health)
    }
    
    @Test
    fun testEntityFactoryCreatesEnemies() {
        val engine = PooledEngine()
        val factory = EntityFactory(engine)
        
        val straight = factory.createStraightShooter(100f, 500f, 1)
        assertNotNull(straight)
        val straightComp = straight.getComponent(EnemyComponent::class.java)
        assertEquals(EnemyType.STRAIGHT, straightComp.enemyType)
        assertEquals(false, straightComp.isBoss)
        
        val spread = factory.createSpreadShooter(200f, 500f, 1)
        assertNotNull(spread)
        val spreadComp = spread.getComponent(EnemyComponent::class.java)
        assertEquals(EnemyType.SPREAD, spreadComp.enemyType)
        
        val boss = factory.createBoss(1)
        assertNotNull(boss)
        val bossComp = boss.getComponent(EnemyComponent::class.java)
        assertEquals(EnemyType.BOSS, bossComp.enemyType)
        assertEquals(true, bossComp.isBoss)
        assertEquals(3, bossComp.maxBossPhases)
    }
    
    @Test
    fun testEntityFactoryCreatesPickups() {
        val engine = PooledEngine()
        val factory = EntityFactory(engine)
        
        val weaponUp = factory.createPickup(100f, 500f, PickupType.WEAPON_UPGRADE)
        assertNotNull(weaponUp)
        val pickupComp = weaponUp.getComponent(PickupComponent::class.java)
        assertEquals(PickupType.WEAPON_UPGRADE, pickupComp.pickupType)
        
        val ultCharge = factory.createPickup(100f, 500f, PickupType.ULTIMATE_CHARGE)
        assertNotNull(ultCharge)
        assertEquals(PickupType.ULTIMATE_CHARGE, ultCharge.getComponent(PickupComponent::class.java).pickupType)
        
        val health = factory.createPickup(100f, 500f, PickupType.HEALTH)
        assertNotNull(health)
        assertEquals(PickupType.HEALTH, health.getComponent(PickupComponent::class.java).pickupType)
        
        val score = factory.createPickup(100f, 500f, PickupType.SCORE_BONUS)
        assertNotNull(score)
        assertEquals(PickupType.SCORE_BONUS, score.getComponent(PickupComponent::class.java).pickupType)
    }
    
    @Test
    fun testWeaponUpgrade() {
        val engine = PooledEngine()
        val factory = EntityFactory(engine)
        
        val player = factory.createPlayer(ShipType.INTERCEPTOR)
        val weaponComp = player.getComponent(WeaponComponent::class.java)
        val playerComp = player.getComponent(PlayerComponent::class.java)
        
        val initialFireRate = weaponComp.fireRate
        val initialLevel = playerComp.weaponLevel
        
        factory.upgradePlayerWeapon(player)
        
        assertEquals(initialLevel + 1, playerComp.weaponLevel)
        assertTrue(weaponComp.fireRate < initialFireRate) // Fire rate should improve (decrease)
    }
    
    @Test
    fun testAllShipTypesHaveUniqueUltimates() {
        val engine = PooledEngine()
        val factory = EntityFactory(engine)
        
        val interceptor = factory.createPlayer(ShipType.INTERCEPTOR)
        val balanced = factory.createPlayer(ShipType.BALANCED)
        val heavy = factory.createPlayer(ShipType.HEAVY)
        
        val interceptorUlt = interceptor.getComponent(UltimateComponent::class.java)
        val balancedUlt = balanced.getComponent(UltimateComponent::class.java)
        val heavyUlt = heavy.getComponent(UltimateComponent::class.java)
        
        assertEquals(UltimateType.INTERCEPTOR_BURST, interceptorUlt.ultimateType)
        assertEquals(UltimateType.BALANCED_HOMING, balancedUlt.ultimateType)
        assertEquals(UltimateType.HEAVY_NOVA, heavyUlt.ultimateType)
    }
}
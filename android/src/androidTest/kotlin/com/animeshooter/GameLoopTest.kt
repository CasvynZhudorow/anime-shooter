package com.animeshooter

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.animeshooter.ecs.ShipType
import com.animeshooter.entities.EntityFactory
import com.animeshooter.systems.GameWorld
import com.badlogic.ashley.core.PooledEngine
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class GameLoopTest {
    
    @Test
    fun testGameLoopInitialization() {
        val context: Context = ApplicationProvider.getApplicationContext()
        assertNotNull(context)
        
        val world = GameWorld()
        world.startGame(ShipType.INTERCEPTOR)
        
        assertEquals(1, world.currentStage)
        assertEquals(1, world.currentWave)
        assertEquals(0, world.score)
        assertEquals(false, world.isGameOver)
        assertNotNull(world.getPlayerEntity())
        
        val player = world.getPlayerEntity()!!
        val playerComp = player.getComponent(com.animeshooter.ecs.PlayerComponent::class.java)
        assertEquals(ShipType.INTERCEPTOR, playerComp.shipType)
        assertEquals(3, playerComp.health)
    }
    
    @Test
    fun testGameLoopUpdate() {
        val world = GameWorld()
        world.startGame(ShipType.BALANCED)
        
        // Simulate a few frames
        for (i in 0..10) {
            world.update(1/60f)
        }
        
        assertEquals(false, world.isGameOver)
        assertNotNull(world.getPlayerEntity())
    }
    
    @Test
    fun testPlayerInputHandling() {
        val world = GameWorld()
        world.startGame(ShipType.HEAVY)
        
        val player = world.getPlayerEntity()!!
        val initialTransform = player.getComponent(com.animeshooter.ecs.TransformComponent::class.java)
        val initialX = initialTransform.position.x
        val initialY = initialTransform.position.y
        
        // Simulate touch input
        world.setPlayerInput(300f, 200f, true, false)
        world.update(1/60f)
        
        val newTransform = player.getComponent(com.animeshooter.ecs.TransformComponent::class.java)
        // Player should have moved towards touch position
        assertTrue(newTransform.position.x != initialX || newTransform.position.y != initialY)
    }
    
    @Test
    fun testUltimateActivation() {
        val world = GameWorld()
        world.startGame(ShipType.INTERCEPTOR)
        
        val player = world.getPlayerEntity()!!
        val playerComp = player.getComponent(com.animeshooter.ecs.PlayerComponent::class.java)
        val ultimateComp = player.getComponent(com.animeshooter.ecs.UltimateComponent::class.java)
        
        // Fill ultimate meter
        playerComp.ultimateMeter = playerComp.maxUltimateMeter
        
        // Try to activate ultimate
        world.setPlayerInput(0f, 0f, false, true)
        world.update(1/60f)
        
        assertEquals(true, ultimateComp.isActive)
        assertEquals(0f, playerComp.ultimateMeter)
    }
    
    @Test
    fun testScoreTracking() {
        val world = GameWorld()
        world.startGame(ShipType.INTERCEPTOR)
        
        world.addScore(100)
        assertEquals(100, world.score)
        
        world.addScore(250)
        assertEquals(350, world.score)
    }
    
    @Test
    fun testPauseResume() {
        val world = GameWorld()
        world.startGame(ShipType.INTERCEPTOR)
        
        assertEquals(false, world.isPaused())
        
        world.pause()
        assertEquals(true, world.isPaused())
        
        world.resume()
        assertEquals(false, world.isPaused())
        
        world.togglePause()
        assertEquals(true, world.isPaused())
    }
    
    @Test
    fun testAllShipTypes() {
        for (shipType in ShipType.values()) {
            val world = GameWorld()
            world.startGame(shipType)
            
            val player = world.getPlayerEntity()!!
            val playerComp = player.getComponent(com.animeshooter.ecs.PlayerComponent::class.java)
            
            assertEquals(shipType, playerComp.shipType)
            assertNotNull(playerComp.ultimateType)
            assertTrue(playerComp.moveSpeed > 0f)
        }
    }
}
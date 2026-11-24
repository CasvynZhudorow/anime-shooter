package com.animeshooter.models

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g3d.Material
import com.badlogic.gdx.graphics.g3d.Model
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder
import com.badlogic.gdx.math.Vector3

object ModelGenerator {
    
    private val modelBuilder = ModelBuilder()
    
    fun createPlayerModel(shipType: com.animeshooter.ecs.ShipType): ModelInstance {
        val color = when (shipType) {
            com.animeshooter.ecs.ShipType.INTERCEPTOR -> Color.CYAN
            com.animeshooter.ecs.ShipType.BALANCED -> Color.GREEN
            com.animeshooter.ecs.ShipType.HEAVY -> Color.ORANGE
        }
        val model = createShipModel(shipType, color)
        return ModelInstance(model)
    }
    
    private fun createShipModel(shipType: com.animeshooter.ecs.ShipType, color: Color): Model {
        val material = Material(ColorAttribute.createDiffuse(color))
        
        return when (shipType) {
            com.animeshooter.ecs.ShipType.INTERCEPTOR -> {
                // Sleek, narrow fighter
                modelBuilder.begin()
                // Main body - long and narrow
                modelBuilder.part("body", GL20.GL_TRIANGLES, material).apply { box(8f, 24f, 4f) }
                // Cockpit
                modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material).apply { box(4f, 6f, 3f); transform.setToTranslation(0f, 8f, 0f) }
                // Wings
                modelBuilder.part("wings", GL20.GL_TRIANGLES, material).apply { box(20f, 2f, 1f) }
                // Engine
                modelBuilder.part("engine", GL20.GL_TRIANGLES, 
                    Material(ColorAttribute.createDiffuse(Color.valueOf("00FFFF")))).apply { box(6f, 4f, 3f); transform.setToTranslation(0f, -10f, 0f) }
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.BALANCED -> {
                // Classic fighter shape
                modelBuilder.begin()
                // Main body
                modelBuilder.part("body", GL20.GL_TRIANGLES, material).apply { box(12f, 18f, 4f) }
                // Cockpit
                modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material).apply { box(6f, 6f, 4f); transform.setToTranslation(0f, 6f, 0f) }
                // Wings
                modelBuilder.part("wings", GL20.GL_TRIANGLES, material).apply { box(18f, 2f, 2f); transform.setToTranslation(0f, 2f, 0f) }
                // Engines
                modelBuilder.part("engines", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("00FF00")))).apply { box(8f, 4f, 3f); transform.setToTranslation(0f, -7f, 0f) }
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.HEAVY -> {
                // Broad, heavy bomber
                modelBuilder.begin()
                // Main body - wide
                modelBuilder.part("body", GL20.GL_TRIANGLES, material).apply { box(20f, 16f, 6f) }
                // Cockpit
                modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material).apply { box(8f, 6f, 4f); transform.setToTranslation(0f, 6f, 0f) }
                // Large wings
                modelBuilder.part("wings", GL20.GL_TRIANGLES, material).apply { box(28f, 3f, 2f) }
                // Twin engines
                modelBuilder.part("engine1", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500")))).apply { box(6f, 5f, 4f); transform.setToTranslation(-6f, -8f, 0f) }
                modelBuilder.part("engine2", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500")))).apply { box(6f, 5f, 4f); transform.setToTranslation(6f, -8f, 0f) }
                // Gun barrels
                modelBuilder.part("guns", GL20.GL_TRIANGLES, material).apply { box(2f, 10f, 2f); transform.setToTranslation(-5f, 4f, 2f) }
                modelBuilder.part("guns2", GL20.GL_TRIANGLES, material).apply { box(2f, 10f, 2f); transform.setToTranslation(5f, 4f, 2f) }
                modelBuilder.end()
            }
        }
    }
    
    fun createEnemyModel(enemyType: com.animeshooter.ecs.EnemyType): ModelInstance {
        val (model, color) = when (enemyType) {
            com.animeshooter.ecs.EnemyType.STRAIGHT -> {
                modelBuilder.begin()
                modelBuilder.part("body", GL20.GL_TRIANGLES, 
                    Material(ColorAttribute.createDiffuse(Color.RED))).apply { box(10f, 10f, 4f) }
                // Sensor eye
                modelBuilder.part("eye", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FF0000")))).apply { sphere(2f, 2f, 2f, 8, 8); transform.setToTranslation(0f, 4f, 2f) }
                modelBuilder.end() to Color.RED
            }
            com.animeshooter.ecs.EnemyType.SPREAD -> {
                modelBuilder.begin()
                modelBuilder.part("body", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA))).apply { box(12f, 12f, 4f) }
                // Three prongs
                modelBuilder.part("prong1", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA))).apply { box(3f, 8f, 2f); transform.setToTranslation(-5f, 2f, 0f) }
                modelBuilder.part("prong2", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA))).apply { box(3f, 8f, 2f); transform.setToTranslation(0f, 6f, 0f) }
                modelBuilder.part("prong3", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA))).apply { box(3f, 8f, 2f); transform.setToTranslation(5f, 2f, 0f) }
                // Energy cores
                modelBuilder.part("core", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FF00FF")))).apply { sphere(1.5f, 1.5f, 1.5f, 8, 8); transform.setToTranslation(0f, 0f, 2f) }
                modelBuilder.end() to Color.MAGENTA
            }
            com.animeshooter.ecs.EnemyType.BOSS -> {
                modelBuilder.begin()
                // Central core
                modelBuilder.part("core", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("000080")))).apply { sphere(20f, 20f, 20f, 16, 16) }
                // Rotating shield segments
                for (i in 0..2) {
                    modelBuilder.part("shield$i", GL20.GL_TRIANGLES,
                        Material(ColorAttribute.createDiffuse(Color.valueOf("FFD700")))).apply { box(50f, 5f, 5f) }
                }
                // Weapon ports
                modelBuilder.part("weapons", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.RED))).apply { box(4f, 4f, 8f); transform.setToTranslation(0f, 0f, 22f) }
                modelBuilder.end() to Color.valueOf("FFD700")
            }
        }
        return ModelInstance(model)
    }
    
    fun createProjectileModel(source: com.animeshooter.ecs.ProjectileSource): ModelInstance {
        val (model, color) = when (source) {
            com.animeshooter.ecs.ProjectileSource.PLAYER -> {
                modelBuilder.begin()
                modelBuilder.part("projectile", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.CYAN))).apply { box(2f, 8f, 2f) }
                modelBuilder.end() to Color.CYAN
            }
            com.animeshooter.ecs.ProjectileSource.ENEMY -> {
                modelBuilder.begin()
                modelBuilder.part("projectile", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.RED))).apply { box(2f, 8f, 2f) }
                modelBuilder.end() to Color.RED
            }
        }
        return ModelInstance(model)
    }
    
    fun createPickupModel(pickupType: com.animeshooter.ecs.PickupType): ModelInstance {
        val (model, color) = when (pickupType) {
            com.animeshooter.ecs.PickupType.WEAPON_UPGRADE -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.GREEN))).apply { box(6f, 6f, 6f) }
                modelBuilder.end() to Color.GREEN
            }
            com.animeshooter.ecs.PickupType.ULTIMATE_CHARGE -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.GOLD))).apply { box(6f, 6f, 6f) }
                modelBuilder.end() to Color.GOLD
            }
            com.animeshooter.ecs.PickupType.HEALTH -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.RED))).apply { box(6f, 6f, 6f) }
                modelBuilder.end() to Color.RED
            }
            com.animeshooter.ecs.PickupType.SCORE_BONUS -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES,
                    Material(ColorAttribute.createDiffuse(Color.WHITE))).apply { box(6f, 6f, 6f) }
                modelBuilder.end() to Color.WHITE
            }
        }
        return ModelInstance(model)
    }
    
    fun createExplosionParticle(): ModelInstance {
        modelBuilder.begin()
        modelBuilder.part("particle", GL20.GL_TRIANGLES,
            Material(ColorAttribute.createDiffuse(Color.ORANGE))).apply { box(4f, 4f, 4f) }
        return ModelInstance(modelBuilder.end())
    }
}
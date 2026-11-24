package com.animeshooter.models

import com.badlogic.gdx.graphics.Color
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
                modelBuilder.part("body", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(8f, 24f, 4f)
                // Cockpit
                modelBuilder.part("cockpit", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(4f, 6f, 3f).translate(0f, 8f, 0f)
                // Wings
                modelBuilder.part("wings", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(20f, 2f, 1f).translate(0f, 0f, 0f)
                // Engine
                modelBuilder.part("engine", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, 
                    Material(ColorAttribute.createDiffuse(Color.valueOf("00FFFF"))))
                    .box(6f, 4f, 3f).translate(0f, -10f, 0f)
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.BALANCED -> {
                // Classic fighter shape
                modelBuilder.begin()
                // Main body
                modelBuilder.part("body", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(12f, 18f, 4f)
                // Cockpit
                modelBuilder.part("cockpit", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(6f, 6f, 4f).translate(0f, 6f, 0f)
                // Wings
                modelBuilder.part("wings", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(18f, 2f, 2f).translate(0f, 2f, 0f)
                // Engines
                modelBuilder.part("engines", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("00FF00"))))
                    .box(8f, 4f, 3f).translate(0f, -7f, 0f)
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.HEAVY -> {
                // Broad, heavy bomber
                modelBuilder.begin()
                // Main body - wide
                modelBuilder.part("body", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(20f, 16f, 6f)
                // Cockpit
                modelBuilder.part("cockpit", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(8f, 6f, 4f).translate(0f, 6f, 0f)
                // Large wings
                modelBuilder.part("wings", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(28f, 3f, 2f).translate(0f, 0f, 0f)
                // Twin engines
                modelBuilder.part("engine1", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500"))))
                    .box(6f, 5f, 4f).translate(-6f, -8f, 0f)
                modelBuilder.part("engine2", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500"))))
                    .box(6f, 5f, 4f).translate(6f, -8f, 0f)
                // Gun barrels
                modelBuilder.part("guns", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(2f, 10f, 2f).translate(-5f, 4f, 2f)
                modelBuilder.part("guns2", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent, material)
                    .box(2f, 10f, 2f).translate(5f, 4f, 2f)
                modelBuilder.end()
            }
        }
    }
    
    fun createEnemyModel(enemyType: com.animeshooter.ecs.EnemyType): ModelInstance {
        val (model, color) = when (enemyType) {
            com.animeshooter.ecs.EnemyType.STRAIGHT -> {
                val model = modelBuilder.begin()
                model.part("body", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.RED)))
                    .box(10f, 10f, 4f)
                // Sensor eye
                model.part("eye", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FF0000"))))
                    .sphere(2f, 2f, 2f, 8, 8).translate(0f, 4f, 2f)
                model.end() to Color.RED
            }
            com.animeshooter.ecs.EnemyType.SPREAD -> {
                val model = modelBuilder.begin()
                model.part("body", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
                    .box(12f, 12f, 4f)
                // Three prongs
                model.part("prong1", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
                    .box(3f, 8f, 2f).translate(-5f, 2f, 0f)
                model.part("prong2", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
                    .box(3f, 8f, 2f).translate(0f, 6f, 0f)
                model.part("prong3", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.MAGENTA)))
                    .box(3f, 8f, 2f).translate(5f, 2f, 0f)
                // Energy cores
                model.part("core", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("FF00FF"))))
                    .sphere(1.5f, 1.5f, 1.5f, 8, 8).translate(0f, 0f, 2f)
                model.end() to Color.MAGENTA
            }
            com.animeshooter.ecs.EnemyType.BOSS -> {
                val model = modelBuilder.begin()
                // Central core
                model.part("core", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.valueOf("000080"))))
                    .sphere(20f, 20f, 20f, 16, 16)
                // Rotating shield segments
                for (i in 0..2) {
                    model.part("shield$i", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                        Material(ColorAttribute.createDiffuse(Color.valueOf("FFD700"))))
                        .box(50f, 5f, 5f).translate(0f, 0f, 0f)
                }
                // Weapon ports
                model.part("weapons", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.RED)))
                    .box(4f, 4f, 8f).translate(0f, 0f, 22f)
                model.end() to Color.valueOf("FFD700")
            }
        }
        return ModelInstance(model)
    }
    
    fun createProjectileModel(source: com.animeshooter.ecs.ProjectileSource): ModelInstance {
        val (model, color) = when (source) {
            com.animeshooter.ecs.ProjectileSource.PLAYER -> {
                val model = modelBuilder.begin()
                model.part("projectile", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.CYAN)))
                    .box(2f, 8f, 2f)
                model.end() to Color.CYAN
            }
            com.animeshooter.ecs.ProjectileSource.ENEMY -> {
                val model = modelBuilder.begin()
                model.part("projectile", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.RED)))
                    .box(2f, 8f, 2f)
                model.end() to Color.RED
            }
        }
        return ModelInstance(model)
    }
    
    fun createPickupModel(pickupType: com.animeshooter.ecs.PickupType): ModelInstance {
        val (model, color) = when (pickupType) {
            com.animeshooter.ecs.PickupType.WEAPON_UPGRADE -> {
                val model = modelBuilder.begin()
                model.part("pickup", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.GREEN)))
                    .box(6f, 6f, 6f)
                model.end() to Color.GREEN
            }
            com.animeshooter.ecs.PickupType.ULTIMATE_CHARGE -> {
                val model = modelBuilder.begin()
                model.part("pickup", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.GOLD)))
                    .box(6f, 6f, 6f)
                model.end() to Color.GOLD
            }
            com.animeshooter.ecs.PickupType.HEALTH -> {
                val model = modelBuilder.begin()
                model.part("pickup", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.RED)))
                    .box(6f, 6f, 6f)
                model.end() to Color.RED
            }
            com.animeshooter.ecs.PickupType.SCORE_BONUS -> {
                val model = modelBuilder.begin()
                model.part("pickup", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
                    Material(ColorAttribute.createDiffuse(Color.WHITE)))
                    .box(6f, 6f, 6f)
                model.end() to Color.WHITE
            }
        }
        return ModelInstance(model)
    }
    
    fun createExplosionParticle(): ModelInstance {
        val model = modelBuilder.begin()
        model.part("particle", com.badlogic.gdx.graphics.glutils.ShaderProgram.PositionTextureColorNormalBinormalTangent,
            Material(ColorAttribute.createDiffuse(Color.ORANGE)))
            .box(4f, 4f, 4f)
        model.end()
        return ModelInstance(model)
    }
}
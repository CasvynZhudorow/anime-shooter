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
                modelBuilder.begin()
                // Main body - long and narrow
                modelBuilder.part("body", GL20.GL_TRIANGLES, material, null)
                // Cockpit
                val cockpitPart = modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material, null)
                cockpitPart.box(4f, 6f, 3f)
                cockpitPart.transform.setToTranslation(0f, 8f, 0f)
                // Wings
                modelBuilder.part("wings", GL20.GL_TRIANGLES, material, null)
                // Engine
                val enginePart = modelBuilder.part("engine", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("00FFFF"))), null)
                enginePart.box(6f, 4f, 3f)
                enginePart.transform.setToTranslation(0f, -10f, 0f)
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.BALANCED -> {
                modelBuilder.begin()
                modelBuilder.part("body", GL20.GL_TRIANGLES, material, null)
                val cockpitPart = modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material, null)
                cockpitPart.box(6f, 6f, 4f)
                cockpitPart.transform.setToTranslation(0f, 6f, 0f)
                val wingsPart = modelBuilder.part("wings", GL20.GL_TRIANGLES, material, null)
                wingsPart.box(18f, 2f, 2f)
                wingsPart.transform.setToTranslation(0f, 2f, 0f)
                val enginesPart = modelBuilder.part("engines", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("00FF00"))), null)
                enginesPart.box(8f, 4f, 3f)
                enginesPart.transform.setToTranslation(0f, -7f, 0f)
                modelBuilder.end()
            }
            com.animeshooter.ecs.ShipType.HEAVY -> {
                modelBuilder.begin()
                modelBuilder.part("body", GL20.GL_TRIANGLES, material, null)
                val cockpitPart = modelBuilder.part("cockpit", GL20.GL_TRIANGLES, material, null)
                cockpitPart.box(8f, 6f, 4f)
                cockpitPart.transform.setToTranslation(0f, 6f, 0f)
                modelBuilder.part("wings", GL20.GL_TRIANGLES, material, null)
                val engine1Part = modelBuilder.part("engine1", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500"))), null)
                engine1Part.box(6f, 5f, 4f)
                engine1Part.transform.setToTranslation(-6f, -8f, 0f)
                val engine2Part = modelBuilder.part("engine2", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("FFA500"))), null)
                engine2Part.box(6f, 5f, 4f)
                engine2Part.transform.setToTranslation(6f, -8f, 0f)
                val gunsPart = modelBuilder.part("guns", GL20.GL_TRIANGLES, material, null)
                gunsPart.box(2f, 10f, 2f)
                gunsPart.transform.setToTranslation(-5f, 4f, 2f)
                val guns2Part = modelBuilder.part("guns2", GL20.GL_TRIANGLES, material, null)
                guns2Part.box(2f, 10f, 2f)
                guns2Part.transform.setToTranslation(5f, 4f, 2f)
                modelBuilder.end()
            }
        }
    }
    
    fun createEnemyModel(enemyType: com.animeshooter.ecs.EnemyType): ModelInstance {
        val (model, color) = when (enemyType) {
            com.animeshooter.ecs.EnemyType.STRAIGHT -> {
                modelBuilder.begin()
                val bodyPart = modelBuilder.part("body", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.RED)), null)
                bodyPart.box(10f, 10f, 4f)
                val eyePart = modelBuilder.part("eye", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("FF0000"))), null)
                eyePart.sphere(2f, 2f, 2f, 8, 8)
                eyePart.transform.setToTranslation(0f, 4f, 2f)
                modelBuilder.end() to Color.RED
            }
            com.animeshooter.ecs.EnemyType.SPREAD -> {
                modelBuilder.begin()
                modelBuilder.part("body", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.MAGENTA)), null)
                val prong1Part = modelBuilder.part("prong1", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.MAGENTA)), null)
                prong1Part.box(3f, 8f, 2f)
                prong1Part.transform.setToTranslation(-5f, 2f, 0f)
                val prong2Part = modelBuilder.part("prong2", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.MAGENTA)), null)
                prong2Part.box(3f, 8f, 2f)
                prong2Part.transform.setToTranslation(0f, 6f, 0f)
                val prong3Part = modelBuilder.part("prong3", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.MAGENTA)), null)
                prong3Part.box(3f, 8f, 2f)
                prong3Part.transform.setToTranslation(5f, 2f, 0f)
                val corePart = modelBuilder.part("core", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("FF00FF"))), null)
                corePart.sphere(1.5f, 1.5f, 1.5f, 8, 8)
                corePart.transform.setToTranslation(0f, 0f, 2f)
                modelBuilder.end() to Color.MAGENTA
            }
            com.animeshooter.ecs.EnemyType.BOSS -> {
                modelBuilder.begin()
                val corePart = modelBuilder.part("core", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("000080"))), null)
                corePart.sphere(20f, 20f, 20f, 16, 16)
                for (i in 0..2) {
                    modelBuilder.part("shield$i", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.valueOf("FFD700"))), null)
                }
                val weaponsPart = modelBuilder.part("weapons", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.RED)), null)
                weaponsPart.box(4f, 4f, 8f)
                weaponsPart.transform.setToTranslation(0f, 0f, 22f)
                modelBuilder.end() to Color.valueOf("FFD700")
            }
        }
        return ModelInstance(model)
    }
    
    fun createProjectileModel(source: com.animeshooter.ecs.ProjectileSource): ModelInstance {
        val (model, color) = when (source) {
            com.animeshooter.ecs.ProjectileSource.PLAYER -> {
                modelBuilder.begin()
                modelBuilder.part("projectile", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.CYAN)), null)
                modelBuilder.end() to Color.CYAN
            }
            com.animeshooter.ecs.ProjectileSource.ENEMY -> {
                modelBuilder.begin()
                modelBuilder.part("projectile", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.RED)), null)
                modelBuilder.end() to Color.RED
            }
        }
        return ModelInstance(model)
    }
    
    fun createPickupModel(pickupType: com.animeshooter.ecs.PickupType): ModelInstance {
        val (model, color) = when (pickupType) {
            com.animeshooter.ecs.PickupType.WEAPON_UPGRADE -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.GREEN)), null)
                modelBuilder.end() to Color.GREEN
            }
            com.animeshooter.ecs.PickupType.ULTIMATE_CHARGE -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.GOLD)), null)
                modelBuilder.end() to Color.GOLD
            }
            com.animeshooter.ecs.PickupType.HEALTH -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.RED)), null)
                modelBuilder.end() to Color.RED
            }
            com.animeshooter.ecs.PickupType.SCORE_BONUS -> {
                modelBuilder.begin()
                modelBuilder.part("pickup", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.WHITE)), null)
                modelBuilder.end() to Color.WHITE
            }
        }
        return ModelInstance(model)
    }
    
    fun createExplosionParticle(): ModelInstance {
        modelBuilder.begin()
        modelBuilder.part("particle", GL20.GL_TRIANGLES, Material(ColorAttribute.createDiffuse(Color.ORANGE)), null)
        return ModelInstance(modelBuilder.end())
    }
}
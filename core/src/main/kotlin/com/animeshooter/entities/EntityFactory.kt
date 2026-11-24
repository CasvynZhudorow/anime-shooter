package com.animeshooter.entities

import com.animeshooter.ecs.*
import com.animeshooter.models.ModelGenerator
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.EntityEngine
import com.badlogic.ashley.core.PooledEngine
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g3d.ModelInstance
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Array

class EntityFactory(private val engine: PooledEngine) {
    
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()
    
    fun createPlayer(shipType: ShipType): Entity {
        val player = engine.createEntity()
        
        val transform = TransformComponent().apply {
            position.set(screenWidth / 2f, 100f, 0f)
        }
        
        val playerComp = PlayerComponent().apply {
            this.shipType = shipType
            weaponLevel = 1
            moveSpeed = when (shipType) {
                ShipType.INTERCEPTOR -> 400f
                ShipType.BALANCED -> 300f
                ShipType.HEAVY -> 200f
            }
            health = 3
            maxHealth = 3
            ultimateMeter = 0f
            maxUltimateMeter = 100f
            ultimateCooldown = 0f
            maxUltimateCooldown = 30f
        }
        
        val weaponComp = WeaponComponent().apply {
            configureForShipType(shipType, 1)
        }
        
        val ultimateComp = UltimateComponent().apply {
            ultimateType = when (shipType) {
                ShipType.INTERCEPTOR -> UltimateType.INTERCEPTOR_BURST
                ShipType.BALANCED -> UltimateType.BALANCED_HOMING
                ShipType.HEAVY -> UltimateType.HEAVY_NOVA
            }
            maxDuration = 5f
            maxCooldown = 30f
        }
        
        val modelComp = ModelComponent().apply {
            modelInstance = ModelGenerator.createPlayerModel(shipType)
            color = when (shipType) {
                ShipType.INTERCEPTOR -> Color.CYAN
                ShipType.BALANCED -> Color.GREEN
                ShipType.HEAVY -> Color.ORANGE
            }
        }
        
        val collisionComp = CollisionComponent().apply {
            radius = 16f
            collisionLayer = 1
            collisionMask = 2 | 4 // Enemy + Pickup
        }
        
        val healthComp = HealthComponent().apply {
            currentHealth = 3
            maxHealth = 3
        }
        
        player.add(transform)
        player.add(VelocityComponent())
        player.add(playerComp)
        player.add(weaponComp)
        player.add(ultimateComp)
        player.add(modelComp)
        player.add(collisionComp)
        player.add(healthComp)
        player.add(InputComponent())
        
        return player
    }
    
    private fun WeaponComponent.configureForShipType(shipType: ShipType, level: Int) {
        when (shipType) {
            ShipType.INTERCEPTOR -> {
                // Fast, piercing shots
                fireRate = 0.1f - (level - 1) * 0.01f
                projectileSpeed = 800f
                damage = 1 + (level - 1)
                spreadAngle = 0f
                projectileCount = 1
                isHoming = false
                pierceCount = level - 1 // Pierces through enemies
            }
            ShipType.BALANCED -> {
                // Homing shots
                fireRate = 0.18f - (level - 1) * 0.015f
                projectileSpeed = 500f
                damage = 1 + (level - 1)
                spreadAngle = 0f
                projectileCount = 1 + (level - 1) / 2
                isHoming = true
                pierceCount = 0
            }
            ShipType.HEAVY -> {
                // Wide spread
                fireRate = 0.25f - (level - 1) * 0.02f
                projectileSpeed = 450f
                damage = 2 + (level - 1)
                spreadAngle = (level - 1) * 8f // Degrees
                projectileCount = 3 + (level - 1)
                isHoming = false
                pierceCount = 0
            }
        }
    }
    
    fun upgradePlayerWeapon(player: Entity) {
        val playerComp = player.getComponent(PlayerComponent::class.java)
        val weaponComp = player.getComponent(WeaponComponent::class.java)
        
        if (playerComp.weaponLevel < playerComp.maxWeaponLevel) {
            playerComp.weaponLevel++
            weaponComp.configureForShipType(playerComp.shipType, playerComp.weaponLevel)
        }
    }
    
    fun createStraightShooter(x: Float, y: Float, stage: Int): Entity {
        val enemy = engine.createEntity()
        val health = 1 + stage / 2
        
        enemy.add(TransformComponent().apply { position.set(x, y, 0f) })
        enemy.add(VelocityComponent().apply { velocity.set(0f, -80f - stage * 10f) })
        enemy.add(EnemyComponent().apply {
            enemyType = EnemyType.STRAIGHT
            this.health = health
            maxHealth = health
            moveSpeed = 80f + stage * 10f
            fireRate = 1.5f - stage * 0.1f
            scoreValue = 10 + stage * 5
            dropChance = 0.15f
        })
        enemy.add(WeaponComponent().apply {
            fireRate = 1.5f - stage * 0.1f
            projectileSpeed = 300f
            damage = 1
            projectileCount = 1
            spreadAngle = 0f
        })
        enemy.add(ModelComponent().apply { modelInstance = ModelGenerator.createEnemyModel(EnemyType.STRAIGHT) })
        enemy.add(CollisionComponent().apply { radius = 14f; collisionLayer = 2; collisionMask = 1 })
        enemy.add(HealthComponent().apply { currentHealth = health; maxHealth = health })
        
        return enemy
    }
    
    fun createSpreadShooter(x: Float, y: Float, stage: Int): Entity {
        val enemy = engine.createEntity()
        val health = 2 + stage / 2
        
        enemy.add(TransformComponent().apply { position.set(x, y, 0f) })
        enemy.add(VelocityComponent().apply { velocity.set(0f, -60f - stage * 8f) })
        enemy.add(EnemyComponent().apply {
            enemyType = EnemyType.SPREAD
            this.health = health
            maxHealth = health
            moveSpeed = 60f + stage * 8f
            fireRate = 2f - stage * 0.1f
            scoreValue = 20 + stage * 5
            dropChance = 0.15f
        })
        enemy.add(WeaponComponent().apply {
            fireRate = 2f - stage * 0.1f
            projectileSpeed = 250f
            damage = 1
            projectileCount = 3
            spreadAngle = 30f
        })
        enemy.add(ModelComponent().apply { modelInstance = ModelGenerator.createEnemyModel(EnemyType.SPREAD) })
        enemy.add(CollisionComponent().apply { radius = 16f; collisionLayer = 2; collisionMask = 1 })
        enemy.add(HealthComponent().apply { currentHealth = health; maxHealth = health })
        
        return enemy
    }
    
    fun createBoss(stage: Int): Entity {
        val boss = engine.createEntity()
        val maxHealth = 50 + stage * 30
        
        boss.add(TransformComponent().apply { position.set(screenWidth / 2f, screenHeight + 100f, 0f) })
        boss.add(VelocityComponent().apply { velocity.set(0f, -30f) })
        boss.add(EnemyComponent().apply {
            enemyType = EnemyType.BOSS
            isBoss = true
            this.health = maxHealth
            maxHealth = maxHealth
            moveSpeed = 30f
            fireRate = 0.8f
            scoreValue = 500 + stage * 200
            dropChance = 1f
            bossPhase = 1
            maxBossPhases = 3
        })
        boss.add(WeaponComponent().apply {
            fireRate = 0.8f
            projectileSpeed = 200f
            damage = 2
            projectileCount = 5
            spreadAngle = 45f
        })
        boss.add(ModelComponent().apply { modelInstance = ModelGenerator.createEnemyModel(EnemyType.BOSS) })
        boss.add(CollisionComponent().apply { radius = 60f; collisionLayer = 2; collisionMask = 1 })
        boss.add(HealthComponent().apply { currentHealth = maxHealth; maxHealth = maxHealth })
        
        return boss
    }
    
    fun createPlayerProjectile(player: Entity, x: Float, y: Float, angleOffset: Float = 0f): Entity {
        val projectile = engine.createEntity()
        val playerComp = player.getComponent(PlayerComponent::class.java)
        val weaponComp = player.getComponent(WeaponComponent::class.java)
        val transform = player.getComponent(TransformComponent::class.java)
        
        val baseAngle = MathUtils.degreesToRadians * angleOffset
        val dir = Vector2(MathUtils.sin(baseAngle), MathUtils.cos(baseAngle))
        
        projectile.add(TransformComponent().apply { position.set(x, y, 0f) })
        projectile.add(VelocityComponent().apply { 
            velocity.set(dir).scl(weaponComp.projectileSpeed) 
        })
        projectile.add(ProjectileComponent().apply {
            source = ProjectileSource.PLAYER
            damage = weaponComp.damage
            speed = weaponComp.projectileSpeed
            direction.set(dir)
            isHoming = weaponComp.isHoming
            pierceRemaining = weaponComp.pierceCount
            lifetime = 3f
            maxLifetime = 3f
        })
        projectile.add(ModelComponent().apply { 
            modelInstance = ModelGenerator.createProjectileModel(ProjectileSource.PLAYER) 
        })
        projectile.add(CollisionComponent().apply { radius = 4f; collisionLayer = 1; collisionMask = 2 })
        
        return projectile
    }
    
    fun createEnemyProjectile(enemy: Entity, x: Float, y: Float, targetX: Float, targetY: Float, angleOffset: Float = 0f): Entity {
        val projectile = engine.createEntity()
        val weaponComp = enemy.getComponent(WeaponComponent::class.java)
        val enemyComp = enemy.getComponent(EnemyComponent::class.java)
        
        val dir = Vector2(targetX - x, targetY - y)
        if (angleOffset != 0f) {
            val angle = MathUtils.atan2(dir.y, dir.x) + MathUtils.degreesToRadians * angleOffset
            dir.set(MathUtils.cos(angle), MathUtils.sin(angle))
        } else {
            dir.nor()
        }
        
        projectile.add(TransformComponent().apply { position.set(x, y, 0f) })
        projectile.add(VelocityComponent().apply { 
            velocity.set(dir).scl(weaponComp.projectileSpeed) 
        })
        projectile.add(ProjectileComponent().apply {
            source = ProjectileSource.ENEMY
            damage = weaponComp.damage
            speed = weaponComp.projectileSpeed
            direction.set(dir)
            lifetime = 4f
            maxLifetime = 4f
        })
        projectile.add(ModelComponent().apply { 
            modelInstance = ModelGenerator.createProjectileModel(ProjectileSource.ENEMY) 
        })
        projectile.add(CollisionComponent().apply { radius = 4f; collisionLayer = 2; collisionMask = 1 })
        
        return projectile
    }
    
    fun createPickup(x: Float, y: Float, pickupType: PickupType): Entity {
        val pickup = engine.createEntity()
        
        pickup.add(TransformComponent().apply { position.set(x, y, 0f) })
        pickup.add(VelocityComponent().apply { velocity.set(0f, -80f) })
        pickup.add(PickupComponent().apply { 
            this.pickupType = pickupType
            value = when (pickupType) {
                PickupType.WEAPON_UPGRADE -> 1
                PickupType.ULTIMATE_CHARGE -> 25
                PickupType.HEALTH -> 1
                PickupType.SCORE_BONUS -> 100
            }
        })
        pickup.add(ModelComponent().apply { modelInstance = ModelGenerator.createPickupModel(pickupType) })
        pickup.add(CollisionComponent().apply { radius = 12f; collisionLayer = 4; collisionMask = 1 })
        
        return pickup
    }
    
    fun createExplosion(x: Float, y: Float): Entity {
        val explosion = engine.createEntity()
        
        explosion.add(TransformComponent().apply { position.set(x, y, 0f) })
        explosion.add(ParticleComponent().apply {
            particleType = ParticleType.EXPLOSION
            lifetime = 0f
            maxLifetime = 0.5f
            scale = 1f
            color.set(Color.ORANGE)
        })
        explosion.add(ModelComponent().apply { modelInstance = ModelGenerator.createExplosionParticle() })
        
        return explosion
    }
}
package com.animeshooter.systems

import com.animeshooter.ecs.*
import com.animeshooter.entities.EntityFactory
import com.badlogic.ashley.core.ComponentMapper
import com.badlogic.ashley.core.Entity
import com.badlogic.ashley.core.Family
import com.badlogic.ashley.systems.IteratingSystem
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import kotlin.math.sqrt

// Movement System - Priority 0
class MovementSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transformMapper.get(entity)
        val velocity = velocityMapper.get(entity)
        
        transform.position.x += velocity.velocity.x * deltaTime
        transform.position.y += velocity.velocity.y * deltaTime
    }
}

// Player Movement System - Priority 1
class PlayerMovementSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java, PlayerComponent::class.java, InputComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    private val playerMapper = ComponentMapper.getFor(PlayerComponent::class.java)
    private val inputMapper = ComponentMapper.getFor(InputComponent::class.java)
    
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()
    private val margin = 30f
    
    var playerEntity: Entity? = null
    
    fun setPlayerEntity(player: Entity?) {
        playerEntity = player
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transformMapper.get(entity)
        val velocity = velocityMapper.get(entity)
        val player = playerMapper.get(entity)
        val input = inputMapper.get(entity)
        
        if (input.isTouching) {
            val targetX = input.touchX
            val targetY = input.touchY
            
            val dx = targetX - transform.position.x
            val dy = targetY - transform.position.y
            val dist = sqrt(dx * dx + dy * dy)
            
            if (dist > 5f) {
                velocity.velocity.set(dx, dy).nor().scl(player.moveSpeed)
            } else {
                velocity.velocity.setZero()
            }
        } else {
            velocity.velocity.setZero()
        }
        
        // Clamp to screen bounds
        transform.position.x = MathUtils.clamp(transform.position.x, margin, screenWidth - margin)
        transform.position.y = MathUtils.clamp(transform.position.y, margin, screenHeight - margin)
    }
}

// Weapon System - Priority 2
class WeaponSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, PlayerComponent::class.java, WeaponComponent::class.java, InputComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val playerMapper = ComponentMapper.getFor(PlayerComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(WeaponComponent::class.java)
    private val inputMapper = ComponentMapper.getFor(InputComponent::class.java)
    
    var entityFactory: EntityFactory? = null
    var engine: com.badlogic.ashley.core.Engine? = null
    
    fun setEntityFactory(factory: EntityFactory) {
        entityFactory = factory
    }
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = playerMapper.get(entity)
        val weapon = weaponMapper.get(entity)
        val input = inputMapper.get(entity)
        val transform = transformMapper.get(entity)
        
        weapon.fireTimer += deltaTime
        
        // Auto-fire when touching
        if (input.isTouching && weapon.fireTimer >= weapon.fireRate) {
            fireWeapon(entity, transform, player, weapon)
            weapon.fireTimer = 0f
        }
    }
    
    private fun fireWeapon(entity: Entity, transform: TransformComponent, player: PlayerComponent, weapon: WeaponComponent) {
        val factory = entityFactory ?: return
        val eng = engine ?: return
        
        val baseX = transform.position.x
        val baseY = transform.position.y + 20f // Fire from front of ship
        
        when (player.shipType) {
            ShipType.INTERCEPTOR -> {
                // Single straight piercing shot
                val proj = factory.createPlayerProjectile(entity, baseX, baseY)
                eng.addEntity(proj)
            }
            ShipType.BALANCED -> {
                // Homing shots - slight spread if multiple
                for (i in 0 until weapon.projectileCount) {
                    val angle = if (weapon.projectileCount > 1) (i - (weapon.projectileCount - 1) / 2f) * 5f else 0f
                    val proj = factory.createPlayerProjectile(entity, baseX, baseY, angle)
                    eng.addEntity(proj)
                }
            }
            ShipType.HEAVY -> {
                // Wide spread
                for (i in 0 until weapon.projectileCount) {
                    val angle = (i - (weapon.projectileCount - 1) / 2f) * weapon.spreadAngle / (weapon.projectileCount - 1)
                    val proj = factory.createPlayerProjectile(entity, baseX, baseY, angle)
                    eng.addEntity(proj)
                }
            }
        }
    }
}

// Enemy AI System - Priority 3
class EnemyAISystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java, EnemyComponent::class.java, WeaponComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    private val enemyMapper = ComponentMapper.getFor(EnemyComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(WeaponComponent::class.java)
    
    var entityFactory: EntityFactory? = null
    var engine: com.badlogic.ashley.core.Engine? = null
    var playerEntity: Entity? = null
    
    fun setEntityFactory(factory: EntityFactory) {
        entityFactory = factory
    }
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setPlayerEntity(player: Entity?) {
        playerEntity = player
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val enemy = enemyMapper.get(entity)
        val weapon = weaponMapper.get(entity)
        val transform = transformMapper.get(entity)
        
        if (enemy.isBoss) {
            // Boss AI handled by BossAISystem
            return
        }
        
        weapon.fireTimer += deltaTime
        
        // Fire at player
        if (playerEntity != null && weapon.fireTimer >= weapon.fireRate) {
            val playerTransform = playerEntity!!.getComponent(TransformComponent::class.java)
            fireAtPlayer(entity, transform, enemy, weapon, playerTransform)
            weapon.fireTimer = 0f
        }
        
        // Remove if off screen
        if (transform.position.y < -50f) {
            engine?.removeEntity(entity)
        }
    }
    
    private fun fireAtPlayer(entity: Entity, transform: TransformComponent, enemy: EnemyComponent, weapon: WeaponComponent, playerTransform: TransformComponent) {
        val factory = entityFactory ?: return
        val eng = engine ?: return
        
        val startX = transform.position.x
        val startY = transform.position.y - 15f
        
        when (enemy.enemyType) {
            EnemyType.STRAIGHT -> {
                val proj = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y)
                eng.addEntity(proj)
            }
            EnemyType.SPREAD -> {
                for (i in 0 until weapon.projectileCount) {
                    val angle = (i - (weapon.projectileCount - 1) / 2f) * weapon.spreadAngle / (weapon.projectileCount - 1)
                    val proj = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y, angle)
                    eng.addEntity(proj)
                }
            }
            else -> {} // BOSS handled by BossAISystem
        }
    }
}

// Boss AI System - Priority 4
class BossAISystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java, EnemyComponent::class.java, WeaponComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    private val enemyMapper = ComponentMapper.getFor(EnemyComponent::class.java)
    private val weaponMapper = ComponentMapper.getFor(WeaponComponent::class.java)
    
    var entityFactory: EntityFactory? = null
    var engine: com.badlogic.ashley.core.Engine? = null
    var playerEntity: Entity? = null
    
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()
    private var moveTimer = 0f
    private var moveDir = 1
    
    fun setEntityFactory(factory: EntityFactory) {
        entityFactory = factory
    }
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setPlayerEntity(player: Entity?) {
        playerEntity = player
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val enemy = enemyMapper.get(entity)
        val weapon = weaponMapper.get(entity)
        val transform = transformMapper.get(entity)
        val velocity = velocityMapper.get(entity)
        
        if (!enemy.isBoss) return
        
        // Entry phase - move down to position
        if (transform.position.y > screenHeight * 0.7f) {
            velocity.velocity.set(0f, -enemy.moveSpeed)
            return
        }
        
        // Boss behavior based on phase
        moveTimer += deltaTime
        weapon.fireTimer += deltaTime
        
        // Horizontal movement
        if (moveTimer > 2f) {
            moveDir *= -1
            moveTimer = 0f
            velocity.velocity.x = moveDir * enemy.moveSpeed * 1.5f
        }
        
        // Clamp horizontal position
        transform.position.x = MathUtils.clamp(transform.position.x, 80f, screenWidth - 80f)
        if (transform.position.x <= 80f || transform.position.x >= screenWidth - 80f) {
            moveDir *= -1
        }
        
        // Phase-based attack patterns
        val healthPercent = enemy.health.toFloat() / enemy.maxHealth
        val newPhase = when {
            healthPercent > 0.66f -> 1
            healthPercent > 0.33f -> 2
            else -> 3
        }
        
        if (newPhase != enemy.bossPhase) {
            enemy.bossPhase = newPhase
            enemy.phaseTimer = 0f
        }
        
        enemy.phaseTimer += deltaTime
        
        // Fire patterns per phase
        if (playerEntity != null && weapon.fireTimer >= weapon.fireRate) {
            val playerTransform = playerEntity!!.getComponent(TransformComponent::class.java)
            fireBossPattern(entity, transform, enemy, weapon, playerTransform)
            weapon.fireTimer = 0f
        }
    }
    
    private fun fireBossPattern(entity: Entity, transform: TransformComponent, enemy: EnemyComponent, weapon: WeaponComponent, playerTransform: TransformComponent) {
        val factory = entityFactory ?: return
        val eng = engine ?: return
        
        val startX = transform.position.x
        val startY = transform.position.y - 30f
        
        when (enemy.bossPhase) {
            1 -> {
                // Phase 1: Spread pattern
                for (i in 0..4) {
                    val angle = (i - 2) * 20f
                    val proj = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y, angle)
                    eng.addEntity(proj)
                }
                weapon.fireRate = 1.2f
            }
            2 -> {
                // Phase 2: Spiral + aimed
                for (i in 0..5) {
                    val angle = (enemy.phaseTimer * 180f + i * 60f) % 360f
                    val proj = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y, angle)
                    eng.addEntity(proj)
                }
                // Also aimed shot
                val aimed = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y)
                eng.addEntity(aimed)
                weapon.fireRate = 0.8f
            }
            3 -> {
                // Phase 3: Dense spread + fast aimed
                for (i in 0..7) {
                    val angle = (i - 3.5f) * 15f
                    val proj = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y, angle)
                    eng.addEntity(proj)
                }
                // Fast aimed
                for (j in 0..1) {
                    val aimed = factory.createEnemyProjectile(entity, startX, startY, playerTransform.position.x, playerTransform.position.y)
                    (aimed.getComponent(VelocityComponent::class.java)?.velocity)?.scl(1.5f)
                    eng.addEntity(aimed)
                }
                weapon.fireRate = 0.6f
            }
        }
    }
}

// Projectile System - Priority 5
class ProjectileSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java, ProjectileComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    private val projectileMapper = ComponentMapper.getFor(ProjectileComponent::class.java)
    
    var engine: com.badlogic.ashley.core.Engine? = null
    var playerEntity: Entity? = null
    
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setPlayerEntity(player: Entity?) {
        playerEntity = player
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val projectile = projectileMapper.get(entity)
        val transform = transformMapper.get(entity)
        
        projectile.lifetime -= deltaTime
        
        // Homing logic
        if (projectile.isHoming && playerEntity != null) {
            val playerTransform = playerEntity!!.getComponent(TransformComponent::class.java)
            val vel = velocityMapper.get(entity)
            
            val toTarget = Vector2(
                playerTransform.position.x - transform.position.x,
                playerTransform.position.y - transform.position.y
            )
            
            if (!toTarget.isZero()) {
                toTarget.nor()
                // Smooth rotation towards target
                val currentAngle = MathUtils.atan2(vel.velocity.y, vel.velocity.x)
                val targetAngle = MathUtils.atan2(toTarget.y, toTarget.x)
                var diff = targetAngle - currentAngle
                
                // Normalize angle difference
                while (diff > MathUtils.PI) diff -= 2 * MathUtils.PI
                while (diff < -MathUtils.PI) diff += 2 * MathUtils.PI
                
                val turnRate = 3f * deltaTime
                val newAngle = currentAngle + MathUtils.clamp(diff, -turnRate, turnRate)
                
                vel.velocity.set(MathUtils.cos(newAngle), MathUtils.sin(newAngle)).scl(projectile.speed)
            }
        }
        
        // Remove if lifetime expired or off screen
        if (projectile.lifetime <= 0f ||
            transform.position.y > screenHeight + 50f ||
            transform.position.y < -50f ||
            transform.position.x < -50f ||
            transform.position.x > screenWidth + 50f) {
            engine?.removeEntity(entity)
        }
    }
}

// Pickup System - Priority 6
class PickupSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, VelocityComponent::class.java, PickupComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val velocityMapper = ComponentMapper.getFor(VelocityComponent::class.java)
    private val pickupMapper = ComponentMapper.getFor(PickupComponent::class.java)
    
    var engine: com.badlogic.ashley.core.Engine? = null
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val pickup = pickupMapper.get(entity)
        val transform = transformMapper.get(entity)
        
        pickup.lifetime -= deltaTime
        
        if (pickup.lifetime <= 0f || transform.position.y < -50f) {
            engine?.removeEntity(entity)
        }
    }
}

// Ultimate System - Priority 7
class UltimateSystem(priority: Int) : IteratingSystem(
    Family.all(PlayerComponent::class.java, UltimateComponent::class.java, InputComponent::class.java).get(),
    priority
) {
    private val playerMapper = ComponentMapper.getFor(PlayerComponent::class.java)
    private val ultimateMapper = ComponentMapper.getFor(UltimateComponent::class.java)
    private val inputMapper = ComponentMapper.getFor(InputComponent::class.java)
    
    var entityFactory: EntityFactory? = null
    var engine: com.badlogic.ashley.core.Engine? = null
    var playerEntity: Entity? = null
    
    fun setEntityFactory(factory: EntityFactory) {
        entityFactory = factory
    }
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setPlayerEntity(player: Entity?) {
        playerEntity = player
    }
    
    fun tryActivateUltimate() {
        val player = playerEntity?.getComponent(PlayerComponent::class.java) ?: return
        val ultimate = playerEntity?.getComponent(UltimateComponent::class.java) ?: return
        
        if (player.ultimateMeter >= player.maxUltimateMeter && ultimate.cooldown <= 0f && !ultimate.isActive) {
            activateUltimate(player, ultimate)
        }
    }
    
    private fun activateUltimate(player: PlayerComponent, ultimate: UltimateComponent) {
        ultimate.isActive = true
        ultimate.duration = ultimate.maxDuration
        ultimate.cooldown = ultimate.maxCooldown
        player.ultimateMeter = 0f
        player.isUltimateActive = true
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val player = playerMapper.get(entity)
        val ultimate = ultimateMapper.get(entity)
        
        // Update cooldown
        if (ultimate.cooldown > 0f) {
            ultimate.cooldown -= deltaTime
        }
        
        // Update ultimate duration
        if (ultimate.isActive) {
            ultimate.duration -= deltaTime
            player.isUltimateActive = true
            
            if (ultimate.duration <= 0f) {
                ultimate.isActive = false
                player.isUltimateActive = false
            } else {
                // Ultimate effects
                applyUltimateEffect(entity, player, ultimate, deltaTime)
            }
        }
    }
    
    private fun applyUltimateEffect(entity: Entity, player: PlayerComponent, ultimate: UltimateComponent, deltaTime: Float) {
        val factory = entityFactory ?: return
        val eng = engine ?: return
        val transform = entity.getComponent(TransformComponent::class.java)
        
        when (ultimate.ultimateType) {
            UltimateType.INTERCEPTOR_BURST -> {
                // Rapid fire burst
                val weapon = entity.getComponent(WeaponComponent::class.java)
                if (weapon.fireTimer >= 0.03f) { // Very fast fire rate
                    val proj = factory.createPlayerProjectile(entity, transform.position.x, transform.position.y + 20f)
                    eng.addEntity(proj)
                    weapon.fireTimer = 0f
                }
            }
            UltimateType.BALANCED_HOMING -> {
                // Spawn homing missiles that seek all enemies
                // Handled by spawning special projectiles periodically
                if (MathUtils.random() < 0.3f) {
                    // Find enemies and fire homing projectiles
                    val enemies = eng.getEntitiesFor(Family.all(EnemyComponent::class.java).get())
                    for (i in 0 until Math.min(3, enemies.count())) {
                        val enemy = enemies[MathUtils.random(enemies.count() - 1)]
                        val enemyTransform = enemy.getComponent(TransformComponent::class.java)
                        val proj = factory.createPlayerProjectile(entity, transform.position.x, transform.position.y + 20f)
                        proj.getComponent(ProjectileComponent::class.java)?.isHoming = true
                        eng.addEntity(proj)
                    }
                }
            }
            UltimateType.HEAVY_NOVA -> {
                // Expanding ring of projectiles
                if (MathUtils.random() < 0.15f) {
                    for (i in 0..11) {
                        val angle = i * 30f
                        val proj = factory.createPlayerProjectile(entity, transform.position.x, transform.position.y, angle)
                        eng.addEntity(proj)
                    }
                }
            }
            else -> {}
        }
    }
}

// Collision System - Priority 10
class CollisionSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java, CollisionComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val collisionMapper = ComponentMapper.getFor(CollisionComponent::class.java)
    private val healthMapper = ComponentMapper.getFor(HealthComponent::class.java)
    private val playerMapper = ComponentMapper.getFor(PlayerComponent::class.java)
    private val enemyMapper = ComponentMapper.getFor(EnemyComponent::class.java)
    private val projectileMapper = ComponentMapper.getFor(ProjectileComponent::class.java)
    private val pickupMapper = ComponentMapper.getFor(PickupComponent::class.java)
    private val ultimateMapper = ComponentMapper.getFor(UltimateComponent::class.java)
    
    var engine: com.badlogic.ashley.core.Engine? = null
    var entityFactory: EntityFactory? = null
    var gameWorld: GameWorld? = null
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setEntityFactory(factory: EntityFactory) {
        entityFactory = factory
    }
    
    fun setGameWorld(world: GameWorld) {
        gameWorld = world
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        // This is a broad-phase system - actual collision detection is done by checking all pairs
        // For simplicity, we'll do a simple O(n^2) check here (fine for this scale)
    }
    
    fun checkCollisions() {
        val eng = engine ?: return
        val factory = entityFactory ?: return
        val world = gameWorld ?: return
        
        val playerEntities = eng.getEntitiesFor(Family.all(PlayerComponent::class.java, CollisionComponent::class.java, TransformComponent::class.java, HealthComponent::class.java).get())
        val enemyEntities = eng.getEntitiesFor(Family.all(EnemyComponent::class.java, CollisionComponent::class.java, TransformComponent::class.java, HealthComponent::class.java).get())
        val projectileEntities = eng.getEntitiesFor(Family.all(ProjectileComponent::class.java, CollisionComponent::class.java, TransformComponent::class.java).get())
        val pickupEntities = eng.getEntitiesFor(Family.all(PickupComponent::class.java, CollisionComponent::class.java, TransformComponent::class.java).get())
        
        // Player projectiles vs Enemies
        for (proj in projectileEntities) {
            val pProj = projectileMapper.get(proj)
            if (pProj.source != ProjectileSource.PLAYER) continue
            
            val pTrans = transformMapper.get(proj)
            val pColl = collisionMapper.get(proj)
            
            for (enemy in enemyEntities) {
                val eTrans = transformMapper.get(enemy)
                val eColl = collisionMapper.get(enemy)
                val eHealth = healthMapper.get(enemy)
                val eEnemy = enemyMapper.get(enemy)
                
                if (checkCircleCollision(pTrans.position, pColl.radius, eTrans.position, eColl.radius)) {
                    // Hit enemy
                    eHealth.currentHealth -= pProj.damage
                    eHealth.isInvulnerable = true
                    eHealth.invulnerableTimer = 0f
                    
                    // Create hit spark
                    val spark = factory.createExplosion(eTrans.position.x, eTrans.position.y)
                    eng.addEntity(spark)
                    
                    // Pierce logic
                    if (pProj.pierceRemaining > 0) {
                        pProj.pierceRemaining--
                    } else {
                        eng.removeEntity(proj)
                        break
                    }
                    
                    // Check enemy death
                    if (eHealth.currentHealth <= 0) {
                        world.addScore(eEnemy.scoreValue)
                        
                        // Drop pickup
                        if (MathUtils.random() < eEnemy.dropChance) {
                            val pickupType = when {
                                MathUtils.random() < 0.3f -> PickupType.WEAPON_UPGRADE
                                MathUtils.random() < 0.6f -> PickupType.ULTIMATE_CHARGE
                                MathUtils.random() < 0.8f -> PickupType.HEALTH
                                else -> PickupType.SCORE_BONUS
                            }
                            val pickup = factory.createPickup(eTrans.position.x, eTrans.position.y, pickupType)
                            eng.addEntity(pickup)
                        }
                        
                        // Create explosion
                        val explosion = factory.createExplosion(eTrans.position.x, eTrans.position.y)
                        eng.addEntity(explosion)
                        
                        eng.removeEntity(enemy)
                        
                        if (eEnemy.isBoss) {
                            world.onBossDefeated()
                        } else {
                            world.onEnemyDestroyed()
                        }
                    }
                }
            }
        }
        
        // Enemy projectiles vs Player
        val player = playerEntities.firstOrNull() ?: return
        val pTrans = transformMapper.get(player)
        val pColl = collisionMapper.get(player)
        val pHealth = healthMapper.get(player)
        val pPlayer = playerMapper.get(player)
        
        if (pHealth.isInvulnerable) return
        
        for (proj in projectileEntities) {
            val pProj = projectileMapper.get(proj)
            if (pProj.source != ProjectileSource.ENEMY) continue
            
            val projTrans = transformMapper.get(proj)
            val projColl = collisionMapper.get(proj)
            
            if (checkCircleCollision(pTrans.position, pColl.radius, projTrans.position, projColl.radius)) {
                pHealth.currentHealth--
                pHealth.isInvulnerable = true
                pHealth.invulnerableTimer = 0f
                
                val spark = factory.createExplosion(projTrans.position.x, projTrans.position.y)
                eng.addEntity(spark)
                
                eng.removeEntity(proj)
                
                if (pHealth.currentHealth <= 0) {
                    world.onPlayerDeath()
                }
            }
        }
        
        // Player vs Enemy collision
        for (enemy in enemyEntities) {
            val eTrans = transformMapper.get(enemy)
            val eColl = collisionMapper.get(enemy)
            
            if (checkCircleCollision(pTrans.position, pColl.radius, eTrans.position, eColl.radius)) {
                pHealth.currentHealth--
                pHealth.isInvulnerable = true
                pHealth.invulnerableTimer = 0f
                
                val spark = factory.createExplosion(pTrans.position.x, pTrans.position.y)
                eng.addEntity(spark)
                
                if (pHealth.currentHealth <= 0) {
                    world.onPlayerDeath()
                }
            }
        }
        
        // Player vs Pickup collision
        for (pickup in pickupEntities) {
            val pickTrans = transformMapper.get(pickup)
            val pickColl = collisionMapper.get(pickup)
            val pickComp = pickupMapper.get(pickup)
            
            if (checkCircleCollision(pTrans.position, pColl.radius, pickTrans.position, pickColl.radius)) {
                applyPickup(player, pPlayer, pickComp)
                val spark = factory.createExplosion(pickTrans.position.x, pickTrans.position.y)
                eng.addEntity(spark)
                eng.removeEntity(pickup)
            }
        }
    }
    
    private fun checkCircleCollision(pos1: com.badlogic.gdx.math.Vector3, r1: Float, pos2: com.badlogic.gdx.math.Vector3, r2: Float): Boolean {
        val dx = pos1.x - pos2.x
        val dy = pos1.y - pos2.y
        val distSq = dx * dx + dy * dy
        val radiusSum = r1 + r2
        return distSq <= radiusSum * radiusSum
    }
    
    private fun applyPickup(player: Entity, playerComp: PlayerComponent, pickup: PickupComponent) {
        val factory = entityFactory ?: return
        val world = gameWorld ?: return
        
        when (pickup.pickupType) {
            PickupType.WEAPON_UPGRADE -> {
                factory.upgradePlayerWeapon(player)
            }
            PickupType.ULTIMATE_CHARGE -> {
                playerComp.ultimateMeter = MathUtils.clamp(playerComp.ultimateMeter + pickup.value, 0f, playerComp.maxUltimateMeter)
            }
            PickupType.HEALTH -> {
                val health = player.getComponent(HealthComponent::class.java)
                health.currentHealth = MathUtils.clamp(health.currentHealth + pickup.value, 0, health.maxHealth)
            }
            PickupType.SCORE_BONUS -> {
                world.addScore(pickup.value)
            }
        }
    }
}

// Cleanup System - Priority 100
class CleanupSystem(priority: Int) : IteratingSystem(
    Family.all(TransformComponent::class.java).get(),
    priority
) {
    private val transformMapper = ComponentMapper.getFor(TransformComponent::class.java)
    private val particleMapper = ComponentMapper.getFor(ParticleComponent::class.java)
    private val healthMapper = ComponentMapper.getFor(HealthComponent::class.java)
    private val enemyMapper = ComponentMapper.getFor(EnemyComponent::class.java)
    private val pickupMapper = ComponentMapper.getFor(PickupComponent::class.java)
    
    var engine: com.badlogic.ashley.core.Engine? = null
    var gameWorld: GameWorld? = null
    
    private val screenWidth = Gdx.graphics.width.toFloat()
    private val screenHeight = Gdx.graphics.height.toFloat()
    
    fun setEngine(e: com.badlogic.ashley.core.Engine) {
        engine = e
    }
    
    fun setGameWorld(world: GameWorld) {
        gameWorld = world
    }
    
    override fun processEntity(entity: Entity, deltaTime: Float) {
        val transform = transformMapper.get(entity)
        val eng = engine ?: return
        
        // Remove particles when lifetime expires
        val particle = particleMapper.get(entity)
        if (particle != null) {
            particle.lifetime += deltaTime
            if (particle.lifetime >= particle.maxLifetime) {
                eng.removeEntity(entity)
            }
            return
        }
        
        // Remove off-screen enemies (non-boss)
        val enemy = enemyMapper.get(entity)
        if (enemy != null && !enemy.isBoss) {
            if (transform.position.y < -100f) {
                eng.removeEntity(entity)
            }
            return
        }
        
        // Remove off-screen pickups
        val pickup = pickupMapper.get(entity)
        if (pickup != null) {
            if (transform.position.y < -100f) {
                eng.removeEntity(entity)
            }
            return
        }
        
        // Update invulnerability timer
        val health = healthMapper.get(entity)
        if (health != null && health.isInvulnerable) {
            health.invulnerableTimer += deltaTime
            if (health.invulnerableTimer >= health.invulnerableDuration) {
                health.isInvulnerable = false
                health.invulnerableTimer = 0f
            }
        }
    }
}
package com.animeshooter.ecs

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.g3d.ModelInstance

// Transform component
data class TransformComponent(
    var position: Vector3 = Vector3(),
    var rotation: Float = 0f,
    var scale: Vector3 = Vector3(1f, 1f, 1f)
) : Component

// Velocity component
data class VelocityComponent(
    var velocity: Vector2 = Vector2()
) : Component

// Player component
enum class ShipType { INTERCEPTOR, BALANCED, HEAVY }

data class PlayerComponent(
    var shipType: ShipType = ShipType.INTERCEPTOR,
    var weaponLevel: Int = 1,
    var maxWeaponLevel: Int = 5,
    var ultimateMeter: Float = 0f,
    var maxUltimateMeter: Float = 100f,
    var ultimateCooldown: Float = 0f,
    var maxUltimateCooldown: Float = 30f,
    var isUltimateActive: Boolean = false,
    var ultimateDuration: Float = 0f,
    var maxUltimateDuration: Float = 5f,
    var moveSpeed: Float = 300f,
    var health: Int = 3,
    var maxHealth: Int = 3
) : Component

// Weapon component
data class WeaponComponent(
    var fireRate: Float = 0.15f,
    var fireTimer: Float = 0f,
    var projectileSpeed: Float = 600f,
    var damage: Int = 1,
    var spreadAngle: Float = 0f,
    var projectileCount: Int = 1,
    var isHoming: Boolean = false,
    var pierceCount: Int = 0
) : Component

// Ultimate component
data class UltimateComponent(
    var ultimateType: UltimateType = UltimateType.NONE,
    var isActive: Boolean = false,
    var duration: Float = 0f,
    var maxDuration: Float = 5f,
    var cooldown: Float = 0f,
    var maxCooldown: Float = 30f
) : Component

enum class UltimateType { NONE, INTERCEPTOR_BURST, BALANCED_HOMING, HEAVY_NOVA }

// Enemy component
enum class EnemyType { STRAIGHT, SPREAD, BOSS }

data class EnemyComponent(
    var enemyType: EnemyType = EnemyType.STRAIGHT,
    var health: Int = 1,
    var maxHealth: Int = 1,
    var moveSpeed: Float = 100f,
    var fireRate: Float = 1f,
    var fireTimer: Float = 0f,
    var scoreValue: Int = 10,
    var dropChance: Float = 0.15f,
    var isBoss: Boolean = false,
    var bossPhase: Int = 1,
    var maxBossPhases: Int = 3,
    var phaseTimer: Float = 0f
) : Component

// Projectile component
enum class ProjectileSource { PLAYER, ENEMY }

data class ProjectileComponent(
    var source: ProjectileSource = ProjectileSource.PLAYER,
    var damage: Int = 1,
    var speed: Float = 600f,
    var direction: com.badlogic.gdx.math.Vector2 = com.badlogic.gdx.math.Vector2(0f, 1f),
    var isHoming: Boolean = false,
    var pierceRemaining: Int = 0,
    var lifetime: Float = 3f,
    var maxLifetime: Float = 3f
) : Component

// Pickup component
enum class PickupType { WEAPON_UPGRADE, ULTIMATE_CHARGE, HEALTH, SCORE_BONUS }

data class PickupComponent(
    var pickupType: PickupType = PickupType.WEAPON_UPGRADE,
    var value: Int = 1,
    var fallSpeed: Float = 80f,
    var lifetime: Float = 10f,
    var maxLifetime: Float = 10f
) : Component

// Health component
data class HealthComponent(
    var currentHealth: Int = 1,
    var maxHealth: Int = 1,
    var isInvulnerable: Boolean = false,
    var invulnerableTimer: Float = 0f,
    var invulnerableDuration: Float = 1f
) : Component

// Collision component
data class CollisionComponent(
    var radius: Float = 16f,
    var collisionLayer: Int = 0,
    var collisionMask: Int = 0
) : Component

// Model component
data class ModelComponent(
    var modelInstance: ModelInstance? = null,
    var modelPath: String = "",
    var isVisible: Boolean = true,
    var color: com.badlogic.gdx.graphics.Color = com.badlogic.gdx.graphics.Color.WHITE
) : Component

// Particle component
data class ParticleComponent(
    var particleType: ParticleType = ParticleType.NONE,
    var lifetime: Float = 0f,
    var maxLifetime: Float = 1f,
    var scale: Float = 1f,
    var color: com.badlogic.gdx.graphics.Color = com.badlogic.gdx.graphics.Color.WHITE
) : Component

enum class ParticleType { NONE, EXPLOSION, HIT_SPARK, ENGINE_TRAIL, ULTIMATE_EFFECT }

// Screen shake component
data class ScreenShakeComponent(
    var intensity: Float = 0f,
    var duration: Float = 0f,
    var timer: Float = 0f
) : Component

// Stage component
data class StageComponent(
    var currentStage: Int = 1,
    var maxStages: Int = 5,
    var wave: Int = 1,
    var maxWaves: Int = 3,
    var enemiesRemaining: Int = 0,
    var isBossWave: Boolean = false,
    var bossSpawned: Boolean = false,
    var stageTimer: Float = 0f
) : Component

// Input component
data class InputComponent(
    var touchX: Float = 0f,
    var touchY: Float = 0f,
    var isTouching: Boolean = false,
    var ultimatePressed: Boolean = false
) : Component
package mod.azure.doom.entity.tierboss;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import mod.azure.doom.config.DoomConfig;
import mod.azure.doom.entity.DemonEntity;
import mod.azure.doom.entity.ai.goal.KnockbackGoal;
import mod.azure.doom.entity.ai.goal.RandomFlyConvergeOnTargetGoal;
import mod.azure.doom.entity.ai.goal.RangedAttackGoal;
import mod.azure.doom.entity.attack.FireballAttack;
import mod.azure.doom.entity.projectiles.entity.DoomFireEntity;
import mod.azure.doom.util.registry.DoomSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class ArchMakyrEntity extends DemonEntity implements IAnimatable, IAnimationTickable {

	public static final TrackedData<Integer> DEATH_STATE = DataTracker.registerData(ArchMakyrEntity.class,
			TrackedDataHandlerRegistry.INTEGER);
	private AnimationFactory factory = new AnimationFactory(this);
	public static final TrackedData<Integer> VARIANT = DataTracker.registerData(ArchMakyrEntity.class,
			TrackedDataHandlerRegistry.INTEGER);
	private final ServerBossBar bossBar = (ServerBossBar) (new ServerBossBar(this.getDisplayName(),
			BossBar.Color.PURPLE, BossBar.Style.PROGRESS)).setDarkenSky(true).setThickenFog(true);

	public ArchMakyrEntity(EntityType<ArchMakyrEntity> entityType, World worldIn) {
		super(entityType, worldIn);
		this.moveControl = new GhastMoveControl(this);
	}

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		if ((this.dead || this.getHealth() < 0.01 || this.isDead()) && this.dataTracker.get(DEATH_STATE) < 5) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("death_phaseone", false));
			return PlayState.CONTINUE;
		}
		if ((this.dead || this.getHealth() < 0.01 || this.isDead()) && this.dataTracker.get(DEATH_STATE) == 5) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("death", false));
			return PlayState.CONTINUE;
		}
		event.getController().setAnimation(new AnimationBuilder().addAnimation("flying", true));
		return PlayState.CONTINUE;
	}

	private <E extends IAnimatable> PlayState predicate1(AnimationEvent<E> event) {
		if (this.dataTracker.get(STATE) == 1 && !(this.dead || this.getHealth() < 0.01 || this.isDead())) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("attacking_ranged", true));
			return PlayState.CONTINUE;
		}
		if (this.dataTracker.get(STATE) == 2 && !(this.dead || this.getHealth() < 0.01 || this.isDead())) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("attacking_aoe", true));
			return PlayState.CONTINUE;
		}
		if (this.dataTracker.get(STATE) == 3 && !(this.dead || this.getHealth() < 0.01 || this.isDead())) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("flying_up", true));
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<ArchMakyrEntity>(this, "controller", 0, this::predicate));
		data.addAnimationController(new AnimationController<ArchMakyrEntity>(this, "controller1", 0, this::predicate1));
	}

	@Override
	public int tickTimer() {
		return age;
	}

	@Override
	public AnimationFactory getFactory() {
		return this.factory;
	}

	@Override
	protected void updatePostDeath() {
		++this.deathTime;
		if (this.deathTime == 80 && this.dataTracker.get(DEATH_STATE) == 0) {
			this.setHealth(this.getMaxHealth());
			this.setDeathState(1);
			this.deathTime = 0;
		}
		if (this.deathTime == 80 && this.dataTracker.get(DEATH_STATE) == 1) {
			this.setHealth(this.getMaxHealth());
			this.setDeathState(2);
			this.deathTime = 0;
		}
		if (this.deathTime == 80 && this.dataTracker.get(DEATH_STATE) == 2) {
			this.setHealth(this.getMaxHealth());
			this.setDeathState(3);
			this.deathTime = 0;
		}
		if (this.deathTime == 80 && this.dataTracker.get(DEATH_STATE) == 3) {
			this.setHealth(this.getMaxHealth());
			this.setDeathState(4);
			this.deathTime = 0;
		}
		if (this.deathTime == 80 && this.dataTracker.get(DEATH_STATE) == 4) {
			this.setHealth(this.getMaxHealth());
			this.setDeathState(5);
			this.deathTime = 0;
		}
		if (this.deathTime == 40 && this.dataTracker.get(DEATH_STATE) == 5) {
			this.world.sendEntityStatus(this, (byte) 60);
			this.remove(Entity.RemovalReason.KILLED);
			this.dropXp();
		}
	}

	public int getDeathState() {
		return this.dataTracker.get(DEATH_STATE);
	}

	public void setDeathState(int state) {
		this.dataTracker.set(DEATH_STATE, state);
	}

	@Override
	public void onDeath(DamageSource source) {
		if (!this.world.isClient) {
			if (source == DamageSource.OUT_OF_WORLD) {
				this.setDeathState(5);
			}
			if (this.dataTracker.get(DEATH_STATE) > 5) {
				AreaEffectCloudEntity areaeffectcloudentity = new AreaEffectCloudEntity(this.world, this.getX(),
						this.getY(), this.getZ());
				areaeffectcloudentity.setParticleType(ParticleTypes.EXPLOSION);
				areaeffectcloudentity.setRadius(3.0F);
				areaeffectcloudentity.setDuration(55);
				areaeffectcloudentity.setPos(this.getX(), this.getY(), this.getZ());
				this.world.spawnEntity(areaeffectcloudentity);
				this.goalSelector.getRunningGoals().forEach(PrioritizedGoal::stop);
				this.onAttacking(this.getAttacker());
				this.world.sendEntityStatus(this, (byte) 3);
			}
			if (this.dataTracker.get(DEATH_STATE) == 5) {
				super.onDeath(source);
			}
		}
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.goalSelector.add(5, new RandomFlyConvergeOnTargetGoal(this, 2, 15, 0.5));
		this.goalSelector.add(4, new RangedAttackGoal(this, new FireballAttack(this, true)
				.setProjectileOriginOffset(0.8, 0.4, 0.8)
				.setDamage(DoomConfig.archmaykr_ranged_damage + (this.dataTracker.get(DEATH_STATE) == 1
						? DoomConfig.archmaykr_phaseone_damage_boost
						: this.dataTracker.get(DEATH_STATE) == 2 ? DoomConfig.archmaykr_phasetwo_damage_boost
								: this.dataTracker.get(DEATH_STATE) == 3 ? DoomConfig.archmaykr_phasethree_damage_boost
										: this.dataTracker.get(DEATH_STATE) == 4
												? DoomConfig.archmaykr_phasefour_damage_boost
												: 0)),
				1.0D));
		this.targetSelector.add(4, new KnockbackGoal(this, 1.0D));
		this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(2, new TargetGoal<>(this, MerchantEntity.class, true));
		this.targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
	}

	static class GhastMoveControl extends MoveControl {
		private final ArchMakyrEntity ghast;
		private int collisionCheckCooldown;

		public GhastMoveControl(ArchMakyrEntity ghast) {
			super(ghast);
			this.ghast = ghast;
		}

		public void tick() {
			if (this.state == MoveControl.State.MOVE_TO) {
				if (this.collisionCheckCooldown-- <= 0) {
					this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
					Vec3d vec3d = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(),
							this.targetZ - this.ghast.getZ());
					double d = vec3d.length();
					vec3d = vec3d.normalize();
					if (this.willCollide(vec3d, MathHelper.ceil(d))) {
						this.ghast.setVelocity(this.ghast.getVelocity().add(vec3d.multiply(0.1D)));
					} else {
						this.state = MoveControl.State.WAIT;
					}
				}

			}
		}

		private boolean willCollide(Vec3d direction, int steps) {
			Box box = this.ghast.getBoundingBox();

			for (int i = 1; i < steps; ++i) {
				box = box.offset(direction);
				if (!this.ghast.world.isSpaceEmpty(this.ghast, box)) {
					return false;
				}
			}

			return true;
		}
	}

	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
	}

	public void travel(Vec3d movementInput) {
		if (this.isTouchingWater()) {
			this.updateVelocity(0.02F, movementInput);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply(0.800000011920929D));
		} else if (this.isInLava()) {
			this.updateVelocity(0.02F, movementInput);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply(0.5D));
		} else {
			float f = 0.91F;
			if (this.onGround) {
				f = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock()
						.getSlipperiness() * 0.91F;
			}

			float g = 0.16277137F / (f * f * f);
			f = 0.91F;
			if (this.onGround) {
				f = this.world.getBlockState(new BlockPos(this.getX(), this.getY() - 1.0D, this.getZ())).getBlock()
						.getSlipperiness() * 0.91F;
			}

			this.updateVelocity(this.onGround ? 0.1F * g : 0.02F, movementInput);
			this.move(MovementType.SELF, this.getVelocity());
			this.setVelocity(this.getVelocity().multiply((double) f));
		}
		this.updateLimbs(this, false);
	}

	public boolean isClimbing() {
		return false;
	}

	public boolean causeFallDamage(float distance, float damageMultiplier) {
		return false;
	}

	public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
		return false;
	}

	protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
	}

	protected EntityNavigation createNavigation(World world) {
		BirdNavigation birdNavigation = new BirdNavigation(this, world);
		birdNavigation.setCanPathThroughDoors(false);
		birdNavigation.setCanSwim(true);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}

	@Override
	protected void updateGoalControls() {
		boolean flag = this.getTarget() != null && this.canSee(this.getTarget());
		this.goalSelector.setControlEnabled(Goal.Control.LOOK, flag);
		super.updateGoalControls();
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(VARIANT, 0);
		this.dataTracker.startTracking(DEATH_STATE, 0);
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putInt("Variant", this.getVariant());
		tag.putInt("Phase", this.getDeathState());
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		this.setVariant(tag.getInt("Variant"));
		this.setDeathState(tag.getInt("Phase"));
		if (this.hasCustomName()) {
			this.bossBar.setName(this.getDisplayName());
		}
	}

	public int getVariant() {
		return MathHelper.clamp((Integer) this.dataTracker.get(VARIANT), 1, 2);
	}

	public void setVariant(int variant) {
		this.dataTracker.set(VARIANT, variant);
	}

	public int getVariants() {
		return 2;
	}

	public static boolean spawning(EntityType<ArchMakyrEntity> p_223337_0_, World p_223337_1_, SpawnReason reason,
			BlockPos p_223337_3_, Random p_223337_4_) {
		return p_223337_1_.getDifficulty() != Difficulty.PEACEFUL;
	}

	public static DefaultAttributeContainer.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 150.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.55D).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25D)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, DoomConfig.archmaykr_health)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0D).add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D);
	}

	@Override
	public void takeKnockback(double strength, double x, double z) {
		super.takeKnockback(0, 0, 0);
	}

	@Override
	public EntityData initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty difficulty,
			SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
		entityData = super.initialize(serverWorldAccess, difficulty, spawnReason, entityData, entityTag);
		this.setVariant(this.random.nextInt());
		return entityData;
	}

	@Override
	protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
		return 1.0F;
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean shouldRender(double distance) {
		return true;
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return DoomSounds.MAKYR_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return DoomSounds.MAKYR_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return DoomSounds.MAKYR_DEATH;
	}

	@Override
	public int getLimitPerChunk() {
		return 1;
	}

	@Override
	public void baseTick() {
		super.baseTick();
		final Box aabb = new Box(this.getBlockPos().up()).expand(64D, 64D, 64D);
		this.getCommandSenderWorld().getOtherEntities(this, aabb).forEach(e -> {
			if (e instanceof ArchMakyrEntity && e.age < 1) {
				e.remove(RemovalReason.KILLED);
			}
		});
	}

	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		this.bossBar.addPlayer(player);
	}

	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		this.bossBar.removePlayer(player);
	}

	@Override
	public void setCustomName(@Nullable Text name) {
		super.setCustomName(name);
		this.bossBar.setName(this.getDisplayName());
	}

	@Override
	protected void mobTick() {
		super.mobTick();
		this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());
	}

	public void spawnFlames(double x, double z, double maxY, double y, float yaw, int warmup) {
		BlockPos blockPos = new BlockPos(x, y, z);
		boolean bl = false;
		double d = -0.75D;
		do {
			BlockPos blockPos2 = blockPos.down();
			BlockState blockState = this.world.getBlockState(blockPos2);
			if (blockState.isSideSolidFullSquare(this.world, blockPos2, Direction.UP)) {
				if (!this.world.isAir(blockPos)) {
					BlockState blockState2 = this.world.getBlockState(blockPos);
					VoxelShape voxelShape = blockState2.getCollisionShape(this.world, blockPos);
					if (!voxelShape.isEmpty()) {
						d = voxelShape.getMax(Direction.Axis.Y);
					}
				}
				bl = true;
				break;
			}
			blockPos = blockPos.down();
		} while (blockPos.getY() >= MathHelper.floor(maxY) - 1);

		if (bl) {
			DoomFireEntity fang = new DoomFireEntity(this.world, x, (double) blockPos.getY() + d, z, yaw, warmup, this,
					DoomConfig.archmaykr_ranged_damage + (this.dataTracker.get(DEATH_STATE) == 1
							? DoomConfig.archmaykr_phaseone_damage_boost
							: this.dataTracker.get(DEATH_STATE) == 2 ? DoomConfig.archmaykr_phasetwo_damage_boost
									: this.dataTracker.get(DEATH_STATE) == 3
											? DoomConfig.archmaykr_phasethree_damage_boost
											: this.dataTracker.get(DEATH_STATE) == 4
													? DoomConfig.archmaykr_phasefour_damage_boost
													: 0));
			fang.setFireTicks(age);
			fang.isInvisible();
			fang.age = -150;
			this.world.spawnEntity(fang);
		}
	}

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public void checkDespawn() {
	}

}

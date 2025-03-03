package mod.azure.doom.entity.tierboss;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import mod.azure.doom.config.DoomConfig;
import mod.azure.doom.entity.DemonEntity;
import mod.azure.doom.entity.projectiles.CustomFireballEntity;
import mod.azure.doom.entity.projectiles.entity.DoomFireEntity;
import mod.azure.doom.entity.tierambient.TentacleEntity;
import mod.azure.doom.util.registry.DoomEntities;
import mod.azure.doom.util.registry.DoomSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class MotherDemonEntity extends DemonEntity implements IAnimatable, IAnimationTickable {

	public static final TrackedData<Integer> DEATH_STATE = DataTracker.registerData(MotherDemonEntity.class,
			TrackedDataHandlerRegistry.INTEGER);
	private final ServerBossBar bossBar = (ServerBossBar) (new ServerBossBar(this.getDisplayName(),
			BossBar.Color.PURPLE, BossBar.Style.PROGRESS)).setDarkenSky(true).setThickenFog(true);

	public MotherDemonEntity(EntityType<MotherDemonEntity> entityType, World worldIn) {
		super(entityType, worldIn);
	}

	private AnimationFactory factory = new AnimationFactory(this);

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		if (event.isMoving()) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("moving", true));
			return PlayState.CONTINUE;
		}
		if ((this.dead || this.getHealth() < 0.01 || this.isDead()) && this.dataTracker.get(DEATH_STATE) == 0) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("death_phaseone", false));
			return PlayState.CONTINUE;
		}
		if ((this.dead || this.getHealth() < 0.01 || this.isDead()) && this.dataTracker.get(DEATH_STATE) == 1) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("death", false));
			return PlayState.CONTINUE;
		}
		event.getController().setAnimation(new AnimationBuilder().addAnimation("moving", true));
		return PlayState.CONTINUE;
	}

	private <E extends IAnimatable> PlayState predicate1(AnimationEvent<E> event) {
		if (this.dataTracker.get(STATE) == 1 && !(this.dead || this.getHealth() < 0.01 || this.isDead())) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("shooting", true));
			return PlayState.CONTINUE;
		}
		if (this.dataTracker.get(STATE) == 2 && !(this.dead || this.getHealth() < 0.01 || this.isDead())) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("fire", true));
			return PlayState.CONTINUE;
		}
		return PlayState.STOP;
	}

	@Override
	public int tickTimer() {
		return age;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<MotherDemonEntity>(this, "controller", 0, this::predicate));
		data.addAnimationController(
				new AnimationController<MotherDemonEntity>(this, "controller1", 0, this::predicate1));
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
		if (this.deathTime == 40 && this.dataTracker.get(DEATH_STATE) == 1) {
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
				this.setDeathState(1);
			}
			if (this.dataTracker.get(DEATH_STATE) == 0) {
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
			if (this.dataTracker.get(DEATH_STATE) == 1) {
				super.onDeath(source);
			}
		}
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putInt("Phase", this.getDeathState());
	}

	@Override
	public boolean handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
		return false;
	}

	@Override
	public boolean isPushable() {
		return false;
	}

	@Override
	protected void tickCramming() {
	}

	public static boolean spawning(EntityType<MotherDemonEntity> p_223337_0_, World p_223337_1_, SpawnReason reason,
			BlockPos p_223337_3_, Random p_223337_4_) {
		return p_223337_1_.getDifficulty() != Difficulty.PEACEFUL;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAtEntityGoal(this, MerchantEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAtEntityGoal(this, IronGolemEntity.class, 8.0F));
		this.goalSelector.add(8, new LookAroundGoal(this));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8D));
		this.initCustomGoals();
	}

	protected void initCustomGoals() {
		this.goalSelector.add(1, new MotherDemonEntity.AttackGoal(this));
		this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(2, new TargetGoal<>(this, MerchantEntity.class, true));
		this.targetSelector.add(2, new TargetGoal<>(this, IronGolemEntity.class, true));
		this.targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
	}

	@Override
	protected void updateGoalControls() {
		boolean flag = this.getTarget() != null && this.canSee(this.getTarget());
		this.goalSelector.setControlEnabled(Goal.Control.LOOK, flag);
		super.updateGoalControls();
	}

	@Override
	public void baseTick() {
		super.baseTick();
		final Box aabb = new Box(this.getBlockPos().up()).expand(64D, 64D, 64D);
		this.getCommandSenderWorld().getOtherEntities(this, aabb).forEach(e -> {
			if (e instanceof MotherDemonEntity && e.age < 1) {
				e.remove(RemovalReason.KILLED);
			}
		});
	}

	@Override
	public float getEyeHeight(EntityPose pose) {
		return 9.05F;
	}

	static class AttackGoal extends Goal {
		private final MotherDemonEntity parentEntity;
		protected int cooldown = 0;

		public AttackGoal(MotherDemonEntity parentEntity) {
			this.parentEntity = parentEntity;
		}

		public boolean canStart() {
			return this.parentEntity.getTarget() != null;
		}

		public void start() {
			super.start();
			this.parentEntity.setAttacking(true);
			this.cooldown = 0;
			this.parentEntity.setAttackingState(0);
		}

		@Override
		public void stop() {
			super.stop();
			this.parentEntity.setAttacking(false);
			this.parentEntity.setAttackingState(0);
		}

		public void tick() {
			LivingEntity livingEntity = this.parentEntity.getTarget();
			if (livingEntity.squaredDistanceTo(this.parentEntity) < 4096.0D && this.parentEntity.canSee(livingEntity)) {
				World world = this.parentEntity.world;
				Vec3d vec3d = this.parentEntity.getRotationVec(1.0F);
				++this.cooldown;
				Random rand = new Random();
				double f = livingEntity.getX() - (this.parentEntity.getX() + vec3d.x * 2.0D);
				double g = livingEntity.getBodyY(0.5D) - (0.5D + this.parentEntity.getBodyY(0.5D));
				double h = livingEntity.getZ() - (this.parentEntity.getZ() + vec3d.z * 2.0D);
				CustomFireballEntity fireballEntity = new CustomFireballEntity(world, this.parentEntity, f, g, h,
						DoomConfig.motherdemon_ranged_damage + (this.parentEntity.dataTracker.get(DEATH_STATE) == 1
								? DoomConfig.motherdemon_phaseone_damage_boos
								: 0));
				CustomFireballEntity fireballEntity1 = new CustomFireballEntity(world, this.parentEntity, f, g, h,
						DoomConfig.motherdemon_ranged_damage + (this.parentEntity.dataTracker.get(DEATH_STATE) == 1
								? DoomConfig.motherdemon_phaseone_damage_boos
								: 0));
				CustomFireballEntity fireballEntity2 = new CustomFireballEntity(world, this.parentEntity, f, g, h,
						DoomConfig.motherdemon_ranged_damage + (this.parentEntity.dataTracker.get(DEATH_STATE) == 1
								? DoomConfig.motherdemon_phaseone_damage_boos
								: 0));
				double d = Math.min(livingEntity.getY(), parentEntity.getY());
				double e1 = Math.max(livingEntity.getY(), parentEntity.getY()) + 1.0D;
				float f2 = (float) MathHelper.atan2(livingEntity.getZ() - parentEntity.getZ(),
						livingEntity.getX() - parentEntity.getX());
				int j;
				this.parentEntity.getNavigation().startMovingTo(livingEntity, 1.5D);
				if (this.cooldown == 15) {
					float h2;
					if (parentEntity.getHealth() <= (parentEntity.getMaxHealth() * 0.50)) {
						for (j = 0; j < 32; ++j) {
							h2 = f2 + (float) j * 3.1415927F * 0.4F;
							for (int y = 0; y < 5; ++y) {
								parentEntity.spawnFlames(
										parentEntity.getX() + (double) MathHelper.cos(h2) * rand.nextDouble() * 11.5D,
										parentEntity.getZ() + (double) MathHelper.sin(h2) * rand.nextDouble() * 11.5D,
										d, e1, h2, 0);
							}
							parentEntity.world.playSound(this.parentEntity.getX(), this.parentEntity.getY(),
									this.parentEntity.getZ(), DoomSounds.MOTHER_ATTACK, SoundCategory.HOSTILE,
									0.25F, 1.0F, true);
							this.parentEntity.setAttackingState(2);
						}
						livingEntity.setVelocity(livingEntity.getVelocity().add(0.4f, 1.4f, 0.4f));
						TentacleEntity lost_soul = DoomEntities.TENTACLE.create(world);
						lost_soul.refreshPositionAndAngles(livingEntity.getX(), livingEntity.getY(),
								livingEntity.getZ(), 0, 0);
						parentEntity.world.spawnEntity(lost_soul);
					} else {
						this.parentEntity.setAttackingState(1);
						fireballEntity.updatePosition(this.parentEntity.getX() + vec3d.x * 1.0D,
								this.parentEntity.getBodyY(0.5D) + 0.5D, parentEntity.getZ() + vec3d.z * 1.0D);
						world.spawnEntity(fireballEntity);
						fireballEntity1.updatePosition((this.parentEntity.getX() + 3) + vec3d.x * 1.0D,
								this.parentEntity.getBodyY(0.5D) + 0.5D, parentEntity.getZ() + vec3d.z * 1.0D);
						world.spawnEntity(fireballEntity1);
						fireballEntity2.updatePosition((this.parentEntity.getX() - 3) + vec3d.x * 1.0D,
								this.parentEntity.getBodyY(0.5D) + 0.5D, parentEntity.getZ() + vec3d.z * 1.0D);
						world.spawnEntity(fireballEntity2);
						parentEntity.world.playSound(this.parentEntity.getX(), this.parentEntity.getY(),
								this.parentEntity.getZ(), DoomSounds.MOTHER_ATTACK, SoundCategory.HOSTILE, 0.25F,
								1.0F, true);
						TentacleEntity lost_soul = DoomEntities.TENTACLE.create(world);
						lost_soul.refreshPositionAndAngles(livingEntity.getX(), livingEntity.getY(),
								livingEntity.getZ(), 0, 0);
						parentEntity.world.spawnEntity(lost_soul);
					}
				}
				if (this.cooldown == 30) {
					this.parentEntity.setAttackingState(0);
					this.cooldown = -5;
				}
			} else if (this.cooldown > 0) {
				--this.cooldown;
			}
			this.parentEntity.lookAtEntity(livingEntity, 30.0F, 30.0F);
		}
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
					DoomConfig.motherdemon_ranged_damage
							+ (this.dataTracker.get(DEATH_STATE) == 1 ? DoomConfig.motherdemon_phaseone_damage_boos
									: 0));
			fang.setFireTicks(age);
			fang.isInvisible();
			this.world.spawnEntity(fang);
		}
	}

	public static DefaultAttributeContainer.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 25.0D)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, DoomConfig.motherdemon_health)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D)
				.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 0.0D)
				.add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1000.0D);
	}

	@Override
	public void takeKnockback(double strength, double x, double z) {
		super.takeKnockback(0, 0, 0);
	}

	@Override
	protected SoundEvent getAmbientSound() {
		return DoomSounds.MOTHER_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return DoomSounds.MOTHER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return DoomSounds.MOTHER_DEATH;
	}

	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		this.bossBar.addPlayer(player);
	}

	public void onStoppedTrackingBy(ServerPlayerEntity player) {
		super.onStoppedTrackingBy(player);
		this.bossBar.removePlayer(player);
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(DEATH_STATE, 0);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		if (this.hasCustomName()) {
			this.bossBar.setName(this.getDisplayName());
		}
		this.setDeathState(tag.getInt("Phase"));
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

	@Override
	public boolean cannotDespawn() {
		return true;
	}

	@Override
	public void checkDespawn() {
	}

}

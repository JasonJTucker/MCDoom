package mod.azure.doom.entity.tierfodder;

import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import mod.azure.doom.config.DoomConfig;
import mod.azure.doom.entity.DemonEntity;
import mod.azure.doom.entity.ai.goal.DemonAttackGoal;
import mod.azure.doom.entity.tierheavy.PainEntity;
import mod.azure.doom.network.DoomEntityPacket;
import mod.azure.doom.util.registry.DoomSounds;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityGroup;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.explosion.Explosion;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class LostSoulEntity extends DemonEntity implements Monster, IAnimatable, IAnimationTickable {

	protected static final TrackedData<Byte> VEX_FLAGS = DataTracker.registerData(LostSoulEntity.class,
			TrackedDataHandlerRegistry.BYTE);
	public static final TrackedData<Integer> VARIANT = DataTracker.registerData(LostSoulEntity.class,
			TrackedDataHandlerRegistry.INTEGER);
	public int explosionPower = 1;
	public int flameTimer;
	@Nullable
	private BlockPos bounds;

	public LostSoulEntity(EntityType<? extends LostSoulEntity> type, World worldIn) {
		super(type, worldIn);
		this.moveControl = new GhastMoveControl(this);
	}

	private AnimationFactory factory = new AnimationFactory(this);

	private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event) {
		if (!(lastLimbDistance > -0.15F && lastLimbDistance < 0.15F)) {
			event.getController().setAnimation(new AnimationBuilder().addAnimation("walking", true));
			return PlayState.CONTINUE;
		}
		event.getController().setAnimation(new AnimationBuilder().addAnimation("idle", true));
		return PlayState.CONTINUE;
	}

	@Override
	public void registerControllers(AnimationData data) {
		data.addAnimationController(new AnimationController<LostSoulEntity>(this, "controller", 0, this::predicate));
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
		if (this.deathTime == 5) {
			this.remove(Entity.RemovalReason.KILLED);
			this.dropXp();
			if (!world.isClient) {
				this.explode();
			}
		}
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(VEX_FLAGS, (byte) 0);
		this.dataTracker.startTracking(VARIANT, 0);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		this.setVariant(tag.getInt("Variant"));
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putInt("Variant", this.getVariant());
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

	@Override
	public EntityData initialize(ServerWorldAccess serverWorldAccess, LocalDifficulty difficulty,
			SpawnReason spawnReason, EntityData entityData, NbtCompound entityTag) {
		entityData = super.initialize(serverWorldAccess, difficulty, spawnReason, entityData, entityTag);
		this.setVariant(this.random.nextInt());
		return entityData;
	}

	public boolean handleFallDamage(float fallDistance, float damageMultiplier) {
		return false;
	}

	protected void fall(double heightDifference, boolean onGround, BlockState landedState, BlockPos landedPosition) {
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
		return true;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return DoomEntityPacket.createPacket(this);
	}

	public static DefaultAttributeContainer.Builder createMobAttributes() {
		return LivingEntity.createLivingAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 25.0D)
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25D).add(EntityAttributes.GENERIC_FLYING_SPEED, 0.25D)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, DoomConfig.lost_soul_health)
				.add(EntityAttributes.GENERIC_ATTACK_KNOCKBACK, 1.0D).add(EntityAttributes.HORSE_JUMP_STRENGTH, 2.0D)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, DoomConfig.lost_soul_melee_damage);
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(4, new DemonAttackGoal(this, 1.25D, 2));
		this.goalSelector.add(5, new WanderAroundFarGoal(this, 1.0D));
		this.targetSelector.add(2, new TargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.add(2, new TargetGoal<>(this, MerchantEntity.class, true));
		this.targetSelector.add(2, new RevengeGoal(this).setGroupRevenge());
	}

	public static boolean canSpawn(EntityType<PainEntity> type, WorldAccess world, SpawnReason spawnReason,
			BlockPos pos, RandomGenerator random) {
		return world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0
				&& canMobSpawn(type, world, spawnReason, pos, random);
	}

	private boolean areFlagsSet(int mask) {
		int i = (Byte) this.dataTracker.get(VEX_FLAGS);
		return (i & mask) != 0;
	}

	private void setVexFlag(int mask, boolean value) {
		int i = (Byte) this.dataTracker.get(VEX_FLAGS);
		if (value) {
			i = i | mask;
		} else {
			i = i & ~mask;
		}

		this.dataTracker.set(VEX_FLAGS, (byte) (i & 255));
	}

	public boolean isCharging() {
		return this.areFlagsSet(1);
	}

	public void setCharging(boolean charging) {
		this.setVexFlag(1, charging);
	}

	@Nullable
	public BlockPos getBounds() {
		return this.bounds;
	}

	static class GhastMoveControl extends MoveControl {
		private final LostSoulEntity ghast;
		private int collisionCheckCooldown;

		public GhastMoveControl(LostSoulEntity ghast) {
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

	class ChargeTargetGoal extends Goal {
		public int attackTimer;
		private final LostSoulEntity parentEntity;

		public ChargeTargetGoal(LostSoulEntity ghast) {
			this.setControls(EnumSet.of(Goal.Control.MOVE));
			this.parentEntity = ghast;
		}

		public boolean canStart() {
			return parentEntity.getTarget() != null;
		}

		public boolean shouldContinue() {
			return parentEntity.getTarget() != null && parentEntity.getTarget().isAlive();
		}

		public void start() {
			LivingEntity livingEntity = LostSoulEntity.this.getTarget();
			Vec3d vec3d = livingEntity.getCameraPosVec(1.0F);
			LostSoulEntity.this.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 4.0D);
			LostSoulEntity.this.setCharging(true);
			LostSoulEntity.this.playSound(DoomSounds.LOST_SOUL_AMBIENT, 1.0F, 1.0F);
			this.attackTimer = 0;
		}

		public void stop() {
			LostSoulEntity.this.setCharging(false);
		}

		public void tick() {
			LivingEntity livingentity = parentEntity.getTarget();
			++this.attackTimer;
			parentEntity.setCharging(false);
			Vec3d vec3d = livingentity.getCameraPosVec(1.0F);
			parentEntity.moveControl.moveTo(vec3d.x, vec3d.y, vec3d.z, 1.0D);
			if (this.parentEntity.getBoundingBox().expand(0.20000000298023224D)
					.intersects(livingentity.getBoundingBox())) {
				this.parentEntity.tryAttack(livingentity);
			}
			this.attackTimer = Math.max(this.attackTimer - 0, 0);
		}
	}

	@Override
	public void tick() {
		super.tick();
		flameTimer = (flameTimer + 1) % 8;
	}

	protected void explode() {
		this.world.createExplosion(this, this.getX(), this.getBodyY(0.0625D), this.getZ(), 1.0F,
				Explosion.DestructionType.NONE);
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
		return DoomSounds.LOST_SOUL_DEATH;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return DoomSounds.LOST_SOUL_DEATH;
	}

	@Override
	public EntityGroup getGroup() {
		return EntityGroup.UNDEAD;
	}

	@Override
	protected float getSoundVolume() {
		return 1.0F;
	}

	@Override
	public boolean shouldRender(double distance) {
		return true;
	}

	public int getFlameTimer() {
		return flameTimer;
	}

}
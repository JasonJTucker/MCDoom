package mod.azure.doom.entity.ai.goal;

import java.util.EnumSet;
import java.util.SplittableRandom;

import mod.azure.doom.entity.attack.AbstractRangedAttack;
import mod.azure.doom.entity.tierboss.IconofsinEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.explosion.Explosion;

public class IconAttackGoal extends Goal {
	private final IconofsinEntity entity;
	private double moveSpeedAmp = 1;
	private int attackTime = -1;
	private int summonTime = -1;
	private AbstractRangedAttack attack;

	public IconAttackGoal(IconofsinEntity mob, AbstractRangedAttack attack, double moveSpeedAmpIn) {
		this.entity = mob;
		this.moveSpeedAmp = moveSpeedAmpIn;
		this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
		this.attack = attack;
	}

	public boolean canStart() {
		return this.entity.getTarget() != null;
	}

	public boolean shouldContinue() {
		return this.canStart();
	}

	public void start() {
		super.start();
		this.entity.setAttacking(true);
	}

	public void stop() {
		super.stop();
		this.entity.setAttacking(false);
		this.entity.setAttackingState(0);
		this.attackTime = -1;
	}

	public void tick() {
		LivingEntity livingentity = this.entity.getTarget();
		if (livingentity != null) {
			this.attackTime++;
			this.entity.lookAtEntity(livingentity, 30.0F, 30.0F);
			this.entity.getNavigation().startMovingTo(livingentity, this.moveSpeedAmp);
			SplittableRandom random = new SplittableRandom();
			int randomAttack = random.nextInt(0, 4);
			if (this.attackTime == 1) {
				this.entity.setAttackingState(0);
				this.summonTime++;
			}
			if (this.attackTime == 4) {
				if (this.summonTime == 10) {
					entity.spawnWave(random.nextInt(0, 11), livingentity); // Summons roughly 2 minutes
					this.summonTime = -300;
				}
				if (randomAttack == 1) {// Summon Fire on target
					for (int i = 1; i < 5; ++i) {
						float f1 = (float) MathHelper.atan2(livingentity.getZ() - entity.getZ(),
								livingentity.getX() - entity.getX()) + (float) i * (float) Math.PI * 0.4F;
						for (int y = 0; y < 5; ++y) {
							entity.spawnFlames(
									livingentity.getX() + (double) MathHelper.cos(f1)
											* livingentity.getRandom().nextDouble() * 1.5D,
									livingentity.getZ() + (double) MathHelper.sin(f1)
											* livingentity.getRandom().nextDouble() * 1.5D,
									Math.min(livingentity.getY(), livingentity.getY()),
									Math.max(livingentity.getY(), livingentity.getY()) + 1.0D, f1, 0);
						}
					}
					if (entity.getHealth() < (entity.getMaxHealth() * 0.50)) {
						this.entity.setAttackingState(6); // no armor
					} else {
						this.entity.setAttackingState(5); // armor
					}
				} else if (randomAttack == 2) { // shoots fireball
					this.attack.shoot();
					if (entity.getHealth() < (entity.getMaxHealth() * 0.50)) {
						this.entity.setAttackingState(2); // no armor
					} else {
						this.entity.setAttackingState(1); // armor
					}
				} else { // melee if in range to melee
					if (entity.getHealth() < (entity.getMaxHealth() * 0.50)) {
						this.entity.setAttackingState(4); // no armor
					} else {
						this.entity.setAttackingState(3); // armor
					}
					this.entity.tryAttack(livingentity);
					this.entity.world.createExplosion(this.entity, livingentity.getX(), livingentity.getY(),
							livingentity.getZ(), 3.0F, false, Explosion.DestructionType.BREAK);
					livingentity.timeUntilRegen = 0;
				}
			}
			if (this.attackTime == 8) {
				this.entity.setAttackingState(0);
			}
			if (this.attackTime == 25) {
				this.attackTime = -15;
			}
		}
	}

	protected double getAttackReachSqr(LivingEntity attackTarget) {
		return (double) (this.entity.getWidth() * 2.0F * this.entity.getWidth() * 2.0F + attackTarget.getWidth());
	}
}

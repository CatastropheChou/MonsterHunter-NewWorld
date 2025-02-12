package com.carro1001.mhnw.entities;

import com.carro1001.mhnw.entities.ai.DragonWalkGoal;
import com.carro1001.mhnw.entities.interfaces.IGrows;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.IAnimationTickable;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Random;

public abstract class DragonEntity extends PathfinderMob implements IAnimatable, IAnimationTickable, IGrows {
    private AnimationFactory factory = GeckoLibUtil.createFactory(this);

    public int prevAnimation = 0;
    public float animationProgress = 0;
    public static final EntityDataAccessor<Integer> STATE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.INT);
    // 0 is IDLE, 1 is Fireball, 2 is moving
    private static final EntityDataAccessor<Float> SCALE = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> HAS_ROARED = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HAS_AGRO = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);

    private static final EntityDataAccessor<Boolean> SCALESSIGNED = SynchedEntityData.defineId(DragonEntity.class, EntityDataSerializers.BOOLEAN);

    public String name;

    public int navigatorType;
    /*
        0 = ground/walking
        1 = ai flight
     */
    public DragonPart headPart;
    public DragonPart neckPart;

    public DragonPart body;
    public DragonPart rightWingUpperPart;
    public DragonPart leftWingUpperPart;
    public DragonPart tail1Part;
    public DragonPart tail2Part;
    public DragonPart tail3Part;
    public DragonPart[] dragonParts;
    public int posPointer = -1;
    public final double[][] positions = new double[64][3];
    public DragonEntity(EntityType<? extends PathfinderMob > p_27557_, Level p_27558_, String name) {
        super(p_27557_, p_27558_);
        this.name = name;
        this.noCulling = true;
        //resetParts();
        //this.setId(ENTITY_COUNTER.getAndAdd(this.dragonParts.length + 1) + 1);

    }
    protected void registerGoals() {
        //this.goalSelector.addGoal(3, new DragonFireball(this));
        this.goalSelector.addGoal(4, new DragonWalkGoal(this, 1.0D,0.001f));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 32.0F));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, this::okTarget));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.isNoAi()) {
            //aiStepDragonParts();
            if(shouldRoar()){
                if(animationProgress == 0){
                    prevAnimation = getStateDir();
                    setStateDir(3);
                }
                animationProgress++;
                if(animationProgress >= 20*10){
                    animationProgress = 0;
                    setHasRoared(true);
                    setStateDir(prevAnimation);
                    prevAnimation = 3;
                }
            }

        }
    }

    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor p_149132_, @NotNull DifficultyInstance p_149133_, @NotNull MobSpawnType p_149134_, @Nullable SpawnGroupData p_149135_, @Nullable CompoundTag p_149136_) {
        GenerateScale();
        //resetParts();
        setStateDir(0);
        return super.finalizeSpawn(p_149132_, p_149133_, p_149134_, p_149135_, p_149136_);
    }

    //region Entity Data
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SCALESSIGNED, false);
        this.entityData.define(SCALE, 1f);
        this.entityData.define(STATE, 0);
        this.entityData.define(HAS_ROARED, false);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("scale_assign", getScaleAssignedDir());
        pCompound.putFloat("monster_scale", getScaleDir());
        pCompound.putInt("mon_state", getStateDir());
        pCompound.putBoolean("mon_roared", hasRoared());
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setScaleDir(pCompound.getFloat("monster_scale"));
        this.setStateDir(pCompound.getInt("mon_state"));
        this.setHasRoared(pCompound.getBoolean("mon_roared"));

    }
    //endregion

    //region Misc
    @Override
    public float getViewXRot(float pPartialTicks) {
        return pPartialTicks == 1.0F ? this.getXRot()/2 : Mth.lerp(pPartialTicks, this.xRotO/2, this.getXRot()/2);
    }

    private boolean okTarget(LivingEntity livingEntity) {
        return this.getTarget() == null || (this.getTarget().getDisplayName() != livingEntity.getDisplayName() && livingEntity.distanceTo(this) < this.getTarget().distanceTo(this));
    }

    @Override
    public int getMaxHeadXRot() {
        return 10;
    }

    @Override
    public int getHeadRotSpeed() {
        return 5;
    }

    //endregion

    //region Animation
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }

    @Override
    public int tickTimer() {
        return tickCount;
    }
    //endregion

    //region Roar
    public boolean hasRoared() {
        return this.entityData.get(HAS_ROARED);
    }

    public void setHasRoared(boolean roared) {
        this.entityData.set(HAS_ROARED, roared);
    }

    public boolean shouldRoar() {
        return getTarget() != null && !hasRoared();
    }
    //endregion

    //region Scale
    protected void GenerateScale(){
        if(!getScaleAssignedDir()){
            setScaleDir((float) new Random().nextDouble(0.5,1.5));
            setScaleAssignedDir(true);
        }
    }
    public void setScaleDir(float scale) {
        if(!getScaleAssignedDir()){
            this.entityData.set(SCALE, scale);
            this.setScaleAssignedDir(true);
        }
    }
    public void setScaleAssignedDir(boolean pState) {
        this.entityData.set(SCALESSIGNED, pState);
    }
    public boolean getScaleAssignedDir() {
        return this.entityData.get(SCALESSIGNED);
    }
    public float getScaleDir() {
        return this.entityData.get(SCALE);
    }
    //endregion

    //region State
    public void setStateDir(int state) {
        this.entityData.set(STATE, state);
    }

    public int getStateDir() {
        return this.entityData.get(STATE);
    }
    //endregion

    //region Scale
    @Override
    public float getScale() {
        return getMonsterScale();
    }

    @Override
    public float getMonsterScale() {
        return getScaleDir();
    }
    //endregion

    //region MultiPart code temp removed until animations are sorted
//
//    private float rotWrap(double pAngle) {
//        return (float)Mth.wrapDegrees(pAngle);
//    }
//
//    public double[] getLatencyPos(int pBufferIndexOffset, float pPartialTicks) {
//        if (this.isDeadOrDying()) {
//            pPartialTicks = 0.0F;
//        }
//
//        pPartialTicks = 1.0F - pPartialTicks;
//        int i = this.posPointer - pBufferIndexOffset & 63;
//        int j = this.posPointer - pBufferIndexOffset - 1 & 63;
//        double[] adouble = new double[3];
//        double d0 = this.positions[i][0];
//        double d1 = Mth.wrapDegrees(this.positions[j][0] - d0);
//        adouble[0] = d0 + d1 * (double)pPartialTicks;
//        d0 = this.positions[i][1];
//        d1 = this.positions[j][1] - d0;
//        adouble[1] = d0 + d1 * (double)pPartialTicks;
//        adouble[2] = Mth.lerp((double)pPartialTicks, this.positions[i][2], this.positions[j][2]);
//        return adouble;
//    }
//    private void tickPart(DragonPart pPart, double pOffsetX, double pOffsetY, double pOffsetZ) {
//        pPart.setPos(this.getX() + pOffsetX, this.getY() + pOffsetY, this.getZ() + pOffsetZ);
//    }
//
//    @Override
//    public boolean isMultipartEntity() {
//        return true;
//    }
//
//    @Override
//    public boolean canBeCollidedWith() {
//        return false;
//    }
//
//    @Override
//    public boolean isInvulnerableTo(DamageSource pSource) {
//        return super.isInvulnerableTo(pSource) || pSource.isExplosion();
//    }
//
//    @Override
//    public net.minecraftforge.entity.PartEntity<?>[] getParts() {
//        return this.dragonParts;
//    }
//
//    @Override
//    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
//        if (pSource instanceof EntityDamageSource && ((EntityDamageSource)pSource).isThorns() && !this.level.isClientSide) {
//            this.hurt(this.body, pSource, pAmount);
//        }
//
//        return false;
//    }
//
//    public boolean hurt(DragonPart pPart, DamageSource pSource, float pDamage) {
//
//        if (pPart != this.headPart) {
//            pDamage = pDamage / 2.0F;
//        }
//
//        if (pDamage < 0.01F) {
//            return false;
//        } else {
//            if (pSource.getEntity() instanceof Player || pSource.isExplosion()) {
//                float f = this.getHealth();
//                this.reallyHurt(pSource, pDamage);
//            }
//            return true;
//        }
//
//    }
//
//    protected boolean reallyHurt(DamageSource pDamageSource, float pAmount) {
//        return super.hurt(pDamageSource, pAmount);
//    }
//    public boolean attackEntityPartFrom(DragonPart entityPart, DamageSource source, float amount) {
//        return this.hurt(entityPart, source, amount);
//    }
//    private void setPartPosition(DragonPart part, double offsetX, double offsetY, double offsetZ) {
//        part.setPos(this.getX() + offsetX * part.scale, this.getY() + offsetY * part.scale, this.getZ() + offsetZ * part.scale);
//    }
//    @Override
//    public void setId(int pId) {
//        super.setId(pId);
//        //for (int i = 0; i < this.dragonParts.length; i++) // Forge: Fix MC-158205: Set part ids to successors of parent mob id
//        //this.dragonParts[i].setId(pId + i + 1);
//    }
//    public void aiStepDragonParts(){
//        if (this.posPointer < 0) {
//            for(int i = 0; i < this.positions.length; ++i) {
//                this.positions[i][0] = (double)this.getYRot();
//                this.positions[i][1] = this.getY();
//            }
//        }
//
//        if (++this.posPointer == this.positions.length) {
//            this.posPointer = 0;
//        }
//
//        this.positions[this.posPointer][0] = (double)this.getYRot();
//        this.positions[this.posPointer][1] = this.getY();
//        if (this.level.isClientSide) {
//            if (this.lerpSteps > 0) {
//                double d6 = this.getX() + (this.lerpX - this.getX()) / (double)this.lerpSteps;
//                double d0 = this.getY() + (this.lerpY - this.getY()) / (double)this.lerpSteps;
//                double d1 = this.getZ() + (this.lerpZ - this.getZ()) / (double)this.lerpSteps;
//                double d2 = Mth.wrapDegrees(this.lerpYRot - (double)this.getYRot());
//                this.setYRot(this.getYRot() + (float)d2 / (float)this.lerpSteps);
//                this.setXRot(this.getXRot() + (float)(this.lerpXRot - (double)this.getXRot()) / (float)this.lerpSteps);
//                --this.lerpSteps;
//                this.setPos(d6, d0, d1);
//                this.setRot(this.getYRot(), this.getXRot());
//            }
//        }
//        this.yBodyRot = this.getYRot();
//        Vec3[] avector3d = new Vec3[this.dragonParts.length];
//
//        for (int j = 0; j < this.dragonParts.length; ++j) {
//            this.dragonParts[j].collideWithNearbyEntities();
//            avector3d[j] = new Vec3(this.dragonParts[j].getX(), this.dragonParts[j].getY(), this.dragonParts[j].getZ());
//        }
//        headPart.copyPosition(this);
//        neckPart.copyPosition(this);
//        rightWingUpperPart.copyPosition(this);
//        leftWingUpperPart.copyPosition(this);
//        tail1Part.copyPosition(this);
//        tail2Part.copyPosition(this);
//        tail3Part.copyPosition(this);
//        body.copyPosition(this);
//        float f12 = (float)(this.getLatencyPos(5, 1.0F)[1] - this.getLatencyPos(10, 1.0F)[1]) * 10.0F * ((float)Math.PI / 180F);
//        float f13 = Mth.cos(f12);
//        float f1 = Mth.sin(f12);
//        float f14 = this.getYRot() * ((float)Math.PI / 180F);
//        float f2 = Mth.sin(f14);
//        float f15 = Mth.cos(f14);
//        //this.setPartPosition(this.body, (double)(f2 * 0.5F), 0.0D, (double)(-f15 * 0.5F));
//        //this.setPartPosition(this.leftWingUpperPart, (double)(f15 * 4.5F), 2.6D, (double)(f2 * 4.5F));
//        //this.setPartPosition(this.rightWingUpperPart, (double)(f15 * -4.5F), 2.6D, (double)(f2 * -4.5F));
//
//        float f3 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F) - 0.01F);
//        float f16 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F) - 0.01F);
//        double[] adouble = this.getLatencyPos(5, 1.0F);
//        double[] adouble1 = this.getLatencyPos(0, 1.0F);
//        float f4 = (float)(adouble[1] - adouble1[1]);
//        //this.setPartPosition(this.headPart, (double)(f3 * 6.5F * f13), (double)(f4 + f1 * 6.5F), (double)(-f16 * 6.5F * f13));
//        //this.setPartPosition(this.neckPart, (double)(f3 * 5.5F * f13), (double)(f4 + f1 * 5.5F), (double)(-f16 * 5.5F * f13));
//        adouble = this.getLatencyPos(5, 1.0F);
//        for(int k = 0; k < 3; ++k) {
//            DragonPart enderdragonpart = null;
//            if (k == 0) {
//                enderdragonpart = this.tail1Part;
//            }
//
//            if (k == 1) {
//                enderdragonpart = this.tail2Part;
//            }
//
//            if (k == 2) {
//                enderdragonpart = this.tail3Part;
//            }
//
//            adouble1 = this.getLatencyPos(12 + k * 2, 1.0F);
//            float f17 = this.getYRot() * ((float)Math.PI / 180F) + this.rotWrap(adouble1[0] - adouble[0]) * ((float)Math.PI / 180F);
//            float f18 = Mth.sin(f17);
//            float f20 = Mth.cos(f17);
//            float f21 = 1.5F;
//            float f22 = (float)(k + 1) * 2.0F;
//            //this.tickPart(enderdragonpart, (double)(-(f2 * 1.5F + f18 * f22) * f13), adouble1[1] - adouble[1] - (double)((f22 + 1.5F) * f1) + 1.5D, ((f15 * 1.5F + f20 * f22) * f13));
//        }
//        for (int l = 0; l < this.dragonParts.length; ++l) {
//            this.dragonParts[l].xo = avector3d[l].x;
//            this.dragonParts[l].yo = avector3d[l].y;
//            this.dragonParts[l].zo = avector3d[l].z;
//            this.dragonParts[l].xOld = avector3d[l].x;
//            this.dragonParts[l].yOld = avector3d[l].y;
//            this.dragonParts[l].zOld = avector3d[l].z;
//        }
//    }
//    public void resetParts() {
//        removeParts();
//        this.headPart = new DragonPart(this, 2.1F*getScale(), 2.0F*getScale());
//        this.neckPart = new DragonPart(this, 2.1F*getScale(), 2.1F*getScale());
//        this.body = new DragonPart(this, 4.0F*getScale(), 2.5F*getScale());
//        this.rightWingUpperPart = new DragonPart(this, 4.0F*getScale(), 0.7F*getScale());
//        this.leftWingUpperPart = new DragonPart(this, 4.0F*getScale(), 0.7F*getScale());
//        this.tail1Part = new DragonPart(this, 2.2F*getScale(), 2.2F*getScale());
//        this.tail2Part = new DragonPart(this, 2.2F*getScale(), 2.2F*getScale());
//        this.tail3Part = new DragonPart(this, 2.2F*getScale(), 2.2F*getScale());
//
//        headPart.copyPosition(this);
//        neckPart.copyPosition(this);
//        rightWingUpperPart.copyPosition(this);
//        leftWingUpperPart.copyPosition(this);
//        tail1Part.copyPosition(this);
//        tail2Part.copyPosition(this);
//        tail3Part.copyPosition(this);
//        body.copyPosition(this);
//        dragonParts = new DragonPart[]{headPart,neckPart,body,rightWingUpperPart,leftWingUpperPart,tail1Part,tail2Part};
//    }
//
//    private void removeParts() {
//        if (headPart != null) {
//            headPart.remove(RemovalReason.CHANGED_DIMENSION);
//            headPart = null;
//        }
//        if (neckPart != null) {
//            neckPart.remove(RemovalReason.CHANGED_DIMENSION);
//            neckPart = null;
//        }
//
//        if (body != null) {
//            body.remove(RemovalReason.CHANGED_DIMENSION);
//            body = null;
//        }
//        if (leftWingUpperPart != null) {
//            leftWingUpperPart.remove(RemovalReason.CHANGED_DIMENSION);
//            leftWingUpperPart = null;
//        }
//        if (rightWingUpperPart != null) {
//            rightWingUpperPart.remove(RemovalReason.CHANGED_DIMENSION);
//            rightWingUpperPart = null;
//        }
//        if (tail1Part != null) {
//            tail1Part.remove(RemovalReason.CHANGED_DIMENSION);
//            tail1Part = null;
//        }
//        if (tail2Part != null) {
//            tail2Part.remove(RemovalReason.CHANGED_DIMENSION);
//            tail2Part = null;
//        }
//        if (tail3Part != null) {
//            tail3Part.remove(RemovalReason.CHANGED_DIMENSION);
//            tail3Part = null;
//        }
//    }
    //endregion
}

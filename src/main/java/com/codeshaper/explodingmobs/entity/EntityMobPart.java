package com.codeshaper.explodingmobs.entity;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import com.codeshaper.explodingmobs.Util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.Rotations;
import net.minecraft.world.World;

public class EntityMobPart extends Entity {

	// Constants:
	private static final Rotations ZERO_ROT = new Rotations(0.0f, 0.0f, 0.0f);

	// Data Manager Parameters:
	public static final DataParameter<Rotations> ROTATION = EntityDataManager.<Rotations>createKey(EntityMobPart.class,
			DataSerializers.ROTATIONS);
	public static final DataParameter<Rotations> ROTATION_SPEED = EntityDataManager
			.<Rotations>createKey(EntityMobPart.class, DataSerializers.ROTATIONS);
	public static final DataParameter<Integer> PART_INDEX = EntityDataManager.<Integer>createKey(EntityMobPart.class,
			DataSerializers.VARINT);
	public static final DataParameter<String> ENTITY_JSON = EntityDataManager.<String>createKey(EntityMobPart.class,
			DataSerializers.STRING);

	/**
	 * Note, this is not synced, the client is trusted with this as it is only a
	 * graphical effect.
	 */
	public int age;

	/**
	 * Entity is saved so we don't need to create an instance every time we need a
	 * reference to the entity.
	 */
	private EntityLivingBase targetEntity;

	// Server side specific constructor.
	public EntityMobPart(World worldIn, double x, double y, double z, EntityLivingBase living, int partIndex) {
		this(worldIn);
		this.setPosition(x, y, z);

		String s = Util.entityToString(living);
		if (s != null) {
			this.setTargetEntity(s);
		} else {
			Util.log("Entity from class " + living.getClass().toString()
					+ " doesn't seem to have been registered!  Removing Mob Part!");
			this.setDead();
			return;
		}

		this.setPartIndex(partIndex);

		// Set random motion.
		this.motionX = this.getRandomSizeMotion();
		this.motionY = (this.rand.nextDouble() + 0.5f) / 4;
		this.motionZ = this.getRandomSizeMotion();
	}

	// Client and Server side constructor.
	public EntityMobPart(World worldIn) {
		super(worldIn);
		this.setSize(0.1f, 0.1f);

		this.age = 100;
	}

	@Override
	protected void entityInit() {
		this.dataManager.register(EntityMobPart.ROTATION, EntityMobPart.ZERO_ROT);
		this.dataManager.register(EntityMobPart.ROTATION_SPEED, EntityMobPart.ZERO_ROT);
		this.dataManager.register(EntityMobPart.PART_INDEX, 0);
		this.dataManager.register(EntityMobPart.ENTITY_JSON, null);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound tagCompound) {
		this.age = tagCompound.getShort("Age");
		this.setPartIndex(tagCompound.getInteger("PartIndex"));

		NBTTagList nbttaglist = tagCompound.getTagList("Rot", 5);
		if (nbttaglist.tagCount() > 0) {
			this.setRotation(new Rotations(nbttaglist));
		} else {
			this.setRotation(ZERO_ROT);
		}

		NBTTagList nbttaglist1 = tagCompound.getTagList("RotSpeed", 5);
		if (nbttaglist1.tagCount() > 0) {
			this.setRotationSpeed(new Rotations(nbttaglist1));
		} else {
			this.setRotationSpeed(ZERO_ROT);
		}

		this.setTargetEntity(tagCompound.getString("TargetEntity"));
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound tagCompound) {
		tagCompound.setInteger("Age", this.age);
		tagCompound.setInteger("PartIndex", this.getPartIndex());
		tagCompound.setTag("Rot", this.getRotation().writeToNBT());
		tagCompound.setTag("RotSpeed", this.getRotationSpeed().writeToNBT());
		tagCompound.setString("TargetEntity", this.getTargetEntityAsString());// Util.entityToString(this.getTargetEntity()));
	}

	/**
	 * returns if this entity triggers Block.onEntityWalking on the blocks they walk
	 * on. used for spiders and wolves to prevent them from trampling crops
	 */
	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	/**
	 * Returns true if other Entities should be prevented from moving through this
	 * Entity.
	 */
	@Override
	public boolean canBeCollidedWith() {
		return !this.isDead;
	}

	/**
	 * Called to update the entity's position/logic.
	 */
	@Override
	public void onUpdate() {
		if (this.onGround) {
			this.setRotationSpeed(ZERO_ROT);
		} else {
			Rotations r = this.getRotation();
			Rotations rs = this.getRotationSpeed();
			this.setRotation(new Rotations(r.getX() + rs.getX(), r.getY() + rs.getY(), r.getZ() + rs.getZ()));
		}

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;

		if (!this.hasNoGravity()) {
			this.motionY -= 0.03999999910593033D;
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (this.onGround) {
			this.motionX *= 0.699999988079071D;
			this.motionZ *= 0.699999988079071D;
			this.motionY *= -0.5D;
		}

		this.age--;

		this.handleWaterMovement();

		if (!this.world.isRemote && this.age < 0) {
			this.setDead();
		}
	}

	@Override
	public String toString() {
		return this.toString() + "[part id: " + Util.entityToString(this.targetEntity) + "]";
	}

	private double getRandomSizeMotion() {
		return (this.rand.nextDouble() - 0.5d) / 4;
	}

	// ////////////////////////////////////////////
	// Getters and setters for the Data Watcher //
	// ////////////////////////////////////////////

	public void setPartIndex(int i) {
		this.dataManager.set(EntityMobPart.PART_INDEX, i);
	}

	public int getPartIndex() {
		return this.dataManager.get(EntityMobPart.PART_INDEX);
	}

	public void setRotation(Rotations r) {
		this.dataManager.set(EntityMobPart.ROTATION, r);
	}

	public Rotations getRotation() {
		return this.dataManager.get(EntityMobPart.ROTATION);
	}

	public void setRotationSpeed(Rotations r) {
		this.dataManager.set(EntityMobPart.ROTATION_SPEED, r);
	}

	public Rotations getRotationSpeed() {
		return this.dataManager.get(EntityMobPart.ROTATION_SPEED);
	}

	public void setTargetEntity(String s) {
		this.dataManager.set(EntityMobPart.ENTITY_JSON, s);
	}

	public String getTargetEntityAsString() {
		return this.dataManager.get(EntityMobPart.ENTITY_JSON);
	}

	/**
	 * @return The Entity this part is based from. Can be null if the data watcher
	 *         is slow.
	 */
	@Nullable
	public Entity getTargetEntity() {
		if (this.targetEntity == null) {
			String string = this.getTargetEntityAsString();
			if (!StringUtils.isBlank(string)) {
				this.targetEntity = Util.stringToEntity(this.world, string);
			} else {
				Util.warn("The data manager may need a moment to catch up.");
			}
		}
		return this.targetEntity;
	}
}

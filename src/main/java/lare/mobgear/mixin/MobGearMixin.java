package lare.mobgear.mixin;

import lare.mobgear.interfaces.EquipmentHolderAdditions;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static lare.mobgear.MobGear.getGearTable;

@Mixin(MobEntity.class)
public abstract class MobGearMixin extends LivingEntity implements EquipmentHolder, EquipmentHolderAdditions {

    @Unique
    private LocalDifficulty spawnDifficulty = null;
    @Unique
    private SpawnReason spawnReason = null;


    protected MobGearMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    };

    @Inject(at = @At("RETURN"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        // We're unable to override the gear during initialization since not all mob data has been set at this point yet.
        // So we're only saving the necessary data temporarily and doing the gear override after the first tick.
        this.spawnDifficulty = difficulty;
        this.spawnReason = spawnReason;
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo ci) {
        if (this.spawnReason != null) {
            // Attempt gear override only one time.
            this.gearOverride(this.spawnDifficulty, this.spawnReason);
            this.spawnDifficulty = null;
            this.spawnReason = null;
        } else if (this.spawnDifficulty != null) {
            this.spawnDifficulty = null;
        }
    }

    @Unique
    private void gearOverride(LocalDifficulty difficulty, SpawnReason spawnReason) {
        LootTable lootTable = getGearTable(this);
        // Don't override gear if spawned from Spawner or Trial Spawner since spawners are able to set their own equipment.
        // Maybe make a gamerule for this?
        if (lootTable != LootTable.EMPTY && spawnReason != SpawnReason.CONVERSION && !SpawnReason.isAnySpawner(spawnReason)) {
            LootWorldContext loot = (new LootWorldContext.Builder((ServerWorld) this.getEntityWorld())).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getPos()).luck(difficulty.getLocalDifficulty()).build(LootContextTypes.EQUIPMENT);
            this.mobGear$setEquipmentFromTableWithLootPoolCheck(lootTable, loot);
        }
    }

    public void mobGear$clearEquipment() {
        this.equipment.clear();
    }
}

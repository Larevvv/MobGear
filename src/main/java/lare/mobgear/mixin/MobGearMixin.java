package lare.mobgear.mixin;

import lare.mobgear.interfaces.EquipmentHolderAdditions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static lare.mobgear.MobGear.*;

@Mixin(MobEntity.class)
public abstract class MobGearMixin extends LivingEntity implements EquipmentHolder, EquipmentHolderAdditions {

    @Shadow private Optional<RegistryKey<LootTable>> lootTable;

    protected MobGearMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("RETURN"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        // We're unable to override the gear during initialization since not all mob data has been set at this point yet.
        // So we're only saving the necessary data to CUSTOM_DATA temporarily and doing the gear override after the first tick.
        LootTable lootTable = getGearTable(this);

        if (lootTable != LootTable.EMPTY) {
            NbtComponent data = this.get(DataComponentTypes.CUSTOM_DATA);
            if (data != null) {
                var nbt = data.copyNbt();

                nbt.putString(SpawnReasonEntityData.toString(), spawnReason.name());
                nbt.putDouble(DifficultyEntityData.toString(), difficulty.getLocalDifficulty());

                this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo ci) {

        NbtComponent data = this.get(DataComponentTypes.CUSTOM_DATA);
        if (data != null && data.contains(SpawnReasonEntityData.toString())) {

            NbtCompound nbt = data.copyNbt();

            SpawnReason spawnReason = null;
            Optional<String> spawnReasonValue = nbt.getString(SpawnReasonEntityData.toString());
            if (spawnReasonValue.isPresent()) {
                spawnReason = SpawnReason.valueOf(spawnReasonValue.get());
            }

            Double difficulty = null;
            Optional<Double> difficultyValue = nbt.getDouble(DifficultyEntityData.toString());
            if (difficultyValue.isPresent()) {
                difficulty = difficultyValue.get();
            }

            this.gearOverride(difficulty, spawnReason);

            nbt.remove(SpawnReasonEntityData.toString());
            nbt.remove(DifficultyEntityData.toString());

            this.setComponent(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt));
        }
    }

    @Unique
    private void gearOverride(Double difficulty, SpawnReason spawnReason) {
        LootTable lootTable = getGearTable(this);
        // Don't override gear if spawned from Spawner or Trial Spawner since spawners are able to set their own equipment.
        // Maybe make a gamerule for this?
        if (lootTable != LootTable.EMPTY && spawnReason != SpawnReason.CONVERSION && !SpawnReason.isAnySpawner(spawnReason)) {
            LootWorldContext loot = (new LootWorldContext.Builder((ServerWorld) this.getWorld())).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getPos()).luck(difficulty.floatValue()).build(LootContextTypes.EQUIPMENT);
            this.mobGear$setEquipmentFromTableWithLootPoolCheck(lootTable, loot);
        }
    }

    public void mobGear$clearEquipment() {
        this.equipment.clear();
    }

    public void mobGear$setDeathLootTable(String DeathLootTableKey) {
        this.lootTable = Optional.of(RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(DeathLootTableKey)));
    }
}

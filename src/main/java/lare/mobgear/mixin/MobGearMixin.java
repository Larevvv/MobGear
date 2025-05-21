package lare.mobgear.mixin;

import lare.mobgear.MobGear;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Optional;

import static lare.mobgear.MobGear.getGearTable;

@Mixin(MobEntity.class)
public abstract class MobGearMixin extends LivingEntity implements EquipmentHolder {

    private boolean didInitEquipment = false;

    @Shadow protected abstract void initEquipment(Random random, LocalDifficulty localDifficulty);
    @Shadow protected abstract LootWorldContext createEquipmentLootParameters(ServerWorld world);

    @Shadow public abstract Optional<RegistryKey<LootTable>> getLootTableKey();

    protected MobGearMixin(EntityType<? extends MobEntity> entityType, World world) {
        super(entityType, world);
    };

    @Inject(at = @At("HEAD"), method = "updateEnchantments", cancellable = true)
    private void updateEnchantments(ServerWorldAccess world, Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        // Enchanting will happen from the loot tables in initEquipment
        if (getGearTable(this) != LootTable.EMPTY) {
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "initEquipment", cancellable = true)
    private void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        // Prevents double calls
        if (didInitEquipment) {
            ci.cancel();
            return;
        }

        didInitEquipment = true;

        Identifier mobType = Registries.ENTITY_TYPE.getId(this.getType());
        RegistryKey<LootTable> gearTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(mobType.getNamespace(),"gear/"+mobType.getPath()));

        World world = this.getEntityWorld();
        MinecraftServer server = world.getServer();
        if (server != null) {
            LootTable lootTable = server.getReloadableRegistries().getLootTable(gearTable);
            if (lootTable != LootTable.EMPTY) {

                LootWorldContext loot = (new LootWorldContext.Builder((ServerWorld) world)).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getPos()).luck(localDifficulty.getLocalDifficulty()).build(LootContextTypes.EQUIPMENT);
                this.setEquipmentFromTable(gearTable, loot, Map.of());

                ci.cancel();
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "initialize")
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        var asd = ((MobEntity)(Object)this);
        if (!(asd instanceof ZombieEntity) && getGearTable(this) != LootTable.EMPTY && spawnReason != SpawnReason.CONVERSION && !SpawnReason.isAnySpawner(spawnReason)) {
            Random random = world.getRandom();
            this.initEquipment(random, difficulty);
        }
    }


}

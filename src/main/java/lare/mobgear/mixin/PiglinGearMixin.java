package lare.mobgear.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static lare.mobgear.MobGear.getGearTable;

@Mixin(PiglinEntity.class)
public abstract class PiglinGearMixin extends HostileEntity {
    PiglinGearMixin(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("HEAD"), method = "initEquipment", cancellable = true)
    private void initEquipment(Random random, LocalDifficulty localDifficulty, CallbackInfo ci) {
        if (getGearTable(this) != LootTable.EMPTY) {
            super.initEquipment(random, localDifficulty);
            ci.cancel();
        }
    }
}

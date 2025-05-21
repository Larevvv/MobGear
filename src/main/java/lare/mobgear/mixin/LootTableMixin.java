package lare.mobgear.mixin;

import lare.mobgear.interfaces.LootPoolAdditions;
import lare.mobgear.interfaces.LootTableAdditions;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@Mixin(LootTable.class)
public class LootTableMixin implements LootTableAdditions {

    @Final
    @Shadow
    private List<LootPool> pools;

    @Final
    @Shadow
    private BiFunction<ItemStack, LootContext, ItemStack> combinedFunction;

    @Final
    @Shadow
    private Optional<Identifier> randomSequenceId;

    public boolean mobGear$generateUnprocessedLootWithTest(LootContext context, Consumer<ItemStack> lootConsumer) {
        LootContext.Entry<?> entry = LootContext.table(((LootTable)(Object)this));
        if (context.markActive(entry)) {
            Consumer<ItemStack> consumer = LootFunction.apply(this.combinedFunction, lootConsumer, context);

            // We want to know if there are valid pools even if no items are returned as this might be on purpose by the loot table.
            boolean hasValidPool = false;
            for(LootPool lootPool : this.pools) {
                // Test if lootPool is valid separately from generating the loot.
                boolean validPool = ((LootPoolAdditions)lootPool).mobGear$test(context);
                if (validPool) {
                    hasValidPool = true;
                    // We know pool is valid so we can generate loot without testing.
                    ((LootPoolAdditions)lootPool).mobGear$addGeneratedLootNoTest(consumer, context);
                }
            }

            context.markInactive(entry);
            return hasValidPool;
        }
        return false;
    }

    public LootContext mobGear$buildLootContext(LootWorldContext parameters) {
        return (new LootContext.Builder(parameters)).random(0L).build(this.randomSequenceId);
    }
}

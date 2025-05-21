package lare.mobgear.mixin;

import lare.mobgear.interfaces.LootPoolAdditions;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(LootPool.class)
public abstract class LootPoolMixin implements LootPoolAdditions {

    @Final
    @Shadow
    public LootNumberProvider rolls;

    @Final
    @Shadow
    public LootNumberProvider bonusRolls;

    @Final
    @Shadow
    private BiFunction<ItemStack, LootContext, ItemStack> javaFunctions;

    @Final
    @Shadow
    private Predicate<LootContext> predicate;

    @Shadow protected abstract void supplyOnce(Consumer<ItemStack> lootConsumer, LootContext context);

    public boolean mobGear$test(LootContext context) {
        return this.predicate.test(context);
    }

    public void mobGear$addGeneratedLootNoTest(Consumer<ItemStack> lootConsumer, LootContext context) {
        Consumer<ItemStack> consumer = LootFunction.apply(this.javaFunctions, lootConsumer, context);
        int i = this.rolls.nextInt(context) + MathHelper.floor(this.bonusRolls.nextFloat(context) * context.getLuck());

        for(int j = 0; j < i; ++j) {
            this.supplyOnce(consumer, context);
        }
    }
}

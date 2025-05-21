package lare.mobgear;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootChoice;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.*;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static lare.mobgear.MobGear.COMBINEDGROUP;


public class CombinedGroupEntry extends CombinedEntry {
    public static final int DEFAULT_WEIGHT = 1;
    public static final int DEFAULT_QUALITY = 0;
    protected final int weight;
    protected final int quality;
    public static final MapCodec<CombinedGroupEntry> CODEC = createCodec(CombinedGroupEntry::new);

    CombinedGroupEntry(int weight, int quality, List<LootPoolEntry> list, List<LootCondition> list2) {
        super(list, list2);
        this.weight = weight;
        this.quality = quality;
    }

    public LootPoolEntryType getType() {
        return COMBINEDGROUP;
    }

    protected EntryCombiner combine(List<? extends EntryCombiner> terms) {
        if (!terms.isEmpty()) {
            return (LootContext context, Consumer<LootChoice> choiceConsumer) -> {

                List<LootChoice> list = Lists.newArrayList();

                for (EntryCombiner entryCombiner : terms) {
                    entryCombiner.expand(context, list::add);
                }

                choiceConsumer.accept(new LootChoice() {
                    @Override
                    public int getWeight(float luck) {
                        return Math.max(MathHelper.floor(weight + quality * luck), 0);
                    }

                    @Override
                    public void generateLoot(Consumer<ItemStack> lootConsumer, LootContext context) {
                        list.stream().filter((choice) -> choice.getWeight(context.getLuck()) > 0).forEach((choice) -> choice.generateLoot(lootConsumer, context));
                    }
                });

                return true;
            };
        } else {
            return EntryCombiner.ALWAYS_TRUE;
        }
    }

    public static Builder create(int weight, int quality, LootPoolEntry.Builder<?>... entries) {
        return new CombinedGroupEntry.Builder(weight, quality, entries);
    }

    public static class Builder extends LootPoolEntry.Builder<CombinedGroupEntry.Builder> {
        protected int weight;
        protected int quality;
        private final ImmutableList.Builder<LootPoolEntry> entries = ImmutableList.builder();

        public Builder(int weight, int quality, LootPoolEntry.Builder<?>... entries) {
            this.weight = weight;
            this.quality = quality;
            for(LootPoolEntry.Builder<?> builder : entries) {
                this.entries.add(builder.build());
            }
        }

        protected Builder getThisBuilder() {
            return this;
        }

        public LootPoolEntry build() {
            return new CombinedGroupEntry(this.weight, this.quality, this.entries.build(), this.getConditions());
        }
    }

    private static MapCodec<CombinedGroupEntry> createCodec(Factory2 factory) {
        return RecordCodecBuilder.mapCodec((instance) -> {
            var var10000 = instance.group(Codec.INT.optionalFieldOf("weight", 1).forGetter((entry) -> entry.weight), Codec.INT.optionalFieldOf("quality", 0).forGetter((entry) -> entry.quality),LootPoolEntryTypes.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter((entry) -> entry.children)).and(addConditionsField(instance).t1());
            Objects.requireNonNull(factory);
            return var10000.apply(instance, CombinedGroupEntry::new);
        });
    }

    @FunctionalInterface
    private interface Factory2 {
        CombinedGroupEntry create(int weight, int quality, List<LootPoolEntry> terms, List<LootCondition> conditions);
    }
}

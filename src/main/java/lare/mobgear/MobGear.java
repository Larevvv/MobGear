package lare.mobgear;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.loot.entry.LootPoolEntryType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MobGear implements ModInitializer {
	public static final String MOD_ID = "mobgear";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final LootPoolEntryType COMBINEDGROUP;

	@Override
	public void onInitialize() {}

	public static LootTable getGearTable(Entity entity) {
		Identifier mobType = Registries.ENTITY_TYPE.getId(entity.getType());
		RegistryKey<LootTable> gearTable = RegistryKey.of(RegistryKeys.LOOT_TABLE, Identifier.of(mobType.getNamespace(),"mobgear/"+mobType.getPath()));

		World world = entity.getEntityWorld();
		MinecraftServer server = world.getServer();
		if (server != null) {
			return server.getReloadableRegistries().getLootTable(gearTable);
		}
		return LootTable.EMPTY;
	}

	static {
		COMBINEDGROUP = Registry.register(Registries.LOOT_POOL_ENTRY_TYPE, Identifier.of(MOD_ID, "combinedgroup") , new LootPoolEntryType(CombinedGroupEntry.CODEC));
	}
}
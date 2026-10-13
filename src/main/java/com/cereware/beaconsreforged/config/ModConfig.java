package com.cereware.beaconsreforged.config;

import com.cereware.beaconsreforged.BeaconsReforged;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import net.fabricmc.loader.api.FabricLoader;

/** Config mínima (fase 2). Se ampliará y sincronizará con el cliente en la fase 5. */
public final class ModConfig {
	public enum Mode { vanilla, full }

	/** vanilla = solo portable; full = puntos, niveles y poderes nuevos. */
	public Mode mode = Mode.full;
	/** Bloque en el que se convierte la pirámide consumida (minecraft:air para borrarla). */
	public String consumedBlock = "minecraft:dirt";
	/** Pedir confirmación antes de consumir la pirámide (cliente). */
	public boolean showConsumeConfirmation = true;
	/** Conservar el beacon del slot al morir (además de la gamerule keepInventory). */
	public boolean keepBeaconOnDeath = false;
	/** Tope total de Fortuna/Botín sumando encantamiento + Suerte. */
	public int luckTotalCap = 5;
	/**
	 * Prisa (modo full): multiplicador de velocidad de picado para los niveles I..V.
	 * Con pico de netherita + Eficiencia V: III rompe deepslate al instante, V rompe obsidiana en ~0,1 s.
	 */
	public double[] hasteMiningMultipliers = {1.5, 2.2, 3.0, 6.0, 22.0};

	/** Nivel máximo de cada poder del modo full (1-5). Resistencia V sería inmunidad total. */
	public java.util.Map<String, Integer> maxLevelPerEffect = new java.util.LinkedHashMap<>(java.util.Map.of(
		"minecraft:speed", 5, "beaconsreforged:haste", 5, "minecraft:resistance", 4, "minecraft:jump_boost", 5,
		"minecraft:strength", 5, "beaconsreforged:luck", 5, "minecraft:regeneration", 5, "beaconsreforged:reach", 5));

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static ModConfig instance = new ModConfig();
	/** Config recibida del servidor (cliente conectado a un servidor). Tiene prioridad sobre la local. */
	private static ModConfig synced;

	public static ModConfig get() {
		return synced != null ? synced : instance;
	}

	public static String toJson() {
		return GSON.toJson(instance);
	}

	/** Cliente: aplica la config del servidor conservando las opciones solo de cliente. */
	public static void applySynced(String json) {
		try {
			ModConfig server = GSON.fromJson(json, ModConfig.class);
			if (server == null) return;
			server.showConsumeConfirmation = instance.showConsumeConfirmation;
			synced = server.sanitize();
		} catch (Exception e) {
			BeaconsReforged.LOGGER.error("Config del servidor inválida", e);
		}
	}

	public static void clearSynced() {
		synced = null;
		canEditServer = true;
	}

	public int maxLevelFor(String effectId) {
		Integer v = maxLevelPerEffect == null ? null : maxLevelPerEffect.get(effectId);
		return v == null ? 5 : Math.max(1, Math.min(5, v));
	}

	public static boolean isFull() {
		return get().mode == Mode.full;
	}

	/** Cliente: ¿puede este jugador editar la config del servidor al que está conectado? */
	public static boolean canEditServer = true;

	public static boolean isConnectedToServer() {
		return synced != null;
	}

	/** Copia independiente de la config activa (para editarla en la pantalla). */
	public static ModConfig copy() {
		return GSON.fromJson(GSON.toJson(get()), ModConfig.class);
	}

	public static String toJson(ModConfig config) {
		return GSON.toJson(config);
	}

	/** Corrige valores fuera de rango. */
	public ModConfig sanitize() {
		if (mode == null) mode = Mode.full;
		if (consumedBlock == null || net.minecraft.resources.Identifier.tryParse(consumedBlock) == null) consumedBlock = "minecraft:dirt";
		luckTotalCap = Math.max(0, Math.min(20, luckTotalCap));
		if (hasteMiningMultipliers == null || hasteMiningMultipliers.length != 5) hasteMiningMultipliers = new double[] {1.5, 2.2, 3.0, 6.0, 22.0};
		for (int i = 0; i < 5; i++) hasteMiningMultipliers[i] = Math.max(1.0, Math.min(1000.0, hasteMiningMultipliers[i]));
		if (maxLevelPerEffect == null) maxLevelPerEffect = new ModConfig().maxLevelPerEffect;
		return this;
	}

	/** Servidor (o cliente sin conexión): sustituye la config y la guarda. */
	public static void replaceAndSave(ModConfig config) {
		instance = config.sanitize();
		save();
	}

	/** Opción solo de cliente: se guarda siempre en la config local. */
	public static void setShowConsumeConfirmation(boolean value) {
		instance.showConsumeConfirmation = value;
		if (synced != null) synced.showConsumeConfirmation = value;
		save();
	}

	public static void save() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve(BeaconsReforged.MOD_ID + ".json");
		try (Writer w = Files.newBufferedWriter(file)) {
			GSON.toJson(instance, w);
		} catch (Exception e) {
			BeaconsReforged.LOGGER.error("No se pudo guardar la config", e);
		}
	}

	public static void load() {
		Path file = FabricLoader.getInstance().getConfigDir().resolve(BeaconsReforged.MOD_ID + ".json");
		try {
			if (Files.exists(file)) {
				try (Reader r = Files.newBufferedReader(file)) {
					ModConfig read = GSON.fromJson(r, ModConfig.class);
					if (read != null) instance = read;
				}
			}
			try (Writer w = Files.newBufferedWriter(file)) {
				GSON.toJson(instance, w);
			}
		} catch (Exception e) {
			BeaconsReforged.LOGGER.error("No se pudo leer/escribir la config, usando valores por defecto", e);
		}
	}
}

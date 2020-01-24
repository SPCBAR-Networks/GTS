package me.nickimpact.gts.sponge.service;

import co.aikar.commands.CommandIssuer;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import com.nickimpact.impactor.api.registry.BuilderRegistry;
import lombok.Setter;
import me.nickimpact.gts.api.GtsService;
import me.nickimpact.gts.api.enums.CommandResults;
import me.nickimpact.gts.api.holders.EntryClassification;
import me.nickimpact.gts.api.holders.EntryRegistry;
import me.nickimpact.gts.api.listings.ListingManager;
import me.nickimpact.gts.api.listings.entries.Entry;
import me.nickimpact.gts.api.listings.entries.EntryUI;
import me.nickimpact.gts.api.plugin.IGTSPlugin;
import me.nickimpact.gts.api.searching.Searcher;
import me.nickimpact.gts.api.storage.IGtsStorage;
import me.nickimpact.gts.api.util.TriFunction;
import me.nickimpact.gts.sponge.text.SpongeTokenService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Setter
public class SpongeGtsService implements GtsService {

	private final IGTSPlugin plugin;

	private ListingManager manager;
	private IGtsStorage storage;
	private EntryRegistry registry;
	private BuilderRegistry builders;

	private SpongeTokenService tokenService;

	private GsonBuilder gson = new GsonBuilder().setPrettyPrinting();

	private Map<String, Searcher> searcherMap = Maps.newHashMap();
	private Multimap<Class<? extends Entry>, Function<?, Double>> minPriceExtras = ArrayListMultimap.create();

	public SpongeGtsService(IGTSPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public ListingManager getListingManager() {
		return this.manager;
	}

	@Override
	public IGtsStorage getStorage() {
		return this.storage;
	}

	@Override
	public EntryRegistry getEntryRegistry() {
		return this.registry;
	}

	@Override
	public void registerEntry(List<String> identifier, Class<? extends Entry> entry, EntryUI ui, String rep, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmd) {
		try {
			this.registry.getRegistry().register(entry);
			this.registry.getClassifications().add(new SpongeEntryClassification(entry, identifier, rep, ui, cmd));

			plugin.getPluginLogger().info("Loaded element type: " + entry.getSimpleName());
		} catch (Exception e) {
			plugin.getPluginLogger().info("Failed to register type (" + entry.getSimpleName() + ") with reason: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public BuilderRegistry getBuilderRegistry() {
		return this.builders;
	}

	@Override
	public void addSearcher(String key, Searcher searcher) {
		this.searcherMap.put(key, searcher);
	}

	@Override
	public Optional<Searcher> getSearcher(String key) {
		return Optional.ofNullable(this.searcherMap.get(key));
	}

	@Override
	public <T> void addMinPriceOption(Class<? extends Entry<?, T, ?, ?, ?>> type, Function<T, Double> function) {
		this.minPriceExtras.put(type, function);
	}

	@Override
	public <T> List<Function<T, Double>> getMinPriceOptionsForEntryType(Class<? extends Entry<?, T, ?, ?, ?>> type) {
		return this.minPriceExtras.entries().stream()
				.filter(entry -> entry.getKey().equals(type))
				.map(Map.Entry::getValue)
				.map(function -> (Function<T, Double>) function)
				.collect(Collectors.toList());
	}
	public static class SpongeEntryClassification extends EntryClassification<CommandIssuer> {
		SpongeEntryClassification(Class<? extends Entry> classification, List<String> identifers, String itemRep, EntryUI ui, TriFunction<CommandIssuer, List<String>, Boolean, CommandResults> cmdHandler) {
			super(classification, identifers, itemRep, ui, cmdHandler);
		}
	}
}

package org.soul.elasticSearch.plugin;

import java.util.Collection;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.Version;
import org.elasticsearch.action.ActionModule;
import org.elasticsearch.common.collect.Lists;
import org.elasticsearch.common.component.LifecycleComponent;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.analysis.AnalysisModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.suggest.elasticsearch.action.termlist.TermlistAction;
import org.suggest.elasticsearch.action.termlist.TransportTermlistAction;
import org.suggest.elasticsearch.module.AttachmentsIndexModule;
import org.suggest.elasticsearch.module.ShardSuggestModule;
import org.suggest.elasticsearch.module.SuggestClientModule;
import org.suggest.elasticsearch.module.SuggestModule;
import org.suggest.elasticsearch.rest.action.RestRefreshSuggestAction;
import org.suggest.elasticsearch.rest.action.RestStatisticsAction;
import org.suggest.elasticsearch.rest.action.RestSuggestAction;
import org.suggest.elasticsearch.rest.action.RestTermlistAction;
import org.suggest.elasticsearch.service.SuggestService;

public class SoulAnalysisPlugin extends AbstractPlugin {

	private final Settings settings;

	@Inject
	public SoulAnalysisPlugin(Settings settings) {
		this.settings = settings;

		// Check if the plugin is newer than elasticsearch
		// First failure, if the versions don't match
		// Second failure: if the Version specified in before() does not yet
		// exist, therefore catching Throwable
		try {
			if (Version.CURRENT.before(Version.V_0_90_3)) {
				throw new Exception();
			}
		} catch (Throwable e) {
			String error = String
					.format("The elasticsearch suggest plugin needs a newer version of elasticsearch than %s",
							Version.CURRENT);
			throw new ElasticSearchException(error);
		}
	}

	@Override
	public String name() { // very important
		return "soul-analysis";
	}

	@Override
	public String description() {
		return "soul-analysis";
	}

	public void onModule(ActionModule module) {
		module.registerAction(TermlistAction.INSTANCE,
				TransportTermlistAction.class);
		// this is very important
	}

	@Override
	public void processModule(Module module) {
		if (module instanceof AnalysisModule) {
			AnalysisModule analysisModule = (AnalysisModule) module;
			analysisModule.addProcessor(new SoulAnalysisBinderProcessor());
		}

		// if (module instanceof RestModule) {
		// RestModule restModule = (RestModule) module;
		// restModule.addRestAction(RestSuggestAction.class);
		// restModule.addRestAction(RestRefreshSuggestAction.class);
		// restModule.addRestAction(RestStatisticsAction.class);
		// }

	}

	public void onModule(RestModule restModule) {
		restModule.addRestAction(RestSuggestAction.class);
		restModule.addRestAction(RestRefreshSuggestAction.class);
		restModule.addRestAction(RestStatisticsAction.class);
		restModule.addRestAction(RestTermlistAction.class);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Collection<Class<? extends LifecycleComponent>> services() {
		Collection<Class<? extends LifecycleComponent>> services = Lists
				.newArrayList();

		if (!isClient()) {
			services.add(SuggestService.class);
		}
		return services;
	}

	@Override
	public Collection<Class<? extends Module>> modules() {
		Collection<Class<? extends Module>> modules = Lists.newArrayList();
		if (isClient()) {
			modules.add(SuggestClientModule.class);
		} else {
			modules.add(SuggestModule.class);
		}
		return modules;
	}

	@Override
	public Collection<Class<? extends Module>> shardModules() {
		Collection<Class<? extends Module>> modules = Lists.newArrayList();
		modules.add(ShardSuggestModule.class);
		return modules;
	}

	@Override
	public Collection<Class<? extends Module>> indexModules() {
		Collection<Class<? extends Module>> modules = Lists.newArrayList();
		modules.add(AttachmentsIndexModule.class);
		return modules;
	}

	private boolean isClient() {
		return settings.getAsBoolean("node.client", false);
	}

}
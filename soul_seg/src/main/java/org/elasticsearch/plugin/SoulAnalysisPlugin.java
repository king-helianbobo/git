package org.elasticsearch.plugin;

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
import org.elasticsearch.module.AttachmentsIndexModule;
import org.elasticsearch.module.ShardSuggestModule;
import org.elasticsearch.module.SuggestClientModule;
import org.elasticsearch.module.SuggestModule;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.rest.RestModule;
import org.suggest.elasticsearch.action.restful.RestRefreshAction;
import org.suggest.elasticsearch.action.restful.RestStatisticAction;
import org.suggest.elasticsearch.action.restful.RestSuggestAction;
import org.suggest.elasticsearch.action.restful.RestTermlistAction;
import org.suggest.elasticsearch.action.termlist.TermlistAction;
import org.suggest.elasticsearch.action.termlist.TransportTermlistAction;
import org.suggest.elasticsearch.service.SuggestService;

public class SoulAnalysisPlugin extends AbstractPlugin {

	private final Settings settings;

	@Inject
	public SoulAnalysisPlugin(Settings settings) {
		this.settings = settings;
		try {
			if (Version.CURRENT.before(Version.V_0_90_3)) {
				throw new Exception();
			}
		} catch (Throwable e) {
			String error = String.format(
					"Plugin needs a newer version of elasticsearch than %s",
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
			analysisModule.addProcessor(new SoulAnalysisBindProcessor());
			analysisModule.addTokenFilter("file_watcher_synonym",
					SynonymTokenFilterFactory.class);
		}
	}

	public void onModule(RestModule restModule) {
		restModule.addRestAction(RestSuggestAction.class);
		restModule.addRestAction(RestRefreshAction.class);
		restModule.addRestAction(RestStatisticAction.class);
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
package org.suggest.elasticsearch.module;

import org.elasticsearch.action.GenericAction;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.multibindings.MapBinder;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshAction;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsAction;
import org.suggest.elasticsearch.action.suggest.SuggestAction;

public class SuggestClientModule extends AbstractModule {

	@Override
	protected void configure() {
		@SuppressWarnings("rawtypes")
		MapBinder<String, GenericAction> actionsBinder = MapBinder
				.newMapBinder(binder(), String.class, GenericAction.class);
		actionsBinder.addBinding(SuggestAction.NAME).toInstance(
				SuggestAction.INSTANCE);
		actionsBinder.addBinding(SuggestRefreshAction.NAME).toInstance(
				SuggestRefreshAction.INSTANCE);
		actionsBinder.addBinding(SuggestStatisticsAction.NAME).toInstance(
				SuggestStatisticsAction.INSTANCE);
	}
}

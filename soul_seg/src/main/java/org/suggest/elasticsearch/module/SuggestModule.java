package org.suggest.elasticsearch.module;

import org.elasticsearch.action.GenericAction;
import org.elasticsearch.action.support.TransportAction;
import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.multibindings.MapBinder;
import org.suggest.elasticsearch.action.refresh.SuggestRefreshAction;
import org.suggest.elasticsearch.action.refresh.TransportSuggestRefreshAction;
import org.suggest.elasticsearch.action.statistics.SuggestStatisticsAction;
import org.suggest.elasticsearch.action.statistics.TransportSuggestStatisticsAction;
import org.suggest.elasticsearch.action.suggest.SuggestAction;
import org.suggest.elasticsearch.action.suggest.TransportSuggestAction;

public class SuggestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(TransportSuggestAction.class).asEagerSingleton();
        bind(TransportSuggestRefreshAction.class).asEagerSingleton();
        bind(TransportSuggestStatisticsAction.class).asEagerSingleton();

        MapBinder<GenericAction, TransportAction> transportActionsBinder =
            MapBinder.newMapBinder(binder(), GenericAction.class, TransportAction.class);

        transportActionsBinder.addBinding(SuggestAction.INSTANCE).to(TransportSuggestAction.class).asEagerSingleton();
        transportActionsBinder.addBinding(SuggestRefreshAction.INSTANCE).to(TransportSuggestRefreshAction.class).asEagerSingleton();
        transportActionsBinder.addBinding(SuggestStatisticsAction.INSTANCE).to(TransportSuggestStatisticsAction.class).asEagerSingleton();

        MapBinder<String, GenericAction> actionsBinder = MapBinder.newMapBinder(binder(), String.class, GenericAction.class);
        actionsBinder.addBinding(SuggestAction.NAME).toInstance(SuggestAction.INSTANCE);
        actionsBinder.addBinding(SuggestRefreshAction.NAME).toInstance(SuggestRefreshAction.INSTANCE);
        actionsBinder.addBinding(SuggestStatisticsAction.NAME).toInstance(SuggestStatisticsAction.INSTANCE);
    }
}

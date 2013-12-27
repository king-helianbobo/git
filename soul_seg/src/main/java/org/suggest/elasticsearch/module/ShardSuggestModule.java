package org.suggest.elasticsearch.module;

import org.elasticsearch.common.inject.AbstractModule;
import org.suggest.elasticsearch.service.ShardSuggestService;


public class ShardSuggestModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ShardSuggestService.class).asEagerSingleton();
    }

}

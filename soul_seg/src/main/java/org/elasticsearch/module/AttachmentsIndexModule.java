package org.elasticsearch.module;

import org.elasticsearch.common.inject.AbstractModule;
import org.index.mapper.attachment.RegisterAttachmentTypeParser;

public class AttachmentsIndexModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(RegisterAttachmentTypeParser.class).asEagerSingleton();
	}
}

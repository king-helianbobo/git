package org.elasticsearch.module;

import org.elasticsearch.attachment.RegisterAttachmentTypeParser;
import org.elasticsearch.common.inject.AbstractModule;

public class AttachmentsIndexModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(RegisterAttachmentTypeParser.class).asEagerSingleton();
	}
}

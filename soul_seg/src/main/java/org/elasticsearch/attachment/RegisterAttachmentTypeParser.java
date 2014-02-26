package org.elasticsearch.attachment;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.AbstractIndexComponent;
import org.elasticsearch.index.Index;
import org.elasticsearch.index.mapper.MapperService;
import org.elasticsearch.index.settings.IndexSettings;

public class RegisterAttachmentTypeParser extends AbstractIndexComponent {

	@Inject
	public RegisterAttachmentTypeParser(Index index,
			@IndexSettings Settings indexSettings, MapperService mapperService) {
		super(index, indexSettings);
		mapperService.documentMapperParser().putTypeParser("attachment",
				new AttachmentMapper.TypeParser());
	}
}

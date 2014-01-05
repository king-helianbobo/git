/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.index.mapper.attachment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tika.metadata.Metadata;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.index.mapper.*;
import org.elasticsearch.index.mapper.core.DateFieldMapper;
import org.elasticsearch.index.mapper.core.IntegerFieldMapper;
import org.elasticsearch.index.mapper.core.StringFieldMapper;
import org.elasticsearch.index.mapper.multifield.MultiFieldMapper;
import org.index.mapper.xcontent.test.EncryptedDocMapperTest;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.mapper.MapperBuilders.*;
import static org.elasticsearch.index.mapper.core.TypeParsers.parsePathType;
import static org.soul.utility.TikaInstance.tika;

/**
 * 
 * <pre>
 * {
 *      file1 : {
 *          _content_type : "application/pdf",
 *          _content_length : "500000000",
 *          _name : "..../something.pdf",
 *          content : ""
 *      }
 * }
 * </pre>
 * <p/>
 * _content_length = Specify the maximum amount of characters to extract from
 * the attachment. If not specified, then the default for tika is 100,000
 * characters. Caution is required when setting large values as this can cause
 * memory issues.
 */
public class AttachmentMapper implements Mapper {

	private static Log log = LogFactory.getLog(AttachmentMapper.class);

	private static ESLogger logger = ESLoggerFactory
			.getLogger(AttachmentMapper.class.getName());

	public static final String CONTENT_TYPE = "attachment";

	public static class Defaults {
		public static final ContentPath.Type PATH_TYPE = ContentPath.Type.FULL;
	}

	public static class Builder extends
			Mapper.Builder<Builder, AttachmentMapper> {

		private ContentPath.Type pathType = Defaults.PATH_TYPE;

		private Integer defaultIndexedChars = null;

		private Boolean ignoreErrors = null;

		private Mapper.Builder contentBuilder;

		private Mapper.Builder titleBuilder = stringField("title");

		private Mapper.Builder nameBuilder = stringField("name");

		private Mapper.Builder authorBuilder = stringField("author");

		private Mapper.Builder keywordsBuilder = stringField("keywords");

		private Mapper.Builder dateBuilder = dateField("date");

		private Mapper.Builder contentTypeBuilder = stringField("content_type");

		private Mapper.Builder contentLengthBuilder = integerField("content_length");

		public Builder(String name) {
			// name is the document fieldName
			super(name);
			this.builder = this;
			this.contentBuilder = stringField(name);
		}

		public Builder pathType(ContentPath.Type pathType) {
			this.pathType = pathType; // 设置path Type
			return this;
		}

		public Builder content(Mapper.Builder content) {
			this.contentBuilder = content;
			return this;
		}

		public Builder date(Mapper.Builder date) {
			this.dateBuilder = date;
			return this;
		}

		public Builder author(Mapper.Builder author) {
			this.authorBuilder = author;
			return this;
		}

		public Builder title(Mapper.Builder title) {
			this.titleBuilder = title;
			return this;
		}

		public Builder name(Mapper.Builder name) {
			this.nameBuilder = name;
			return this;
		}

		public Builder keywords(Mapper.Builder keywords) {
			this.keywordsBuilder = keywords;
			return this;
		}

		public Builder contentType(Mapper.Builder contentType) {
			this.contentTypeBuilder = contentType;
			return this;
		}

		public Builder contentLength(Mapper.Builder contentType) {
			this.contentLengthBuilder = contentType;
			return this;
		}

		@Override
		public AttachmentMapper build(BuilderContext context) {
			ContentPath.Type origPathType = context.path().pathType();
			context.path().pathType(pathType);

			// create the content mapper under the actual name
			Mapper contentMapper = contentBuilder.build(context);

			// create the DC one under the name
			context.path().add(name);
			Mapper dateMapper = dateBuilder.build(context);
			Mapper authorMapper = authorBuilder.build(context);
			Mapper titleMapper = titleBuilder.build(context);
			Mapper nameMapper = nameBuilder.build(context);
			Mapper keywordsMapper = keywordsBuilder.build(context);
			Mapper contentTypeMapper = contentTypeBuilder.build(context);
			Mapper contentLength = contentLengthBuilder.build(context);
			context.path().remove();

			context.path().pathType(origPathType);

			if (defaultIndexedChars == null && context.indexSettings() != null) {
				defaultIndexedChars = context.indexSettings().getAsInt(
						"index.mapping.attachment.indexed_chars", 100000);
			}
			if (defaultIndexedChars == null) {
				defaultIndexedChars = 100000;
			}

			if (ignoreErrors == null && context.indexSettings() != null) {
				ignoreErrors = context.indexSettings().getAsBoolean(
						"index.mapping.attachment.ignore_errors", Boolean.TRUE);
			}
			if (ignoreErrors == null) {
				ignoreErrors = Boolean.TRUE;
			}

			// name is fieldName
			return new AttachmentMapper(name, pathType, defaultIndexedChars,
					ignoreErrors, contentMapper, dateMapper, titleMapper,
					nameMapper, authorMapper, keywordsMapper,
					contentTypeMapper, contentLength);
		}
	}

	/**
	 * <pre>
	 *  field1 : { type : "attachment" }
	 * </pre>
	 * 
	 * Or:
	 * 
	 * <pre>
	 *  field1 : {
	 *      type : "attachment",
	 *      fields : {
	 *          field1 : {type : "binary"},
	 *          title : {store : "yes"},
	 *          date : {store : "yes"},
	 *          name : {store : "yes"},
	 *          author : {store : "yes"},
	 *          keywords : {store : "yes"},
	 *          content_type : {store : "yes"},
	 *          content_length : {store : "yes"}
	 *      }
	 * }
	 * </pre>
	 */
	public static class TypeParser implements Mapper.TypeParser {

		@SuppressWarnings({ "unchecked" })
		@Override
		public Mapper.Builder parse(String fieldName, Map<String, Object> node,
				ParserContext parserContext) throws MapperParsingException {
			AttachmentMapper.Builder builder = new AttachmentMapper.Builder(
					fieldName);

			if (!node.containsKey("type")
					|| !(node.get("type").equals("attachment"))) {
				throw new MapperParsingException(
						"field type must be attachment!");
			}

			for (Map.Entry<String, Object> entry : node.entrySet()) {
				String mapKey = entry.getKey();
				Object mapValue = entry.getValue();
				if (mapKey.equals("path")) { // whether is is fullPath or not
					builder.pathType(parsePathType(fieldName,
							mapValue.toString()));
				} else if (mapKey.equals("fields")) {
					// fields中不再允许出现multi-field
					Map<String, Object> innerMap = (Map<String, Object>) mapValue;
					for (Map.Entry<String, Object> entry1 : innerMap.entrySet()) {
						String _FieldName = entry1.getKey();
						Object _FieldDescription = entry1.getValue();
						boolean isString = false;
						if (_FieldDescription != null
								&& _FieldDescription instanceof Map) {
							Object oType = ((Map<String, Object>) _FieldDescription)
									.get("type");
							if (oType != null
									&& oType.equals(MultiFieldMapper.CONTENT_TYPE)) {
								throw new MapperParsingException(
										"multi-field type is not allowed in attachMent type!");
								// isMultifield = true;
							} else if (oType != null
									&& oType.equals(StringFieldMapper.CONTENT_TYPE)) {
								isString = true;
							} else {
								isString = false;
							}
						}
						if (fieldName.equals(_FieldName)) {
							builder.content(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									fieldName,
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("date".equals(_FieldName)) {
							// If a specific format is already defined here, we
							// should use it
							// builder.date(parserContext
							// .typeParser(
							// isMultifield ? MultiFieldMapper.CONTENT_TYPE
							// : isString ? StringFieldMapper.CONTENT_TYPE
							// : DateFieldMapper.CONTENT_TYPE)
							// .parse("date",
							// (Map<String, Object>) propNode,
							// parserContext));

							builder.date(parserContext
									.typeParser(
											isString ? StringFieldMapper.CONTENT_TYPE
													: DateFieldMapper.CONTENT_TYPE)
									.parse("date",
											(Map<String, Object>) _FieldDescription,
											parserContext));
						} else if ("title".equals(_FieldName)) {
							builder.title(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									"title",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("name".equals(_FieldName)) {
							builder.name(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									"name",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("author".equals(_FieldName)) {
							builder.author(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									"author",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("keywords".equals(_FieldName)) {
							builder.keywords(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									"keywords",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("content_type".equals(_FieldName)) {
							builder.contentType(parserContext.typeParser(
									StringFieldMapper.CONTENT_TYPE).parse(
									"content_type",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						} else if ("content_length".equals(_FieldName)) {
							builder.contentLength(parserContext.typeParser(
									IntegerFieldMapper.CONTENT_TYPE).parse(
									"content_length",
									(Map<String, Object>) _FieldDescription,
									parserContext));
						}
					}
				}
			}

			return builder;
		}
	}

	private final String name;

	private final ContentPath.Type pathType;

	private final int defaultIndexedChars;

	private final boolean ignoreErrors;

	private final Mapper contentMapper;

	private final Mapper dateMapper;

	private final Mapper authorMapper;

	private final Mapper titleMapper;

	private final Mapper nameMapper;

	private final Mapper keywordsMapper;

	private final Mapper contentTypeMapper;

	private final Mapper contentLengthMapper;

	public AttachmentMapper(String name, ContentPath.Type pathType,
			int defaultIndexedChars, Boolean ignoreErrors,
			Mapper contentMapper, Mapper dateMapper, Mapper titleMapper,
			Mapper nameMapper, Mapper authorMapper, Mapper keywordsMapper,
			Mapper contentTypeMapper, Mapper contentLengthMapper) {
		this.name = name;
		this.pathType = pathType;
		this.defaultIndexedChars = defaultIndexedChars;
		this.ignoreErrors = ignoreErrors;
		this.contentMapper = contentMapper;
		this.dateMapper = dateMapper;
		this.titleMapper = titleMapper;
		this.nameMapper = nameMapper;
		this.authorMapper = authorMapper;
		this.keywordsMapper = keywordsMapper;
		this.contentTypeMapper = contentTypeMapper;
		this.contentLengthMapper = contentLengthMapper;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void parse(ParseContext context) throws IOException {
		byte[] content = null;
		String contentType = null;
		int indexedChars = defaultIndexedChars;
		String name = null;

		XContentParser parser = context.parser();
		XContentParser.Token token = parser.currentToken();
		if (token == XContentParser.Token.VALUE_STRING) {
			content = parser.binaryValue();
			log.info("VALUE_STRING is got!" + content);
		} else {
			String currentFieldName = null;
			while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
				if (token == XContentParser.Token.FIELD_NAME) {
					currentFieldName = parser.currentName();
					log.info("currentFieldName " + currentFieldName);
				} else if (token == XContentParser.Token.VALUE_STRING) {
					if ("content".equals(currentFieldName)) {
						content = parser.binaryValue();
					} else if ("_content_type".equals(currentFieldName)) {
						contentType = parser.text();
					} else if ("_name".equals(currentFieldName)) {
						name = parser.text();
					}
				} else if (token == XContentParser.Token.VALUE_NUMBER) {
					if ("_indexed_chars".equals(currentFieldName)
							|| "_indexedChars".equals(currentFieldName)) {
						indexedChars = parser.intValue();
					}
				}
			}
		}

		// Throw clean exception when no content is provided Fix #23
		if (content == null) {
			throw new MapperParsingException("No content is provided.");
		}

		Metadata metadata = new Metadata();
		if (contentType != null) {
			metadata.add(Metadata.CONTENT_TYPE, contentType);
		}
		if (name != null) {
			metadata.add(Metadata.RESOURCE_NAME_KEY, name);
		}

		String parsedContent;
		try {
			// Set the maximum length of strings returned by the parseToString
			// method, -1 sets no limit
			parsedContent = tika().parseToString(
					new BytesStreamInput(content, false), metadata,
					indexedChars);
			// if (parsedContent != null)
			// log.info(parsedContent);
		} catch (Throwable e) {
			// #18: we could ignore errors when Tika does not parse data
			if (!ignoreErrors)
				throw new MapperParsingException("Failed to extract ["
						+ indexedChars + "] characters of text for [" + name
						+ "]", e);
			return;
		}

		context.externalValue(parsedContent);
		contentMapper.parse(context);

		try {
			context.externalValue(name);
			nameMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing name: {}",
						e.getMessage());
		}

		try {
			context.externalValue(metadata.get(Metadata.DATE));
			dateMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing date: {}: {}",
						e.getMessage(), context.externalValue());
		}

		try {
			context.externalValue(metadata.get(Metadata.TITLE));
			titleMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing title: {}: {}",
						e.getMessage(), context.externalValue());
		}

		try {
			context.externalValue(metadata.get(Metadata.AUTHOR));
			authorMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing author: {}: {}",
						e.getMessage(), context.externalValue());
		}

		try {
			context.externalValue(metadata.get(Metadata.KEYWORDS));
			keywordsMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing keywords: {}: {}",
						e.getMessage(), context.externalValue());
		}

		try {
			context.externalValue(metadata.get(Metadata.CONTENT_TYPE));
			contentTypeMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing content_type: {}: {}",
						e.getMessage(), context.externalValue());
		}

		try {
			if (metadata.get(Metadata.CONTENT_LENGTH) != null) {
				// We try to get CONTENT_LENGTH from Tika first
				context.externalValue(metadata.get(Metadata.CONTENT_LENGTH));
			} else {
				// Otherwise, we use our byte[] length
				context.externalValue(content.length);
			}
			contentLengthMapper.parse(context);
		} catch (MapperParsingException e) {
			if (!ignoreErrors)
				throw e;
			if (logger.isDebugEnabled())
				logger.debug(
						"Ignoring MapperParsingException catch while parsing content_length: {}: {}",
						e.getMessage(), context.externalValue());
		}
	}

	@Override
	public void merge(Mapper mergeWith, MergeContext mergeContext)
			throws MergeMappingException {
		// ignore this for now
	}

	@Override
	public void traverse(FieldMapperListener fieldMapperListener) {
		contentMapper.traverse(fieldMapperListener);
		dateMapper.traverse(fieldMapperListener);
		titleMapper.traverse(fieldMapperListener);
		nameMapper.traverse(fieldMapperListener);
		authorMapper.traverse(fieldMapperListener);
		keywordsMapper.traverse(fieldMapperListener);
		contentTypeMapper.traverse(fieldMapperListener);
		contentLengthMapper.traverse(fieldMapperListener);
	}

	@Override
	public void traverse(ObjectMapperListener objectMapperListener) {
	}

	@Override
	public void close() {
		contentMapper.close();
		dateMapper.close();
		titleMapper.close();
		nameMapper.close();
		authorMapper.close();
		keywordsMapper.close();
		contentTypeMapper.close();
		contentLengthMapper.close();
	}

	@Override
	public XContentBuilder toXContent(XContentBuilder builder, Params params)
			throws IOException {
		builder.startObject(name);
		builder.field("type", CONTENT_TYPE);
		builder.field("path", pathType.name().toLowerCase());

		builder.startObject("fields");
		contentMapper.toXContent(builder, params);
		authorMapper.toXContent(builder, params);
		titleMapper.toXContent(builder, params);
		nameMapper.toXContent(builder, params);
		dateMapper.toXContent(builder, params);
		keywordsMapper.toXContent(builder, params);
		contentTypeMapper.toXContent(builder, params);
		contentLengthMapper.toXContent(builder, params);
		builder.endObject();

		builder.endObject();
		return builder;
	}
}
package org.elasticsearch.hadoop.serailize;

import java.util.Map;
import org.apache.hadoop.io.Text;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.elasticsearch.hadoop.cfg.Settings;
import org.elasticsearch.hadoop.util.BytesArray;
import org.elasticsearch.hadoop.util.FastByteArrayOutputStream;
import org.elasticsearch.hadoop.util.ObjectUtils;
import org.elasticsearch.hadoop.util.StringUtils;

/**
 * Base class for Bulk commands.
 */
abstract class AbstractCommand implements Command {

	private static Log log = LogFactory.getLog(BulkCommands.class);

	protected final IdExtractor idExtractor;
	protected final ValueWriter<?> valueWriter;

	private BytesArray scratchPad = new BytesArray(1024);
	private byte[] idAsBytes = null;

	static final byte[] CARRIER_RETURN = "\n".getBytes(StringUtils.UTF_8);

	AbstractCommand(Settings settings) {
		this.valueWriter = ObjectUtils.instantiate(
				settings.getSerializerValueWriterClassName(), settings);
		this.idExtractor = (StringUtils.hasText(settings.getMappingId()) ? ObjectUtils
				.<IdExtractor> instantiate(
						settings.getMappingIdExtractorClassName(), settings)
				: null);
		if (log.isTraceEnabled()) {
			log.trace(String.format("Instantiated value writer [%s]",
					valueWriter));
			if (idExtractor != null) {
				log.trace(String.format("Instantiated id extractor [%s]",
						idExtractor));
			}
		}
	}

	@Override
	public int prepare(Object object) {
		int entrySize = 0;
		if (object instanceof SerializedObject) {
			SerializedObject so = ((SerializedObject) object);
			idAsBytes = so.id;
		} else { // when is WriteAble object
			String id = extractId(object);
			if (StringUtils.hasText(id)) {
				idAsBytes = id.getBytes(StringUtils.UTF_8);
			}
		}
		if (idAsBytes != null) {
			entrySize += headerPrefix().length;
			entrySize += idAsBytes.length;
			entrySize += headerSuffix().length;
		} else {
			if (isIdRequired()) {
				throw new IllegalArgumentException(
						String.format(
								"Operation [%s] requires an id but none was given/found",
								this.toString()));
			} else {
				entrySize += header().length;
			}
		}
		if (isSourceRequired()) {
			if (object instanceof SerializedObject) {
				entrySize += ((SerializedObject) object).size;
			} else {
				String fieldName = idExtractor.getIdFieldName();
				Text key = new Text(fieldName.getBytes());
				if ((key != null) && (object instanceof Map)) {
					// we pay special attention to Map
					Map<?, ?> map = (Map<?, ?>) object;
					map.remove(key);
				}
				serialize(object);
				entrySize += scratchPad.size();
			}
			entrySize++; // add newline char length
		}
		return entrySize;
	}

	@Override
	public void write(Object object, BytesArray buffer) {
		writeActionAndMetadata(object, buffer);
		if (isSourceRequired()) {
			writeSource(object, buffer);
			writeTrailingReturn(buffer); // write newline character
		}
	}

	protected void writeActionAndMetadata(Object object, BytesArray data) {
		if (idAsBytes != null) {
			copyIntoBuffer(headerPrefix(), data);
			copyIntoBuffer(idAsBytes, data);
			copyIntoBuffer(headerSuffix(), data);
		} else {
			copyIntoBuffer(header(), data);
		}
	}

	private String extractId(Object object) {
		return (idExtractor != null ? idExtractor.getIdValue(object) : null);
	}

	protected abstract byte[] headerPrefix();

	protected abstract byte[] headerSuffix();

	protected abstract byte[] header();

	protected boolean isSourceRequired() {
		return true;
	}

	protected void writeSource(Object object, BytesArray buffer) {
		// object was serialized - just write it down
		if (object instanceof SerializedObject) {
			SerializedObject so = (SerializedObject) object;
			System.arraycopy(so.data, 0, buffer.bytes(), buffer.size(), so.size);
			buffer.increment(so.size);
		} else {
			System.arraycopy(scratchPad.bytes(), 0, buffer.bytes(),
					buffer.size(), scratchPad.size());
			buffer.increment(scratchPad.size());
		}
	}

	private void writeTrailingReturn(BytesArray buffer) {
		System.arraycopy(CARRIER_RETURN, 0, buffer.bytes(), buffer.size(),
				CARRIER_RETURN.length);
		buffer.increment(1);
	}

	private void serialize(Object object) {
		scratchPad.reset();
		FastByteArrayOutputStream bos = new FastByteArrayOutputStream(
				scratchPad);
		ContentBuilder.generate(bos, valueWriter).value(object).flush().close();
	}

	protected boolean isIdRequired() {
		return false;
	}

	static final void copyIntoBuffer(byte[] content, BytesArray bytes) {
		System.arraycopy(content, 0, bytes.bytes(), bytes.size(),
				content.length);
		bytes.increment(content.length);
	}
}
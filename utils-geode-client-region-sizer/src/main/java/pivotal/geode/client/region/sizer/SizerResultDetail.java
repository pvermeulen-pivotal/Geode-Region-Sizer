package pivotal.geode.client.region.sizer;

import java.io.Serializable;

public class SizerResultDetail implements Serializable {

	private static final long serialVersionUID = 2956868595173797200L;

	private Object key = null;
	private long deserializedRegionEntrySizeBefore = 0;
	private long serializedValueSize = 0;
	private long deserializedKeySize = 0;
	private long deserializedValueSize = 0;

	public SizerResultDetail() {
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public long getDeserializedRegionEntrySizeBefore() {
		return deserializedRegionEntrySizeBefore;
	}

	public void setDeserializedRegionEntrySizeBefore(
			long deserializedRegionEntrySizeBefore) {
		this.deserializedRegionEntrySizeBefore = deserializedRegionEntrySizeBefore;
	}

	public long getSerializedValueSize() {
		return serializedValueSize;
	}

	public void setSerializedValueSize(long serializedValueSize) {
		this.serializedValueSize = serializedValueSize;
	}

	public long getDeserializedKeySize() {
		return deserializedKeySize;
	}

	public void setDeserializedKeySize(long deserializedKeySize) {
		this.deserializedKeySize = deserializedKeySize;
	}

	public long getDeserializedValueSize() {
		return deserializedValueSize;
	}

	public void setDeserializedValueSize(long deserializedValueSize) {
		this.deserializedValueSize = deserializedValueSize;
	}
}

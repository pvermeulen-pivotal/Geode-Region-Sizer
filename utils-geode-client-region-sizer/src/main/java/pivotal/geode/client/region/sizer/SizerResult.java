package pivotal.geode.client.region.sizer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SizerResult implements Serializable {

	private static final long serialVersionUID = -4445935443461365541L;

	private long totalDeserializedRegionEntrySizeBefore;
	private long totalDeserializedKeySize;
	private long totalDeserializedValueSize;
	private long totalSerializedValueSize;
	private long regionSize;
	private long regionSizeSampled;
	private long averageRegionEntrySizeSerialized;
	private long averageRegionEntrySizeDeserialized;
	private long averageKeySize;
	private long averageValueSizeSerialized;
	private long averageValueSizeDeserialized;
	private long largestHistogramObjectSize;
	
	private List<SizerResult> memberResults;
	private List<SizerResultDetail> regionDetail;

	private String memberName;
	private String regionName;
	private String regionType;
	private String largestHistogram;

	public SizerResult() {
		regionDetail = new ArrayList<SizerResultDetail>();
		memberResults = new ArrayList<SizerResult>();
	}

	public String getRegionName() {
		return regionName;
	}

	public void setRegionName(String regionName) {
		this.regionName = regionName;
	}

	public String getRegionType() {
		return regionType;
	}

	public void setRegionType(String regionType) {
		this.regionType = regionType;
	}

	public long getTotalDeserializedRegionEntrySizeBefore() {
		return totalDeserializedRegionEntrySizeBefore;
	}

	public void setTotalDeserializedRegionEntrySizeBefore(
			long totalDeserializedRegionEntrySizeBefore) {
		this.totalDeserializedRegionEntrySizeBefore = totalDeserializedRegionEntrySizeBefore;
	}

	public long getTotalDeserializedKeySize() {
		return totalDeserializedKeySize;
	}

	public void setTotalDeserializedKeySize(long totalDeserializedKeySize) {
		this.totalDeserializedKeySize = totalDeserializedKeySize;
	}

	public long getTotalDeserializedValueSize() {
		return totalDeserializedValueSize;
	}

	public void setTotalDeserializedValueSize(long totalDeserializedValueSize) {
		this.totalDeserializedValueSize = totalDeserializedValueSize;
	}

	public long getTotalSerializedValueSize() {
		return totalSerializedValueSize;
	}

	public void setTotalSerializedValueSize(long totalSerializedValueSize) {
		this.totalSerializedValueSize = totalSerializedValueSize;
	}

	public long getRegionSize() {
		return regionSize;
	}

	public void setRegionSize(long totalRegionSize) {
		this.regionSize = totalRegionSize;
	}

	public long getRegionSizeSampled() {
		return regionSizeSampled;
	}

	public void setRegionSizeSampled(long regionSizeSampled) {
		this.regionSizeSampled = regionSizeSampled;
	}

	public long getAverageRegionEntrySizeSerialized() {
		return averageRegionEntrySizeSerialized;
	}

	public void setAverageRegionEntrySizeSerialized(
			long averageRegionEntrySizeSerialized) {
		this.averageRegionEntrySizeSerialized = averageRegionEntrySizeSerialized;
	}

	public long getAverageRegionEntrySizeDeserialized() {
		return averageRegionEntrySizeDeserialized;
	}

	public void setAverageRegionEntrySizeDeserialized(
			long averageRegionEntrySizeDeserialized) {
		this.averageRegionEntrySizeDeserialized = averageRegionEntrySizeDeserialized;
	}

	public long getAverageKeySize() {
		return averageKeySize;
	}

	public void setAverageKeySize(long averageKeySize) {
		this.averageKeySize = averageKeySize;
	}

	public long getAverageValueSizeSerialized() {
		return averageValueSizeSerialized;
	}

	public void setAverageValueSizeSerialized(long averageValueSizeSerialized) {
		this.averageValueSizeSerialized = averageValueSizeSerialized;
	}

	public long getAverageValueSizeDeserialized() {
		return averageValueSizeDeserialized;
	}

	public void setAverageValueSizeDeserialized(long averageValueSizeDeserialized) {
		this.averageValueSizeDeserialized = averageValueSizeDeserialized;
	}

	public List<SizerResultDetail> getRegionDetail() {
		return regionDetail;
	}

	public void setRegionDetail(List<SizerResultDetail> regionDetail) {
		this.regionDetail = regionDetail;
	}

	public void addRegionDetail(SizerResultDetail sizerResultDetail) {
		this.regionDetail.add(sizerResultDetail);
	}

	public void addAllRegionDetail(List<SizerResultDetail> results) {
		this.regionDetail.addAll(results);
	}

	public List<SizerResult> getMemberResults() {
		return memberResults;
	}

	public void addMemberResults(List<SizerResult> memberResults) {
		this.memberResults.addAll(memberResults);
	}
	
	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}

	public String getLargestHistogram() {
		return largestHistogram;
	}

	public void setLargestHistogram(String largestHistogram) {
		this.largestHistogram = largestHistogram;
	}

	public long getLargestHistogramObjectSize() {
		return largestHistogramObjectSize;
	}

	public void setLargestHistogramObjectSize(long largestHistogramObjectSize) {
		this.largestHistogramObjectSize = largestHistogramObjectSize;
	}
}

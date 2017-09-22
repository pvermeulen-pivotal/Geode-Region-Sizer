package pivotal.geode.server.region.sizer.internal;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Set;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.partition.PartitionRegionHelper;
import org.apache.geode.internal.GemFireVersion;
import org.apache.geode.internal.cache.CachedDeserializable;
import org.apache.geode.internal.cache.EntrySnapshot;
import org.apache.geode.internal.cache.LocalRegion;
import org.apache.geode.internal.cache.NonLocalRegionEntryWithStats;
import org.apache.geode.internal.cache.PartitionedRegion;
import org.apache.geode.internal.cache.RegionEntry;
import org.apache.geode.internal.size.ObjectGraphSizer;
import org.apache.geode.internal.size.ReflectionObjectSizer;
import org.apache.geode.internal.size.ReflectionSingleObjectSizer;
import org.apache.geode.internal.size.SizeClassOnceObjectSizer;
import org.apache.geode.internal.size.WellKnownClassSizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pivotal.geode.client.region.sizer.SizerResult;
import pivotal.geode.client.region.sizer.SizerResultDetail;

public class SizeCalculator {

	private Logger logger = LoggerFactory.getLogger(SizeCalculator.class);

	private long totalDeserializedRegionEntrySizeBefore;
	private long totalDeserializedKeySize;
	private long totalDeserializedValueSize;
	private long totalSerializedValueSize;

	/**
	 * Creates an instance that logs all the output to <code>System.out</code>.
	 */
	public SizeCalculator() {
	}

	/**
	 * Creates an instance that logs to the provided <code>LogWriter</code>.
	 * 
	 * @param logger
	 *            <code>LogWriter</code> to use for all the output.
	 */
	public SizeCalculator(Logger logger) {
		this.logger = logger;
	}

	public static int sizeSizeClassOnceObjectSizer(Object o) {
		return SizeClassOnceObjectSizer.getInstance().sizeof(o);
	}

	public static int sizeObjectWellKnownClassSizer(Object o) {
		return WellKnownClassSizer.sizeof(o);
	}

	public static int sizeObjectReflectionSingleObjectSizer(Object o) {
		ReflectionSingleObjectSizer sizer = new ReflectionSingleObjectSizer();
		return (int) sizer.sizeof(o);
	}

	public static int sizeObjectReflectionObjectSizer(Object o) {
		return ReflectionObjectSizer.getInstance().sizeof(o);
	}

	public static int sizeObjectReflectionObjectSizer(Object o, boolean includeStatistics) {
		return ReflectionObjectSizer.getInstance().sizeof(o);
	}

	public static long objectGraphSizer(Object o) throws Exception {
		return ObjectGraphSizer.size(o);
	}

	public static String histObject(Object o) throws Exception {
		return ObjectGraphSizer.histogram(o, false);
	}

	/**
	 * Calculates and logs the size of all entries in the region.
	 * 
	 * @param region
	 * @throws Exception
	 */
	public void sizeRegion(Region<?, ?> region) throws Exception {
		sizeRegion(region, 0);
	}

	/**
	 * Calculates and logs the size of first numEntries in the region.
	 * 
	 * @param region
	 * @param numEntries
	 *            The number of entries to calculate the size for. If 0 all the
	 *            entries in the region are included.
	 * @throws Exception
	 */
	public SizerResult sizeRegion(Region<?, ?> region, int numEntries) throws Exception {

		if (region == null) {
			throw new IllegalArgumentException("Region is null.");
		}

		if (region instanceof PartitionedRegion) {
			return sizePartitionedRegion(region, numEntries);
		} else {
			return sizeReplicatedOrLocalRegion(region, numEntries);
		}
	}

	/**
	 * Sizes numEntries of a partitioned region, or all the entries if numEntries is
	 * 0.
	 * 
	 * @param numEntries
	 *            Number of entries to size. If the value is 0, all the entries are
	 *            sized.
	 * @throws Exception
	 */
	private SizerResult sizePartitionedRegion(Region<?, ?> region, int numEntries) throws Exception {
		long largestObjectSize = 0;
		String histogramLargestObjectSize = new String();

		SizerResult results = new SizerResult();

		Region<?, ?> primaryDataSet = (Region<?, ?>) PartitionRegionHelper.getLocalData(region);

		results.setRegionName(region.getName());

		int regionSize = primaryDataSet.size();

		if (numEntries == 0) {
			numEntries = primaryDataSet.size();
		} else if (numEntries > regionSize) {
			numEntries = regionSize;
		}

		results.setMemberName(CacheFactory.getAnyInstance().getName());
		results.setRegionName(region.getName());
		results.setRegionType("Partitioned");
		results.setRegionSize(regionSize);

		int count = 0;
		for (Iterator<?> i = primaryDataSet.entrySet().iterator(); i.hasNext();) {
			if (count == numEntries) {
				break;
			}
			EntrySnapshot entry = (EntrySnapshot) i.next();
			RegionEntry re = entry.getRegionEntry();
			SizerResultDetail srd = dumpSizes(entry, re, true);
			if (srd.getDeserializedValueSize() > largestObjectSize) {
				histogramLargestObjectSize = histObject(entry.getValue());
				largestObjectSize = srd.getDeserializedValueSize() ;
			}
			results.addRegionDetail(srd);
			count++;
		}

		results = updateResults(results, numEntries, regionSize, largestObjectSize, histogramLargestObjectSize);

		clearTotals();

		return results;
	}

	/**
	 * Sizes numEntries of a replicated or local region, or all the entries if
	 * numEntries is 0.
	 * 
	 * @param numEntries
	 *            Number of entries to size. If the value is 0, all the entries are
	 *            sized.
	 * @throws Exception
	 */
	public SizerResult sizeReplicatedOrLocalRegion(Region<?, ?> region, int numEntries) throws Exception {
		long largestObjectSize = 0;
		String histogramLargestObjectSize = new String();
		boolean version6 = false;

		String versionId = GemFireVersion.getGemFireVersion();
		if (versionId.contains("6.6.4")) {
			version6 = true;
		}

		SizerResult results = new SizerResult();

		results.setRegionName(region.getName());

		Set<?> keys = (Set<?>) region.keySet();

		int regionSize = keys.size();

		if (numEntries == 0) {
			numEntries = regionSize;
		} else if (numEntries > regionSize) {
			numEntries = regionSize;
		}

		results.setMemberName(CacheFactory.getAnyInstance().getName());
		results.setRegionName(region.getName());
		results.setRegionType("Replicated");
		results.setRegionSize(regionSize);

		int count = 0;
		for (Iterator<?> i = region.entrySet().iterator(); i.hasNext();) {
			if (count == numEntries)
				break;
			LocalRegion.NonTXEntry entry = (LocalRegion.NonTXEntry) i.next();
			RegionEntry re = entry.getRegionEntry();
			NonLocalRegionEntryWithStats nlRe;
			if (!version6) {
				Class<?> clazz = Class.forName("com.gemstone.gemfire.internal.cache.NonLocalRegionEntryWithStats");
				Constructor<?> ctor = clazz.getConstructor(RegionEntry.class, LocalRegion.class, boolean.class);
				nlRe = (NonLocalRegionEntryWithStats) ctor.newInstance(new Object[] { re, region, false });
			} else {
				Class<?> clazz = Class.forName("org.apache.geode.internal.cache.NonLocalRegionEntryWithStats");
				Constructor<?> ctor = clazz.getConstructor(RegionEntry.class, LocalRegion.class);
				nlRe = (NonLocalRegionEntryWithStats) ctor.newInstance(new Object[] { re, region });
			}
			SizerResultDetail srd = dumpSizes(entry, re, true);
			if (srd.getDeserializedValueSize() > largestObjectSize) {
				histogramLargestObjectSize = histObject(entry.getValue());
				largestObjectSize = srd.getDeserializedValueSize();
			}
			results.addRegionDetail(dumpSizes(entry, nlRe, false));
			count++;
		}

		results = updateResults(results, numEntries, regionSize, largestObjectSize, histogramLargestObjectSize);

		clearTotals();

		return results;
	}

	public SizerResultDetail dumpSizes(Region.Entry<?, ?> entry, RegionEntry re, boolean partition) throws Exception {

		int deserializedRegionEntrySizeBefore = ReflectionObjectSizer.getInstance().sizeof(re);

		int serializedValueSize = calculateSerializedValueSize(entry, re, partition);

		int deserializedKeySize = ReflectionObjectSizer.getInstance().sizeof(entry.getKey());

		Object value = entry.getValue();
		int deserializedValueSize = 0;

		deserializedValueSize = sizeObjectReflectionObjectSizer(value);

		this.totalDeserializedRegionEntrySizeBefore += deserializedRegionEntrySizeBefore;
		this.totalDeserializedKeySize += deserializedKeySize;
		this.totalDeserializedValueSize += deserializedValueSize;
		this.totalSerializedValueSize += serializedValueSize;

		SizerResultDetail detail = new SizerResultDetail();
		detail.setKey(re.getKey());
		detail.setDeserializedValueSize(deserializedValueSize);
		detail.setDeserializedRegionEntrySizeBefore(deserializedRegionEntrySizeBefore);
		detail.setDeserializedKeySize(deserializedKeySize);
		detail.setSerializedValueSize(serializedValueSize);

		return detail;
	}

	private int calculateSerializedValueSize(Region.Entry<?, ?> entry, RegionEntry re, boolean partition) {
		Object valueInVm;
		valueInVm = re.getValue(null);
		int serializedValueSize = 0;
		if (valueInVm instanceof CachedDeserializable) {
			// Value is a wrapper
			Object cdValue = ((CachedDeserializable) valueInVm).getValue();
			if (cdValue instanceof byte[]) {
				// The wrapper wraps a serialized domain object
				serializedValueSize = ((byte[]) cdValue).length;
			} else {
				// The wrapper wraps a deserialized domain object
				serializedValueSize = ReflectionObjectSizer.getInstance().sizeof(cdValue);
			}
		} else {
			// Value is a domain object
			serializedValueSize = ReflectionObjectSizer.getInstance().sizeof(valueInVm);
		}

		return serializedValueSize;
	}

	private SizerResult updateResults(SizerResult results, int totalEntries, int regionSize, long histogramSize, String histogram) {
		results.setRegionSizeSampled(totalEntries);
		results.setTotalDeserializedRegionEntrySizeBefore(this.totalDeserializedRegionEntrySizeBefore);
		results.setTotalDeserializedKeySize(this.totalDeserializedKeySize);
		results.setTotalSerializedValueSize(this.totalSerializedValueSize);
		results.setTotalDeserializedValueSize(this.totalDeserializedValueSize);
		results.setLargestHistogram(histogram);
		results.setLargestHistogramObjectSize(histogramSize);
		return results;
	}

	private void clearTotals() {
		this.totalDeserializedRegionEntrySizeBefore = 0;
		this.totalDeserializedKeySize = 0;
		this.totalDeserializedValueSize = 0;
		this.totalSerializedValueSize = 0;
	}

	protected void log(String message) {
		if (logger != null) {
			logger.info(message);
		} else {
			System.out.println(message);
		}
	}
}
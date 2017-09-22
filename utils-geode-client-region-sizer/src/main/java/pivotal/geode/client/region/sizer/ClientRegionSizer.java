package pivotal.geode.client.region.sizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geode.GemFireException;

import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.execute.Execution;
import org.apache.geode.cache.execute.FunctionService;
import org.apache.geode.cache.execute.ResultCollector;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ClientRegionSizer {

	private BufferedWriter detailWriter = null;
	private BufferedWriter csvWriter = null;

	private Log logger = LogFactory.getLog(ClientRegionSizer.class);

	private boolean needRunDatePrinted = true;
	private boolean needCSVHeaderPrinted = true;

	public void getRegionSize(Region<?, ?> region, int records, boolean needDetail, boolean needCsv,
			String contextName) {
		Execution exec = null;
		ResultCollector<?, ?> rc = null;
		Serializable[] functionArgs = new Serializable[1];
		functionArgs[0] = records;

		try {
			exec = FunctionService.onRegion(region).setArguments(functionArgs)
					.withCollector(new RegionSizerCollector());

			rc = exec.execute("RegionSizer");

			SizerResult result = (SizerResult) rc.getResult();

			result = resize(result);

			if (detailWriter != null)
				getSummary(result, contextName);

			if (detailWriter != null)
				getDetail(result, needDetail);

			if ((needCsv) || (csvWriter != null))
				getCsv(result);

		} catch (GemFireException ex) {
			logger.error(ex.getMessage());
		}
	}

	private void getSummary(SizerResult result, String contextName) {
		try {
			if (needRunDatePrinted) {
				needRunDatePrinted = false;
				detailWriter.write("Run Date: " + new Date() + "\n\n");
				detailWriter.write("Spring Context: " + contextName + "\n\n");

			}
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
		StringBuilder str = new StringBuilder();
		// detail member/region info
		str.append("Region Summary\n");
		str.append("--------------\n");
		str.append("Region Name: " + result.getRegionName() + "\n");
		str.append("Region Type: " + result.getRegionType() + "\n");
		str.append("Region Size: " + result.getRegionSize() + "\n");
		str.append("Sample Size: " + result.getRegionSizeSampled() + "\n\n");
		// detail total sizes
		str.append("Total Entry Size:                " + result.getTotalDeserializedRegionEntrySizeBefore() + "\n");
		str.append("Total Key Size:                  " + result.getTotalDeserializedKeySize() + "\n");
		str.append("Total Value Size [Serialized]:   " + result.getTotalSerializedValueSize() + "\n");
		str.append("Total Value Size [Deserialized]: " + result.getTotalDeserializedValueSize() + "\n");
		// detail average sizes
		if (result.getRegionSizeSampled() > 0) {
			str.append("Avg Entry Size:                  "
					+ (result.getTotalDeserializedRegionEntrySizeBefore() / result.getRegionSizeSampled()) + "\n");
			str.append("Avg Key Size:                    "
					+ (result.getTotalDeserializedKeySize() / result.getRegionSizeSampled()) + "\n");
			str.append("Avg Value Size [Serialized]:     "
					+ (result.getTotalSerializedValueSize() / result.getRegionSizeSampled()) + "\n");
			str.append("Avg Value Size [Deserialized]:   "
					+ (result.getTotalDeserializedValueSize() / result.getRegionSizeSampled()) + "\n\n");
		} else {
			str.append("Avg Entry Size:                  0\n");
			str.append("Avg Key Size:                    0\n");
			str.append("Avg Value Size [Serialized]:     0\n");
			str.append("Avg Value Size [Deserialized]:   0\n");
		}
		str.append("\n");
		str.append("Largest Histogram Object Size:   " + result.getLargestHistogramObjectSize() + "\n");
		str.append("Largest Histogram:               \n");
		str.append(result.getLargestHistogram() + "\n\n");
		str.append("----------------------------------------------\n\n");
		try {
			detailWriter.write(str.toString());
			detailWriter.flush();
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		} finally {
			str = null;
		}
	}

	private void getDetail(SizerResult result, boolean needDetail) {
		for (SizerResult sResult : result.getMemberResults()) {
			StringBuilder str = new StringBuilder();
			// detail member/region info
			String padded = String.format("%" + sResult.getMemberName().length() + "s", "").replace(' ', '-');
			str.append("Region Detail - Member " + sResult.getMemberName() + "\n");
			str.append("-----------------------" + padded + "\n");
			str.append("  Region Name: " + sResult.getRegionName() + "\n");
			str.append("  Region Type: " + sResult.getRegionType() + "\n");
			str.append("  Region Size: " + sResult.getRegionSize() + "\n");
			str.append("  Sample Size: " + sResult.getRegionSizeSampled() + "\n\n");
			// detail total sizes
			str.append(
					"  Total Entry Size:                " + sResult.getTotalDeserializedRegionEntrySizeBefore() + "\n");
			str.append("  Total Key Size:                  " + sResult.getTotalDeserializedKeySize() + "\n");
			str.append("  Total Value Size [Serialized]:   " + sResult.getTotalSerializedValueSize() + "\n");
			str.append("  Total Value Size [Deserialized]: " + sResult.getTotalDeserializedValueSize() + "\n");
			// detail average sizes
			if (sResult.getRegionSizeSampled() > 0) {
				str.append("  Avg Entry Size:                  "
						+ (sResult.getTotalDeserializedRegionEntrySizeBefore() / sResult.getRegionSizeSampled())
						+ "\n");
				str.append("  Avg Key Size:                    "
						+ (sResult.getTotalDeserializedKeySize() / sResult.getRegionSizeSampled()) + "\n");
				str.append("  Avg Value Size [Serialized]:     "
						+ (sResult.getTotalSerializedValueSize() / sResult.getRegionSizeSampled()) + "\n");
				str.append("  Avg Value Size [Deserialized]:   "
						+ (sResult.getTotalDeserializedValueSize() / sResult.getRegionSizeSampled()) + "\n\n");
			} else {
				str.append("  Avg Entry Size:                  0\n");
				str.append("  Avg Key Size:                    0\n");
				str.append("  Avg Value Size [Serialized]:     0\n");
				str.append("  Avg Value Size [Deserialized]:   0\n\n");
			}
			if (needDetail) {
				// entry detail
				for (SizerResultDetail detail : sResult.getRegionDetail()) {
					str.append("    Region Entry - Key: " + detail.getKey() + "\n");
					str.append(
							"       Entry Size:              " + detail.getDeserializedRegionEntrySizeBefore() + "\n");
					str.append("       Key Size:                " + detail.getDeserializedKeySize() + "\n");
					str.append("       Value Size [Serialized]: " + detail.getSerializedValueSize() + "\n");
					str.append("                [Deserialized]: " + detail.getDeserializedValueSize() + "\n");
				}
			}
			str.append("----------------------------------------------\n\n");
			try {
				detailWriter.write(str.toString());
				detailWriter.flush();
			} catch (IOException ex) {
				logger.error(ex.getMessage());
			} finally {
				str = null;
			}
		}
	}

	private void getCsv(SizerResult result) {
		Date date = new Date();
		try {
			if (needCSVHeaderPrinted) {
				needCSVHeaderPrinted = false;
				csvWriter.write("Region Name," + "Region Type," + "Number of Objects," + "Object Size," + "Key Size,"
						+ "Run Date\n");
			}
			csvWriter.write(result.getRegionName() + "," + result.getRegionType() + "," + result.getRegionSize() + ","
					+ result.getAverageValueSizeSerialized() + "," + result.getAverageKeySize() + "," + date + "\n");
			csvWriter.flush();
		} catch (IOException ex) {
			logger.error(ex.getMessage());
		}
	}

	private SizerResult resize(SizerResult result) {
		for (SizerResult sResult : result.getMemberResults()) {

			// get region specifics
			result.setRegionName(sResult.getRegionName());
			result.setRegionType(sResult.getRegionType());

			// add region sizes
			result.setRegionSize(result.getRegionSize() + sResult.getRegionSize());
			result.setRegionSizeSampled(result.getRegionSizeSampled() + sResult.getRegionSizeSampled());

			// add totals
			result.setTotalDeserializedKeySize(
					result.getTotalDeserializedKeySize() + sResult.getTotalDeserializedKeySize());
			result.setTotalDeserializedRegionEntrySizeBefore(result.getTotalDeserializedRegionEntrySizeBefore()
					+ sResult.getTotalDeserializedRegionEntrySizeBefore());
			result.setTotalDeserializedValueSize(
					result.getTotalDeserializedValueSize() + sResult.getTotalDeserializedValueSize());
			result.setTotalSerializedValueSize(
					result.getTotalSerializedValueSize() + sResult.getTotalSerializedValueSize());
		}

		// compute average
		if (result.getRegionSizeSampled() > 0) {
			result.setAverageKeySize(result.getTotalDeserializedKeySize() / result.getRegionSizeSampled());

			result.setAverageRegionEntrySizeSerialized(
					result.getTotalDeserializedRegionEntrySizeBefore() / result.getRegionSizeSampled());

			result.setAverageValueSizeDeserialized(
					result.getTotalDeserializedValueSize() / result.getRegionSizeSampled());

			result.setAverageValueSizeSerialized(result.getTotalSerializedValueSize() / result.getRegionSizeSampled());
		}

		for (int i = 0; i < result.getMemberResults().size(); i++) {
			if (result.getMemberResults().get(i).getRegionSizeSampled() > 0) {
				result.getMemberResults().get(i)
						.setAverageKeySize(result.getMemberResults().get(i).getTotalDeserializedKeySize()
								/ result.getMemberResults().get(i).getRegionSizeSampled());
				result.getMemberResults().get(i).setAverageRegionEntrySizeSerialized(
						result.getMemberResults().get(i).getTotalDeserializedRegionEntrySizeBefore()
								/ result.getMemberResults().get(i).getRegionSizeSampled());
				result.getMemberResults().get(i).setAverageValueSizeDeserialized(
						result.getMemberResults().get(i).getTotalDeserializedValueSize()
								/ result.getMemberResults().get(i).getRegionSizeSampled());
				result.getMemberResults().get(i)
						.setAverageValueSizeSerialized(result.getMemberResults().get(i).getTotalSerializedValueSize()
								/ result.getMemberResults().get(i).getRegionSizeSampled());

			}
		}
		return result;
	}

	private void processSpringContext(SizerInput si) throws Exception {
		ClientCache cache;
		ClassPathXmlApplicationContext ctx;

		try {
			ctx = new ClassPathXmlApplicationContext(si.getInputFileName());
		} catch (Exception ex) {
			throw new RuntimeException("No/Invalid spring context file provided");
		}

		cache = ctx.getBean("cache", ClientCache.class);
		Map<String, Region> map = ctx.getBeansOfType(Region.class);

		ctx.close();

		Set<String> names = map.keySet();

		List<String> nameList = new ArrayList<String>(names);

		java.util.Collections.sort(nameList);

		for (String name : nameList) {
			long start = System.currentTimeMillis();
			System.out.println("Started processing region:   " + name + " Start time: " + new Date(start)
					+ " records selected=" + si.getNumberRecords());
			Region<?, ?> region = cache.getRegion(name);
			this.getRegionSize(region, si.getNumberRecords(), si.isDetail(), si.isCsv(), si.getInputFileName());
			long end = System.currentTimeMillis();
			System.out.println("Completed processing region: " + name + " region - End Time: " + new Date()
					+ " - Elapsed time: " + (end - start) + "ms");
		}

		if ((si.isCsv()) && (si.getCsvWriter() != null)) {
			si.getCsvWriter().close();
			System.out.println("CSV file: " + si.getCsvFileName() + " created");
		}

		if (si.getOutputWriter() != null) {
			si.getOutputWriter().close();
			System.out.println("Report file: " + si.getOutputFileName() + " created");
		}
	}

	private void processXML(SizerInput si) throws Exception {
		ClientCacheFactory ccf = new ClientCacheFactory();
		ClientCache cache = ccf.set("cache-xml", si.getInputFileName()).create();

		if (cache == null) {
			throw new RuntimeException("No/Invalid geode clinet cache xml provided");
		}

		Set<Region<?, ?>> regions = cache.rootRegions();

		for (Region<?, ?> region : regions) {
			long start = System.currentTimeMillis();
			System.out.println("Started processing region:   " + region.getName() + " Start time: " + new Date(start)
					+ " records selected=" + si.getNumberRecords());
			this.getRegionSize(region, si.getNumberRecords(), si.isDetail(), si.isCsv(), si.getInputFileName());
			long end = System.currentTimeMillis();
			System.out.println("Completed processing region: " + region.getName() + " region - End Time: " + new Date()
					+ " - Elapsed time: " + (end - start) + "ms");
		}

		if ((si.isCsv()) && (si.getCsvWriter() != null)) {
			si.getCsvWriter().close();
			System.out.println("CSV file: " + si.getCsvFileName() + " created");
		}

		if (si.getOutputWriter() != null) {
			si.getOutputWriter().close();
			System.out.println("Report file: " + si.getOutputFileName() + " created");
		}
	}

	private SizerInput getInput(String[] args) {
		SizerInput si = new SizerInput();

		if ((args == null) || (args.length < 2)) {
			throw new RuntimeException(
					"Argument[1] (xml={client cache xml}) or (spring={spring context file name}) [required]\n"
							+ "Argument[2] file={summary/detail file name [required]}\n"
							+ "Argument[3] records={numbers records to sample/0 for all}\n"
							+ "Argument[4] detail={true/false} [detail report]\n"
							+ "Argument[5] csv={csv file name for sizing}\n");
		}

		try {
			for (int i = 0; i < args.length; i++) {
				String[] split = args[i].toUpperCase().split("=");
				if ("SPRING".equalsIgnoreCase(split[0])) {
					si.setSpring(true);
					si.setInputFileName(split[1]);
				} else if ("XML".equalsIgnoreCase(split[0])) {
					si.setSpring(false);
					si.setInputFileName(split[1]);
				} else if ("OUTPUTFILE".equalsIgnoreCase(split[0])) {
					si.setOutputFileName(split[1]);
					si.setOutputWriter(new BufferedWriter(new FileWriter(new File(si.getOutputFileName()))));
				} else if ("RECORDS".equalsIgnoreCase(split[0])) {
					si.setNumberRecords(Integer.parseInt(split[1]));
				} else if ("DETAIL".equalsIgnoreCase(split[0])) {
					if ("TRUE".equalsIgnoreCase(split[1]))
						si.setDetail(true);
				} else if ("CSVFILE".equalsIgnoreCase(split[0])) {
					si.setCsv(true);
					si.setCsvWriter(new BufferedWriter(new FileWriter(new File(split[1]))));
				}
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}

		return si;
	}

	public static void main(String[] args) throws Exception {
		ClientRegionSizer crs = new ClientRegionSizer();
		SizerInput si = crs.getInput(args);
		if (si.isSpring()) {
			crs.processSpringContext(si);
		} else {
			crs.processXML(si);
		}
	}

	private static class SizerInput {
		boolean spring = false;
		boolean detail = false;
		boolean csv = false;

		int numberRecords = 0;

		String inputFileName;
		String outputFileName;
		String csvFileName;

		BufferedWriter outputWriter;
		BufferedWriter csvWriter;

		public boolean isSpring() {
			return spring;
		}

		public void setSpring(boolean spring) {
			this.spring = spring;
		}

		public boolean isDetail() {
			return detail;
		}

		public void setDetail(boolean detail) {
			this.detail = detail;
		}

		public boolean isCsv() {
			return csv;
		}

		public void setCsv(boolean csv) {
			this.csv = csv;
		}

		public String getInputFileName() {
			return inputFileName;
		}

		public void setInputFileName(String inputFileName) {
			this.inputFileName = inputFileName;
		}

		public String getOutputFileName() {
			return outputFileName;
		}

		public void setOutputFileName(String outputFileName) {
			this.outputFileName = outputFileName;
		}

		public int getNumberRecords() {
			return numberRecords;
		}

		public void setNumberRecords(int numberRecords) {
			this.numberRecords = numberRecords;
		}

		public BufferedWriter getOutputWriter() {
			return outputWriter;
		}

		public void setOutputWriter(BufferedWriter writer) {
			this.outputWriter = writer;
		}

		public String getCsvFileName() {
			return csvFileName;
		}

		public void setCsvFileName(String csvFileName) {
			this.csvFileName = csvFileName;
		}

		public BufferedWriter getCsvWriter() {
			return csvWriter;
		}

		public void setCsvWriter(BufferedWriter csvWriter) {
			this.csvWriter = csvWriter;
		}
	}
}

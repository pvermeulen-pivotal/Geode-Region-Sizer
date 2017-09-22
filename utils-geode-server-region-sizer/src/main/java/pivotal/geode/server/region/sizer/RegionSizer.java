package pivotal.geode.server.region.sizer;

import java.io.Serializable;
import java.util.Properties;

import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.execute.Function;
import org.apache.geode.cache.execute.FunctionContext;
import org.apache.geode.cache.execute.RegionFunctionContext;

import pivotal.geode.client.region.sizer.SizerResult;
import pivotal.geode.server.region.sizer.internal.SizeCalculator;


public class RegionSizer implements Function, Declarable {

	private static final long serialVersionUID = -1614279550305470075L;

	public void init(Properties arg0) {
	}

	public void execute(FunctionContext fctx) {
		SizerResult results = new SizerResult();

		RegionFunctionContext rctx = (RegionFunctionContext) fctx;

		Serializable[] args = (Serializable[]) fctx.getArguments();

		int records = (Integer) args[0];

		SizeCalculator calc = new SizeCalculator();

		try {
			results = calc.sizeRegion(rctx.getDataSet(), records);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		fctx.getResultSender().lastResult(results);
	}

	public String getId() {
		return RegionSizer.class.getSimpleName();
	}

	public boolean hasResult() {
		return true;
	}

	public boolean isHA() {
		return false;
	}

	public boolean optimizeForWrite() {
		return false;
	}

}

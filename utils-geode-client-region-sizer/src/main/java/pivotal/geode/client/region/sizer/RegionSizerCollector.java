package pivotal.geode.client.region.sizer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.geode.cache.execute.FunctionException;
import org.apache.geode.cache.execute.ResultCollector;
import org.apache.geode.distributed.DistributedMember;

public class RegionSizerCollector implements
		ResultCollector<SizerResult, SizerResult> {

	private SizerResult sizerResult = new SizerResult();
	
	private CountDownLatch done = new CountDownLatch(1);

	public void clearResults() {
		this.sizerResult = new SizerResult();
		this.done = new CountDownLatch(1);
	}

	public void endResults() {
		this.done.countDown();
	}

	public SizerResult getResult() throws FunctionException {
		while (true) {
			try {
				this.done.await();
				break;
			} catch (InterruptedException x) {
				// wait some more
			}
		}
		return sizerResult;
	}

	public SizerResult  getResult(long time, TimeUnit unit)
			throws FunctionException {
		while (true) {
			try {
				this.done.await(time, unit);
				break;
			} catch (InterruptedException x) {
				// wait some more
			}
		}
		return sizerResult;
	}

	public void addResult(DistributedMember arg0, SizerResult arg1) {
		List<SizerResult> results = new ArrayList<SizerResult>();
		results.add(arg1);
		sizerResult.addMemberResults(results);
	}

}

package beast.util;

public class MyLogAnalyser extends LogAnalyser {

	public MyLogAnalyser(String logfile) throws Exception {
		super(logfile, 0 /* burnin */);
	}

	// suppress stats calculation
	void calcStats() {};
}

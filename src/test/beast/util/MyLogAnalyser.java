package test.beast.util;

import beast.util.LogAnalyser;

public class MyLogAnalyser extends LogAnalyser {

	public MyLogAnalyser(String logfile) throws Exception {
		super(logfile, 0 /* burnin */);
	}

	// suppress stats calculation
	public void calcStats() {};
}

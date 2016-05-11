package test.beast.evolution.operators;

import java.io.PrintStream;

import org.junit.Test;

import beast.core.BEASTObject;
import beast.core.Loggable;
import beast.core.Logger;
import beast.core.MCMC;
import beast.core.State;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.core.util.CompoundDistribution;
import beast.evolution.operators.BMTMergeSplitOperator;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import test.beast.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;
import beast.math.distributions.ModelSetPrior;
import beast.math.distributions.NucleotideRevJumpSubstModelRatePrior;
import beast.util.LogAnalyser;
import beast.util.Randomizer;
import junit.framework.TestCase;

public class MergeSplitOperatorTest extends TestCase {

	@Test
	public void testRatesMergeSplitOperatorOnJC69() throws Exception {
        Randomizer.setSeed(123);
		IntegerParameter modelIndicator = new IntegerParameter("0");
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testRatesMergeSplitOperator(modelIndicator, rates);
	}
	
	@Test
	public void testRatesMergeSplitOperatorOnHKY() throws Exception {
		IntegerParameter modelIndicator = new IntegerParameter("55");
        RealParameter rates = new RealParameter("0.5 2.0 0.0 0.0 0.0 0.0");
        testRatesMergeSplitOperator(modelIndicator, rates);
	}
	
	@Test
	public void testRatesMergeSplitOperatorOnGTR() throws Exception {
		IntegerParameter modelIndicator = new IntegerParameter("30");
        RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
        testRatesMergeSplitOperator(modelIndicator, rates);
	}

	private void testRatesMergeSplitOperator(IntegerParameter modelIndicator, RealParameter rates) throws Exception {
        NucleotideRevJumpSubstModel substModel = NucleotideRevJumpSubstModelTest.getSubstModel();
        substModel.initByName("modelSet", "allreversible");
        rates.setUpper(Double.POSITIVE_INFINITY);
        
		State state = new State();
		state.initByName("stateNode", rates, "stateNode", modelIndicator);
		state.initialise();
		
        BMTMergeSplitOperator operator = new BMTMergeSplitOperator();
        operator.initByName("rates", rates, "substModel", substModel, 
        		"modelIndicator", modelIndicator, "weight", 1.0);
        
        for (int k = 0; k < 1000000; k++) {
        	operator.proposal();
        	
			double sr = 0;
			int modelID = modelIndicator.getValue();
			int dim = (int) substModel.getGroupCount(modelID);
			for (int i = 0; i < dim; i++) {
				sr += substModel.getSubGroupCount(modelID)[i] * rates.getArrayValue(i);
			}
			if (Math.abs(sr - 6.0) > 1e-10) {
				int h = 0; h++;
			}
			assertEquals(6.0, sr, 1e-10);
        }
	}

	final static String LOGFILE = "/tmp/MergeSplitOperatorTest";
	final static String MODEL_ID = "modelID";
	final static int CHAINLENGTH = 1000000;

	
	@Test
	public void testModelSetMergeSplitOperatorOnAllReversible() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("30");
        RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
        testModelSetMergeSplitOperator(modelIndicator, rates, "allreversible", LOGFILE + "1.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnTTSplit() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("0");
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testModelSetMergeSplitOperator(modelIndicator, rates, "transitionTransversionSplit", LOGFILE + "2.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnNamedExtended() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("0");
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testModelSetMergeSplitOperator(modelIndicator, rates, "namedExtended", LOGFILE + "3.log");
	}

//	@Test
//	public void testModelSetMergeSplitOperatorOnNamedSimple() throws Exception {
//		Randomizer.setSeed(127);
//		IntegerParameter modelIndicator = new IntegerParameter("0");
//        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
//        testModelSetMergeSplitOperator(modelIndicator, rates, "namedSimple", LOGFILE + "4.log");
//	}
	
	
	private void testModelSetMergeSplitOperator(IntegerParameter modelIndicator, RealParameter rates, String modelSet, String logFile) throws Exception {
		modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);
        rates.setUpper(Double.POSITIVE_INFINITY);
        
		State state = new State();
		state.initByName("stateNode", rates, "stateNode", modelIndicator);
		state.initialise();
		
        BMTMergeSplitOperator operator = new BMTMergeSplitOperator();
        operator.initByName("rates", rates, "substModel", substModel, 
        		"modelIndicator", modelIndicator, "weight", 1.0);
        
        NucleotideRevJumpSubstModelRatePrior prior1 = new NucleotideRevJumpSubstModelRatePrior();
        prior1.initByName("x", rates, "modelIndicator", modelIndicator, "substModel", substModel, 
        		//"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.onTransitionsAndTraversals);
        		"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.asScaledDirichlet);

		ModelSetPrior prior2 = new ModelSetPrior();        
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnModel);
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", modelIndicator);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", CHAINLENGTH,
                "state", state,
                "distribution", prior,
                "operator", operator,
                "logger", logger
        );
        
        mcmc.run();
        
        
        int [] hist = new int[substModel.getModelCount()];
        LogAnalyser analyser = new LogAnalyser(logFile);
        
        Double [] trace = analyser.getTrace(MODEL_ID);
        for (double d : trace) {
        	hist[(int)d]++;
        }
        
        int delta = CHAINLENGTH / hist.length / 20;
        int lower = CHAINLENGTH / hist.length - delta;
        int upper = CHAINLENGTH / hist.length + delta;
        int failCount = 0;
        for (int i = 0; i < hist.length; i++) {
        	if (lower > hist[i] || hist[i] > upper) {
        		System.err.println(i + ": expected " + lower +"<"+ hist[i] + "<" + upper);
        		failCount ++;
        	}
       }

        // at most 10% exceeding -5% or +5% deviation from mean.
       assertTrue(failCount <= hist.length / 10);
	}


	@Test
	public void testAltModelSetMergeSplitOperator() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("30");
        RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
        String modelSet = "allreversible";
        String logFile = LOGFILE + "1alt.log";
        
        modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);
        rates.setUpper(Double.POSITIVE_INFINITY);
        
		State state = new State();
		state.initByName("stateNode", rates, "stateNode", modelIndicator);
		state.initialise();
		
        BMTMergeSplitOperator operator = new BMTMergeSplitOperator();
        operator.initByName("rates", rates, "substModel", substModel, 
        		"modelIndicator", modelIndicator, "weight", 1.0);
        operator.useAlt = true;
        
        NucleotideRevJumpSubstModelRatePrior prior1 = new NucleotideRevJumpSubstModelRatePrior();
        prior1.initByName("x", rates, "modelIndicator", modelIndicator, "substModel", substModel, 
        		//"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.onTransitionsAndTraversals);
        		"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.asScaledDirichlet);

		ModelSetPrior prior2 = new ModelSetPrior();        
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnModel);
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", modelIndicator);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", CHAINLENGTH,
                "state", state,
                "distribution", prior,
                "operator", operator,
                "logger", logger
        );
        
        mcmc.run();
        
        
        int [] hist = new int[substModel.getModelCount()];
        LogAnalyser analyser = new LogAnalyser(logFile);
        
        Double [] trace = analyser.getTrace(MODEL_ID);
        for (double d : trace) {
        	hist[(int)d]++;
        }
        
        int delta = CHAINLENGTH / hist.length / 20;
        int lower = CHAINLENGTH / hist.length - delta;
        int upper = CHAINLENGTH / hist.length + delta;
        int failCount = 0;
        for (int i = 0; i < hist.length; i++) {
        	if (lower > hist[i] || hist[i] > upper) {
        		System.err.println(i + ": expected " + lower +"<"+ hist[i] + "<" + upper);
        		failCount ++;
        	}
       }

        // at most 10% exceeding -5% or +5% deviation from mean.
       assertTrue(failCount <= hist.length / 10);
	}
	
	@Test
	public void testModelSetMergeSplitOperatorOnAllReversibleWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("30");
        RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "allreversible", LOGFILE + "1b.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnTTSplitWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("0");
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "transitionTransversionSplit", LOGFILE + "2b.log");
	}
	
	@Test
	public void testModelSetMergeSplitOperatorOnNamedExtendedWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntegerParameter modelIndicator = new IntegerParameter("0");
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "namedExtended", LOGFILE + "3b.log");
	}

	class GroupCountLogger extends BEASTObject implements Loggable {
		NucleotideRevJumpSubstModel substModel;
		IntegerParameter modelIndicator;
		
		GroupCountLogger(NucleotideRevJumpSubstModel substModel, IntegerParameter modelIndicator) {
			this.substModel = substModel;
			this.modelIndicator  = modelIndicator;
		}
		
		@Override
		public void initAndValidate() {
		}

		@Override
		public void init(PrintStream out) {
			out.append("paramcount\t");
		}

		@Override
		public void log(int nSample, PrintStream out) {
			int groupCount = substModel.getGroupCount(modelIndicator.getValue());
			out.append(groupCount + "\t");
		}

		@Override
		public void close(PrintStream out) {
		}
		
	}
	
	private void testModelSetMergeSplitOperatorWithUniformOnParameterCount(IntegerParameter modelIndicator, RealParameter rates, String modelSet, String logFile) throws Exception {
		modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);
        rates.setUpper(Double.POSITIVE_INFINITY);
        
		State state = new State();
		state.initByName("stateNode", rates, "stateNode", modelIndicator);
		state.initialise();
		
        BMTMergeSplitOperator operator = new BMTMergeSplitOperator();
        operator.initByName("rates", rates, "substModel", substModel, 
        		"modelIndicator", modelIndicator, "weight", 1.0);
        
        NucleotideRevJumpSubstModelRatePrior prior1 = new NucleotideRevJumpSubstModelRatePrior();
        prior1.initByName("x", rates, "modelIndicator", modelIndicator, "substModel", substModel, 
        		//"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.onTransitionsAndTraversals);
        		"priorType", NucleotideRevJumpSubstModelRatePrior.BMTPriorType.asScaledDirichlet);

		ModelSetPrior prior2 = new ModelSetPrior();        
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnParameterCount);
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        GroupCountLogger gcLogger = new GroupCountLogger(substModel, modelIndicator);
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", gcLogger);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", CHAINLENGTH,
                "state", state,
                "distribution", prior,
                "operator", operator,
                "logger", logger
        );
        
        mcmc.run();
        
        
        double [] hist = new double[6];
        LogAnalyser analyser = new LogAnalyser(logFile);
        
        Double [] trace = analyser.getTrace("paramcount");
        for (double d : trace) {
        	hist[(int)d - 1]++;
        }

        
        double chisquare = 0;
        int n = CHAINLENGTH;
        for (int i = 0; i < 6; i++) {
        	chisquare +=  n * (hist[i]/n - 1.0/6.0) * (hist[i]/n - 1.0/6.0)/ (1.0/6.0);
        }
        // TODO: find proper value for cut-off of Chi Square test
        assertTrue(chisquare <= 100);
	}
}

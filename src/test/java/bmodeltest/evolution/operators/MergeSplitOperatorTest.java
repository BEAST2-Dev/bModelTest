package bmodeltest.evolution.operators;

import java.io.PrintStream;

import org.junit.Test;

import beast.base.core.BEASTObject;
import beast.base.core.Loggable;
import beast.base.inference.Logger;
import beast.base.inference.MCMC;
import beast.base.inference.State;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.inference.CompoundDistribution;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;
import beast.base.inference.distribution.Uniform;
import beastfx.app.tools.LogAnalyser;
import bmodeltest.evolution.operators.BMTMergeSplitOperator;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import bmodeltest.math.distributions.ModelSetPrior;
import bmodeltest.math.distributions.NucleotideRevJumpSubstModelRatePrior;
import beast.base.util.Randomizer;
import junit.framework.TestCase;

public class MergeSplitOperatorTest extends TestCase {

	@Test
	public void testRatesMergeSplitOperatorOnJC69() throws Exception {
        Randomizer.setSeed(123);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testRatesMergeSplitOperator(modelIndicator, rates);
	}
	
	@Test
	public void testRatesMergeSplitOperatorOnHKY() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(55, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("0.5 2.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testRatesMergeSplitOperator(modelIndicator, rates);
	}
	
	@Test
	public void testRatesMergeSplitOperatorOnGTR() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(30, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 1.0 1.0 1.0 1.0 1.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testRatesMergeSplitOperator(modelIndicator, rates);
	}

	private void testRatesMergeSplitOperator(IntScalarParam<NonNegativeInt> modelIndicator, RealVectorParam<NonNegativeReal> rates) throws Exception {
        NucleotideRevJumpSubstModel substModel = NucleotideRevJumpSubstModelTest.getSubstModel();
        substModel.initByName("modelSet", "allreversible");

        
		State state = new State();
		state.initByName("stateNode", rates, "stateNode", modelIndicator);
		state.initialise();
		
        BMTMergeSplitOperator operator = new BMTMergeSplitOperator();
        operator.initByName("rates", rates, "substModel", substModel, 
        		"modelIndicator", modelIndicator, "weight", 1.0);
        
        for (int k = 0; k < 1000000; k++) {
        	operator.proposal();
        	
			double sr = 0;
			int modelID = modelIndicator.get();
			int dim = (int) substModel.getGroupCount(modelID);
			for (int i = 0; i < dim; i++) {
				sr += substModel.getSubGroupCount(modelID)[i] * rates.get(i);
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
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(30, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 1.0 1.0 1.0 1.0 1.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperator(modelIndicator, rates, "allreversible", LOGFILE + "1.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnTTSplit() throws Exception {
		Randomizer.setSeed(127);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperator(modelIndicator, rates, "transitionTransversionSplit", LOGFILE + "2.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnNamedExtended() throws Exception {
		Randomizer.setSeed(127);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperator(modelIndicator, rates, "namedExtended", LOGFILE + "3.log");
	}

//	@Test
//	public void testModelSetMergeSplitOperatorOnNamedSimple() throws Exception {
//		Randomizer.setSeed(127);
//		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
//      double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
//      RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
//      testModelSetMergeSplitOperator(modelIndicator, rates, "namedSimple", LOGFILE + "4.log");
//	}
	
	
	private void testModelSetMergeSplitOperator(IntScalarParam<NonNegativeInt> modelIndicator, RealVectorParam<NonNegativeReal> rates, String modelSet, String logFile) throws Exception {
		modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);

        
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
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnModel, 
				"distr", new Uniform());
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", modelIndicator);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", (long) (CHAINLENGTH * 100/90),
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
		// fails with seed=127, passes with 128, 129
		boolean [] pass = new boolean[20];
		for (int k = 0; k < 20; k++) {
		Randomizer.setSeed(131+k);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(30, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 1.0 1.0 1.0 1.0 1.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        String modelSet = "allreversible";
        String logFile = LOGFILE + "1alt.log";
        
        modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);

        
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
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnModel, 
				"distr", new Uniform());
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", modelIndicator);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", (long) (CHAINLENGTH * 100/90),
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
        pass[k] = failCount <= hist.length / 10;
        System.out.println(k + ": " + java.util.Arrays.toString(pass));
	   }
        // at most 10% exceeding -5% or +5% deviation from mean.
       // assertTrue(failCount <= hist.length / 10);
	}
	
	@Test
	public void testModelSetMergeSplitOperatorOnAllReversibleWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(30, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 1.0 1.0 1.0 1.0 1.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "allreversible", LOGFILE + "1b.log");
	}

	@Test
	public void testModelSetMergeSplitOperatorOnTTSplitWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "transitionTransversionSplit", LOGFILE + "2b.log");
	}
	
	@Test
	public void testModelSetMergeSplitOperatorOnNamedExtendedWithUniformOnParameterCount() throws Exception {
		Randomizer.setSeed(127);
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        double[] _r = java.util.Arrays.stream("1.0 0.0 0.0 0.0 0.0 0.0".split(" ")).mapToDouble(Double::parseDouble).toArray();
        RealVectorParam<NonNegativeReal> rates = new RealVectorParam<>(_r, NonNegativeReal.INSTANCE);
        testModelSetMergeSplitOperatorWithUniformOnParameterCount(modelIndicator, rates, "namedExtended", LOGFILE + "3b.log");
	}

	class GroupCountLogger extends BEASTObject implements Loggable {
		NucleotideRevJumpSubstModel substModel;
		IntScalarParam<NonNegativeInt> modelIndicator;
		
		GroupCountLogger(NucleotideRevJumpSubstModel substModel, IntScalarParam<NonNegativeInt> modelIndicator) {
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
		public void log(long nSample, PrintStream out) {
			int groupCount = substModel.getGroupCount(modelIndicator.get());
			out.append(groupCount + "\t");
		}

		@Override
		public void close(PrintStream out) {
		}
		
	}
	
	private void testModelSetMergeSplitOperatorWithUniformOnParameterCount(IntScalarParam<NonNegativeInt> modelIndicator, RealVectorParam<NonNegativeReal> rates, String modelSet, String logFile) throws Exception {
		modelIndicator.setID(MODEL_ID);
        NucleotideRevJumpSubstModel substModel = new NucleotideRevJumpSubstModel();
    	substModel.initByName("rates", rates, "modelIndicator", modelIndicator, "frequencies",
    					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", modelSet);

        
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
		prior2.initByName("x", modelIndicator, "substModel", substModel, "priorType", ModelSetPrior.PriorType.uniformOnParameterCount,
				"distr", new Uniform());
		CompoundDistribution prior = new CompoundDistribution();
		
		prior.initByName("distribution", prior1, "distribution", prior2);
		
        
        Logger logger = new Logger();
        GroupCountLogger gcLogger = new GroupCountLogger(substModel, modelIndicator);
        Logger.FILE_MODE = Logger.FILE_MODE.overwrite; 
        logger.initByName("logEvery", 1, "fileName", logFile, "log", gcLogger);//, "log", prior);
        
        MCMC mcmc = new MCMC();
        mcmc.initByName(
                "chainLength", (long) (CHAINLENGTH * 100 / 90),
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

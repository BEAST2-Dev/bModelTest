package bmodeltest.evolution.operators;


import org.junit.Test;

import beast.base.inference.State;
import beast.base.inference.parameter.RealParameter;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.IntScalarParam;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;
import beast.base.util.Randomizer;
import bmodeltest.evolution.operators.BMTExchangeOperator;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import junit.framework.TestCase;

public class ExchangeOperatorTest extends TestCase {

	@Test
	public void testExchangeOperatorOnJC69() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
        RealParameter rates = new RealParameter("1.0 0.0 0.0 0.0 0.0 0.0");
        testExchangeOperator(modelIndicator, rates, 6.0);
	}

	@Test
	public void testExchangeOperatorOnHKY() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(1, NonNegativeInt.INSTANCE);
        RealParameter rates = new RealParameter("0.5 2.0 0.0 0.0 0.0 0.0");
        testExchangeOperator(modelIndicator, rates, 6.0);
	}

	@Test
	public void testExchangeOperatorOnGTR() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(30, NonNegativeInt.INSTANCE);
        RealParameter rates = new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0");
        testExchangeOperator(modelIndicator, rates, 6.0);
	}

	private void testExchangeOperator(IntScalarParam<NonNegativeInt> modelIndicator, RealParameter rates, double sumOfRates) throws Exception {
        NucleotideRevJumpSubstModel substModel = NucleotideRevJumpSubstModelTest.getSubstModel();
        rates.setUpper(Double.POSITIVE_INFINITY);

		State state = new State();
		state.initByName("stateNode", rates);
		state.initialise();

        BMTExchangeOperator operator = new BMTExchangeOperator();
        operator.initByName("rates", rates, "substModel", substModel,
        		"modelIndicator", modelIndicator, "weight", 1.0);

        Randomizer.setSeed(123);
        for (int k = 0; k < 1000000; k++) {
        	operator.proposal();

			double sr = 0;
			int modelID = modelIndicator.get();
			int dim = (int) substModel.getGroupCount(modelID);
			for (int i = 0; i < dim; i++) {
				sr += substModel.getSubGroupCount(modelID)[i] * rates.getArrayValue(i);
			}
			assertEquals(6.0, sr, 1e-10);
        }
	}

}

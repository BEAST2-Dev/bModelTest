package bmodeltest.math.distributions;


import org.junit.Test;

import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import bmodeltest.math.distributions.ModelSetPrior;
import beast.base.inference.distribution.Uniform;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.IntScalarParam;
import junit.framework.TestCase;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;

public class ModelSetPriorTest extends TestCase {

	@Test
	public void testUniformModelSetPrior() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel(modelIndicator);
		ModelSetPrior prior = new ModelSetPrior();
		prior.initByName("x", modelIndicator,
				"substModel", sm, "distr", new Uniform());

		for (int i = 0; i < sm.getModelCount(); i++) {
			modelIndicator.set(i);
			assertEquals(-Math.log(31), prior.calculateLogP(), 1e-13);
		}
	}

	@Test
	public void testUniformParameterCountPrior() throws Exception {
		IntScalarParam<NonNegativeInt> modelIndicator = new IntScalarParam<>(0, NonNegativeInt.INSTANCE);
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel(modelIndicator);
		ModelSetPrior prior = new ModelSetPrior();
		prior.initByName("x", modelIndicator,
				"substModel", sm,
				"priorType", "uniformOnParameterCount",
				"distr", new Uniform());

		// JC69
		modelIndicator.set(0);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);

		// HKY
		modelIndicator.set(1);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);

		// 3 parameter TN93, etc
		modelIndicator.set(2);
		assertEquals(Math.log(1.0/6.0/8.0), prior.calculateLogP(), 1e-13);

		// 4 parameter
		modelIndicator.set(15);
		assertEquals(Math.log(1.0/6.0/13.0), prior.calculateLogP(), 1e-13);

		// 5 parameter
		modelIndicator.set(29);
		assertEquals(Math.log(1.0/6.0/7.0), prior.calculateLogP(), 1e-13);

		// GTR
		modelIndicator.set(30);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);
	}

}

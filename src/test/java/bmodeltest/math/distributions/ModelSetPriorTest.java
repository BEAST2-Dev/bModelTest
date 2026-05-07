package bmodeltest.math.distributions;


import org.junit.Test;

import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import bmodeltest.math.distributions.ModelSetPrior;
import beast.base.inference.distribution.Uniform;
import junit.framework.TestCase;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;

public class ModelSetPriorTest extends TestCase {
	
	@Test
	public void testUniformModelSetPrior() throws Exception {
		ModelSetPrior prior = new ModelSetPrior();
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel();
		prior.initByName("x", sm.modelIndicatorInput.get(), 
				"substModel", sm, "distr", new Uniform());
		
		for (int i = 0; i < sm.getModelCount(); i++) {
			sm.modelIndicatorInput.get().set(i);
			assertEquals(-Math.log(31), prior.calculateLogP(), 1e-13);
		}
	}

	@Test
	public void testUniformParameterCountPrior() throws Exception {
		ModelSetPrior prior = new ModelSetPrior();
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel();
		prior.initByName("x", sm.modelIndicatorInput.get(), 
				"substModel", sm,
				"priorType", "uniformOnParameterCount",
				"distr", new Uniform());
		
		// JC69
		sm.modelIndicatorInput.get().set(0);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);
			
		// HKY
		sm.modelIndicatorInput.get().set(1);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);

		// 3 parameter TN93, etc
		sm.modelIndicatorInput.get().set(2);
		assertEquals(Math.log(1.0/6.0/8.0), prior.calculateLogP(), 1e-13);

		// 4 parameter
		sm.modelIndicatorInput.get().set(15);
		assertEquals(Math.log(1.0/6.0/13.0), prior.calculateLogP(), 1e-13);

		// 5 parameter
		sm.modelIndicatorInput.get().set(29);
		assertEquals(Math.log(1.0/6.0/7.0), prior.calculateLogP(), 1e-13);

		// GTR
		sm.modelIndicatorInput.get().set(30);
		assertEquals(Math.log(1.0/6.0), prior.calculateLogP(), 1e-13);
	}

}

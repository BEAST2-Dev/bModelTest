package bmodeltest.math.distributions;

import org.junit.Test;

import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.inference.distribution.Exponential;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;
import junit.framework.TestCase;

public class BMTPriorTest extends TestCase {

	@Test
	public void testActivePrior() throws Exception {
		BMTPrior prior = new BMTPrior();
		Exponential exp = new Exponential();
		exp.initByName("mean", new RealScalarParam<>(1.0, PositiveReal.INSTANCE));
		prior.initByName(
				"x", new RealScalarParam<>(0.5, PositiveReal.INSTANCE),
				"count", new IntScalarParam<>(1, NonNegativeInt.INSTANCE),
				"distr", exp);
		// Exp(1) density at 0.5 = exp(-0.5); log = -0.5
		assertEquals(-0.5, prior.calculateLogP(), 1e-12);
	}

	@Test
	public void testInactivePrior() throws Exception {
		BMTPrior prior = new BMTPrior();
		Exponential exp = new Exponential();
		exp.initByName("mean", new RealScalarParam<>(1.0, PositiveReal.INSTANCE));
		prior.initByName(
				"x", new RealScalarParam<>(0.5, PositiveReal.INSTANCE),
				"count", new IntScalarParam<>(0, NonNegativeInt.INSTANCE),
				"distr", exp);
		assertEquals(0.0, prior.calculateLogP(), 1e-12);
	}

}

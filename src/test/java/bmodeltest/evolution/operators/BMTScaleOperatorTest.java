package bmodeltest.evolution.operators;

import org.junit.Test;

import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.util.Randomizer;
import junit.framework.TestCase;

public class BMTScaleOperatorTest extends TestCase {

	@Test
	public void testInactiveProposalRejected() throws Exception {
		RealScalarParam<PositiveReal> param = new RealScalarParam<>(1.0, PositiveReal.INSTANCE);
		BMTScaleOperator op = new BMTScaleOperator();
		op.initByName(
				"parameter", param,
				"count", new IntScalarParam<>(0, NonNegativeInt.INSTANCE),
				"scaleFactor", 0.5,
				"weight", 1.0);
		double hr = op.proposal();
		assertEquals(Double.NEGATIVE_INFINITY, hr);
		assertEquals(1.0, param.get(), 1e-12);
	}

	@Test
	public void testActiveProposalScales() throws Exception {
		Randomizer.setSeed(42);
		RealScalarParam<PositiveReal> param = new RealScalarParam<>(1.0, PositiveReal.INSTANCE);
		BMTScaleOperator op = new BMTScaleOperator();
		op.initByName(
				"parameter", param,
				"count", new IntScalarParam<>(1, NonNegativeInt.INSTANCE),
				"scaleFactor", 0.5,
				"weight", 1.0);
		double hr = op.proposal();
		assertTrue("expected finite hastings ratio, got " + hr, Double.isFinite(hr));
		assertTrue("parameter should have changed", param.get() != 1.0);
	}

}

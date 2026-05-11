package bmodeltest.evolution.sitemodel;

import org.junit.Test;

import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.PositiveReal;
import beast.base.spec.domain.UnitInterval;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModelTest;
import junit.framework.TestCase;

public class BEASTModelTestSiteModelTest extends TestCase {

	private BEASTModelTestSiteModel build(int hasGamma, int hasInvar) throws Exception {
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel();
		BEASTModelTestSiteModel siteModel = new BEASTModelTestSiteModel();
		siteModel.initByName(
				"substModel", sm,
				"gammaCategoryCount", 4,
				"shape", new RealScalarParam<>(1.0, PositiveReal.INSTANCE),
				"proportionInvariant", new RealScalarParam<>(0.1, UnitInterval.INSTANCE),
				"hasGammaRates", new IntScalarParam<>(hasGamma, NonNegativeInt.INSTANCE),
				"hasInvariantSites", new IntScalarParam<>(hasInvar, NonNegativeInt.INSTANCE));
		return siteModel;
	}

	@Test
	public void testInitWithGammaAndInvariant() throws Exception {
		BEASTModelTestSiteModel siteModel = build(1, 1);
		double[] rates = siteModel.getCategoryRates(null);
		double[] props = siteModel.getCategoryProportions(null);
		assertEquals(rates.length, props.length);
		double sum = 0;
		double mean = 0;
		for (int i = 0; i < rates.length; i++) {
			sum += props[i];
			mean += props[i] * rates[i];
		}
		assertEquals(1.0, sum, 1e-10);
		assertEquals(1.0, mean, 1e-10);
		assertEquals(0.1, siteModel.getProportionInvariant(), 1e-12);
	}

	@Test
	public void testGammaOffInvariantOn() throws Exception {
		BEASTModelTestSiteModel siteModel = build(0, 1);
		assertEquals(0.1, siteModel.getProportionInvariant(), 1e-12);
	}

	@Test
	public void testGammaOnInvariantOff() throws Exception {
		BEASTModelTestSiteModel siteModel = build(1, 0);
		// invariant flag off so reported proportion invariant must be 0 even though param value is 0.1
		assertEquals(0.0, siteModel.getProportionInvariant(), 1e-12);
	}

	@Test
	public void testNeitherGammaNorInvariant() throws Exception {
		BEASTModelTestSiteModel siteModel = build(0, 0);
		assertEquals(0.0, siteModel.getProportionInvariant(), 1e-12);
	}

	@Test
	public void testShapeRequired() throws Exception {
		NucleotideRevJumpSubstModel sm = NucleotideRevJumpSubstModelTest.getSubstModel();
		BEASTModelTestSiteModel siteModel = new BEASTModelTestSiteModel();
		try {
			siteModel.initByName(
					"substModel", sm,
					"gammaCategoryCount", 4,
					"proportionInvariant", new RealScalarParam<>(0.1, UnitInterval.INSTANCE),
					"hasGammaRates", new IntScalarParam<>(1, NonNegativeInt.INSTANCE),
					"hasInvariantSites", new IntScalarParam<>(1, NonNegativeInt.INSTANCE));
			fail("expected exception for missing shape parameter");
		} catch (Exception expected) {
			assertTrue("expected message to mention shape, got: " + expected.getMessage(),
					expected.getMessage() != null && expected.getMessage().contains("shape"));
		}
	}

}

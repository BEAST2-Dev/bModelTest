package bmodeltest.evolution.substitutionmodel;

import org.junit.Test;

import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.evolution.substitutionmodel.Frequencies;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.spec.inference.parameter.SimplexParam;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import junit.framework.TestCase;

public class NucleotideRevJumpSubstModelTest extends TestCase {

	public static Frequencies getFreqs() {
		try {
			SimplexParam freqs = new SimplexParam(new double[]{0.25, 0.25, 0.25, 0.25});
			Frequencies frequencies = new Frequencies();
			frequencies.initByName("frequencies", freqs);
			return frequencies;
		} catch (Exception e) {
		}
		return null;
	}

	private static RealVectorParam<NonNegativeReal> rates6() {
		return new RealVectorParam<>(new double[]{1.0, 1.0, 1.0, 1.0, 1.0, 1.0}, NonNegativeReal.INSTANCE);
	}

	public static NucleotideRevJumpSubstModel getSubstModel() {
		return getSubstModel(new IntScalarParam<>(0, NonNegativeInt.INSTANCE));
	}

	public static NucleotideRevJumpSubstModel getSubstModel(IntScalarParam<NonNegativeInt> modelIndicator) {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		try {
			sm.initByName("rates", rates6(), "modelIndicator", modelIndicator, "frequencies",
					NucleotideRevJumpSubstModelTest.getFreqs(), "modelSet", "transitionTransversionSplit");
			return sm;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	@Test
	public void testSubstModel() throws Exception {

		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", rates6(), "modelIndicator", new IntScalarParam<>(0, NonNegativeInt.INSTANCE), "frequencies", getFreqs(),
				"modelSet", "transitionTransversionSplit");
		assertEquals(false, sm.isSplit(25, 11));
		assertEquals(true, sm.isSplit(11, 25));

		String dotty = sm.toDotty();
		System.out.println(dotty);
		for (int i = 0; i < 31; i++) {
			int [] model = sm.getModel(i);
			System.out.print("$m[" + i + "] = ");
			for (int d : model) {
				System.out.print(d+1);
			}
			System.out.println(";");
		}

	}

	@Test
	public void testAllReversibleModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", rates6(), "modelIndicator", new IntScalarParam<>(0, NonNegativeInt.INSTANCE), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.allreversible);
		assertEquals(203, sm.getModelCount());

	}

	@Test
	public void testTransitionTransversionModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", rates6(), "modelIndicator", new IntScalarParam<>(0, NonNegativeInt.INSTANCE), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.transitionTransversionSplit);
		assertEquals(31, sm.getModelCount());

	}

	@Test
	public void testNamedExtendedModel2() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", rates6(), "modelIndicator", new IntScalarParam<>(0, NonNegativeInt.INSTANCE), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.namedExtended);
		String dotty = sm.toDotty();
		System.out.println(dotty);
		assertEquals(9, sm.getModelCount());
	}

	@Test
	public void testNamedModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", rates6(), "modelIndicator", new IntScalarParam<>(0, NonNegativeInt.INSTANCE), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.namedSimple);
		assertEquals(4, sm.getModelCount());

	}
}

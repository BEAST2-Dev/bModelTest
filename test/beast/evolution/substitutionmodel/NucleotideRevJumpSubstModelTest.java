package beast.evolution.substitutionmodel;

import org.junit.Test;

import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import junit.framework.TestCase;

public class NucleotideRevJumpSubstModelTest extends TestCase {

	public static Frequencies getFreqs() {
		try {
			Frequencies frequencies = new Frequencies();
			frequencies.initByName("frequencies", "0.25 0.25 0.25 0.25");
			return frequencies;
		} catch (Exception e) {
		}
		return null;
	}

	public static NucleotideRevJumpSubstModel getSubstModel() {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		try {
			sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies",
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
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", getFreqs(),
				"modelSet", "transitionTransversionSplit");
		assertEquals(false, sm.isSplit(25, 11));
		assertEquals(true, sm.isSplit(11, 25));

		String dotty = sm.toDotty();
		System.out.println(dotty);

	}

	@Test
	public void testAllReversibleModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.allreversible);
		assertEquals(203, sm.getModelCount());

	}

	@Test
	public void testTransitionTransversionModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.transitionTransversionSplit);
		assertEquals(31, sm.getModelCount());

	}

	@Test
	public void testNamedExtendedModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.namedExtended);
		assertEquals(7, sm.getModelCount());

	}

	@Test
	public void testNamedModel() throws Exception {
		NucleotideRevJumpSubstModel sm = new NucleotideRevJumpSubstModel();
		sm.initByName("rates", new RealParameter("1.0 1.0 1.0 1.0 1.0 1.0"), "modelIndicator", new IntegerParameter("0"), "frequencies", getFreqs(),
				"modelSet", NucleotideRevJumpSubstModel.ModelSet.namedSimple);
		assertEquals(4, sm.getModelCount());

	}
}

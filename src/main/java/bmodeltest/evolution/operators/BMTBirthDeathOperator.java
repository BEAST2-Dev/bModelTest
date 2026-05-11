package bmodeltest.evolution.operators;



import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealScalarParam;
import beast.base.inference.distribution.Exponential;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.util.Randomizer;
import bmodeltest.math.distributions.BMTPrior;

@Description("Operator for bModelTest to jump between presence/absence of gamma rate heterogeneity and/or invariant sites")
public class BMTBirthDeathOperator extends Operator {
	public Input<RealScalarParam<? extends NonNegativeReal>> rateInput = new Input<>("rates","scalar parameter switched on (count=1) or off (count=0)", Validate.REQUIRED);
	public Input<IntScalarParam<? extends NonNegativeInt>> countInput = new Input<>("count","count parameter; 0 means rate is inactive, 1 means active", Validate.REQUIRED);

	private RealScalarParam<? extends NonNegativeReal> rate;
	private IntScalarParam<? extends NonNegativeInt> counts;
	ParametricDistribution distr;

	@Override
	public void initAndValidate() {
		rate = rateInput.get();
		distr = new Exponential();
		for (Object plugin : rate.getOutputs()) {
			if (plugin instanceof BMTPrior) {
				BMTPrior prior = (BMTPrior) plugin;
				distr = prior.distInput.get();
			}
		}
		counts = countInput.get();
	}

	@Override
	public double proposal() {
		int count = counts.get();

		if (Randomizer.nextBoolean()) {
			// activate: count 0 -> 1
			if (count == 1) {
				return Double.NEGATIVE_INFINITY;
			}
			double p = Randomizer.nextDouble();
			double scale = distr.inverseCumulativeProbability(p);
			if (!rate.isValid(scale)) {
				return Double.NEGATIVE_INFINITY;
			}
			rate.set(scale);
			counts.set(1);
			return -distr.logDensity(scale);
		} else {
			// deactivate: count 1 -> 0
			if (count == 0) {
				return Double.NEGATIVE_INFINITY;
			}
			double scale = rate.get();
			counts.set(0);
			return distr.logDensity(scale);
		}
	}

}

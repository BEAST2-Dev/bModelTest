package bmodeltest.evolution.operators;



import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.inference.distribution.Exponential;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.util.Randomizer;
import bmodeltest.math.distributions.BMTPrior;

@Description("Operator for bModelTest to jump between presence/absence of gamma rate heterogeneity and/or invariant sites")
public class BMTBirthDeathOperator extends Operator {
	public Input<RealVectorParam<? extends NonNegativeReal>> rateInput = new Input<>("rates","rate parameter containing rates for hierarchical subst model", Validate.REQUIRED);
	public Input<IntScalarParam<? extends NonNegativeInt>> countInput = new Input<>("count","count parameter indicating the nr of rates to use", Validate.REQUIRED);

	private RealVectorParam<? extends NonNegativeReal> rates;
	private IntScalarParam<? extends NonNegativeInt> counts;
//	private Double scaleFactor;
	//final static int [] countmap = {-1, -1, -1, 0, 2, 0};
	//final static int [] countmap = {-1, 0, 1, 2, 3, 4};
	//final static int [] countmap = {-1, 0, 0, 0, 0, 0};
	ParametricDistribution distr;

	@Override
	public void initAndValidate() {
		rates = rateInput.get();
		distr = new Exponential();
		for (Object plugin : rates.getOutputs()) {
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
		double scale = 1;// = (scaleFactor + (Randomizer.nextDouble() * ((1.0 / scaleFactor) - scaleFactor)));


		if (Randomizer.nextBoolean()) {
			// increase nr of rates
			if (count == rates.size()	) {
				// cannot increase any further
				return Double.NEGATIVE_INFINITY;
			}
			double p = Randomizer.nextDouble();
			scale = distr.inverseCumulativeProbability(p);
			double newValue = scale;
			if (!rates.isValid(newValue)) {
				return Double.NEGATIVE_INFINITY;
			}
			rates.set(count, scale);
			counts.set(count + 1);
			//double logHR = -Math.log(1/scaleFactor - scaleFactor) + Math.log(oldvalue);
			double logHR = -distr.logDensity(scale);
			return logHR;
		} else {
			// decrease nr of rates
			if (count == 0) {
				// cannot decrease any further
				return Double.NEGATIVE_INFINITY;
			}
			counts.set(count - 1);
			scale = rates.get(count-1);
			//double logHR = Math.log(1/scaleFactor - scaleFactor) - Math.log(oldvalue);
			double logHR = distr.logDensity(scale);
			return logHR;
		}
	}

}

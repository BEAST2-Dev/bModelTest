package beast.evolution.operators;


import org.apache.commons.math.MathException;

import beast.core.Description;
import beast.core.Input;
import beast.core.Operator;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.math.distributions.Exponential;
import beast.math.distributions.ParametricDistribution;
import beast.math.distributions.BMTPrior;
import beast.util.Randomizer;

@Description("Operator for bModelTest to jump between presence/absence of gamma rate heterogeneity and/or invariant sites")
public class BMTBirthDeathOperator extends Operator {
	public Input<RealParameter> rateInput = new Input<RealParameter>("rates","rate parameter containing rates for hierarchical subst model", Validate.REQUIRED);
	public Input<IntegerParameter> countInput = new Input<IntegerParameter>("count","count parameter indicating the nr of rates to use", Validate.REQUIRED);
	
	private RealParameter rates;
	private IntegerParameter counts;
//	private Double scaleFactor;
	//final static int [] countmap = {-1, -1, -1, 0, 2, 0}; 
	//final static int [] countmap = {-1, 0, 1, 2, 3, 4}; 
	//final static int [] countmap = {-1, 0, 0, 0, 0, 0}; 
	ParametricDistribution distr;
	
	@Override
	public void initAndValidate() throws Exception {
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
		int count = (int) counts.getArrayValue();
		double scale = 1;// = (scaleFactor + (Randomizer.nextDouble() * ((1.0 / scaleFactor) - scaleFactor)));

		
		if (Randomizer.nextBoolean()) {
			// increase nr of rates
			if (count == rates.getDimension()	) {
				// cannot increase any further
				return Double.NEGATIVE_INFINITY;
			}
			double p = Randomizer.nextDouble();
			try {
				scale = distr.inverseCumulativeProbability(p);
			} catch (MathException e) {
				e.printStackTrace();
			}
			double newValue = scale;
			if (newValue < rates.getLower() || newValue > rates.getUpper()) {
				return Double.NEGATIVE_INFINITY;
			}
			rates.setValue(count, scale);
			counts.setValue(count + 1);
			//double logHR = -Math.log(1/scaleFactor - scaleFactor) + Math.log(oldvalue);
			double logHR = -distr.logDensity(scale);
			return logHR;
		} else {
			// decrease nr of rates
			if (count == 0) {
				// cannot decrease any further
				return Double.NEGATIVE_INFINITY;
			}
			counts.setValue(count - 1);
			scale = rates.getValue(count-1); 
			//double logHR = Math.log(1/scaleFactor - scaleFactor) - Math.log(oldvalue);
			double logHR = distr.logDensity(scale);
			return logHR;
		}
	}

}

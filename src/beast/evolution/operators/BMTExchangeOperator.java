package beast.evolution.operators;


import java.text.DecimalFormat;

import beast.core.Description;
import beast.core.Input;
import beast.core.Operator;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.util.Randomizer;

@Description("Exchange rate values such that sum remains the same")
// adapted from DeltaExchangeOperator
public class BMTExchangeOperator extends Operator {
	public Input<IntegerParameter> modelIndicatorInput = new Input<IntegerParameter>("modelIndicator", "number of the model to be used", Validate.REQUIRED);
	public Input<NucleotideRevJumpSubstModel> substModelInput = new Input<NucleotideRevJumpSubstModel>("substModel", "model test substitution model representing the individual models", Validate.REQUIRED);
    public Input<RealParameter> ratesInput = new Input<RealParameter>("rates", "Rate parameter which defines the transition rate matrix. ", Validate.REQUIRED);
    public final Input<Double> deltaInput = new Input<Double>("delta", "Magnitude of change for two randomly picked values.", 1.0);
    public final Input<Boolean> autoOptimizeiInput =
            new Input<Boolean>("autoOptimize", "if true, window size will be adjusted during the MCMC run to improve mixing.", true);

	IntegerParameter modelIndicator;
	NucleotideRevJumpSubstModel substModel;
	RealParameter rates;
	double delta;
	boolean autoOptimize;
	
	@Override
	public void initAndValidate() {
		modelIndicator = modelIndicatorInput.get();
		substModel = substModelInput.get();
		rates = ratesInput.get();
		delta = deltaInput.get();
		autoOptimize = autoOptimizeiInput.get();
	}

	@Override
	public double proposal() {
		int currentModel = modelIndicator.getValue();
		
		int dim = substModel.getGroupCount(currentModel);
		if (dim == 1) {
			// cannot exchange anything if there is only one candidate
			return Double.NEGATIVE_INFINITY;
		}
        final int dim1 = Randomizer.nextInt(dim);
        int dim2 = dim1;
        while (dim1 == dim2) {
            dim2 = Randomizer.nextInt(dim);
        }
        int n1 = substModel.getSubGroupCount(currentModel)[dim1];
        int n2 = substModel.getSubGroupCount(currentModel)[dim2];
        
        double scalar1 = rates.getValue(dim1);
        double scalar2 = rates.getValue(dim2);
        final double d = Randomizer.nextDouble() * delta;
        
        scalar1 -= d;
        scalar2 += d * (double) n1 / (double) n2;
        if (scalar1 < rates.getLower() || scalar1 > rates.getUpper() ||
                scalar2 < rates.getLower() || scalar2 > rates.getUpper()) {
            return Double.NEGATIVE_INFINITY;
        } else {
        	rates.setValue(dim1, scalar1);
        	rates.setValue(dim2, scalar2);
        }

        return 0;
	}

    @Override
    public double getCoercableParameterValue() {
        return delta;
    }

    @Override
    public void setCoercableParameterValue(final double fValue) {
        delta = fValue;
    }


    @Override
    public void optimize(final double logAlpha) {
        // must be overridden by operator implementation to have an effect
        if (autoOptimize) {
            double fDelta = calcDelta(logAlpha);
            fDelta += Math.log(delta);
            delta = Math.exp(fDelta);
        }

    }

    @Override
    public final String getPerformanceSuggestion() {
        final double prob = m_nNrAccepted / (m_nNrAccepted + m_nNrRejected + 0.0);
        final double targetProb = getTargetAcceptanceProbability();

        double ratio = prob / targetProb;
        if (ratio > 2.0) ratio = 2.0;
        if (ratio < 0.5) ratio = 0.5;

        // new scale factor
        final double newDelta = delta * ratio;

        final DecimalFormat formatter = new DecimalFormat("#.###");
        if (prob < 0.10) {
            return "Try setting delta to about " + formatter.format(newDelta);
        } else if (prob > 0.40) {
            return "Try setting delta to about " + formatter.format(newDelta);
        } else return "";
    }

}

package bmodeltest.evolution.operators;


import java.text.DecimalFormat;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Operator;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.inference.parameter.RealVectorParam;
import beast.base.util.Randomizer;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;

@Description("Exchange rate values such that sum remains the same")
// adapted from DeltaExchangeOperator
public class BMTExchangeOperator extends Operator {
	public Input<IntScalarParam<? extends NonNegativeInt>> modelIndicatorInput = new Input<>("modelIndicator", "number of the model to be used", Validate.REQUIRED);
	public Input<NucleotideRevJumpSubstModel> substModelInput = new Input<NucleotideRevJumpSubstModel>("substModel", "model test substitution model representing the individual models", Validate.REQUIRED);
    public Input<RealVectorParam<? extends NonNegativeReal>> ratesInput = new Input<>("rates", "Rate parameter which defines the transition rate matrix. ", Validate.REQUIRED);
    public final Input<Double> deltaInput = new Input<Double>("delta", "Magnitude of change for two randomly picked values.", 1.0);
    public final Input<Boolean> autoOptimizeiInput =
            new Input<Boolean>("autoOptimize", "if true, window size will be adjusted during the MCMC run to improve mixing.", true);

	IntScalarParam<? extends NonNegativeInt> modelIndicator;
	NucleotideRevJumpSubstModel substModel;
	RealVectorParam<? extends NonNegativeReal> rates;
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
		int currentModel = modelIndicator.get();

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

        double scalar1 = rates.get(dim1);
        double scalar2 = rates.get(dim2);
        final double d = Randomizer.nextDouble() * delta;

        scalar1 -= d;
        scalar2 += d * (double) n1 / (double) n2;
        if (!rates.isValid(scalar1) || !rates.isValid(scalar2)) {
            return Double.NEGATIVE_INFINITY;
        } else {
        	rates.set(dim1, scalar1);
        	rates.set(dim2, scalar2);
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

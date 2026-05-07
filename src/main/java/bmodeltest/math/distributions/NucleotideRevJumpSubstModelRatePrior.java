package bmodeltest.math.distributions;


import java.util.Arrays;
import java.util.List;
import java.util.Random;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.inference.Distribution;
import beast.base.inference.State;
import beast.base.inference.distribution.Exponential;
import beast.base.inference.distribution.LogNormalDistributionModel;
import beast.base.inference.distribution.ParametricDistribution;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.spec.type.RealVector;
import beast.base.core.Log;
import beast.base.util.GammaFunction;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;

@Description("Prior on rates for reversible jump based substitution model, one of "
		+ "1. Dirichlet prior on rates ensuring they sum to 6. "
		+ "2. Parametric distribution on rates. "
		+ "3. Parametric distributions on rates distinguishing between transitions and transversions.")
public class NucleotideRevJumpSubstModelRatePrior extends Distribution {
	public enum BMTPriorType {asScaledDirichlet, onRates, onTransitionsAndTraversals};

	public Input<BMTPriorType> priorTypeInput = new Input<NucleotideRevJumpSubstModelRatePrior.BMTPriorType>("priorType", "rate prior, one of " + Arrays.toString(BMTPriorType.values()),
			BMTPriorType.onTransitionsAndTraversals, BMTPriorType.values());
	public Input<RealVector<? extends NonNegativeReal>> xInput = new Input<>("x", "rate vector to apply this prior to", Validate.REQUIRED);
	public Input<IntScalarParam<? extends NonNegativeInt>> modelIndicatorInput = new Input<>("modelIndicator", "number of the model to be used", Validate.REQUIRED);
	public Input<NucleotideRevJumpSubstModel> substModelInput = new Input<NucleotideRevJumpSubstModel>("substModel", "model test substitution model representing the individual models", Validate.REQUIRED);

	public Input<ParametricDistribution> distInput = new Input<>("distr", "distribution used to calculate prior on (transversion) rates");
	public Input<ParametricDistribution> transDistInput = new Input<ParametricDistribution>("transDistr", "distribution used to calculate prior on transition rates.");

	final static boolean debug = true;

	IntScalarParam<? extends NonNegativeInt> modelIndicator;
	NucleotideRevJumpSubstModel substModel;
	RealVector<? extends NonNegativeReal> rates;
	BMTPriorType priorType;
	ParametricDistribution dist;
	ParametricDistribution transDist;

	@Override
	public void initAndValidate() {
		priorType = priorTypeInput.get();
		dist = distInput.get();
		transDist = transDistInput.get();

		switch (priorType) {
		case onTransitionsAndTraversals:
			if (transDist == null) {
				Log.warning.println("Setting transitions rate prior to log-normal(1, 1.25)");
				transDist = new LogNormalDistributionModel();
				transDist.initByName("M", "1.0", "S", "1.25");
			}
			if (dist == null) {
				Log.warning.println("Setting transversion rate prior to exponential(1)");
			}
			break;
		case onRates:
			if (dist == null) {
				Log.warning.println("Setting rate prior to exponential(1)");
			}
			break;
		default:
		}
		if (dist == null) {
			dist = new Exponential();
			dist.setID(getID() + "x");
			distInput.setValue(dist, this);
		}

		modelIndicator = modelIndicatorInput.get();
		substModel = substModelInput.get();
		rates = xInput.get();
	}

	@Override
	public double calculateLogP() {
		logP = 0;
		if (debug) {
			double sr = 0;
			int modelID = modelIndicator.get();
			int dim = (int) substModel.getGroupCount(modelID);
			for (int i = 0; i < dim; i++) {
				sr += substModel.getSubGroupCount(modelID)[i] * rates.get(i);
			}
			if (Math.abs(sr - 6.0) > 1e-6) {
				throw new RuntimeException("Rates do not add to 6.00000 but " + sr);
			}
		}



		switch (priorType) {
		case asScaledDirichlet:
			{
				int modelID = modelIndicator.get();
				int K = substModel.getGroupCount(modelID);
				logP += GammaFunction.lnGamma(K);
				for (int i = 0; i < K; i++) {
					logP += Math.log(substModel.getSubGroupCount(modelID)[i]);
				}
				logP -= K * Math.log(6);
			}
			break;
		case onRates:
			{
				int modelID = modelIndicator.get();
				int dim = (int) substModel.getGroupCount(modelID);
				double fOffset = dist.offsetInput.get();
				logP = 0;
				for (int i = 0; i < dim; i++) {
					double fX = rates.get(i) - fOffset;
					logP += dist.logDensity(fX);
				}
			}
			break;
		case onTransitionsAndTraversals:
			{
				int modelID = modelIndicator.get();
				int [] model = substModel.getModel(modelID);
				int dim = (int) substModel.getGroupCount(modelID);
				logP = 0;
				for (int i = 0; i < dim; i++) {
					if (model[1] == i || model[4] == i) {
						double fOffset = transDist.offsetInput.get();
						double fX = rates.get(i) - fOffset;
						logP += transDist.logDensity(fX);
					} else {
						double fOffset = dist.offsetInput.get();
						double fX = rates.get(i) - fOffset;
						logP += dist.logDensity(fX);
					}
				}
			}
			break;
		}
		return logP;
	}

	@Override
	public List<String> getArguments() {return null;}

	@Override
	public List<String> getConditions() {return null;}

	@Override
	public void sample(State state, Random random) {}

}

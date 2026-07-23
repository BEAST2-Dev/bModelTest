package bmodeltest.math.distributions;



import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.Distribution;
import beast.base.inference.State;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.domain.Real;
import beast.base.spec.inference.distribution.ScalarDistribution;
import beast.base.spec.type.IntScalar;
import beast.base.spec.type.RealScalar;

@Description("Prior for reversible jump based parameters, applies prior only when the count parameter is non-zero")
public class BMTPrior extends Distribution {
	public Input<RealScalar<? extends Real>> xInput = new Input<>("x",
			"scalar real parameter this prior applies to", Validate.REQUIRED);
	public Input<IntScalar<? extends NonNegativeInt>> countInput = new Input<>("count",
			"count parameter; the prior contributes only when count > 0", Validate.REQUIRED);
	public Input<ScalarDistribution<RealScalar<? extends Real>, Double>> distInput = new Input<>("distr",
			"scalar distribution evaluated when the prior is active. " +
			"Wrap in OffsetReal if you need a non-zero shift; the legacy " +
			"ParametricDistribution.offset input no longer applies.", Validate.REQUIRED);

	private RealScalar<? extends Real> x;
	private IntScalar<? extends NonNegativeInt> counts;
	private ScalarDistribution<RealScalar<? extends Real>, Double> dist;

	@Override
	public void initAndValidate() {
		x = xInput.get();
		counts = countInput.get();
		dist = distInput.get();
	}

	@Override
	public double calculateLogP() {
		logP = 0;
		if (counts.get() > 0) {
			logP = dist.logDensity(x.get());
		}
		return logP;
	}

	@Override
	public List<String> getArguments() {
		List<String> arguments = new ArrayList<>();
		if (x instanceof beast.base.core.BEASTInterface b && b.getID() != null) {
			arguments.add(b.getID());
		}
		return arguments;
	}

	@Override
	public List<String> getConditions() {
		return new ArrayList<>();
	}

	@Override
	public void sample(State state, Random random) {
		// not implemented
	}
}

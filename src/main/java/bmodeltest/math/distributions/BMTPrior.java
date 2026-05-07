package bmodeltest.math.distributions;



import beast.base.core.Description;
import beast.base.core.Function;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.inference.parameter.IntScalarParam;
import beast.base.inference.distribution.Prior;

@Description("Prior for reversible jump based parameters, applies prior only to the rates that are in use")
public class BMTPrior extends Prior {
	public Input<IntScalarParam<? extends NonNegativeInt>> countInput = new Input<>("count","count parameter indicating the nr of rates to use", Validate.REQUIRED);

	private IntScalarParam<? extends NonNegativeInt> counts;

	@Override
	public void initAndValidate() {
		counts = countInput.get();
		super.initAndValidate();
	}

	@Override
	public double calculateLogP() {
		Function x = m_x.get();
		int dim = counts.get();
		double fOffset = dist.offsetInput.get();
		logP = 0;
		for (int i = 0; i < dim; i++) {
			double fX = x.getArrayValue(i) - fOffset;
			//fLogP += Math.log(density(fX));
			logP += dist.logDensity(fX);
		}
		return logP;
	}


}

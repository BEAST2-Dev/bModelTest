package beast.math.distributions;



import beast.core.Description;
import beast.core.Function;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.IntegerParameter;
import beast.math.distributions.Prior;

@Description("Prior for reversible jump based parameters, applies prior only to the rates that are in use")
public class BMTPrior extends Prior {
	public Input<IntegerParameter> countInput = new Input<IntegerParameter>("count","count parameter indicating the nr of rates to use", Validate.REQUIRED);

	private IntegerParameter counts;
	
	@Override
	public void initAndValidate() {
		counts = countInput.get();
		super.initAndValidate();
	}
	
	@Override
	public double calculateLogP() {
		Function x = m_x.get();
		int dim = (int) counts.getArrayValue();
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

package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.parameter.BooleanParameter;

@Description("Frequencies for ModelTest model, can switch between equal and empricial/estimated frequencies")
public class ModelFrequencies extends Frequencies {
	public Input<BooleanParameter> hasEqualFreqslInput = new Input<>("hasEqualFreqs", "flag to indicate equal frequencies shouldbe used", Validate.REQUIRED);

	
	BooleanParameter hasEqualFreqs;
	
	@Override
	public void initAndValidate() throws Exception {
		hasEqualFreqs = hasEqualFreqslInput.get();
		super.initAndValidate();
	}

	@Override
	public double[] getFreqs() {
		if (hasEqualFreqs.getValue()) {
			return new double[]{0.25, 0.25, 0.25, 0.25};
		}
		return super.getFreqs();
	}
	
}

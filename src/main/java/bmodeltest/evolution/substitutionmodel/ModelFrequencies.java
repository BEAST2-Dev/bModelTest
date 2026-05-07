package bmodeltest.evolution.substitutionmodel;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.evolution.substitutionmodel.Frequencies;
import beast.base.inference.parameter.BooleanParameter;

@Description("Frequencies for ModelTest model, can switch between equal and empricial/estimated frequencies")
public class ModelFrequencies extends Frequencies {
	public Input<BooleanParameter> hasEqualFreqslInput = new Input<>("hasEqualFreqs", "flag to indicate equal frequencies shouldbe used", Validate.REQUIRED);

	// need the following flag for BEAUti
	public Input<Boolean> empiricalInput = new Input<>("empirical", "flag for using empirical frequencies (if true), instead of estimated frequencies", true);

	
	BooleanParameter hasEqualFreqs;
	
	@Override
	public void initAndValidate() {
		hasEqualFreqs = hasEqualFreqslInput.get();
		
		if (empiricalInput.get() && dataInput.get() == null) {
			throw new IllegalArgumentException("data should be specified if empirical=true. Either specify the data attribute or set empirical to false.");
		}
		if (dataInput.get() != null) {
			estimateFrequencies();
		}
		
		super.initAndValidate();
	}

	@Override
	public double[] getFreqs() {
		if (hasEqualFreqs.getValue()) {
			return new double[]{0.25, 0.25, 0.25, 0.25};
		}
		return super.getFreqs();
	}
	
    protected void update() {
        if (frequenciesInput.get() != null) {
        	super.update();
        }
        needsUpdate = false;
    } // update
	
    @Override
    protected boolean requiresRecalculation() {
        boolean recalculates = false;
        if (frequenciesInput.get() != null && frequenciesInput.get().somethingIsDirty()) {

            needsUpdate = true;
            recalculates = true;
        }

        return recalculates;
    }
    
}

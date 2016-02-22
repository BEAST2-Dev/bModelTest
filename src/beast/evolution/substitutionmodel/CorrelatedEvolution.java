package beast.evolution.substitutionmodel;

import beast.core.Description;
import beast.core.Function;
import beast.core.Input;
import beast.core.parameter.BooleanParameter;
import beast.evolution.datatype.DataType;

// Pagel & Meade 2005
@Description("Substitution model for correlated evolution of discrete characters by reversible‐jump")
public class CorrelatedEvolution extends NucleotideRevJumpSubstModel {
	public Input<Boolean> isSymmetricInput = new Input<Boolean>("symmetric", "force rates to be symmetric", false);
	public Input<BooleanParameter> rateMaskInput = new Input<BooleanParameter>("rateMask", "Allows switching rates off by setting the associated mask "
			+ "entry to false, if not specified, the mask is ignored");
	
	
	boolean isSymmetric;
	BooleanParameter rateMask;
	
	@Override
	public void initAndValidate() {
		isSymmetric = isSymmetricInput.get();
		rateMask = rateMaskInput.get();
		if (rateMask == null) {
			rateMask = new BooleanParameter(new Boolean[]{true});
			if (isSymmetric) {
				rateMask.setDimension(4);
			} else {
				rateMask.setDimension(8);
			}
		}
		
		super.initAndValidate();
	}
	
	protected int[][] generateAllModels() {
		if (isSymmetric) {
			return generateAllReversibleModels(new int[4]);
		} else {
			return generateAllReversibleModels(new int[8]);			
		}
	}

	@Override
	protected void setupRelativeRates() {
        Function rates = this.ratesInput.get();
    	int [] model = getModel(modelIndicator.getValue());
    	Boolean [] mask = rateMask.getValues();
    	if (isSymmetric) {
	        relativeRates[0] = (mask[0] ? rates.getArrayValue(model[0]) : 0.0); // 00->01
	        relativeRates[1] = (mask[1] ? rates.getArrayValue(model[1]) : 0.0); // 00->10
	        relativeRates[2] = 0.0; // 00-11

	        relativeRates[3] = relativeRates[0]; // 01->00
	        relativeRates[4] = 0.0; // 01->10
	        relativeRates[5] = (mask[2] ? rates.getArrayValue(model[2]): 0.0); // 01->11

	        relativeRates[6] = relativeRates[1]; // 10->00
	        relativeRates[7] = 0.0; // 10->01
	        relativeRates[8] = (mask[3] ? rates.getArrayValue(model[3]): 0.0); // 10->11

	        relativeRates[9] = 0.0; // 11->00
	        relativeRates[10] = relativeRates[5]; //11->01
	        relativeRates[11] = relativeRates[8]; //11->10
		} else {
	        relativeRates[0] = (mask[0] ? rates.getArrayValue(model[0]) : 0.0); // 00->01
	        relativeRates[1] = (mask[1] ? rates.getArrayValue(model[1]) : 0.0); // 00->10
	        relativeRates[2] = 0.0; // 00-11

	        relativeRates[3] = (mask[2] ? rates.getArrayValue(model[2]) : 0.0); // 01->00
	        relativeRates[4] = 0.0; // 01->10
	        relativeRates[5] = (mask[3] ? rates.getArrayValue(model[3]) : 0.0); // 01->11

	        relativeRates[6] = (mask[4] ? rates.getArrayValue(model[4]) : 0.0); // 10->00
	        relativeRates[7] = 0.0; // 10->01
	        relativeRates[8] = (mask[5] ? rates.getArrayValue(model[5]) : 0.0); // 10->11

	        relativeRates[9] = 0.0; // 11->00
	        relativeRates[10] = (mask[6] ? rates.getArrayValue(model[6]) : 0.0); //11->01
	        relativeRates[11] = (mask[7] ? rates.getArrayValue(model[7]) : 0.0); //11->10
		}
    }

	@Override
	public boolean canHandleDataType(DataType dataType) {
		return (dataType.getStateCount() == 4);
	}
}

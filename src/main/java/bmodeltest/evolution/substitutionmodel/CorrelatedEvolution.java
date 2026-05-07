package bmodeltest.evolution.substitutionmodel;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.spec.domain.NonNegativeReal;
import beast.base.spec.inference.parameter.BoolVectorParam;
import beast.base.spec.type.RealVector;
import beast.base.evolution.datatype.DataType;

// Pagel & Meade 2005
@Description("Substitution model for correlated evolution of discrete characters by reversible‐jump")
public class CorrelatedEvolution extends NucleotideRevJumpSubstModel {
	public Input<Boolean> isSymmetricInput = new Input<Boolean>("symmetric", "force rates to be symmetric", false);
	public Input<BoolVectorParam> rateMaskInput = new Input<>("rateMask", "Allows switching rates off by setting the associated mask "
			+ "entry to false, if not specified, the mask is ignored");


	boolean isSymmetric;
	BoolVectorParam rateMask;

	@Override
	public void initAndValidate() {
		isSymmetric = isSymmetricInput.get();
		rateMask = rateMaskInput.get();
		if (rateMask == null) {
			if (isSymmetric) {
				rateMask = new BoolVectorParam(4, new boolean[]{true});
			} else {
				rateMask = new BoolVectorParam(8, new boolean[]{true});
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
	public void setupRelativeRates() {
        RealVector<? extends NonNegativeReal> rates = this.ratesInput.get();
    	int [] model = getModel(modelIndicator.get());
    	boolean [] mask = rateMask.getValues();
    	if (isSymmetric) {
	        relativeRates[0] = (mask[0] ? rates.get(model[0]) : 0.0); // 00->01
	        relativeRates[1] = (mask[1] ? rates.get(model[1]) : 0.0); // 00->10
	        relativeRates[2] = 0.0; // 00->11

	        relativeRates[3] = relativeRates[0]; // 01->00
	        relativeRates[4] = 0.0; // 01->10
	        relativeRates[5] = (mask[2] ? rates.get(model[2]): 0.0); // 01->11

	        relativeRates[6] = relativeRates[1]; // 10->00
	        relativeRates[7] = 0.0; // 10->01
	        relativeRates[8] = (mask[3] ? rates.get(model[3]): 0.0); // 10->11

	        relativeRates[9] = 0.0; // 11->00
	        relativeRates[10] = relativeRates[5]; //11->01
	        relativeRates[11] = relativeRates[8]; //11->10
		} else {
	        relativeRates[0] = (mask[0] ? rates.get(model[0]) : 0.0); // 00->01
	        relativeRates[1] = (mask[1] ? rates.get(model[1]) : 0.0); // 00->10
	        relativeRates[2] = 0.0; // 00->11

	        relativeRates[3] = (mask[2] ? rates.get(model[2]) : 0.0); // 01->00
	        relativeRates[4] = 0.0; // 01->10
	        relativeRates[5] = (mask[3] ? rates.get(model[3]) : 0.0); // 01->11

	        relativeRates[6] = (mask[4] ? rates.get(model[4]) : 0.0); // 10->00
	        relativeRates[7] = 0.0; // 10->01
	        relativeRates[8] = (mask[5] ? rates.get(model[5]) : 0.0); // 10->11

	        relativeRates[9] = 0.0; // 11->00
	        relativeRates[10] = (mask[6] ? rates.get(model[6]) : 0.0); //11->01
	        relativeRates[11] = (mask[7] ? rates.get(model[7]) : 0.0); //11->10
		}
    }

	@Override
	public boolean canHandleDataType(DataType dataType) {
		return (dataType.getStateCount() == 4);
	}
}

package beast.evolution.operators;


import java.util.List;

import beast.core.Description;
import beast.core.Input;
import beast.core.Input.Validate;
import beast.core.Operator;
import beast.core.parameter.IntegerParameter;
import beast.core.parameter.RealParameter;
import beast.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.util.Randomizer;

@Description("Increases or decreases number of groups of subst models")
public class BMTMergeSplitOperator extends Operator {
	public Input<IntegerParameter> modelIndicatorInput = new Input<IntegerParameter>("modelIndicator", "number of the model to be used", Validate.REQUIRED);
	public Input<NucleotideRevJumpSubstModel> substModelInput = new Input<NucleotideRevJumpSubstModel>("substModel", "model test substitution model representing the individual models", Validate.REQUIRED);
    public Input<RealParameter> ratesInput = new Input<RealParameter>("rates", "Rate parameter which defines the transition rate matrix. ", Validate.REQUIRED);

	IntegerParameter modelIndicator;
	NucleotideRevJumpSubstModel substModel;
	RealParameter rates;

	final static boolean debug = true;
	
	@Override
	public void initAndValidate() throws Exception {
		modelIndicator = modelIndicatorInput.get();
		substModel = substModelInput.get();
		rates = ratesInput.get();
	}

	//@Override
	public double proposal() {
		double logHR = 0;
		int currentModelID = modelIndicator.getValue();
		final int [] model = substModel.getModel(currentModelID);
		
		if (Randomizer.nextBoolean()) {
			// split
			List<Integer> candidates = substModel.getSplitCanditates(currentModelID); 
			if (candidates.size() == 0) {
				// cannot split any further
				return Double.NEGATIVE_INFINITY;
			}
			int k = Randomizer.nextInt(candidates.size());
			int newModelID = candidates.get(k);
			modelIndicator.setValue(newModelID);
			
			
			// determine n1, n2, i1, i2
			int [] newModel = substModel.getModel(newModelID);
			k = 0;
			while (newModel[k] == model[k]) {
				k++;
			}
			int i1 = model[k];
			int i2 = newModel[k];
			if (i1 > i2) { // ensure i1 < i2
				int tmp = i1; i1 = i2; i2 = tmp;
			}
			int n1 = substModel.getSubGroupCount(newModelID)[i1];
			int n2 = substModel.getSubGroupCount(newModelID)[i2];
			
			if (debug) {
				// sanity check
				int [] m = newModel.clone();
				for (int j = 0; j < m.length; j++) {
					if (m[j] == i2) {m[j] = i1;}
				}
				NucleotideRevJumpSubstModel.normaliseModel(m);
				for (int j = 0; j < m.length; j++) {
					if (m[j] != model[j]) {
						throw new RuntimeException("something went wrong in split");
					}
				}
			}
			
			// generate new rates
			double r = rates.getValue(i1);
			double u = -n1 * r + Randomizer.nextDouble() * (n2 + n1) * r;
			double r1 = r + u / n1;
			double r2 = r - u / n2;
			double [] newrates = new double[model.length];
			for (int i = 0; i < model.length; i++) {
				newrates[newModel[i]] =  rates.getValue(model[i]);				
			}
			newrates[i1] = r1;
			newrates[i2] = r2;
			for (int i = 0; i < substModel.getGroupCount(newModelID); i++) {
				rates.setValue(i, newrates[i]);
			}
			
			// calc hastings ratio
			List<Integer> reverseCandidates = substModel.getMergeCanditates(newModelID);
			// split/merge probabilities
			logHR -= -Math.log(candidates.size()) + Math.log(reverseCandidates.size());
			// density of getting into new state due to choise of new rates
			logHR -=  - Math.log(r * (n1 + n2));
			// Jacobian
			logHR -= Math.log(n1 * n2) - Math.log(n1 + n2);
			
		} else {
			// merge
			List<Integer> candidates = substModel.getMergeCanditates(currentModelID); 
			if (candidates.size() == 0) {
				// cannot merge any further
				return Double.NEGATIVE_INFINITY;
			}
			int k = Randomizer.nextInt(candidates.size());
			int newModelID = candidates.get(k);
			modelIndicator.setValue(newModelID);

			
			// determine n1, n2, i1, i2
			int [] newModel = substModel.getModel(newModelID);
			k = 0;
			while (newModel[k] == model[k]) {
				k++;
			}
			int i1 = model[k];
			int i2 = newModel[k];
			int n1 = substModel.getSubGroupCount(currentModelID)[i1];
			int n2 = substModel.getSubGroupCount(currentModelID)[i2];
			
			if (debug) {
				// sanity check
				int [] m = model.clone();
				for (int j = 0; j < m.length; j++) {
					if (m[j] == i2) {m[j] = i1;}
				}
				NucleotideRevJumpSubstModel.normaliseModel(m);
				for (int j = 0; j < m.length; j++) {
					if (m[j] != newModel[j]) {
						throw new RuntimeException("something went wrong in merge");
					}
				}
			}
			
			// calc merged rate
			double r1 = rates.getValue(i1);
			double r2 = rates.getValue(i2);
			double r = (n1 * r1 + n2 * r2) / (n1 + n2);
			
			double [] newrates = new double[model.length];
			for (int i = 0; i < model.length; i++) {
				newrates[newModel[i]] =  rates.getValue(model[i]);				
			}
			newrates[Math.min(i1, i2)] = r;
			for (int i = 0; i < substModel.getGroupCount(newModelID); i++) {
				rates.setValue(i, newrates[i]);
			}

			
			
			// calc hastings ratio
			List<Integer> reverseCandidates = substModel.getSplitCanditates(newModelID);
			// split/merge probabilities
			logHR -= -Math.log(candidates.size()) + Math.log(reverseCandidates.size());
			// density of getting into new state due to choise of new rates
			logHR +=  - Math.log(r * (n1 + n2));
			// Jacobian
			logHR += Math.log(n1 * n2) - Math.log(n1 + n2);
			
		}
		return logHR;
	}


	public double proposal2() {
		double logHR = 0;
		int currentModelID = modelIndicator.getValue();
		final int [] model = substModel.getModel(currentModelID);
		
		if (Randomizer.nextBoolean()) {
			// split
			if (substModel.getGroupCount(currentModelID) == substModel.getMaxGroupCount()) {
				// cannot split any further
				return Double.NEGATIVE_INFINITY;
			}
			
			// select a group to split
			int groupCount = substModel.getGroupCount(currentModelID);
			int i1 = Randomizer.nextInt(groupCount);
			while (substModel.getSubGroupCount(currentModelID)[i1] == 1) {
				i1 = Randomizer.nextInt(groupCount);
			}
			
			// split the group
			int [] newmodel = new int[model.length];
			int n1 = 0, n2 = 0;
			int i22 = -1;
			int i21 = -1;
			while (n1 == 0 || n2 == 0) {
				n1 = 0; n2 = 0;
				for (int i = 0; i < model.length; i++) {
					if (model[i] == i1) {
						if (Randomizer.nextBoolean()) {
							newmodel[i] = i1;
							n1++;
							i21 = i;
						} else {
							newmodel[i] = groupCount;
							n2++;
							i22 = i;
						}
					} else {
						newmodel[i] = model[i];
					}
				}
			}
			
			NucleotideRevJumpSubstModel.normaliseModel(newmodel);
			i1 = newmodel[i21];
			int i2 = newmodel[i22];
			int newModelIndicator = substModel.getModelNumber(newmodel);
			modelIndicator.setValue(newModelIndicator);

			// generate new rates
			double r = rates.getValue(Math.min(i1,  i2));
			double u = -n1 * r + Randomizer.nextDouble() * (n2 + n1) * r;
			double r1 = r + u / n1;
			double r2 = r - u / n2;
			double [] newrates = new double[model.length];
			for (int i = 0; i < model.length; i++) {
				newrates[newmodel[i]] =  rates.getValue(model[i]);				
			}
			newrates[i1] = r1;
			newrates[i2] = r2;
			for (int i = 0; i < model.length; i++) {
				rates.setValue(i, newrates[i]);
			}
			
//			double r = rates.getValue(i1);
//			double u = -n1 * r + Randomizer.nextDouble() * (n2 + n1) * r;
//			double r1 = r + u / n1;
//			double r2 = r - u / n2;
//			rates.setValue(i1, r1);
//			rates.setValue(i2, r2);
			
			// calc hastings ratio
			// M is the number of rate classes in the proposed model that will have more than one element 
			// (i.e., these are the ones that can be split).
			int M = 0;
			for (int subGroupCount : substModel.getSubGroupCount(currentModelID)) {
				if (subGroupCount > 1) {
					M++;
				}
			}
			logHR -= -Math.log(M) - Math.log(Math.pow(2, n1+n2-1)-1) - Math.log(r * (n1 + n2)) + Math.log(groupCount * (groupCount - 1) / 2);
			logHR -= Math.log(n1 * n2) - Math.log(n1+n2);
			
		} else {
			// merge
			if (substModel.getGroupCount(currentModelID) == 1) {
				// cannot merge any further
				return Double.NEGATIVE_INFINITY;
			}
			
			// select two groups to merge
			int groupCount = substModel.getGroupCount(currentModelID);
			int i1 = Randomizer.nextInt(groupCount);
			int i2 = i1;
			while (i2 == i1) {
				i2 =Randomizer.nextInt(groupCount);
			}
			if (i1 > i2) {
				// ensure i1 < i2
				int tmp = i1; i1 = i2; i2 = tmp;
			}
			
			// find new model
			int [] newmodel = new int[model.length];
			for (int i = 0; i < model.length; i++) {
				if (model[i] == i2) {
					newmodel[i] = i1;
				} else {
					newmodel[i] = model[i];
				}
			}
			NucleotideRevJumpSubstModel.normaliseModel(newmodel);

			// calc merged rate
			double r1 = rates.getValue(i1);
			double r2 = rates.getValue(i2);
			int n1 = substModel.getSubGroupCount(currentModelID)[i1];
			int n2 = substModel.getSubGroupCount(currentModelID)[i2];
			double r = (n1 * r1 + n2 * r2) / (n1 + n2);
			
			double [] newrates = new double[model.length];
			for (int i = 0; i < model.length; i++) {
				newrates[newmodel[i]] =  rates.getValue(model[i]);				
			}
			newrates[Math.min(i1, i2)] = r;
			for (int i = 0; i < model.length; i++) {
				rates.setValue(i, newrates[i]);
			}
			
//			double r1 = rates.getValue(i1);
//			double r2 = rates.getValue(i2);
//			int n1 = substModel.getSubGroupCount(currentModelID)[i1];
//			int n2 = substModel.getSubGroupCount(currentModelID)[i2];
//			double r = (n1 * r1 + n2 * r2) / (n1 + n2);
//			rates.setValue(i1, r);
			
			int newModelIndicator = substModel.getModelNumber(newmodel);
			modelIndicator.setValue(newModelIndicator);
			
			// calc hastings ratio
			// M is the number of rate classes in the proposed model that will have more than one element 
			// (i.e., these are the ones that can be split).
			int M = 0;
			for (int subGroupCount : substModel.getSubGroupCount(newModelIndicator)) {
				if (subGroupCount > 1) {
					M++;
				}
			}
			logHR += -Math.log(M) - Math.log(Math.pow(2, n1+n2-1)-1) - Math.log(r * (n1 + n2)) + Math.log(groupCount * (groupCount - 1) / 2); 
			logHR += Math.log(n1 * n2) - Math.log(n1+n2);
		}
		return logHR;
	}

}

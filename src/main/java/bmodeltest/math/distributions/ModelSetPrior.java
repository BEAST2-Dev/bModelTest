package bmodeltest.math.distributions;

import java.util.*;

import beast.base.core.Description;
import beast.base.core.Input;
import beast.base.core.Input.Validate;
import beast.base.inference.Distribution;
import beast.base.inference.State;
import beast.base.spec.domain.NonNegativeInt;
import beast.base.spec.type.IntScalar;
import bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel;
import beast.base.core.BEASTInterface;

@Description("Prior on models, uniform on model number, or uniform on parameter number")
public class ModelSetPrior extends Distribution {
	public enum PriorType {uniformOnModel, uniformOnParameterCount};

	public Input<IntScalar<? extends NonNegativeInt>> xInput = new Input<>("x",
			"model indicator parameter to apply this prior to", Validate.REQUIRED);
	public Input<PriorType> priorTypeInput = new Input<ModelSetPrior.PriorType>("priorType", "Prior on model set,"
			+ " uniformOnModel in order not to prefer any model in the set, e.g. JC69 is as likely as model 121131, or"
			+ " uniformOnParameterCount in order to not prefer a number of parameters, e.g., JC66 is as likely as having a 3-parameter model",
			PriorType.uniformOnModel, PriorType.values());
	public Input<NucleotideRevJumpSubstModel> substModelInput = new Input<NucleotideRevJumpSubstModel>("substModel", "model test substitution model representing the individual models", Validate.REQUIRED);


	IntScalar<? extends NonNegativeInt> modelIndicator;
	NucleotideRevJumpSubstModel substModel;

	// parameterCounts[x] = nr of models with x nr of parameters
	Map<Integer, Integer> parameterCounts;

	// normalisation constant for uniformOnParameterCount
	double logC;


	@Override
	public void initAndValidate() {
		modelIndicator = xInput.get();
		substModel = substModelInput.get();

		if (modelIndicator != substModel.modelIndicatorInput.get()) {
			throw new RuntimeException("ModelSetPrior x input should match substModel.modelIndicator input");
		}

		if (priorTypeInput.get() == PriorType.uniformOnParameterCount) {
			parameterCounts = new HashMap<>();
			int max = 0;
			for (int i = 0; i < substModel.getModelCount(); i++) {
				int parameterCount = substModel.getGroupCount(i);
				if (parameterCounts.containsKey(parameterCount)) {
					parameterCounts.put(parameterCount, parameterCounts.get(parameterCount) + 1);
				} else {
					parameterCounts.put(parameterCount, 1);
				}
				max = Math.max(max,  parameterCount);
			}
			logC = -Math.log(parameterCounts.size());
		}
        calculateLogP();

	}

    @Override
    public double calculateLogP() {
    	logP = 0;
    	switch (priorTypeInput.get()) {
    	case uniformOnModel:
    		logP = -Math.log(substModel.getModelCount());
    		break;
    	case uniformOnParameterCount:
    		int modelID = modelIndicator.get();
    		int groupCount = substModel.getGroupCount(modelID);
    		logP = logC - Math.log(parameterCounts.get(groupCount));
    		break;
    	}
    	return logP;
    }

	@Override
	public List<String> getArguments() {
		List<String> arguments = new ArrayList<>();
		if (modelIndicator instanceof BEASTInterface b && b.getID() != null) {
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

open module bmodeltest {
    requires beast.base;
    requires beast.pkgmgmt;
    requires static beast.fx;
    requires static javafx.controls;
    requires static javafx.web;
    requires static javafx.swing;
    requires static java.desktop;
    requires org.apache.commons.statistics.distribution;
	requires junit;

    exports bmodeltest.app.beauti;
    exports bmodeltest.app.tools;
    exports bmodeltest.evolution.operators;
    exports bmodeltest.evolution.sitemodel;
    exports bmodeltest.evolution.substitutionmodel;
    exports bmodeltest.math.distributions;

    provides beast.base.core.BEASTInterface with
        bmodeltest.app.tools.BModelAnalyser,
        bmodeltest.evolution.operators.BMTBirthDeathOperator,
        bmodeltest.evolution.operators.BMTExchangeOperator,
        bmodeltest.evolution.operators.BMTMergeSplitOperator,
        bmodeltest.evolution.operators.BMTScaleOperator,
        bmodeltest.evolution.sitemodel.BEASTModelTest,
        bmodeltest.evolution.sitemodel.BEASTModelTestSiteModel,
        bmodeltest.evolution.substitutionmodel.ActiveLogger,
        bmodeltest.evolution.substitutionmodel.CorrelatedEvolution,
        bmodeltest.evolution.substitutionmodel.ModelFrequencies,
        bmodeltest.evolution.substitutionmodel.NucleotideRevJumpSubstModel,
        bmodeltest.math.distributions.BMTPrior,
        bmodeltest.math.distributions.ModelSetPrior,
        bmodeltest.math.distributions.NucleotideRevJumpSubstModelRatePrior;

    provides beastfx.app.inputeditor.InputEditor with
        bmodeltest.app.beauti.BEASTModelTestInputEditor,
        bmodeltest.app.beauti.NucleotideRevJumpSubstModelRatePriorInputEditor;
}

# bModelTest
Bayesian model test package for BEAST 2

Uses reversible jump to switch between substitution models over nucleotide data, with or without gamma rate heterogeneity and/or with or without invariant sites.

Description of the outputs which you can find in Tracer:

* BMT_ModelInidicator is the index of the substitution model as listed in the appendix of the paper.

* substmodel is the model represented as a 6-digit number, where the position of the digit refers to rates ac, ag, at, cg, ct and gt respectively, and equal digits indicates that rates are shared, so 111111 is Jukes Cantor (if frequencies are kept equal), 121121 is HKY, 123456 is GTR etc.

* rateAC … rateGT are the rates according to the model. ESSs should be good for these rates, but if you plot joint-marginals of pairs you may find high correlation between some of these rates.

* BMT_Rates.1 to 6 are the rates used to build up the rate matrix. If only low parameter models are samples, the higher rates will be sampled very infrequently, and you should expect low ESSs for them. Correlation between pairs of rates should be low.

* BM_gammaShape is the gamma shape parameter as it is being sampled. For parts of the chain that gamma rate heterogeneity is switched off, the parameter will not be sampled, and the trace will show periods where the parameter is stuck.

* hasGammaRates indicates whether gamma rate heterogeneity it used (1) or not used (0). The mean can be interpreted as the proportion of time that gamma rate heterogeneity is switched on.

* ActiveGammaShape is the gamma shape parameter when it is sampled, but it is zero when it it not sampled. To get the estimate of the mean of the shape parameter, divide the mean ActiveGammaShape by the mean of hasGammaRates.

* BMT_ProportionInvariant, hasInvariantSites and ActivePropInvariant are the value for proportion invariant similar to BMG_gammaShape, hasGammaRates and ActiveGammaShape respectively.

* hasEqualFreqs indicates whether equal frequencies are used and the mean can be interpreted as the proportion of time that equal frequencies is used.

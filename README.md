# bModelTest

Bayesian model test package for [BEAST 3](https://github.com/CompEvol/beast3).

Uses reversible-jump MCMC to switch between substitution models over nucleotide data, with or without gamma rate heterogeneity and/or invariant sites.

See the [wiki](https://github.com/BEAST2-Dev/bModelTest/wiki) for usage details.

[Paper](https://bmcevolbiol.biomedcentral.com/articles/10.1186/s12862-017-0890-6): Bouckaert, Remco R., and Alexei J. Drummond. "bModelTest: Bayesian phylogenetic site model averaging and model comparison." BMC evolutionary biology 17.1 (2017): 42.

## Building from source

Requirements:

* JDK 25 or later
* Apache Maven 3.9+

```sh
mvn compile
mvn test
```

To run the bundled example:

```sh
mvn package -DskipTests
java --module-path "$BEAST3/beast-base/target/beast-base-2.8.0-SNAPSHOT.jar:$BEAST3/beast-base/target/lib:target/bModelTest-1.4.0-beta1.jar" \
     -m beast.base/beast.base.minimal.BeastMain examples/bModelTest.xml
```

(replace `$BEAST3` with the path to your beast3 checkout).

To develop against an unreleased BEAST 3 SNAPSHOT, install BEAST 3 from source:

```sh
cd ~/Git/beast3
mvn install -DskipTests
```

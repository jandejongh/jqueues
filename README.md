# jqueues
Discrete-Even Simulation of Queueing Systems in Java

Java implementation of discrete-event simulation of queueing systems, loosely modeled after Graham Birtwistle's DEMOS (Discrete Event Modelling on SIMULA), see e.g., http://staffwww.dcs.shef.ac.uk/people/G.Birtwistle/research/demos.pdf.

Unlike DEMOS, which also includes support for (e.g.) random-number generation, statistics and reporting, jqueues exclusively focusses on the simulation of queueing systems. For instance, hooks for statistics are present, obviously, but you'll need a statistics package for (sophisticated) statistical analysis.

On the other hand, compared to DEMOS, jqueues has a somewhat larger collection of well-known queueing systems, and supports the notion of composite queues in which queueing systems are constructed by "combining" other queueing systems (e.g., by putting them in tandem).

The project depends on jsimulation (https://github.com/jandejongh/jsimulation). Both jsimulation and jqueues have extensive javadoc comments; a 'Guided Tour' is on its way.

License: Apache v2.0.

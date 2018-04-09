# jqueues
Discrete-Even Simulation of Queueing Systems in Java

Java implementation of discrete-event simulation of queueing systems, loosely modeled after Graham Birtwistle's DEMOS (Discrete Event Modelling on SIMULA), see e.g., http://staffwww.dcs.shef.ac.uk/people/G.Birtwistle/research/demos.pdf.

Unlike DEMOS, which also includes support for (e.g.) random-number generation, statistics and reporting, jqueues exclusively focusses on the simulation of queueing systems. For instance, hooks for statistics are present, obviously, but you'll need a statistics package for (sophisticated) statistical analysis.

On the other hand, compared to DEMOS, jqueues has a somewhat larger collection of well-known queueing systems, and supports the notion of composite queues in which queueing systems are constructed by "combining" other queueing systems (e.g., by putting them in tandem).

### Features

The software features a wide range of both well-known as well as hardly known queueing systems,
like FCFS, FCFS_B (limited buffer), FCFS_c (multiple servers),
LCFS (PR), Infinite Server, Random, PS, SJF, SRTF, Wait-Until-Relieved,
GATE, LeakyBucket, ALIMIT (arrival-rate limiter), Social PS, Catch-Up PS, etc.
All queues support job revocations.

It also features generic composite queueing systems like Tandem, FeedBack and Jackson queues.
In a composite queue, the sub-queues and their interconnections are effectively
hidden; instead, a single queueing system is presented.
Composite queueing systems can be nested arbitrarily deep.

With the (composite) CTandem2 queue, you can even connect the "waiting area" of one queue with
the "service area" of another. This allows for many other queueing systems like
Multi-Server Shortest-Job First through SJF_c = CTandem2[SJF, FCFS_c].

A special class of composite queues named "encapsulators"
operate on a single queueing system.
An encapsulator is able to modify specific aspects of the (sub)queue's behavior,
like hiding the start of jobs, or limiting the waiting time of jobs
(jobs that must wait beyond a given threshold are automatically dropped).

The software comes with many abstract base classes for queueing systems,
allowing for smooth realization of new queueing systems.

### What is does NOT do

The software does NOT come with (sophisticated) random-number generation and
statistics; there are excellent Java libraries for that.
Also, it hardly supports GUI operation.
Note that these missing features are NOT on the wishlist/roadmap for jqueues.

### Dependencies

The project depends on jsimulation (https://github.com/jandejongh/jsimulation).

### Documentation

Both jsimulation and jqueues have extensive javadoc comments; a 'Guided Tour' is on its way;
see https://github.com/jandejongh/jqueues-guided-tour.

### License
Apache v2.0.

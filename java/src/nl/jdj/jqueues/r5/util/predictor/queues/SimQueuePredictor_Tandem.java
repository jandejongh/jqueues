package nl.jdj.jqueues.r5.util.predictor.queues;

import java.util.List;
import nl.jdj.jqueues.r5.entity.queue.composite.tandem.BlackTandemSimQueue;
import nl.jdj.jqueues.r5.extensions.composite.AbstractSimQueuePredictor_Composite;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;

/** A {@link SimQueuePredictor} for {@link BlackTandemSimQueue}.
 *
 */
public class SimQueuePredictor_Tandem<Q extends BlackTandemSimQueue>
extends AbstractSimQueuePredictor_Composite<Q>
implements SimQueuePredictor<Q>
{
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S)
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  public SimQueuePredictor_Tandem (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    super (subQueuePredictors);
  }

}
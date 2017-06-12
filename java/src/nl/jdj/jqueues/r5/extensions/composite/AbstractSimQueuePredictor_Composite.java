package nl.jdj.jqueues.r5.extensions.composite;

import java.util.List;
import nl.jdj.jqueues.r5.entity.jq.job.SimJob;
import nl.jdj.jqueues.r5.entity.jq.queue.SimQueue;
import nl.jdj.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import nl.jdj.jqueues.r5.entity.SimEntitySimpleEventType;
import nl.jdj.jqueues.r5.util.predictor.AbstractSimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.SimQueuePredictor;
import nl.jdj.jqueues.r5.util.predictor.state.SimQueueState;

/** A {@link SimQueuePredictor} for {@link SimQueueComposite}.
 *
 * @param <Q> The type of queue supported.
 * 
 * @author Jan de Jongh, TNO
 * 
 * <p>
 * Copyright (C) 2005-2017 Jan de Jongh, TNO
 * 
 * <p>
 * This file is covered by the LICENSE file in the root of this project.
 * 
 */
public abstract class AbstractSimQueuePredictor_Composite<Q extends SimQueueComposite>
extends AbstractSimQueuePredictor<Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTOR(S) / CLONING / FACTORIES
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  public AbstractSimQueuePredictor_Composite (final List<AbstractSimQueuePredictor> subQueuePredictors)
  {
    if (subQueuePredictors == null || subQueuePredictors.size () < 1)
      throw new IllegalArgumentException ();
    this.subQueuePredictors = subQueuePredictors;
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE PREDICTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected final List<AbstractSimQueuePredictor> subQueuePredictors;

  @Override
  public SimQueueState<SimJob, Q> createQueueState (final Q queue, final boolean isROEL)
  {
    return super.createQueueState (queue, isROEL);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SUB-QUEUE SIMPLE EVENT
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected static class SubQueueSimpleEvent
  extends SimEntitySimpleEventType.Member
  {

    public final SimQueue subQueue;
    
    public final SimEntitySimpleEventType.Member subQueueWorkloadEvent;
    
    public final SimEntitySimpleEventType.Member subQueueQueueEvent;
    
    public final SimJob job;
    
    public final Object argument;
    
    public SubQueueSimpleEvent
      (final SimQueue subQueue,
       final SimEntitySimpleEventType.Member subQueueWorkloadEvent,
       final SimEntitySimpleEventType.Member subQueueQueueEvent,
       final SimJob job,
       final Object argument)
    {
      super ("SUBQUEUE");
      if (subQueue == null
        || (subQueueWorkloadEvent == null && subQueueQueueEvent == null)
        || (subQueueWorkloadEvent != null && subQueueQueueEvent != null))
        throw new IllegalArgumentException ();
      this.subQueue = subQueue;
      this.subQueueWorkloadEvent = subQueueWorkloadEvent;
      this.subQueueQueueEvent = subQueueQueueEvent;
      this.job = job;
      this.argument = argument;
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
}
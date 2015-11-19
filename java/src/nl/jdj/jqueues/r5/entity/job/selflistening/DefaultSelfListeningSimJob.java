package nl.jdj.jqueues.r5.entity.job.selflistening;

import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobListener;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jsimulation.r5.SimEventList;

/** A {@link DefaultSimJob} that listens to notifications from itself as a {@link SimJobListener} and providing overridable methods
 * for notifications.
 *
 * @param <J> The type of {@link SimJob}s supported.
 * @param <Q> The type of {@link SimQueue}s supported.
 *
 */
public class DefaultSelfListeningSimJob<J extends DefaultSelfListeningSimJob, Q extends SimQueue>
extends DefaultSimJob<J, Q>
implements SimJobListener<J, Q>
{

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // CONSTRUCTORS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
  /** Creates a new {@link DefaultSelfListeningSimJob}.
   * 
   * @see DefaultSimJob#DefaultSimJob For detailed explanation on the parameters.
   * 
   */
  public DefaultSelfListeningSimJob (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    super (eventList, name, requestedServiceTimeMap);
    registerSimEntityListener (this);
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SimJobListener
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  /** Does nothing.
   * 
   */
  @Override
  public void notifyResetEntity (final SimEntity entity)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyUpdate (final double time, final SimEntity entity)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStateChanged (final double time, final SimEntity entity)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyArrival (double time, J job, Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStart (double time, J job, Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDrop (double time, J job, Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (double time, J job, Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (double time, J job, Q queue)
  {
    /* EMPTY */
  }

}

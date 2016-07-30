package nl.jdj.jqueues.r5.entity.job.selflistening;

import java.util.List;
import java.util.Map;
import nl.jdj.jqueues.r5.SimEntity;
import nl.jdj.jqueues.r5.SimJob;
import nl.jdj.jqueues.r5.SimJobListener;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.job.DefaultSimJob;
import nl.jdj.jqueues.r5.event.simple.SimEntitySimpleEventType;
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
   * @param eventList               The event list.
   * @param name                    The name.
   * @param requestedServiceTimeMap The requested service-time map.
   * 
   * @see DefaultSimJob#DefaultSimJob(nl.jdj.jsimulation.r5.SimEventList, java.lang.String, java.util.Map)
   *        For a more detailed explanation of the parameters.
   * 
   */
  public DefaultSelfListeningSimJob (final SimEventList eventList, final String name, final Map<Q, Double> requestedServiceTimeMap)
  {
    super (eventList, name, requestedServiceTimeMap);
    registerSimEntityListener (this);
  }

  /** Creates a new {@link DefaultSelfListeningSimJob}.
   * 
   * @param eventList            The event list.
   * @param name                 The name.
   * @param requestedServiceTime The requested service-time.
   * 
   * @see DefaultSimJob#DefaultSimJob(nl.jdj.jsimulation.r5.SimEventList, java.lang.String, double)
   *        For a more detailed explanation of the parameters.
   * 
   */
  public DefaultSelfListeningSimJob (final SimEventList eventList, final String name, final double requestedServiceTime)
  {
    super (eventList, name, requestedServiceTime);
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
  public void notifyStateChanged
  (final double time, final SimEntity entity, final List<Map<SimEntitySimpleEventType.Member, J>> notifications)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyArrival (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyStart (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDrop (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyRevocation (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyAutoRevocation (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

  /** Does nothing.
   * 
   */
  @Override
  public void notifyDeparture (final double time, final J job, final Q queue)
  {
    /* EMPTY */
  }

}

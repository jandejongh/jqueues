package nl.jdj.jqueues.r5.entity.queue.mac;

/**
 *
 */
public interface MediumPhyStateObserver<M extends MediumPhyStateMonitor, O extends MediumPhyStateObserver>
{

  public void mediumPhyStateUpdate (double time, MediumPhyState mediumPhyState);
  
  public void lastReceptionUpdate (double time, boolean success);

}

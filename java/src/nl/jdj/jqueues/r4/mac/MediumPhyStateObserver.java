package nl.jdj.jqueues.r4.mac;

/**
 *
 */
public interface MediumPhyStateObserver<M extends MediumPhyStateMonitor, O extends MediumPhyStateObserver>
{

  public void mediumPhyStateUpdate (double time, MediumPhyState mediumPhyState);
  
  public void lastReceptionUpdate (double time, boolean success);

}

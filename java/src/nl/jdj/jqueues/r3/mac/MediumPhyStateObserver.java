package nl.jdj.jqueues.r3.mac;

/**
 *
 */
public interface MediumPhyStateObserver
{

  public void mediumPhyStateUpdate (double time, MediumPhyState mediumPhyState);
  
  public void lastReceptionUpdate (double time, boolean success);

}

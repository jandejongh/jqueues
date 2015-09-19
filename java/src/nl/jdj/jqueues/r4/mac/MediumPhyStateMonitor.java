package nl.jdj.jqueues.r4.mac;

/**
 *
 */
public interface MediumPhyStateMonitor
{

  public void registerMediumPhyStateObserver (MediumPhyStateObserver observer);
  
  public void unregisterMediumPhyStateObserver (MediumPhyStateObserver observer);
  
  public MediumPhyState getMediumPhyState (double time, MediumPhyStateObserver observer);
  
  public void startTransmission (double time, MediumPhyStateObserver observer, DCFSimJob job);
  
}

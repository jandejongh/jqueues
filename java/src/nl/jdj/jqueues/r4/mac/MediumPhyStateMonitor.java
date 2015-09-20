package nl.jdj.jqueues.r4.mac;

/**
 *
 */
public interface MediumPhyStateMonitor<M extends MediumPhyStateMonitor, O extends MediumPhyStateObserver>
{

  public void registerMediumPhyStateObserver (O observer);
  
  public void unregisterMediumPhyStateObserver (O observer);
  
  public MediumPhyState getMediumPhyState (double time, O observer);
  
  public void startTransmission (double time, O observer, DCFSimJob job);
  
}

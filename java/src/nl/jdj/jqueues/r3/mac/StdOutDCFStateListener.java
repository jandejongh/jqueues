package nl.jdj.jqueues.r3.mac;

/**
 *
 */
public class StdOutDCFStateListener
implements DCFStateListener
{

  @Override
  public void newDCFState (final double time, final DCF dcf, final DCFState oldState, final DCFState newState)
  {
    System.out.println ("[StdOutDCFStateListener] t=" + time + ", dcf=" + dcf + ": [" + oldState + " -> " + newState + "].");
  }
  
  
}

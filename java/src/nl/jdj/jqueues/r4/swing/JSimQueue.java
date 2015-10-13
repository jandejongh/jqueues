package nl.jdj.jqueues.r4.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Set;
import javax.swing.JComponent;
import nl.jdj.jqueues.r4.SimJob;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.SimQueueVacationListener;
import nl.jdj.jqueues.r4.composite.BlackSimQueueNetwork;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class JSimQueue
extends JComponent
implements SimQueueVacationListener
{
  
  private final SimEventList eventList;
  
  private final SimQueue queue;
  
  public JSimQueue (final SimEventList eventList, final SimQueue queue)
  {
    super ();
    if (eventList == null || queue == null)
      throw new IllegalArgumentException ();
    this.eventList = eventList;
    this.queue = queue;
    this.queue.registerQueueListener (this);
    if (this.queue instanceof BlackSimQueueNetwork)
    {
      for (final SimQueue q : (Set<SimQueue>) ((BlackSimQueueNetwork) this.queue).getQueues ())
        add (new JSimQueue (eventList, q));
    }
    else
    {
      setMinimumSize (new Dimension (120, 240));
    }
  }

  @Override
  protected void paintComponent (Graphics g)
  {
    super.paintComponent (g);
    final Graphics2D g2d = (Graphics2D) g.create ();
    final int width = getWidth ();
    final int height = getHeight ();
    if (isOpaque ())
    {
      // paint background
      g2d.setColor (getBackground ());
      g.fillRect (0, 0, width, height);
    }
    final int middleX = width / 2;
    final int middleY = height / 2;
    // Draw queueing system outline.
    g2d.setStroke (new BasicStroke (4.0f));
    g2d.setColor (this.queue.isQueueAccessVacation () ? Color.red : Color.green);
    g2d.drawRect (middleX - 50, middleY - 100, 100, 200);
    // Draw arrowed line from wait queue to server.
    g2d.setColor (this.queue.getServerAccessCredits () > 0 ? Color.green : Color.red);
    g2d.drawLine (middleX, middleY - 40, middleX, middleY - 10);
    // Draw number of server-access credits.
    if (this.queue.getServerAccessCredits () < Integer.MAX_VALUE)
      g2d.drawString ("" + this.queue.getServerAccessCredits (), middleX + 10, middleY - 20);
    // Draw wait queue.
    g2d.setColor (Color.black);
    final BasicStroke savedStroke = (BasicStroke) g2d.getStroke ();
    if (this.queue.isNoWaitArmed ())
      g2d.setStroke (new BasicStroke
        (savedStroke.getLineWidth (),
          savedStroke.getEndCap (),
          savedStroke.getLineJoin (),
          savedStroke.getMiterLimit (),
          new float[] { 5f, 10f },
          0f));
    g2d.drawLine (middleX - 30, middleY - 40, middleX + 30, middleY - 40);
    g2d.drawLine (middleX - 30, middleY - 60, middleX - 30, middleY - 40);
    g2d.drawLine (middleX + 30, middleY - 60, middleX + 30, middleY - 40);
    g2d.setStroke (savedStroke);
    // Draw number of jobs waiting.
    g2d.drawString ("" + (this.queue.getNumberOfJobs () - this.queue.getNumberOfJobsExecuting ()), middleX - 10, middleY - 50);
    // Draw (single) server.
    g2d.setColor (Color.black);
    g2d.drawOval (middleX - 30, middleY - 10, 60, 60);
    // Draw number of jobs executing.
    g2d.drawString ("" + this.queue.getNumberOfJobsExecuting (), middleX - 10, middleY +30);    
    // Draw queue name.
    g2d.setColor (Color.black);
    g2d.drawString (this.queue.toString (), middleX - 40, middleY + 90);
    g2d.dispose ();
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SIMQUEUELISTENER; SIMQUEUEVACATIONLISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  protected void notifyQueueChanged (final double t, final SimQueue queue)
  {
    if (queue == this.queue)
      repaint ();
  }
  
  @Override
  public void notifyStartQueueAccessVacation (final double t, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyStopQueueAccessVacation (final double t, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyOutOfServerAccessCredits (final double t, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyRegainedServerAccessCredits (final double t, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyReset (final double oldTime, final SimQueue queue)
  {
    notifyQueueChanged (oldTime, queue);    
  }
  
  @Override
  public void notifyUpdate (final double t, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyArrival (final double t, final SimJob job, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyStart (final double t, final SimJob job, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyDrop (final double t, final SimJob job, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyRevocation (final double t, final SimJob job, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyDeparture (final double t, final SimJob job, final SimQueue queue)
  {
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyNewNoWaitArmed (final double t, final SimQueue queue, final boolean noWaitArmed)
  {
    notifyQueueChanged (t, queue);
  }
  
}

/* 
 * Copyright 2010-2018 Jan de Jongh <jfcmdejongh@gmail.com>, TNO.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.javades.jqueues.r5.util.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import org.javades.jqueues.r5.entity.SimEntity;
import org.javades.jqueues.r5.entity.jq.job.SimJob;
import org.javades.jqueues.r5.entity.jq.queue.SimQueue;
import org.javades.jqueues.r5.entity.jq.queue.SimQueueListener;
import org.javades.jqueues.r5.entity.jq.queue.composite.SimQueueComposite;
import org.javades.jsimulation.r5.SimEventList;

/** A Swing component for a {@link SimQueue}.
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
public class JSimQueue
extends JComponent
implements SimQueueListener
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
    this.queue.registerSimEntityListener (this);
    if (this.queue instanceof SimQueueComposite)
    {
      for (final SimQueue q : (Set<SimQueue>) ((SimQueueComposite) this.queue).getQueues ())
        add (new JSimQueue (eventList, q));
    }
    else
    {
      setMinimumSize (new Dimension (120, 240));
    }
  }
  
  private class ArrivalInfo
  {
    private SimJob job = null;
    private double time;
    private int counter = 0;
  }

  private final ArrivalInfo arrivalInfo = new ArrivalInfo ();
  
  private final void setArrivalInfo (final double t, final SimJob job, final SimQueue queue)
  {
    if (queue == this.queue)
    {
      this.arrivalInfo.job = job;
      this.arrivalInfo.time = t;
      this.arrivalInfo.counter++;
    }
  }
  
  private class DropInfo
  {
    private SimJob job = null;
    private double time;
    private int counter = 0;
  }

  private final DropInfo dropInfo = new DropInfo ();
  
  private final void setDropInfo (final double t, final SimJob job, final SimQueue queue)
  {
    if (queue == this.queue)
    {
      this.dropInfo.job = job;
      this.dropInfo.time = t;
      this.dropInfo.counter++;
    }
  }
  
  private class RevocationInfo
  {
    private SimJob job = null;
    private double time;
    private int counter = 0;
  }

  private final RevocationInfo revocationInfo = new RevocationInfo ();
  
  private final void setRevocationInfo (final double t, final SimJob job, final SimQueue queue)
  {
    if (queue == this.queue)
    {
      this.revocationInfo.job = job;
      this.revocationInfo.time = t;
      this.revocationInfo.counter++;
    }
  }
  
  private class DepartureInfo
  {
    private SimJob job = null;
    private double time;
    private int counter = 0;
  }

  private final DepartureInfo departureInfo = new DepartureInfo ();
  
  private final void setDepartureInfo (final double t, final SimJob job, final SimQueue queue)
  {
    if (queue == this.queue)
    {
      this.departureInfo.job = job;
      this.departureInfo.time = t;
      this.departureInfo.counter++;
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
    // Draw the arrival info.
    g2d.setColor (Color.yellow.darker ());
    g2d.drawString ("Ar:" + this.arrivalInfo.counter, middleX - 47, middleY - 80);
    // Draw the departure info.
    g2d.setColor (Color.green.darker ());
    g2d.drawString ("De:" + this.departureInfo.counter, middleX - 2, middleY - 80);
    // Draw the revocation info.
    g2d.setColor (Color.orange.darker ());
    g2d.drawString ("Re:" + this.revocationInfo.counter, middleX - 47, middleY - 60);
    // Draw the drop info.
    g2d.setColor (Color.red.darker ());
    g2d.drawString ("Dr:" + this.dropInfo.counter, middleX -2, middleY - 60);
    // Draw arrowed line from wait queue to server.
    g2d.setColor (this.queue.getServerAccessCredits () > 0 ? Color.green : Color.red);
    g2d.drawLine (middleX, middleY - 20, middleX, middleY + 10);
    // Draw number of server-access credits.
    if (this.queue.getServerAccessCredits () < Integer.MAX_VALUE)
      g2d.drawString ("" + this.queue.getServerAccessCredits (), middleX + 10, middleY);
    // Draw wait queue.
    g2d.setColor (Color.black);
    final BasicStroke savedStroke = (BasicStroke) g2d.getStroke ();
    if (this.queue.isStartArmed ())
      g2d.setStroke (new BasicStroke
        (savedStroke.getLineWidth (),
          savedStroke.getEndCap (),
          savedStroke.getLineJoin (),
          savedStroke.getMiterLimit (),
          new float[] { 5f, 10f },
          0f));
    g2d.drawLine (middleX - 30, middleY - 20, middleX + 30, middleY - 20);
    g2d.drawLine (middleX - 30, middleY - 40, middleX - 30, middleY - 20);
    g2d.drawLine (middleX + 30, middleY - 40, middleX + 30, middleY - 20);
    g2d.setStroke (savedStroke);
    // Draw number of jobs waiting.
    g2d.drawString ("W:" + (this.queue.getNumberOfJobs () - this.queue.getNumberOfJobsInServiceArea ()), middleX - 20, middleY - 30);
    // Draw (single) server.
    g2d.setColor (Color.black);
    g2d.drawOval (middleX - 30, middleY + 10, 60, 60);
    // Draw number of jobs in the service area.
    g2d.drawString ("S:" + this.queue.getNumberOfJobsInServiceArea (), middleX - 20, middleY + 45);    
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
  public void notifyResetEntity (final SimEntity entity)
  {
    if (entity != null && (entity instanceof SimQueue) && ((SimQueue) entity) == this.queue)
    {
      this.arrivalInfo.job = null;
      this.arrivalInfo.time = Double.NEGATIVE_INFINITY;
      this.arrivalInfo.counter = 0;
      this.dropInfo.job = null;
      this.dropInfo.time = Double.NEGATIVE_INFINITY;
      this.dropInfo.counter = 0;
      this.revocationInfo.job = null;
      this.revocationInfo.time = Double.NEGATIVE_INFINITY;
      this.revocationInfo.counter = 0;    
      this.departureInfo.job = null;
      this.departureInfo.time = Double.NEGATIVE_INFINITY;
      this.departureInfo.counter = 0;
      notifyQueueChanged (this.eventList.getTime (), this.queue);
    }
  }
  
  @Override
  public void notifyUpdate (final double t, final SimEntity entity)
  {
  }

  @Override
  public void notifyStateChanged (double time, SimEntity entity, List notifications)
  {
    notifyQueueChanged (time, (SimQueue) entity);    
  }

  
  @Override
  public void notifyArrival (final double t, final SimJob job, final SimQueue queue)
  {
    setArrivalInfo (t, job, queue);
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
    setDropInfo (t, job, queue);
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyRevocation (final double t, final SimJob job, final SimQueue queue)
  {
    setRevocationInfo (t, job, queue);
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyAutoRevocation (final double t, final SimJob job, final SimQueue queue)
  {
    notifyRevocation (t, job, queue);
  }

  @Override
  public void notifyDeparture (final double t, final SimJob job, final SimQueue queue)
  {
    setDepartureInfo (t, job, queue);
    notifyQueueChanged (t, queue);
  }

  @Override
  public void notifyNewStartArmed (final double t, final SimQueue queue, final boolean startArmed)
  {
    notifyQueueChanged (t, queue);
  }
  
}

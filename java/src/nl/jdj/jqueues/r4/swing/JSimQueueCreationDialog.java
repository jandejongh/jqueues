package nl.jdj.jqueues.r4.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import nl.jdj.jqueues.r4.SimQueue;
import nl.jdj.jqueues.r4.composite.BlackSimQueueNetwork;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class JSimQueueCreationDialog
extends JDialog
implements ItemListener
{
  
  private final SimEventList eventList;
  
  private SimQueue createdQueue = null;
  
  public final SimQueue getCreatedQueue ()
  {
    return this.createdQueue;
  }
  
  private final JComboBox knownQueues;
  
  private final Set<SimQueue> subQueues = new LinkedHashSet<>  ();
  
  private final KnownSimQueue.Parameters parameters = new KnownSimQueue.Parameters ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SWING COMPONENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final TableModel tableModel = new QueuesTableModel ();
  
  private final JTable table = new JTable (this.tableModel);
  
  final JTextField numberOfServersTextField = new JTextField ("Number of Servers Value");
  
  final JTextField bufferSizeTextField = new JTextField ("Buffer Size Value");

  final JTextField waitServiceTimeTextField = new JTextField ("Wait/Service Time Value");
  
  private void setQueueType (final KnownSimQueue queueType)
  {
    if (queueType != null)
    {
      // this.table
      this.table.setEnabled (queueType.isComposite ());
      this.table.setBackground (queueType.isComposite () ? getBackground () : new Color (255, 192, 192));
      // this.numberOfServersTextField
      final int defaultNumberOfServers = queueType.getNumberOfServersProfile ().getDefValue ();
      this.numberOfServersTextField.setText (defaultNumberOfServers == -1 ? "Infinite" : Integer.toString (defaultNumberOfServers));
      this.parameters.numberOfServers = defaultNumberOfServers;
      final boolean numberOfServersEditable = queueType.getNumberOfServersProfile ().isUserSettable ();
      this.numberOfServersTextField.setEditable (numberOfServersEditable);
      this.numberOfServersTextField.setBackground (numberOfServersEditable ? getBackground () : new Color (255, 192, 192));
      // this.bufferSizeTextField
      final int defaultBufferSize = queueType.getBufferSizeProfile ().getDefValue ();
      this.bufferSizeTextField.setText (defaultBufferSize == -1 ? "Infinite" : Integer.toString (defaultBufferSize));
      this.parameters.bufferSize = defaultBufferSize;
      final boolean bufferSizeEditable = queueType.getBufferSizeProfile ().isUserSettable ();
      this.bufferSizeTextField.setEditable (bufferSizeEditable);
      this.bufferSizeTextField.setBackground (bufferSizeEditable ? getBackground () : new Color (255, 192, 192));
      // this.waitServiceTimeTextField
      final double defaultWaitServiceTime = queueType.getWaitServiceTimeProfile ().getDefValue ();
      this.waitServiceTimeTextField.setText (Double.toString (defaultWaitServiceTime));
      this.parameters.waitServiceTime = defaultWaitServiceTime;
      final boolean waitServiceTimeEditable = queueType.getWaitServiceTimeProfile ().isUserSettable ();
      this.waitServiceTimeTextField.setEditable (waitServiceTimeEditable);
      this.waitServiceTimeTextField.setBackground (waitServiceTimeEditable ? getBackground () : new Color (255, 192, 192));
    }
    else
    {
      // XXX
    }
  }
  
  private void createQueueAndClose ()
  {
    final SimQueue createdQueue = ((KnownSimQueue) this.knownQueues.getSelectedItem ()).newInstance (this.parameters);
    if (createdQueue == null)
    {
      JOptionPane.showConfirmDialog
        (this, "Could not create SimQueue!", "Error", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
      // setVisible (false);
      // dispose ();
    }
    else
    {
      this.createdQueue = createdQueue;
      this.eventList.reset (this.parameters.startTime);
      setVisible (false);
      // dispose ();
    }
  }
  
  public JSimQueueCreationDialog (final Frame frame, final SimEventList eventList, final SimQueue queue)
  {
    super (frame, "Create Queue", true);
    if (eventList == null)
      throw new IllegalArgumentException ();
    this.eventList = eventList;
    this.parameters.eventList = this.eventList;
    this.knownQueues = new JComboBox (KnownSimQueue.values ());
    if (queue != null)
      this.knownQueues.setSelectedItem (KnownSimQueue.valueOf (queue));
    if (this.knownQueues.getSelectedItem () != null && ((KnownSimQueue) this.knownQueues.getSelectedItem ()).isComposite ())
      this.subQueues.addAll (((BlackSimQueueNetwork) queue).getQueues ());
    getContentPane ().setLayout (new BoxLayout (getContentPane (), BoxLayout.PAGE_AXIS));
    add (Box.createRigidArea (new Dimension (0, 10)));
    this.knownQueues.setPreferredSize (new Dimension (200, 50));
    this.knownQueues.setMaximumSize (new Dimension (200, 50));
    this.knownQueues.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Select Queue Type"));
    add (this.knownQueues);
    add (Box.createRigidArea (new Dimension (0, 10)));
    if (this.knownQueues.getSelectedItem () != null && ((KnownSimQueue) this.knownQueues.getSelectedItem ()).isComposite ())
    {
      this.table.setBackground (getBackground ());
      this.table.setEnabled (true);
    }
    else
    {
      this.table.setBackground (new Color (255, 192, 192));
      this.table.setEnabled (false);
    }
    this.table.setPreferredSize (new Dimension (200, 200));
    this.table.setMaximumSize (new Dimension (200, 200));
    final JComponent scrollPane = new JScrollPane (this.table);
    scrollPane.setPreferredSize (new Dimension (400, 200));
    scrollPane.setMaximumSize (new Dimension (400, 200));
    scrollPane.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Sub-Queues"));
    add (scrollPane);
    add (Box.createRigidArea (new Dimension (0, 10)));
    final JPanel parametersPanel = new JPanel ();
    final JLabel numberOfServersLabel = new JLabel ("Number of Servers");
    final JLabel bufferSizeLabel = new JLabel ("Buffer Size");
    final JLabel waitServiceTimeLabel = new JLabel ("Wait/Service Time");
    final NumberOfServersTextFieldListener numberOfServersTextFieldListener = new NumberOfServersTextFieldListener ();
    this.numberOfServersTextField.addActionListener (numberOfServersTextFieldListener);
    this.numberOfServersTextField.addFocusListener (numberOfServersTextFieldListener);
    final BufferSizeTextFieldListener bufferSizeTextFieldListener = new BufferSizeTextFieldListener ();
    this.bufferSizeTextField.addActionListener (bufferSizeTextFieldListener);
    this.bufferSizeTextField.addFocusListener (bufferSizeTextFieldListener);
    final WaitServiceTimeTextFieldListener waitServiceTimeTextFieldListener = new WaitServiceTimeTextFieldListener ();
    this.waitServiceTimeTextField.addActionListener (waitServiceTimeTextFieldListener);
    this.waitServiceTimeTextField.addFocusListener (waitServiceTimeTextFieldListener);
    final GroupLayout parametersLayout = new GroupLayout (parametersPanel);
    parametersPanel.setLayout (parametersLayout);
    parametersLayout.setAutoCreateGaps (true);
    parametersLayout.setAutoCreateContainerGaps (true);
    parametersLayout.setHorizontalGroup
    (
      parametersLayout.createSequentialGroup ()
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.LEADING)
            .addComponent (numberOfServersLabel)
            .addComponent (bufferSizeLabel)
            .addComponent (waitServiceTimeLabel)
        )
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.LEADING)
            .addComponent (numberOfServersTextField)
            .addComponent (bufferSizeTextField)
            .addComponent (waitServiceTimeTextField)
        )
    );
    parametersLayout.setVerticalGroup
    (
      parametersLayout.createSequentialGroup ()
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (numberOfServersLabel)
            .addComponent (numberOfServersTextField)
        )
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (bufferSizeLabel)
            .addComponent (bufferSizeTextField)
        )
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (waitServiceTimeLabel)
            .addComponent (waitServiceTimeTextField)
        )
    );
    parametersPanel.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Parameters"));
    add (parametersPanel);
    add (Box.createRigidArea (new Dimension (0, 10)));
    final JPanel initialStatePanel = new JPanel ();
    final JLabel startTimeLabel           = new JLabel ("Start Time");
    final JLabel queueAccessVacationLabel = new JLabel ("Queue Access Vacation");
    final JLabel serverAccessCreditsLabel = new JLabel ("Server-Access Credits");
    final JTextField startTimeTextField = new JTextField ();
    startTimeTextField.setText (Double.toString (this.parameters.startTime));
    startTimeTextField.addActionListener (new ActionListener ()
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        final String text = startTimeTextField.getText ();
        if (text != null)
        {
          final double startTimeDouble;
          try
          {
            startTimeDouble = Double.parseDouble (text);
            JSimQueueCreationDialog.this.parameters.startTime = startTimeDouble;
            return;
          }
          catch (NumberFormatException nfe)
          {
          }
        }
        startTimeTextField.setText (Double.toString (JSimQueueCreationDialog.this.parameters.startTime));
      }
    });
    final JCheckBox queueAccessVacationCheckBox = new JCheckBox ();
    queueAccessVacationCheckBox.setSelected (queue == null ? false : queue.isQueueAccessVacation ());
    this.parameters.queueAccessVacation = (queue == null ? false : queue.isQueueAccessVacation ());
    queueAccessVacationCheckBox.addItemListener (new ItemListener ()
    {
      @Override
      public final void itemStateChanged (final ItemEvent ie)
      {
        JSimQueueCreationDialog.this.parameters.queueAccessVacation = (ie.getStateChange () == ItemEvent.SELECTED);
      }
    });
    final JTextField serverAccessCreditsTextField = new JTextField ("Server-Access Credits Value");
    final String initServerAccessCreditsString;
    if (queue == null || queue.getServerAccessCredits () == Integer.MAX_VALUE)
      initServerAccessCreditsString = "Infinity";
    else
      initServerAccessCreditsString = Integer.toString (queue.getServerAccessCredits ());
    serverAccessCreditsTextField.setText (initServerAccessCreditsString);
    serverAccessCreditsTextField.addActionListener (new ActionListener ()
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        final String text = serverAccessCreditsTextField.getText ();
        if (text != null)
        {
          if (text.trim ().startsWith ("inf") || text.trim ().startsWith ("Inf"))
          {
            serverAccessCreditsTextField.setText ("Infinity");
            JSimQueueCreationDialog.this.parameters.serverAccessCredits = Integer.MAX_VALUE;
          }
          else
          {
            final int serverAccessCreditsInt;
            try
            {
              serverAccessCreditsInt = Integer.parseInt (text);
              if (serverAccessCreditsInt < 0)
              {
                if (JSimQueueCreationDialog.this.parameters.serverAccessCredits == Integer.MAX_VALUE)
                  serverAccessCreditsTextField.setText ("Infinity");
                else
                  serverAccessCreditsTextField.setText (Integer.toString
                    (JSimQueueCreationDialog.this.parameters.serverAccessCredits));        
              }
              else
              {
                JSimQueueCreationDialog.this.parameters.serverAccessCredits = serverAccessCreditsInt;
                if (serverAccessCreditsInt == Integer.MAX_VALUE)
                  numberOfServersTextField.setText ("Infinity");
              }
            }
            catch (NumberFormatException nfe)
            {
              if (JSimQueueCreationDialog.this.parameters.serverAccessCredits == Integer.MAX_VALUE)
                serverAccessCreditsTextField.setText ("Infinity");
              else
                serverAccessCreditsTextField.setText
                  (Integer.toString (JSimQueueCreationDialog.this.parameters.serverAccessCredits));        
            }
          }
        }
        else
        {
          if (JSimQueueCreationDialog.this.parameters.serverAccessCredits == Integer.MAX_VALUE)
            serverAccessCreditsTextField.setText ("Infinity");
          else
            serverAccessCreditsTextField.setText (Integer.toString (JSimQueueCreationDialog.this.parameters.serverAccessCredits));
        }
      }
    });
    final GroupLayout initialStateLayout = new GroupLayout (initialStatePanel);
    initialStatePanel.setLayout (initialStateLayout);
    initialStateLayout.setAutoCreateGaps (true);
    initialStateLayout.setAutoCreateContainerGaps (true);
    initialStateLayout.setHorizontalGroup
    (
      initialStateLayout.createSequentialGroup ()
        .addGroup
        (
          initialStateLayout.createParallelGroup (GroupLayout.Alignment.LEADING)
            .addComponent (startTimeLabel)
            .addComponent (queueAccessVacationLabel)
            .addComponent (serverAccessCreditsLabel)
        )
        .addGroup
        (
          initialStateLayout.createParallelGroup (GroupLayout.Alignment.LEADING)
            .addComponent (startTimeTextField)
            .addComponent (queueAccessVacationCheckBox)
            .addComponent (serverAccessCreditsTextField)
        )
    );
    initialStateLayout.setVerticalGroup
    (
      initialStateLayout.createSequentialGroup ()
        .addGroup
        (
          initialStateLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (startTimeLabel)
            .addComponent (startTimeTextField)
        )
        .addGroup
        (
          initialStateLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (queueAccessVacationLabel)
            .addComponent (queueAccessVacationCheckBox)
        )
        .addGroup
        (
          initialStateLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (serverAccessCreditsLabel)
            .addComponent (serverAccessCreditsTextField)
        )
    );
    initialStatePanel.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Initial State"));
    add (initialStatePanel);
    add (Box.createRigidArea (new Dimension (0, 10)));
    final JPanel exitPanel = new JPanel ();
    final GroupLayout exitLayout = new GroupLayout (exitPanel);
    exitPanel.setLayout (exitLayout);
    exitLayout.setAutoCreateGaps (true);
    exitLayout.setAutoCreateContainerGaps (true);
    final JButton createButton = new JButton (new AbstractAction ("Create")
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        createQueueAndClose ();
      }
    });
    final JButton cancelButton = new JButton (new AbstractAction ("Cancel")
    {
      @Override
      public final void actionPerformed (final ActionEvent ae)
      {
        JSimQueueCreationDialog.this.setVisible (false);
      }
    });
    exitLayout.setHorizontalGroup
    (
      exitLayout.createSequentialGroup ()
        .addComponent (createButton)
        .addComponent (cancelButton)
    );
    exitLayout.setVerticalGroup
    (
      exitLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
        .addComponent (createButton)
        .addComponent (cancelButton)
    );
    exitPanel.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Exit"));
    add (exitPanel);
    add (Box.createRigidArea (new Dimension (0, 10)));
    setQueueType ((KnownSimQueue) this.knownQueues.getSelectedItem ());
    ((AbstractTableModel) this.table.getModel ()).fireTableDataChanged ();
    pack ();
    setLocationRelativeTo (frame);
    this.knownQueues.addItemListener (this);
  }
  
  @Override
  public void itemStateChanged (final ItemEvent event)
  {
    if (event.getStateChange () == ItemEvent.SELECTED)
    {
      final KnownSimQueue item = (KnownSimQueue) event.getItem ();
      setQueueType (item);
    }
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // TABLE MODEL
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class QueuesTableModel
  extends AbstractTableModel
  {

    @Override
    public final int getRowCount ()
    {
      return JSimQueueCreationDialog.this.subQueues.size ();
    }

    @Override
    public final int getColumnCount ()
    {
      return 3;
    }

    @Override
    public final String getColumnName (final int column)
    {
      if (column < 0)
        return null;
      else if (column == 0)
        return "Type";
      else if (column == 1)
        return "Name";
      else if (column == 2)
        return "Composite";
      else
        return null;
    }

    @Override
    public final Class<?> getColumnClass (final int column)
    {
      if (column < 0)
        return null;
      else if (column == 0)
        return KnownSimQueue.class;
      else if (column == 1)
        return String.class;
      else if (column == 2)
        return Boolean.class;
      else
        return null;
    }

    @Override
    public final Object getValueAt (final int r, final int c)
    {
      if (r < 0 || r >= JSimQueueCreationDialog.this.subQueues.size ())
        return null;
      if (c < 0 || c >= 2)
        return null;
      final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.subQueues.iterator ();
      SimQueue q = iterator.next ();
      int i = 0;
      while (i < r)
      {
        q = iterator.next ();
        i++;
      }
      if (c == 0)
        return KnownSimQueue.valueOf (q);
      else if (c == 1)
        return q.toString ();
      else if (c == 2)
        return KnownSimQueue.valueOf (q).isComposite ();
      //else if (c == 2)
      //  return e.getObject ();
      //else if (c == 3)
      //  return e.getEventAction ();
      throw new RuntimeException ();
    }
    
  };

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF SERVERS TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class NumberOfServersTextFieldListener
  implements ActionListener, FocusListener
  {

    @Override
    public final void focusGained (final FocusEvent fe)
    {
    }

    @Override
    public final void focusLost (final FocusEvent fe)
    {
      actionPerformed ();
    }

    
    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      actionPerformed ();
    }
    
    private void actionPerformed ()
    {
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.knownQueues.getSelectedItem ();
      if (knownQueue == null)
      {
        JSimQueueCreationDialog.this.numberOfServersTextField.setText ("0");
        JSimQueueCreationDialog.this.parameters.numberOfServers = 0;
        return;
      }
      final String text = JSimQueueCreationDialog.this.numberOfServersTextField.getText ();
      if (text != null)
      {
        try
        {
          final int numberOfServersInt = Integer.parseInt (text);
          if (knownQueue.getNumberOfServersProfile ().isValidValue (numberOfServersInt))
            JSimQueueCreationDialog.this.parameters.numberOfServers = numberOfServersInt;
          return;
        }
        catch (NumberFormatException nfe)
        {
        }
      }
      if (! knownQueue.getNumberOfServersProfile ().isValidValue (JSimQueueCreationDialog.this.parameters.numberOfServers))
        JSimQueueCreationDialog.this.parameters.numberOfServers = knownQueue.getNumberOfServersProfile ().getDefValue ();
      JSimQueueCreationDialog.this.numberOfServersTextField.setText
        (Integer.toString (JSimQueueCreationDialog.this.parameters.numberOfServers));
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // BUFFER SIZE TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class BufferSizeTextFieldListener
  implements ActionListener, FocusListener
  {

    @Override
    public final void focusGained (final FocusEvent fe)
    {
    }

    @Override
    public final void focusLost (final FocusEvent fe)
    {
      actionPerformed ();
    }

    
    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      actionPerformed ();
    }
    
    private void actionPerformed ()
    {
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.knownQueues.getSelectedItem ();
      if (knownQueue == null)
      {
        JSimQueueCreationDialog.this.bufferSizeTextField.setText ("0");
        JSimQueueCreationDialog.this.parameters.bufferSize = 0;
        return;
      }
      final String text = JSimQueueCreationDialog.this.bufferSizeTextField.getText ();
      if (text != null)
      {
        try
        {
          final int bufferSizeInt = Integer.parseInt (text);
          if (knownQueue.getBufferSizeProfile ().isValidValue (bufferSizeInt))
            JSimQueueCreationDialog.this.parameters.bufferSize = bufferSizeInt;
          return;
        }
        catch (NumberFormatException nfe)
        {
        }
      }
      if (! knownQueue.getBufferSizeProfile ().isValidValue (JSimQueueCreationDialog.this.parameters.bufferSize))
        JSimQueueCreationDialog.this.parameters.bufferSize = knownQueue.getBufferSizeProfile ().getDefValue ();
      JSimQueueCreationDialog.this.bufferSizeTextField.setText
        (Integer.toString (JSimQueueCreationDialog.this.parameters.bufferSize));
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // WAIT/SERVICE TIME TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class WaitServiceTimeTextFieldListener
  implements ActionListener, FocusListener
  {

    @Override
    public final void focusGained (final FocusEvent fe)
    {
    }

    @Override
    public final void focusLost (final FocusEvent fe)
    {
      actionPerformed ();
    }

    
    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      actionPerformed ();
    }
    
    private void actionPerformed ()
    {
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.knownQueues.getSelectedItem ();
      if (knownQueue == null)
      {
        JSimQueueCreationDialog.this.waitServiceTimeTextField.setText ("NaN");
        JSimQueueCreationDialog.this.parameters.waitServiceTime = Double.NaN;
        return;
      }
      final String text = JSimQueueCreationDialog.this.waitServiceTimeTextField.getText ();
      if (text != null)
      {
        try
        {
          final double waitServiceTimeDouble = Double.parseDouble (text);
          if (knownQueue.getWaitServiceTimeProfile ().isValidValue (waitServiceTimeDouble))
            JSimQueueCreationDialog.this.parameters.waitServiceTime = waitServiceTimeDouble;
          return;
        }
        catch (NumberFormatException nfe)
        {
        }
      }
      if (! knownQueue.getWaitServiceTimeProfile ().isValidValue (JSimQueueCreationDialog.this.parameters.waitServiceTime))
        JSimQueueCreationDialog.this.parameters.waitServiceTime = knownQueue.getWaitServiceTimeProfile ().getDefValue ();
      JSimQueueCreationDialog.this.waitServiceTimeTextField.setText
        (Double.toString (JSimQueueCreationDialog.this.parameters.waitServiceTime));
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

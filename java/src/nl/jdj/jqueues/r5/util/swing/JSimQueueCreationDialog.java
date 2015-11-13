package nl.jdj.jqueues.r5.util.swing;

import java.awt.Color;
import java.awt.Dialog;
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
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import nl.jdj.jqueues.r5.SimQueue;
import nl.jdj.jqueues.r5.entity.queue.composite.BlackSimQueueComposite;
import nl.jdj.jsimulation.r4.SimEventList;

/**
 *
 */
public class JSimQueueCreationDialog
extends JDialog
implements ItemListener
{
  
  private final Frame frame;
  
  private final SimEventList eventList;
  
  private SimQueue createdQueue = null;
  
  public final SimQueue getCreatedQueue ()
  {
    return this.createdQueue;
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // PARAMETERS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final KnownSimQueue.Parameters parameters = new KnownSimQueue.Parameters ();

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // [USER-CONTROLLABLE] SWING COMPONENTS
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final JComboBox queueTypeComboBox;
  
  private final TableModel tableModel = new QueuesTableModel ();
  
  private final JTable table = new JTable (this.tableModel);
  
  final JButton moveUpSubQueueButton = new JButton ("Up");
  
  final JButton moveDownSubQueueButton = new JButton ("Down");
  
  final JButton addSubQueueButton = new JButton ("Add");
  
  final JButton insertSubQueueButton = new JButton ("Insert");
  
  final JButton deleteSubQueueButton = new JButton ("Delete");
  
  final JTextField numberOfServersTextField = new JTextField ("Number of Servers Value");
  
  final JTextField bufferSizeTextField = new JTextField ("Buffer Size Value");

  final JTextField waitServiceTimeTextField = new JTextField ("Wait/Service Time Value");
  
  final JTextField startTimeTextField = new JTextField ("Start Time Value");
  
  final JCheckBox queueAccessVacationCheckBox = new JCheckBox ();
    
  final JTextField serverAccessCreditsTextField = new JTextField ("Server-Access Credits Value");
  
  final JCheckBox onlyWaitingJobsCheckBox = new JCheckBox ();
  
  final JTextField feedbackProbabilityTextField = new JTextField ("Feedback Probability Value");
  
  final JTextField numberOfVisitsTextField = new JTextField ("Number of Visits Value");
  
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
    final SimQueue createdQueue = ((KnownSimQueue) this.queueTypeComboBox.getSelectedItem ()).newInstance (this.parameters);
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
    this.frame = frame;
    this.eventList = eventList;
    this.parameters.eventList = this.eventList;
    this.queueTypeComboBox = new JComboBox (KnownSimQueue.values ());
    if (queue != null)
      // XXX What if we have a null queue?
      this.queueTypeComboBox.setSelectedItem (KnownSimQueue.valueOf (queue));
    this.parameters.queues = new LinkedHashSet<> ();
    if (queue != null && (queue instanceof BlackSimQueueComposite))
      this.parameters.queues.addAll (((BlackSimQueueComposite) queue).getQueues ());
    getContentPane ().setLayout (new BoxLayout (getContentPane (), BoxLayout.PAGE_AXIS));
    add (Box.createRigidArea (new Dimension (0, 10)));
    this.queueTypeComboBox.setPreferredSize (new Dimension (200, 50));
    this.queueTypeComboBox.setMaximumSize (new Dimension (200, 50));
    this.queueTypeComboBox.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Select Queue Type"));
    add (this.queueTypeComboBox);
    add (Box.createRigidArea (new Dimension (0, 10)));
    
    final Box subQueuesBox = new Box (BoxLayout.PAGE_AXIS);
    
    if (this.queueTypeComboBox.getSelectedItem () != null
      && ((KnownSimQueue) this.queueTypeComboBox.getSelectedItem ()).isComposite ())
    {
      this.table.setBackground (getBackground ());
      this.table.setEnabled (true);
    }
    else
    {
      this.table.setBackground (new Color (255, 192, 192));
      this.table.setEnabled (false);
    }
    this.table.setSelectionMode (ListSelectionModel.SINGLE_SELECTION);
    this.table.setPreferredSize (new Dimension (200, 200));
    this.table.setMaximumSize (new Dimension (200, 200));
    final JComponent scrollPane = new JScrollPane (this.table);
    scrollPane.setPreferredSize (new Dimension (400, 200));
    scrollPane.setMaximumSize (new Dimension (400, 200));
    subQueuesBox.add (scrollPane);
    
    add (Box.createRigidArea (new Dimension (0, 10)));
    
    final Box tableButtonBox = new Box (BoxLayout.LINE_AXIS);
    
    this.moveUpSubQueueButton.addActionListener (new MoveUpQueueButtonListener ());
    this.moveDownSubQueueButton.addActionListener (new MoveDownQueueButtonListener ());
    this.addSubQueueButton.addActionListener (new AddSubQueueButtonListener ());
    this.insertSubQueueButton.addActionListener (new InsertSubQueueButtonListener ());
    this.deleteSubQueueButton.addActionListener (new DeleteSubQueueButtonListener ());
    tableButtonBox.add (this.moveUpSubQueueButton);
    tableButtonBox.add (this.moveDownSubQueueButton);
    tableButtonBox.add (this.addSubQueueButton);
    tableButtonBox.add (this.insertSubQueueButton);
    tableButtonBox.add (this.deleteSubQueueButton);

    subQueuesBox.add (tableButtonBox);
    
    add (subQueuesBox);
    subQueuesBox.setBorder
      (BorderFactory.createTitledBorder
      (BorderFactory.createLineBorder (Color.orange, 4, true), "Sub-Queues"));
    
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
    final JLabel otherParametersLabel = new JLabel ("Other");
    final JButton otherParametersButton = new JButton ("Edit");
    otherParametersButton.addActionListener (new OtherParametersButtonListener ());
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
            .addComponent (otherParametersLabel)
        )
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.LEADING)
            .addComponent (this.numberOfServersTextField)
            .addComponent (this.bufferSizeTextField)
            .addComponent (this.waitServiceTimeTextField)
            .addComponent (otherParametersButton)
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
        .addGroup
        (
          parametersLayout.createParallelGroup (GroupLayout.Alignment.BASELINE)
            .addComponent (otherParametersLabel)
            .addComponent (otherParametersButton)
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
    this.startTimeTextField.setText (Double.toString (this.parameters.startTime));
    final StartTimeTextFieldListener startTimeTextFieldListener = new StartTimeTextFieldListener ();
    this.startTimeTextField.addActionListener (startTimeTextFieldListener);
    this.startTimeTextField.addFocusListener (startTimeTextFieldListener);
    this.queueAccessVacationCheckBox.setSelected (queue == null ? false : queue.isQueueAccessVacation ());
    this.parameters.queueAccessVacation = (queue == null ? false : queue.isQueueAccessVacation ());
    this.queueAccessVacationCheckBox.addItemListener (new QueueAccessVacationCheckBoxListener ());
    final String initServerAccessCreditsString;
    if (queue == null || queue.getServerAccessCredits () == Integer.MAX_VALUE)
      initServerAccessCreditsString = "Infinity";
    else
      initServerAccessCreditsString = Integer.toString (queue.getServerAccessCredits ());
    this.serverAccessCreditsTextField.setText (initServerAccessCreditsString);
    final ServerAccessCreditsTextFieldListener serverAccessCreditsTextFieldListener = new ServerAccessCreditsTextFieldListener ();
    this.serverAccessCreditsTextField.addActionListener (serverAccessCreditsTextFieldListener);
    this.serverAccessCreditsTextField.addFocusListener (serverAccessCreditsTextFieldListener);
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
    setQueueType ((KnownSimQueue) this.queueTypeComboBox.getSelectedItem ());
    ((AbstractTableModel) this.table.getModel ()).fireTableDataChanged ();
    pack ();
    setLocationRelativeTo (frame);
    this.queueTypeComboBox.addItemListener (this);
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
      // JdJ: all non-null here!
      return JSimQueueCreationDialog.this.parameters.queues.size ();
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
      if (r < 0 || r >= JSimQueueCreationDialog.this.parameters.queues.size ())
        return null;
      if (c < 0 || c >= 2)
        return null;
      final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.parameters.queues.iterator ();
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
      throw new RuntimeException ();
    }
    
  };

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MOVE UP SUB-QUEUE BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class MoveUpQueueButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      final int selectedIndex = JSimQueueCreationDialog.this.table.getSelectedRow ();
      if (selectedIndex <= 0
        || JSimQueueCreationDialog.this.parameters.queues.size () <= 1)
        return;
      if (selectedIndex >= JSimQueueCreationDialog.this.parameters.queues.size ())
      {
        System.err.println ("Unexpected software problem with selected index of sub-queues table!");
        System.err.println ("-> Ignoring request!");
        return;
      }
      final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.parameters.queues.iterator ();
      final Set<SimQueue> newSubQueues = new LinkedHashSet<> ();
      for (int i = 0; i < selectedIndex - 1; i++)
        newSubQueues.add (iterator.next ());
      final SimQueue q1 = iterator.next ();
      final SimQueue q2 = iterator.next ();
      newSubQueues.add (q2);
      newSubQueues.add (q1);
      while (iterator.hasNext ())
        newSubQueues.add (iterator.next ());
      JSimQueueCreationDialog.this.parameters.queues = newSubQueues;
      ((AbstractTableModel) JSimQueueCreationDialog.this.tableModel).fireTableDataChanged ();
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // MOVE DOWN SUB-QUEUE BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class MoveDownQueueButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      final int selectedIndex = JSimQueueCreationDialog.this.table.getSelectedRow ();
      if (selectedIndex < 0
        || JSimQueueCreationDialog.this.parameters.queues.size () <= 1
        || selectedIndex == JSimQueueCreationDialog.this.parameters.queues.size () - 1)
        return;
      if (selectedIndex >= JSimQueueCreationDialog.this.parameters.queues.size ())
      {
        System.err.println ("Unexpected software problem with selected index of sub-queues table!");
        System.err.println ("-> Ignoring request!");
        return;
      }
      final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.parameters.queues.iterator ();
      final Set<SimQueue> newSubQueues = new LinkedHashSet<> ();
      for (int i = 0; i < selectedIndex; i++)
        newSubQueues.add (iterator.next ());
      final SimQueue q1 = iterator.next ();
      final SimQueue q2 = iterator.next ();
      newSubQueues.add (q2);
      newSubQueues.add (q1);
      while (iterator.hasNext ())
        newSubQueues.add (iterator.next ());
      JSimQueueCreationDialog.this.parameters.queues = newSubQueues;
      ((AbstractTableModel) JSimQueueCreationDialog.this.tableModel).fireTableDataChanged ();
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ADD SUB-QUEUE BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class AddSubQueueButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      final JSimQueueCreationDialog dialog =
        new JSimQueueCreationDialog (JSimQueueCreationDialog.this.frame,
          JSimQueueCreationDialog.this.eventList,
          null);
      dialog.setTitle ("Create New Sub-Queue");
      dialog.setVisible (true);
      final SimQueue createdSubQueue = dialog.getCreatedQueue ();
      if (createdSubQueue != null)
      {
        JSimQueueCreationDialog.this.parameters.queues.add (createdSubQueue);
        ((AbstractTableModel) JSimQueueCreationDialog.this.tableModel).fireTableDataChanged ();
      }
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // INSERT SUB-QUEUE BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class InsertSubQueueButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      final JSimQueueCreationDialog dialog =
        new JSimQueueCreationDialog (JSimQueueCreationDialog.this.frame,
          JSimQueueCreationDialog.this.eventList,
          null);
      dialog.setTitle ("Create New Sub-Queue");
      dialog.setVisible (true);
      final SimQueue createdSubQueue = dialog.getCreatedQueue ();
      if (createdSubQueue != null)
      {
        final int selectedIndex = JSimQueueCreationDialog.this.table.getSelectedRow ();
        if (selectedIndex >= JSimQueueCreationDialog.this.parameters.queues.size ())
        {
          System.err.println ("Unexpected software problem with selected index of sub-queues table!");
          System.err.println ("-> Ignoring request!");
          return;
        }
        final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.parameters.queues.iterator ();
        final Set<SimQueue> newSubQueues = new LinkedHashSet<> ();
        for (int i = 0; i < selectedIndex; i++)
          newSubQueues.add (iterator.next ());
        newSubQueues.add (createdSubQueue);
        while (iterator.hasNext ())
          newSubQueues.add (iterator.next ());
        JSimQueueCreationDialog.this.parameters.queues = newSubQueues;
        ((AbstractTableModel) JSimQueueCreationDialog.this.tableModel).fireTableDataChanged ();
      }
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // DELETE SUB-QUEUE BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class DeleteSubQueueButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      final int selectedIndex = JSimQueueCreationDialog.this.table.getSelectedRow ();
      if (selectedIndex < 0)
        return;
      if (selectedIndex >= JSimQueueCreationDialog.this.parameters.queues.size ())
      {
        System.err.println ("Unexpected software problem with selected index of sub-queues table!");
        System.err.println ("-> Ignoring request!");
        return;
      }
      final Iterator<SimQueue> iterator = JSimQueueCreationDialog.this.parameters.queues.iterator ();
      final Set<SimQueue> newSubQueues = new LinkedHashSet<> ();
      for (int i = 0; i < selectedIndex; i++)
        newSubQueues.add (iterator.next ());
      iterator.next ();
      while (iterator.hasNext ())
        newSubQueues.add (iterator.next ());
      JSimQueueCreationDialog.this.parameters.queues = newSubQueues;
      ((AbstractTableModel) JSimQueueCreationDialog.this.tableModel).fireTableDataChanged ();
    }
    
  }
  
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
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.queueTypeComboBox.getSelectedItem ();
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
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.queueTypeComboBox.getSelectedItem ();
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
      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.queueTypeComboBox.getSelectedItem ();
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
  // START TIME TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class StartTimeTextFieldListener
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
      final String text = JSimQueueCreationDialog.this.startTimeTextField.getText ();
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
      JSimQueueCreationDialog.this.startTimeTextField.setText
        (Double.toString (JSimQueueCreationDialog.this.parameters.startTime));
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // QUEUE-ACCESS VACTION CHECKBOX LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final class QueueAccessVacationCheckBoxListener
  implements ItemListener
  {
    
    @Override
    public final void itemStateChanged (final ItemEvent ie)
    {
      JSimQueueCreationDialog.this.parameters.queueAccessVacation = (ie.getStateChange () == ItemEvent.SELECTED);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // SERVER_ACCESS CREDITS TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class ServerAccessCreditsTextFieldListener
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
      final String text = JSimQueueCreationDialog.this.serverAccessCreditsTextField.getText ();
      if (text != null)
      {
        if (text.trim ().startsWith ("inf") || text.trim ().startsWith ("Inf"))
        {
          JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText ("Infinity");
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
                JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText ("Infinity");
              else
                JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText (Integer.toString
                  (JSimQueueCreationDialog.this.parameters.serverAccessCredits));        
            }
            else
            {
              JSimQueueCreationDialog.this.parameters.serverAccessCredits = serverAccessCreditsInt;
              if (serverAccessCreditsInt == Integer.MAX_VALUE)
                JSimQueueCreationDialog.this.numberOfServersTextField.setText ("Infinity");
            }
          }
          catch (NumberFormatException nfe)
          {
            if (JSimQueueCreationDialog.this.parameters.serverAccessCredits == Integer.MAX_VALUE)
              JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText ("Infinity");
            else
              JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText
                (Integer.toString (JSimQueueCreationDialog.this.parameters.serverAccessCredits));        
          }
        }
      }
      else
      {
        if (JSimQueueCreationDialog.this.parameters.serverAccessCredits == Integer.MAX_VALUE)
          JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText ("Infinity");
        else
          JSimQueueCreationDialog.this.serverAccessCreditsTextField.setText
            (Integer.toString (JSimQueueCreationDialog.this.parameters.serverAccessCredits));
      }
    }

  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OTHER PARAMETERS DIALOG
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private OtherParametersDialog otherParametersDialog = null;
  
  private final class OtherParametersDialog
  extends JDialog
  {

    public OtherParametersDialog ()
    {
      super (JSimQueueCreationDialog.this, "Other Parameters", true);
      final JLabel onlyWaitingJobsLabel = new JLabel ("Only Waiting Jobs");
      JSimQueueCreationDialog.this.onlyWaitingJobsCheckBox.setSelected (JSimQueueCreationDialog.this.parameters.onlyWaitingJobs);
      JSimQueueCreationDialog.this.onlyWaitingJobsCheckBox.addItemListener (new OnlyWaitingJobsBoxCheckBoxListener ());
      final JLabel feedbackProbabilityLabel = new JLabel ("Feedback Probability");
      JSimQueueCreationDialog.this.feedbackProbabilityTextField.setText
        (Double.toString (JSimQueueCreationDialog.this.parameters.feedbackProbability));
      final FeedbackProbabilityTextFieldListener feedbackProbabilityTextFieldListener
        = new FeedbackProbabilityTextFieldListener ();
      JSimQueueCreationDialog.this.feedbackProbabilityTextField.addActionListener (feedbackProbabilityTextFieldListener);
      JSimQueueCreationDialog.this.feedbackProbabilityTextField.addFocusListener (feedbackProbabilityTextFieldListener);
      final JLabel numberOfVisitsLabel = new JLabel ("Number of Visits");
      JSimQueueCreationDialog.this.numberOfVisitsTextField.setText
        (Integer.toString (JSimQueueCreationDialog.this.parameters.numberOfVisits));
      final NumberOfVisitsTextFieldListener numberOfVisitsTextFieldListener = new NumberOfVisitsTextFieldListener ();
      JSimQueueCreationDialog.this.numberOfVisitsTextField.addActionListener (numberOfVisitsTextFieldListener);
      JSimQueueCreationDialog.this.numberOfVisitsTextField.addFocusListener (numberOfVisitsTextFieldListener);
      final JPanel jPanel = new JPanel ();
      getContentPane ().add (jPanel);
      jPanel.setBorder
        (BorderFactory.createTitledBorder
          (BorderFactory.createLineBorder (Color.orange, 4, true), "Other Parameters"));
      final GroupLayout layout = new GroupLayout (jPanel);
      jPanel.setLayout (layout);
      layout.setAutoCreateGaps (true);
      layout.setAutoCreateContainerGaps (true);
      layout.setHorizontalGroup
        (layout.createSequentialGroup ()
          .addGroup
            (layout.createParallelGroup (GroupLayout.Alignment.LEADING)
              .addComponent (onlyWaitingJobsLabel)
              .addComponent (feedbackProbabilityLabel)
              .addComponent (numberOfVisitsLabel)
            )
          .addGroup
            (layout.createParallelGroup (GroupLayout.Alignment.LEADING)
              .addComponent (JSimQueueCreationDialog.this.onlyWaitingJobsCheckBox)
              .addComponent (JSimQueueCreationDialog.this.feedbackProbabilityTextField)
              .addComponent (JSimQueueCreationDialog.this.numberOfVisitsTextField)
            )
        );
      layout.setVerticalGroup
        (layout.createSequentialGroup ()
          .addGroup
            (layout.createParallelGroup (GroupLayout.Alignment.BASELINE)
              .addComponent (onlyWaitingJobsLabel)
              .addComponent (JSimQueueCreationDialog.this.onlyWaitingJobsCheckBox)
            )
          .addGroup
            (layout.createParallelGroup (GroupLayout.Alignment.BASELINE)
              .addComponent (feedbackProbabilityLabel)
              .addComponent (JSimQueueCreationDialog.this.feedbackProbabilityTextField)
            )
          .addGroup
            (layout.createParallelGroup (GroupLayout.Alignment.BASELINE)
              .addComponent (numberOfVisitsLabel)
              .addComponent (JSimQueueCreationDialog.this.numberOfVisitsTextField)
            )
        );
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // OTHER PARAMETERS BUTTON LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final class OtherParametersButtonListener
  extends AbstractAction
  {

    @Override
    public final void actionPerformed (final ActionEvent ae)
    {
      if (JSimQueueCreationDialog.this.otherParametersDialog == null)
        JSimQueueCreationDialog.this.otherParametersDialog = new OtherParametersDialog ();
      JSimQueueCreationDialog.this.otherParametersDialog.pack ();
      JSimQueueCreationDialog.this.otherParametersDialog.setVisible (true);
    }
    
  }
    
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // ONLY WAITING JOBS CHECKBOX LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
  private final class OnlyWaitingJobsBoxCheckBoxListener
  implements ItemListener
  {
    
    @Override
    public final void itemStateChanged (final ItemEvent ie)
    {
      JSimQueueCreationDialog.this.parameters.onlyWaitingJobs = (ie.getStateChange () == ItemEvent.SELECTED);
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // FEEDBACK PROBABILITY TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class FeedbackProbabilityTextFieldListener
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
      final String text = JSimQueueCreationDialog.this.feedbackProbabilityTextField.getText ();
      if (text != null)
      {
        final double feedbackProbabilityDouble;
        try
        {
          feedbackProbabilityDouble = Double.parseDouble (text);
          if (feedbackProbabilityDouble >= 0.0 && feedbackProbabilityDouble <= 1.0)
          {
            JSimQueueCreationDialog.this.parameters.feedbackProbability = feedbackProbabilityDouble;
            return;
          }
        }
        catch (NumberFormatException nfe)
        {
        }
      }
      JSimQueueCreationDialog.this.feedbackProbabilityTextField.setText
        (Double.toString (JSimQueueCreationDialog.this.parameters.feedbackProbability));
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // NUMBER OF VISITS TEXTFIELD LISTENER
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private final class NumberOfVisitsTextFieldListener
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
//      final KnownSimQueue knownQueue = (KnownSimQueue) JSimQueueCreationDialog.this.queueTypeComboBox.getSelectedItem ();
//      if (knownQueue == null)
//      {
//        JSimQueueCreationDialog.this.numberOfServersTextField.setText ("0");
//        JSimQueueCreationDialog.this.parameters.numberOfServers = 0;
//        return;
//      }
      final String text = JSimQueueCreationDialog.this.numberOfVisitsTextField.getText ();
      if (text != null)
      {
        try
        {
          final int numberOfVisitsInt = Integer.parseInt (text);
//          if (knownQueue.getNumberOfVisitsProfile ().isValidValue (numberOfVisitsInt))
          if (numberOfVisitsInt >= 0)
          {
            JSimQueueCreationDialog.this.parameters.numberOfVisits = numberOfVisitsInt;
            return;
          }
        }
        catch (NumberFormatException nfe)
        {
        }
      }
      //if (! knownQueue.getNumberOfServersProfile ().isValidValue (JSimQueueCreationDialog.this.parameters.numberOfServers))
      //  JSimQueueCreationDialog.this.parameters.numberOfServers = knownQueue.getNumberOfServersProfile ().getDefValue ();
      JSimQueueCreationDialog.this.numberOfVisitsTextField.setText
        (Integer.toString (JSimQueueCreationDialog.this.parameters.numberOfVisits));
    }
    
  }
  
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // END OF FILE
  //
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

}

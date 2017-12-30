package boeken.label;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import table.*;

/**
 * Frame to show, insert and update records in the label table in schema boeken.
 */
public class EditLabel extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditLabel.class.getCanonicalName() );

    private final JInternalFrame thisFrame;
    private LabelTableModel labelTableModel;
    private TableSorter labelTableSorter;

    public EditLabel(final Connection connection, int x, int y ) {
        super("Edit label", true, true, true, true);
        this.thisFrame = this;

        // Get the container from the internal frame
        final Container container = getContentPane();

        // Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

	constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Label Filter:" ), constraints );
	final JTextField labelFilterTextField = new JTextField( 10 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( labelFilterTextField, constraints );

	labelFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            labelTableSorter.clearSortingState();
            // Setup the label table
            labelTableModel.setupLabelTableModel( labelFilterTextField.getText() );
        } );

	// Create label table from label table model
	labelTableModel = new LabelTableModel( connection, thisFrame );
	labelTableSorter = new TableSorter( labelTableModel );
	final JTable labelTable = new JTable( labelTableSorter );
	labelTableSorter.setTableHeader( labelTable.getTableHeader( ) );
	// labelTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	labelTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	labelTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	labelTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // label

	// Set vertical size just enough for 20 entries
	labelTable.setPreferredScrollableViewportSize( new Dimension( 250, 320 ) );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        constraints.insets = new Insets( 5, 20, 5, 20 );
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( labelTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deleteLabelButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel labelListSelectionModel = labelTable.getSelectionModel( );

	class LabelListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( labelListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteLabelButton.setEnabled( false );
		    return;
		}

		int viewRow = labelListSelectionModel.getMinSelectionIndex( );
		selectedRow = labelTableSorter.modelIndex( viewRow );
		deleteLabelButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add labelListSelectionListener object to the selection model of the musici table
	final LabelListSelectionListener labelListSelectionListener = new LabelListSelectionListener( );
	labelListSelectionModel.addListSelectionListener( labelListSelectionListener );

	// Class to handle button actions: uses labelListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "add" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( label_id ) FROM label" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for label_id in label" );
			    return;
			}
			int labelId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO label SET label_id = " + labelId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in label" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog(thisFrame,
                                                      "SQL exception: " + sqlException.getMessage(),
                                                      "EditLabel SQL exception",
                                                      JOptionPane.ERROR_MESSAGE);
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = labelListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( thisFrame,
						       "Geen Label geselecteerd",
						       "Edit label error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected label id
		    final int selectedLabelId = labelTableModel.getLabelId( selectedRow );

		    // Check if label has been selected
		    if ( selectedLabelId == 0 ) {
			JOptionPane.showMessageDialog( thisFrame,
						       "Geen label geselecteerd",
						       "Edit label error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the label
		    String labelString = labelTableModel.getLabelString( selectedRow );

		    // Replace null or empty string by single space for messages
		    if ( ( labelString == null ) || ( labelString.length( ) == 0 ) ) labelString = " ";

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if label ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT label_id FROM boek WHERE label_id = " +
							selectedLabelId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( thisFrame,
							       "Tabel boek heeft nog verwijzing naar '" + labelString + "'",
							       "Edit label error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog(thisFrame,
                                                          "SQL exception in select: " + sqlException.getMessage(),
                                                          "EditLabel SQL exception",
                                                          JOptionPane.ERROR_MESSAGE);
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result = JOptionPane.showConfirmDialog( thisFrame,
                                                                    "Delete '" + labelString + "' ?",
                                                                    "Delete label record",
                                                                    JOptionPane.YES_NO_OPTION,
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM label WHERE label_id = " + selectedLabelId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				final String errorString = "Could not delete record with label_id  = " + selectedLabelId + " in label";
				JOptionPane.showMessageDialog( thisFrame,
							       errorString,
							       "Edit label error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog(thisFrame,
                                                          "SQL exception in delete: " + sqlException.getMessage(),
                                                          "EditLabel SQL exception",
                                                          JOptionPane.ERROR_MESSAGE);
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the table model again
		labelTableSorter.clearSortingState( );
		labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton addLabelButton = new JButton( "Add" );
	addLabelButton.setActionCommand( "add" );
	addLabelButton.addActionListener( buttonActionListener );
	buttonPanel.add( addLabelButton );

	deleteLabelButton.setActionCommand( "delete" );
	deleteLabelButton.setEnabled( false );
	deleteLabelButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteLabelButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.insets = new Insets( 5, 20, 20, 20 );
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( buttonPanel, constraints );

        setSize( 310, 500 );
        setLocation(x, y);
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}

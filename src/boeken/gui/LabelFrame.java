// frame to show and select records from label

package boeken.gui;

import java.sql.Connection; 
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.*;
import java.util.logging.*;

import table.*;


public class LabelFrame {
    final Logger logger = Logger.getLogger( "boeken.gui.LabelFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Label" );

    LabelTableModel labelTableModel;
    TableSorter labelTableSorter;
    JTable labelTable;


    public LabelFrame( final Connection connection ) {
	this.connection = connection;

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 0, 0, 10, 10 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Label Filter:" ), constraints );
	final JTextField labelFilterTextField = new JTextField( 10 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( labelFilterTextField, constraints );

	class LabelFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the label table
		labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
	    }
	}
	labelFilterTextField.addActionListener( new LabelFilterActionListener( ) );


	// Create label table from title table model
	labelTableModel = new LabelTableModel( connection );
	labelTableSorter = new TableSorter( labelTableModel );
	labelTable = new JTable( labelTableSorter );
	labelTableSorter.setTableHeader( labelTable.getTableHeader( ) );
	// labelTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	labelTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	labelTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	labelTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // label

	// Set vertical size just enough for 20 entries
	labelTable.setPreferredScrollableViewportSize( new Dimension( 250, 320 ) );

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 5;
	constraints.insets = new Insets( 10, 0, 10, 10 );
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
	    int selectedRow = -1;

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

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add labelListSelectionListener object to the selection model of the musici table
	final LabelListSelectionListener labelListSelectionListener = new LabelListSelectionListener( );
	labelListSelectionModel.addListSelectionListener( labelListSelectionListener );

	// Class to handle button actions: uses labelListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
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
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = labelListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen Label geselecteerd",
						       "Label frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected label id
		    final int selectedLabelId = labelTableModel.getLabelId( selectedRow );

		    // Check if label has been selected
		    if ( selectedLabelId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen label geselecteerd",
						       "Label frame error",
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
				JOptionPane.showMessageDialog( frame,
							       "Tabel boek heeft nog verwijzing naar '" +
							       labelString + "'",
							       "Label frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + labelString + "' ?",
							   "Delete Label record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM label";
			deleteString += " WHERE label_id = " + selectedLabelId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with label_id  = " +
						       selectedLabelId + " in label" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Label record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
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
	constraints.gridy = 5;
	constraints.gridwidth = 3;
	constraints.insets = new Insets( 10, 0, 0, 10 );
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	frame.setSize( 360, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}

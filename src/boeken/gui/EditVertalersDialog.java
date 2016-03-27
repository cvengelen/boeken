// Dialog for inserting or updating vertalers, vertalers_persoon and persoon

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;


public class EditVertalersDialog {
    final Logger logger = Logger.getLogger( "boeken.gui.EditVertalersDialog" );

    Connection connection;
    Object parentObject;
    JDialog dialog;

    int vertalersId;
    String vertalersString;
    JTextField vertalersTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    VertalersPersoonTableModel vertalersPersoonTableModel;
    JTable vertalersPersoonTable;

    int nUpdate = 0;
    final String insertVertalersActionCommand = "insertVertalers";
    final String updateVertalersActionCommand = "updateVertalers";

    // Constructor
    public EditVertalersDialog( Connection connection,
				Object     parentObject,
				String     vertalersString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.vertalersString = vertalersString;

	// Setup vertalers_persoon table
	vertalersPersoonTableModel = new VertalersPersoonTableModel( connection );
	vertalersPersoonTable = new JTable( vertalersPersoonTableModel );

	setupVertalersDialog( "Insert vertalers: " + vertalersString, "Insert",
			      insertVertalersActionCommand );
    }

    // Constructor
    public EditVertalersDialog( Connection connection,
				Object     parentObject,
				int        vertalersId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.vertalersId = vertalersId;

	// Setup vertalers_persoon table
	vertalersPersoonTableModel = new VertalersPersoonTableModel( connection, vertalersId );
	vertalersPersoonTable = new JTable( vertalersPersoonTableModel );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT vertalers FROM vertalers WHERE vertalers_id = " +
							  vertalersId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for vertalers_id " +
			       vertalersId + " in vertalers" );
		return;
	    }
	    vertalersString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupVertalersDialog( "Edit vertalers: " + vertalersString, "Update",
			      updateVertalersActionCommand );
    }

    // Setup vertalers dialog
    void setupVertalersDialog( String dialogTitle,
			       String editVertalersButtonText,
			       String editVertalersButtonActionCommand ) {
	// Create modal dialog for editing vertalers
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    logger.severe( "Unexpected parent object class: " +
			   parentObject.getClass( ).getName( ) );
	    return;
	}

	// Set grid bag layout manager
	Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );


	////////////////////////////////////////////////
	// Vertalers text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Vertalers:" ), constraints );

	vertalersTextField = new JTextField( vertalersString, 30 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 4;
	container.add( vertalersTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Filter, Add Persoon button
	////////////////////////////////////////////////

	// Setup a JComboBox with the results of the query on persoon
	persoonComboBox = new PersoonComboBox( connection, dialog );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Persoon:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( persoonComboBox, constraints );

	class SelectPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a persoon record needs to be inserted
		if ( persoonComboBox.newPersoonSelected( ) ) {
		    // Insert new persoon record
		    EditPersoonDialog editPersoonDialog =
			new EditPersoonDialog( connection, dialog, persoonFilterString );

		    // Check if a new persoon record has been inserted
		    if ( editPersoonDialog.persoonUpdated( ) ) {
			// Get the id of the new persoon record
			int selectedPersoonId = editPersoonDialog.getPersoonId( );

			// Setup the persoon combo box again
			persoonComboBox.setupPersoonComboBox( selectedPersoonId );
		    }
		}
	    }
	}
	persoonComboBox.addActionListener( new SelectPersoonActionListener( ) );

	JButton addPersoonInTableButton = new JButton( "Add" );
	addPersoonInTableButton.setActionCommand( "addPersoonInTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( addPersoonInTableButton, constraints );

	class AddPersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Vertalers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		vertalersPersoonTableModel.addRow( selectedPersoonId,
						   persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInTableButton.addActionListener( new AddPersoonInTableActionListener( ) );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterPersoonButton, constraints );

	class FilterPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		persoonFilterString = persoonComboBox.filterPersoonComboBox( );
	    }
	}
	filterPersoonButton.addActionListener( new FilterPersoonActionListener( ) );


	////////////////////////////////////////////////
	// Vertalers Table, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with vertalers_persoon
	vertalersPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	vertalersPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	vertalersPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 350 );

	// Set vertical size just enough for 5 entries
	vertalersPersoonTable.setPreferredScrollableViewportSize( new Dimension( 350, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( vertalersPersoonTable ), constraints );

	// Define Replace button next to table
	final JButton replacePersoonInTableButton = new JButton( "Replace" );
	replacePersoonInTableButton.setActionCommand( "replacePersoonInTable" );
	replacePersoonInTableButton.setEnabled( false );
	constraints.gridheight = 1;
	constraints.gridwidth = 2;
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( replacePersoonInTableButton, constraints );

	// Define Remove button next to table
	final JButton removePersoonFromTableButton = new JButton( "Remove" );
	removePersoonFromTableButton.setActionCommand( "removePersoonFromTable" );
	removePersoonFromTableButton.setEnabled( false );
	constraints.gridy = 3;
	container.add( removePersoonFromTableButton, constraints );


	// Get the selection model related to the vertalers table
	final ListSelectionModel persoonListSelectionModel = vertalersPersoonTable.getSelectionModel( );

	// Class to handle row selection in vertalers table
	class PersoonListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( persoonListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    replacePersoonInTableButton.setEnabled( false );
		    removePersoonFromTableButton.setEnabled( false );
		    return;
		}

		selectedRow = persoonListSelectionModel.getMinSelectionIndex( );
		replacePersoonInTableButton.setEnabled( true );
		removePersoonFromTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add persoonListSelectionListener object to the selection model of the vertalers table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit vertalers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit vertalers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		vertalersPersoonTableModel.replaceRow( selectedRow,
						       selectedPersoonId,
						       persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	replacePersoonInTableButton.addActionListener( new ReplacePersoonInTableActionListener( ) );


	// Class to handle Remove button: uses persoonListSelectionListener
	class RemovePersoonFromTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit vertalers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		vertalersPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTableButton.addActionListener( new RemovePersoonFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditVertalersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertVertalersActionCommand ) ) {
		    insertVertalers( );
		} else if ( ae.getActionCommand( ).equals( updateVertalersActionCommand ) ) {
		    updateVertalers( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editVertalersButton = new JButton( editVertalersButtonText );
	editVertalersButton.setActionCommand( editVertalersButtonActionCommand );
	editVertalersButton.addActionListener( new EditVertalersActionListener( ) );
	buttonPanel.add( editVertalersButton );

	JButton cancelVertalersButton = new JButton( "Cancel" );
	cancelVertalersButton.setActionCommand( "cancelVertalers" );
	cancelVertalersButton.addActionListener( new EditVertalersActionListener( ) );
	buttonPanel.add( cancelVertalersButton );

	constraints.gridx = 1;
	constraints.gridy = 4;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 650, 300 );
	dialog.setVisible( true );
    }

    void insertVertalers( ) {
	vertalersString = vertalersTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( vertalers_id ) FROM vertalers" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for vertalers_id in vertalers" );
		dialog.setVisible( false );
		return;
	    }
	    vertalersId = resultSet.getInt( 1 ) + 1;

	    // Insert a new row in the vertalers table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO vertalers SET " +
					       "vertalers_id = " + vertalersId +
					       ",  vertalers = '" + vertalersString + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in vertalers" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the vertalers_persoon table using the new vertalers ID
	    vertalersPersoonTableModel.insertTable( vertalersId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateVertalers( ) {
	vertalersString = vertalersTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );

	    // Update the vertalers table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE vertalers SET vertalers = '" + vertalersString +
					       "' WHERE vertalers_id = " + vertalersId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in vertalers" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Update the rows in the vertalers_persoon table
	vertalersPersoonTableModel.updateTable( );
    }

    public boolean vertalersUpdated( ) { return nUpdate > 0; }

    public String getVertalers( ) { return vertalersString; }

    public int getVertalersId( ) { return vertalersId; }
}

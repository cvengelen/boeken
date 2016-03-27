// Dialog for inserting or updating editors, editors_persoon and persoon

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


public class EditEditorsDialog {
    final Logger logger = Logger.getLogger( "boeken.gui.EditEditorsDialog" );

    Connection connection;
    Object parentObject;
    JDialog dialog;

    int editorsId;
    String editorsString;
    JTextField editorsTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    EditorsPersoonTableModel editorsPersoonTableModel;
    JTable editorsPersoonTable;

    int nUpdate = 0;
    final String insertEditorsActionCommand = "insertEditors";
    final String updateEditorsActionCommand = "updateEditors";

    // Constructor
    public EditEditorsDialog( Connection connection,
			      Object     parentObject,
			      String     editorsString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.editorsString = editorsString;

	// Setup editors_persoon table
	editorsPersoonTableModel = new EditorsPersoonTableModel( connection );
	editorsPersoonTable = new JTable( editorsPersoonTableModel );

	setupEditorsDialog( "Insert editors: " + editorsString, "Insert",
			     insertEditorsActionCommand );
    }

    // Constructor
    public EditEditorsDialog( Connection connection,
			      Object     parentObject,
			      int        editorsId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.editorsId = editorsId;

	// Setup editors_persoon table
	editorsPersoonTableModel = new EditorsPersoonTableModel( connection, editorsId );
	editorsPersoonTable = new JTable( editorsPersoonTableModel );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT editors FROM editors WHERE editors_id = " +
							  editorsId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for editors_id " +
			       editorsId + " in editors" );
		return;
	    }
	    editorsString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupEditorsDialog( "Update editors: " + editorsString, "Update",
			    updateEditorsActionCommand );
    }

    // Setup editors dialog
    void setupEditorsDialog( String dialogTitle,
			     String editEditorsButtonText,
			     String editEditorsButtonActionCommand ) {
	// Create modal dialog for editing editors
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    logger.severe( "unexpected parent object class: " +
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
	// Editors text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Editors:" ), constraints );

	editorsTextField = new JTextField( editorsString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 4;
	container.add( editorsTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Add, Filter Persoon button
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
						   "Edit Editors error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		editorsPersoonTableModel.addRow( selectedPersoonId,
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
	// Editors Table, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with editors_persoon
	editorsPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	editorsPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	editorsPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 350 );

	// Set vertical size just enough for 5 entries
	editorsPersoonTable.setPreferredScrollableViewportSize( new Dimension( 350, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( editorsPersoonTable ), constraints );

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


	// Get the selection model related to the editors table
	final ListSelectionModel persoonListSelectionModel = editorsPersoonTable.getSelectionModel( );

	// Class to handle row selection in editors table
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

	// Add persoonListSelectionListener object to the selection model of the editors table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Editors error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Editors error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		editorsPersoonTableModel.replaceRow( selectedRow,
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
						   "Edit Editors error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		editorsPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTableButton.addActionListener( new RemovePersoonFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditEditorsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertEditorsActionCommand ) ) {
		    insertEditors( );
		} else if ( ae.getActionCommand( ).equals( updateEditorsActionCommand ) ) {
		    updateEditors( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editEditorsButton = new JButton( editEditorsButtonText );
	editEditorsButton.setActionCommand( editEditorsButtonActionCommand );
	editEditorsButton.addActionListener( new EditEditorsActionListener( ) );
	buttonPanel.add( editEditorsButton );

	JButton cancelEditorsButton = new JButton( "Cancel" );
	cancelEditorsButton.setActionCommand( "cancelEditors" );
	cancelEditorsButton.addActionListener( new EditEditorsActionListener( ) );
	buttonPanel.add( cancelEditorsButton );

	constraints.gridx = 1;
	constraints.gridy = 4;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 650, 300 );
	dialog.setVisible( true );
    }

    void insertEditors( ) {
	editorsString = editorsTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( editors_id ) FROM editors" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for editors_id in editors" );
		dialog.setVisible( false );
		return;
	    }
	    editorsId = resultSet.getInt( 1 ) + 1;

	    // Insert a new row in the editors table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO editors SET " +
					       "editors_id = " + editorsId +
					       ",  editors = '" + editorsString + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in editors" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the editors_persoon table using the new editors ID
	    editorsPersoonTableModel.insertTable( editorsId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateEditors( ) {
	editorsString = editorsTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );

	    // Update the editors table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE editors SET editors = '" + editorsString +
					       "' WHERE editors_id = " + editorsId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in editors" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Update the rows in the editors_persoon table
	editorsPersoonTableModel.updateTable( );
    }

    public boolean editorsUpdated( ) { return nUpdate > 0; }

    public String getEditors( ) { return editorsString; }

    public int getEditorsId( ) { return editorsId; }
}

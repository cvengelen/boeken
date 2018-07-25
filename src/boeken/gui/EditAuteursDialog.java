// Dialog for inserting or updating auteurs, auteurs_persoon and persoon

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


public class EditAuteursDialog {
    final Logger logger = Logger.getLogger( "boeken.gui.EditAuteursDialog" );

    private Connection connection;
    Object parentObject;
    JDialog dialog;

    int auteursId;
    String auteursString;
    JTextField auteursTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    AuteursPersoonTableModel auteursPersoonTableModel;
    JTable auteursPersoonTable;

    int nUpdate = 0;
    final String insertAuteursActionCommand = "insertAuteurs";
    final String updateAuteursActionCommand = "updateAuteurs";


    // Constructor
    public EditAuteursDialog( Connection connection,
			      Object     parentObject,
			      String     auteursString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.auteursString = auteursString;

	// Setup auteurs_persoon table
	auteursPersoonTableModel = new AuteursPersoonTableModel( connection );
	auteursPersoonTable = new JTable( auteursPersoonTableModel );

	String dialogTitle = "Insert auteurs";
	if ( auteursString != null ) dialogTitle += " : " + auteursString;
	setupAuteursDialog( dialogTitle, "Insert",
			     insertAuteursActionCommand );
    }


    // Constructor
    public EditAuteursDialog( Connection connection,
			      Object     parentObject,
			      int        auteursId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.auteursId = auteursId;

	// Setup auteurs_persoon table
	auteursPersoonTableModel = new AuteursPersoonTableModel( connection, auteursId );
	auteursPersoonTable = new JTable( auteursPersoonTableModel );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT auteurs FROM auteurs WHERE auteurs_id = " +
							  auteursId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for auteurs_id " +
			       auteursId + " in auteurs" );
		return;
	    }
	    auteursString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupAuteursDialog( "Edit auteurs: " + auteursString, "Update",
			    updateAuteursActionCommand );
    }


    // Setup auteurs dialog
    void setupAuteursDialog( String dialogTitle,
			     String editAuteursButtonText,
			     String editAuteursButtonActionCommand ) {
	// Create modal dialog for editing auteurs
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    logger.severe( "EditAuteursDialog.setupAuteursDialog, " +
			   "unexpected parent object class: " +
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
	// Auteurs text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Auteurs:" ), constraints );

	auteursTextField = new JTextField( auteursString, 40 );
	constraints.gridwidth = 4;
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( auteursTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Add, Filter, Persoon button
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
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Auteurs error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		auteursPersoonTableModel.addRow( selectedPersoonId,
						 persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInTableButton.addActionListener( new AddPersoonInTableActionListener( ) );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterPersoonButton, constraints );

	class FilterPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		persoonFilterString = persoonComboBox.filterPersoonComboBox( );
	    }
	}
	filterPersoonButton.addActionListener( new FilterPersoonActionListener( ) );


	////////////////////////////////////////////////
	// Auteurs Table, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with auteurs_persoon
	auteursPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	auteursPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	auteursPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 350 );

	// Set vertical size just enough for 5 entries
	auteursPersoonTable.setPreferredScrollableViewportSize( new Dimension( 350, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( auteursPersoonTable ), constraints );

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


	// Get the selection model related to the auteurs table
	final ListSelectionModel persoonListSelectionModel = auteursPersoonTable.getSelectionModel( );

	// Class to handle row selection in auteurs table
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

	// Add persoonListSelectionListener object to the selection model of the auteurs table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Auteurs error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Auteurs error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		auteursPersoonTableModel.replaceRow( selectedRow,
						     selectedPersoonId,
						     persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	replacePersoonInTableButton.addActionListener( new ReplacePersoonInTableActionListener( ) );


	// Class to handle Remove button: uses persoonListSelectionListener
	class RemovePersoonFromTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Auteurs error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		auteursPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTableButton.addActionListener( new RemovePersoonFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditAuteursActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( insertAuteursActionCommand ) ) {
		    insertAuteurs( );
		} else if ( actionEvent.getActionCommand( ).equals( updateAuteursActionCommand ) ) {
		    updateAuteurs( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editAuteursButton = new JButton( editAuteursButtonText );
	editAuteursButton.setActionCommand( editAuteursButtonActionCommand );
	editAuteursButton.addActionListener( new EditAuteursActionListener( ) );
	buttonPanel.add( editAuteursButton );

	JButton cancelAuteursButton = new JButton( "Cancel" );
	cancelAuteursButton.setActionCommand( "cancelAuteurs" );
	cancelAuteursButton.addActionListener( new EditAuteursActionListener( ) );
	buttonPanel.add( cancelAuteursButton );

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 5;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 650, 300 );
	dialog.setVisible( true );
    }

    void insertAuteurs( ) {
	auteursString = auteursTextField.getText( );

	// logger.info( "EditAuteursDialog.insertAuteurs, auteurs: " + auteursString );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( auteurs_id ) FROM auteurs" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for auteurs_id in auteurs" );
		dialog.setVisible( false );
		return;
	    }
	    auteursId = resultSet.getInt( 1 ) + 1;

	    // Insert a new row in the auteurs table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO auteurs SET " +
					       "auteurs_id = " + auteursId +
					       ",  auteurs = '" + auteursString + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in auteurs" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the auteurs_persoon table using the new auteurs ID
	    auteursPersoonTableModel.insertTable( auteursId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateAuteurs( ) {
	auteursString = auteursTextField.getText( );

	// logger.info( "EditAuteursDialog.updateAuteurs, auteurs: " +
	//	     auteursString + ", with id " + auteursId );

	try {
	    Statement statement = connection.createStatement( );

	    // Update the auteurs table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE auteurs SET auteurs = '" + auteursString +
					       "' WHERE auteurs_id = " + auteursId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in auteurs" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Update the rows in the auteurs_persoon table
	auteursPersoonTableModel.updateTable( );
    }

    public boolean auteursUpdated( ) { return nUpdate > 0; }

    public String getAuteursString( ) { return auteursString; }

    public int getAuteursId( ) { return auteursId; }
}

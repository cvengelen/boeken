// Dialog to insert or update a record in titel

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class EditTitelDialog {
    final private Logger logger = Logger.getLogger( "boeken.gui.EditTitelDialog" );

    Connection connection;
    private JFrame parentFrame;
    JDialog dialog;

    private TitelKey titelKey = new TitelKey( );

    private String defaultTitelString;
    private JTextField titelTextField;

    private AuteursComboBox auteursComboBox;
    private String auteursFilterString = null;

    private AuteursPersoonTableModel auteursPersoonTableModel;
    private int defaultAuteursId = 0;

    private BoekComboBox boekComboBox;
    private String boekFilterString = null;
    private int defaultBoekId = 0;

    // Setup a reasonable value for the maximum number of pages
    private final static int boekPaginas = 10000;
    private JSpinner boekPaginaSpinner;
    private int defaultBoekPagina = 0;

    private int defaultOnderwerpId = 20;
    private OnderwerpComboBox onderwerpComboBox;

    private int defaultVormId = 10;
    private VormComboBox vormComboBox;

    private int defaultTaalId = 40;
    private TaalComboBox taalComboBox;

    private JSpinner jaarSpinner;
    private int defaultJaar = 0;

    private JTextField opmerkingenTextField;
    private String defaultOpmerkingenString;

    private int defaultOrgTaalId = 0;
    private TaalComboBox orgTaalComboBox;

    private JTextField orgTitelTextField;
    private String defaultOrgTitelString;

    private VertalersComboBox vertalersComboBox;
    private String vertalersFilterString = null;

    private VertalersPersoonTableModel vertalersPersoonTableModel;
    private int defaultVertalersId = 0;

    int nUpdate = 0;

    private final String insertTitelActionCommand = "insertTitel";
    private final String updateTitelActionCommand = "updateTitel";

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor for inserting a record in titel
    public EditTitelDialog( Connection	connection,
			    JFrame	parentFrame,
			    String	defaultTitelString,
			    String	boekFilterString,
			    int		defaultAuteursId,
			    int		defaultOnderwerpId,
			    int		defaultVormId,
			    int		defaultTaalId ) {
	this.connection = connection;
	this.parentFrame = parentFrame;
	this.defaultTitelString = defaultTitelString;
	this.boekFilterString = boekFilterString;
	this.defaultAuteursId = defaultAuteursId;
	this.defaultOnderwerpId = defaultOnderwerpId;
	this.defaultVormId = defaultVormId;
	if ( defaultTaalId != 0 ) this.defaultTaalId = defaultTaalId;

	setupTitelDialog( "Insert titel", "Insert", insertTitelActionCommand );
    }


    // Constructor for editing an existing record in titel
    public EditTitelDialog( Connection connection,
			    JFrame     parentFrame,
			    TitelKey   titelKey ) {
	this.connection = connection;
	this.parentFrame = parentFrame;
	this.titelKey = titelKey;
	this.defaultTitelString = titelKey.getTitelString( );
	this.defaultBoekId = titelKey.getBoekId( );
	this.defaultAuteursId = titelKey.getAuteursId( );

	// Matcher to find single quotes in titel, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( titelKey.getTitelString( ) );

	final String selectTitelString =
	    "SELECT titel.boek_pagina, titel.vorm_id, titel.onderwerp_id, " +
	    "titel.taal_id, titel.jaar, titel.opmerkingen, titel.vertaling_id, " +
	    "titel.org_titel, titel.vertalers_id FROM titel " +
	    "WHERE titel.titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "' " +
	    "AND titel.auteurs_id = " + titelKey.getAuteursId( ) + " " +
	    "AND titel.boek_id = " + titelKey.getBoekId( );

	logger.fine( "selectTitelString: " + selectTitelString );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( selectTitelString );
	    // Check if combination of titel and auteurs_id could be found
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for titel key " + titelKey + " in titel" );
		return;
	    }

	    defaultBoekPagina = resultSet.getInt( 1 );
	    defaultVormId = resultSet.getInt( 2 );
	    defaultOnderwerpId = resultSet.getInt( 3 );
	    defaultTaalId = resultSet.getInt( 4 );
	    defaultJaar = resultSet.getInt( 5 );
	    defaultOpmerkingenString = resultSet.getString( 6 );
	    defaultOrgTaalId = resultSet.getInt( 7 );
	    defaultOrgTitelString = resultSet.getString( 8 );
	    defaultVertalersId = resultSet.getInt( 9 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupTitelDialog( "Edit titel", "Update", updateTitelActionCommand );
    }


    // Setup titel dialog
    private void setupTitelDialog( String dialogTitle,
                                   String editTitelButtonText,
                                   String editTitelButtonActionCommand ) {
	// Create modal dialog for editing titel
	dialog = new JDialog( parentFrame, dialogTitle, true );

	// Set grid bag layout manager
	Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Titel:" ), constraints );
	titelTextField = new JTextField( defaultTitelString, 55 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( titelTextField, constraints );


	// Setup auteurs table with connection
	auteursPersoonTableModel = new AuteursPersoonTableModel( connection );

	// Setup the auteurs table model with defaults
	auteursPersoonTableModel.showTable( defaultAuteursId );

	// Setup a JComboBox with the results of the query on auteurs
	auteursComboBox = new AuteursComboBox( connection, dialog,
					       defaultAuteursId );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Auteurs:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( auteursComboBox, constraints );

	class SelectAuteursActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected auteurs ID from the combo box
		int selectedAuteursId = auteursComboBox.getSelectedAuteursId( );

		// Check if a auteurs record needs to be inserted
		if ( auteursComboBox.newAuteursSelected( ) ) {
		    // Insert new auteurs record
		    EditAuteursDialog editAuteursDialog =
			new EditAuteursDialog( connection, dialog, auteursFilterString );

		    // Check if a new auteurs record has been inserted
		    if ( editAuteursDialog.auteursUpdated( ) ) {
			// Get the id of the new auteurs record
			selectedAuteursId = editAuteursDialog.getAuteursId( );

			// Setup the auteurs combo box again
			auteursComboBox.setupAuteursComboBox( selectedAuteursId );
		    }
		}

		// Show the selected auteurs
		auteursPersoonTableModel.showTable( selectedAuteursId );
	    }
	}
	auteursComboBox.addActionListener( new SelectAuteursActionListener( ) );

	JButton filterAuteursButton = new JButton( "Filter" );
	filterAuteursButton.setActionCommand( "filterAuteurs" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterAuteursButton, constraints );

	class FilterAuteursActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		auteursFilterString = auteursComboBox.filterAuteursComboBox( );
	    }
	}
	filterAuteursButton.addActionListener( new FilterAuteursActionListener( ) );


	JButton editAuteursButton = new JButton( "Edit" );
	editAuteursButton.setActionCommand( "editAuteurs" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editAuteursButton, constraints );

	class EditAuteursActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Auteurs ID
		int selectedAuteursId = auteursComboBox.getSelectedAuteursId( );

		// Check if auteurs has been selected
		if ( selectedAuteursId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen auteurs geselecteerd",
						   "Edit titel error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditAuteursDialog editAuteursDialog =
		    new EditAuteursDialog( connection, dialog, selectedAuteursId );

		if ( editAuteursDialog.auteursUpdated( ) ) {
		    // Show the selected auteurs
		    auteursPersoonTableModel.showTable( selectedAuteursId );

		    // Setup the auteurs combo box again
		    auteursComboBox.setupAuteursComboBox( selectedAuteursId );
		}
	    }
	}
	editAuteursButton.addActionListener( new EditAuteursActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Auteurs tabel:" ), constraints );

	// Setup a table with auteurs
        final JTable auteursPersoonTable = new JTable( auteursPersoonTableModel );
	auteursPersoonTable.setRowSelectionAllowed( false );
	auteursPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	auteursPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 330 );
	// Set vertical size just enough for 3 entries
	auteursPersoonTable.setPreferredScrollableViewportSize( new Dimension( 330, 48 ) );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( new JScrollPane( auteursPersoonTable ), constraints );


	///////////////////
	// Boek combo box
	///////////////////

	if ( editTitelButtonActionCommand.equals( insertTitelActionCommand ) ) {
	    // Setup a JComboBox with the results of the query on boek with the boek filter string
	    boekComboBox = new BoekComboBox( connection, dialog, boekFilterString );
	} else {
	    // Setup a JComboBox with the results of the query on boek with the selected boek id
	    boekComboBox = new BoekComboBox( connection, dialog, defaultBoekId );
	}
	constraints.gridx = 0;
	constraints.gridy = 4;
	container.add( new JLabel( "Boek:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( boekComboBox, constraints );

	class SelectBoekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a boek record needs to be inserted
		if ( boekComboBox.newBoekSelected( ) ) {
		    // Insert new boek record

		    // Start with composing a default boek string
		    StringBuilder defaultBoekString = new StringBuilder( auteursComboBox.getSelectedAuteursString( ) );
                    if ( defaultBoekString.length( ) > 0 ) {
			    defaultBoekString.append( ": ");
                    }
                    defaultBoekString.append( titelTextField.getText( ) );

		    EditBoekDialog editBoekDialog =
			new EditBoekDialog( connection, dialog, defaultBoekString.toString( ) );

		    // Check if a new boek record has been inserted
		    if ( editBoekDialog.boekUpdated( ) ) {
			// Get the id of the new boek record
			int selectedBoekId = editBoekDialog.getBoekId( );

			// Setup the boek combo box again
			boekComboBox.setupBoekComboBox( selectedBoekId );
		    }
		}
	    }
	}
	boekComboBox.addActionListener( new SelectBoekActionListener( ) );

	JButton filterBoekButton = new JButton( "Filter" );
	filterBoekButton.setActionCommand( "filterBoek" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterBoekButton, constraints );

	class FilterBoekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		boekFilterString = boekComboBox.filterBoekComboBox( );
	    }
	}
	filterBoekButton.addActionListener( new FilterBoekActionListener( ) );

	JButton editBoekButton = new JButton( "Edit" );
	editBoekButton.setActionCommand( "editBoek" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editBoekButton, constraints );

	class EditBoekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Boek ID
		int selectedBoekId = boekComboBox.getSelectedBoekId( );

		// Check if boek has been selected
		if ( selectedBoekId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen boek geselecteerd",
						   "Edit titel error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditBoekDialog editBoekDialog =
		    new EditBoekDialog( connection, dialog, selectedBoekId );

		if ( editBoekDialog.boekUpdated( ) ) {
		    // Setup the boek combo box again
		    boekComboBox.setupBoekComboBox( selectedBoekId );
		}
	    }
	}
	editBoekButton.addActionListener( new EditBoekActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 6;
	container.add( new JLabel( "Boek pagina:" ), constraints );

	SpinnerNumberModel boekPaginaSpinnerNumberModel = new SpinnerNumberModel( defaultBoekPagina,
										  0, boekPaginas, 1 );
	boekPaginaSpinner = new JSpinner( boekPaginaSpinnerNumberModel );
	JFormattedTextField boekPaginaSpinnerTextField = ( ( JSpinner.DefaultEditor )boekPaginaSpinner.getEditor( ) ).getTextField( );
	if ( boekPaginaSpinnerTextField != null ) {
	    boekPaginaSpinnerTextField.setColumns( 5 );
	}
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( boekPaginaSpinner, constraints );

	// Setup a JComboBox for onderwerp
	onderwerpComboBox = new OnderwerpComboBox( connection, defaultOnderwerpId );
	constraints.gridx = 0;
	constraints.gridy = 7;
	container.add( new JLabel( "Onderwerp:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( onderwerpComboBox, constraints );

	// Setup a JComboBox for vorm
	vormComboBox = new VormComboBox( connection, defaultVormId );
	constraints.gridx = 0;
	constraints.gridy = 8;
	container.add( new JLabel( "Vorm:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( vormComboBox, constraints );

	// Setup a JComboBox for taal
	taalComboBox = new TaalComboBox( connection, defaultTaalId );
	constraints.gridx = 0;
	constraints.gridy = 9;
	container.add( new JLabel( "Taal:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( taalComboBox, constraints );

	// Copyright jaar
	GregorianCalendar calendar = new GregorianCalendar( );
	Date latestDate =  calendar.getTime( );
	calendar.set( Calendar.YEAR, 1000 );
	Date earliestDate = calendar.getTime( );
	Date defaultDate = latestDate;
	// Check if the default value for jaar has been set
	if ( defaultJaar > 0 ) {
	    // Default has been set: set default date to the default jaar
	    calendar.set( Calendar.YEAR, defaultJaar );
	    defaultDate = calendar.getTime( );
	}
	SpinnerDateModel jaarSpinnerDatemodel = new SpinnerDateModel( defaultDate,
								      earliestDate,
								      latestDate,
								      Calendar.YEAR );
	jaarSpinner = new JSpinner( jaarSpinnerDatemodel );
	jaarSpinner.setEditor( new JSpinner.DateEditor( jaarSpinner, "yyyy" ) );
	constraints.gridx = 0;
	constraints.gridy = 10;
	container.add( new JLabel( "Copyright jaar:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( jaarSpinner, constraints );

	// Opmerkingen
	opmerkingenTextField = new JTextField( defaultOpmerkingenString, 55 );
	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opmerkingen:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opmerkingenTextField, constraints );

	// Setup a JComboBox for original taal
	orgTaalComboBox = new TaalComboBox( connection, defaultOrgTaalId );
	constraints.gridx = 0;
	constraints.gridy = 12;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Originele taal:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( orgTaalComboBox, constraints );

	constraints.gridx = 0;
	constraints.gridy = 13;
	container.add( new JLabel( "Originele titel:" ), constraints );
	orgTitelTextField = new JTextField( defaultOrgTitelString, 55 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( orgTitelTextField, constraints );

	// Setup vertalers table with connection
	vertalersPersoonTableModel = new VertalersPersoonTableModel( connection );

	// Setup the vertalers table model with defaults
	vertalersPersoonTableModel.showTable( defaultVertalersId );

	// Setup a JComboBox with the results of the query on vertalers
	vertalersComboBox = new VertalersComboBox( connection, dialog,
						   defaultVertalersId );
	constraints.gridx = 0;
	constraints.gridy = 14;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Vertalers:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( vertalersComboBox, constraints );

	class SelectVertalersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected vertalers ID from the combo box
		int selectedVertalersId = vertalersComboBox.getSelectedVertalersId( );

		// Check if a vertalers record needs to be inserted
		if ( vertalersComboBox.newVertalersSelected( ) ) {
		    // Insert new vertalers record
		    EditVertalersDialog editVertalersDialog =
			new EditVertalersDialog( connection, dialog, vertalersFilterString );

		    // Check if a new vertalers record has been inserted
		    if ( editVertalersDialog.vertalersUpdated( ) ) {
			// Get the id of the new vertalers record
			selectedVertalersId = editVertalersDialog.getVertalersId( );

			// Setup the vertalers combo box again
			vertalersComboBox.setupVertalersComboBox( selectedVertalersId );
		    }
		}

		// Show the selected vertalers
		vertalersPersoonTableModel.showTable( selectedVertalersId );
	    }
	}
	vertalersComboBox.addActionListener( new SelectVertalersActionListener( ) );

	JButton filterVertalersButton = new JButton( "Filter" );
	filterVertalersButton.setActionCommand( "filterVertalers" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterVertalersButton, constraints );

	class FilterVertalersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		vertalersFilterString = vertalersComboBox.filterVertalersComboBox( );
	    }
	}
	filterVertalersButton.addActionListener( new FilterVertalersActionListener( ) );

	JButton editVertalersButton = new JButton( "Edit" );
	editVertalersButton.setActionCommand( "editVertalers" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editVertalersButton, constraints );

	class EditVertalersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Vertalers ID
		int selectedVertalersId = vertalersComboBox.getSelectedVertalersId( );

		// Check if vertalers has been selected
		if ( selectedVertalersId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen vertalers geselecteerd",
						   "Edit titel error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditVertalersDialog editVertalersDialog =
		    new EditVertalersDialog( connection, dialog, selectedVertalersId );

		if ( editVertalersDialog.vertalersUpdated( ) ) {
		    // Show the selected vertalers
		    vertalersPersoonTableModel.showTable( selectedVertalersId );

		    // Setup the vertalers combo box again
		    vertalersComboBox.setupVertalersComboBox( selectedVertalersId );
		}
	    }
	}
	editVertalersButton.addActionListener( new EditVertalersActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 16;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Vertalers tabel:" ), constraints );

	// Setup a table with vertalers
	final JTable vertalersPersoonTable = new JTable( vertalersPersoonTableModel );
	vertalersPersoonTable.setRowSelectionAllowed( false );
	vertalersPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	vertalersPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 300 );
	// Set vertical size just enough for 3 entries
	vertalersPersoonTable.setPreferredScrollableViewportSize( new Dimension( 300, 48 ) );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( new JScrollPane( vertalersPersoonTable ), constraints );


	// Insert/cancel buttons
	JPanel buttonPanel = new JPanel( );

	class EditTitelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( insertTitelActionCommand ) ) {
		    if ( !( insertTitel( ) ) ) return;
		} else if ( actionEvent.getActionCommand( ).equals( updateTitelActionCommand ) ) {
		    if ( !( updateTitel( ) ) ) return;
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editTitelButton = new JButton( editTitelButtonText );
	editTitelButton.setActionCommand( editTitelButtonActionCommand );
	editTitelButton.addActionListener( new EditTitelActionListener( ) );
	buttonPanel.add( editTitelButton );

	JButton cancelTitelButton = new JButton( "Cancel" );
	cancelTitelButton.setActionCommand( "cancelTitel" );
	cancelTitelButton.addActionListener( new EditTitelActionListener( ) );
	buttonPanel.add( cancelTitelButton );

	constraints.gridx = 1;
	constraints.gridy = 17;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );


	dialog.setSize( 970, 700 );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible( true );
    }


    ////////////////////////////////////////////////
    // Insert record in titel
    ////////////////////////////////////////////////

    private boolean insertTitel( ) {
	String titelString = titelTextField.getText( );
	if ( titelString == null || titelString.length( ) == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Titel niet ingevuld",
					   "Insert titel error",
					   JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	// Matcher to find single quotes in titelString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	Matcher quoteMatcher = quotePattern.matcher( titelString );
	String insertString =
	    "INSERT INTO titel SET titel = '" +
	    quoteMatcher.replaceAll( "\\\\'" ) + "'";

	int selectedBoekId = boekComboBox.getSelectedBoekId( );
	if ( selectedBoekId != 0 ) insertString += ", boek_id = " + selectedBoekId;

	int boekPagina = ( Integer )boekPaginaSpinner.getValue( );
	if ( boekPagina != 0 ) {
	    insertString += ", boek_pagina = " + boekPagina;
	}

	int selectedAuteursId = auteursComboBox.getSelectedAuteursId( );
	if ( selectedAuteursId != 0 ) insertString += ", auteurs_id = " + selectedAuteursId;

	int vormId = vormComboBox.getSelectedVormId( );
	if ( vormId != 0 ) insertString += ", vorm_id = " + vormId;

	int onderwerpId = onderwerpComboBox.getSelectedOnderwerpId( );
	if ( onderwerpId != 0 ) insertString += ", onderwerp_id = " + onderwerpId;

 	int taalId = taalComboBox.getSelectedTaalId( );
	if ( taalId != 0 ) insertString += ", taal_id = " + taalId;

	SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy" );
	String jaarString = dateFormat.format( ( Date )jaarSpinner.getValue( ) );
	if ( jaarString != null ) {
	    if ( jaarString.length( ) > 0 ) {
		insertString += ", jaar = '" + jaarString + "'";
	    }
	}

	String opmerkingenString = opmerkingenTextField.getText( );
	if ( opmerkingenString != null ) {
	    if ( opmerkingenString.length( ) > 0 ) {
		// Matcher to find single quotes in opmerkingen, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( opmerkingenString );
		insertString += ", opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	    }
	}

 	int orgTaalId = orgTaalComboBox.getSelectedTaalId( );
	if ( orgTaalId != 0 ) insertString += ", vertaling_id = " + orgTaalId;

	String orgTitelString = orgTitelTextField.getText( );
	if ( orgTitelString != null ) {
	    if ( orgTitelString.length( ) > 0 ) {
		// Matcher to find single quotes in orgTitelString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( orgTitelString );
		insertString += ", org_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	    }
	}

	int selectedVertalersId = vertalersComboBox.getSelectedVertalersId( );
	if ( selectedVertalersId != 0 ) insertString += ", vertalers_id = " + selectedVertalersId;

	logger.fine( "insertString: " + insertString );

	try {
	    Statement statement = connection.createStatement( );
	    nUpdate = statement.executeUpdate( insertString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in titel" );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	return true;
    }


    ////////////////////////////////////////////////
    // Update record in titel
    ////////////////////////////////////////////////

    String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
	if ( updateString == null ) {
	    updateString = additionalUpdateString;
	} else {
	    updateString += ", " + additionalUpdateString;
	}
    }

    private boolean updateTitel( ) {
	Matcher quoteMatcher = null;  // Matcher to find single quotes in a string

	// Initialise string holding the update query
	updateString = null;

	String titelString = titelTextField.getText( );
 	// Check if titel changed
	if ( !titelString.equals( defaultTitelString ) ) {
	    if ( titelString == null || titelString.length( ) == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Titel niet ingevuld",
					       "Insert titel error",
					       JOptionPane.ERROR_MESSAGE);
		return false;
	    }

	    // Matcher to find single quotes in titelString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    quoteMatcher = quotePattern.matcher( titelString );
	    addToUpdateString( "titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	int selectedBoekId = boekComboBox.getSelectedBoekId( );
	// Check if boekId changed to a non-zero value
	if ( ( selectedBoekId != defaultBoekId ) && ( selectedBoekId != 0 ) ) {
	    addToUpdateString( "boek_id = " + selectedBoekId );
	}

	int boekPagina = ( Integer )boekPaginaSpinner.getValue( );
	// Check if boek pagina changed to a non-zero value
	if ( boekPagina != defaultBoekPagina ) {
	    if ( boekPagina == 0 ) {
		addToUpdateString( "boek_pagina = NULL" );
	    } else {
		addToUpdateString( "boek_pagina = " + boekPagina );
	    }
	}

	int selectedAuteursId = auteursComboBox.getSelectedAuteursId( );
	// Check if auteursId changed
	if ( selectedAuteursId != defaultAuteursId ) {
	    if ( selectedAuteursId == 0 ) {
		addToUpdateString( "auteurs_id = NULL" );
	    } else {
		addToUpdateString( "auteurs_id = " + selectedAuteursId );
	    }
	}

	int selectedVormId = vormComboBox.getSelectedVormId( );
	// Check if vormId changed
	if ( selectedVormId != defaultVormId ) {
	    if ( selectedVormId == 0 ) {
		addToUpdateString( "vorm_id = NULL" );
	    } else {
		addToUpdateString( "vorm_id = " + selectedVormId );
	    }
	}

	int selectedOnderwerpId = onderwerpComboBox.getSelectedOnderwerpId( );
	// Check if onderwerpId changed
	if ( selectedOnderwerpId != defaultOnderwerpId ) {
	    if ( selectedOnderwerpId == 0 ) {
		addToUpdateString( "onderwerp_id = NULL" );
	    } else {
		addToUpdateString( "onderwerp_id = " + selectedOnderwerpId );
	    }
	}

 	int selectedTaalId = taalComboBox.getSelectedTaalId( );
	// Check if taalId changed
	if ( selectedTaalId != defaultTaalId ) {
	    if ( selectedTaalId == 0 ) {
		addToUpdateString( "taal_id = NULL" );
	    } else {
		addToUpdateString( "taal_id = " + selectedTaalId );
	    }
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy" );
	String jaarString = dateFormat.format( ( Date )jaarSpinner.getValue( ) );
	// Check if jaar changed
	if ( !jaarString.equals( String.valueOf( defaultJaar ) ) ) {
	    addToUpdateString( "jaar = " + jaarString );
	}

	String opmerkingenString = opmerkingenTextField.getText( );
	// Check if opmerkingen changed
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultOpmerkingenString != null ) || ( opmerkingenString.length( ) > 0 ) ) &&
	     ( !opmerkingenString.equals( defaultOpmerkingenString ) ) ) {
	    if ( opmerkingenString.length( ) == 0 ) {
		addToUpdateString( "opmerkingen = NULL" );
	    } else {
		// Matcher to find single quotes in opmerkingen, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( opmerkingenString );
		addToUpdateString( "opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    }
	}

 	int selectedOrgTaalId = orgTaalComboBox.getSelectedTaalId( );
	// Check if orgTaalId changed (allow for a zero value)
	if ( selectedOrgTaalId != defaultOrgTaalId ) {
	    if ( selectedOrgTaalId == 0 ) {
		addToUpdateString( "vertaling_id = NULL" );
	    } else {
		addToUpdateString( "vertaling_id = " + selectedOrgTaalId );
	    }
	}

	String orgTitelString = orgTitelTextField.getText( );
	// Check if orgTitel changed
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultOrgTitelString != null ) || ( orgTitelString.length( ) > 0 ) ) &&
	     ( !orgTitelString.equals( defaultOrgTitelString ) ) ) {
	    if ( orgTitelString.length( ) == 0 ) {
		addToUpdateString( "org_titel = NULL" );
	    } else {
		// Matcher to find single quotes in orgTitelString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( orgTitelString );
		addToUpdateString( "org_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    }
	}

	int selectedVertalersId = vertalersComboBox.getSelectedVertalersId( );
	// Check if vertalersId changed
	if ( selectedVertalersId != defaultVertalersId ) {
	    if ( selectedVertalersId == 0 ) {
		addToUpdateString( "vertalers_id = NULL" );
	    } else {
		addToUpdateString( "vertalers_id = " + selectedVertalersId );
	    }
	}


	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "No update necessary" );
	    return true;
	}

	updateString = "UPDATE titel SET " + updateString;

	// Use the original key for selection of the existing record
	// Matcher to find single quotes in titel, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	quoteMatcher = quotePattern.matcher( titelKey.getTitelString( ) );
	updateString += " WHERE titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	updateString += " AND auteurs_id = " + titelKey.getAuteursId( );
	updateString += " AND boek_id = " + titelKey.getBoekId( );

	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in titel" );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	return true;
    }
}

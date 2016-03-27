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
import javax.swing.table.*;

public class EditBoekDialog {
    final private Logger logger = Logger.getLogger( "boeken.gui.EditBoekDialog" );

    Connection connection;
    Object parentObject;
    JDialog dialog;

    int boekId = 0;
    String defaultBoekString = "";
    JTextField boekTextField;

    int defaultUitgeverId = 0;
    UitgeverComboBox uitgeverComboBox;
    String uitgeverFilterString = null;

    JLabel isbn1Label = null;
    JLabel isbn2Label = null;

    String defaultIsbn3String = "";
    JTextField isbn3TextField;
    String defaultIsbn4String = "";
    JTextField isbn4TextField;

    int defaultPaginas = 0;
    JSpinner paginasSpinner;

    int defaultKaftId = 0;
    JComboBox kaftComboBox = new JComboBox( );
    Map kaftMap = new HashMap( );

    int defaultStatusId = 0;
    StatusComboBox statusComboBox;

    int defaultTypeId = 0;
    TypeComboBox typeComboBox;

    int defaultBundelId = 0;
    JComboBox bundelComboBox = new JComboBox( );
    Map bundelMap = new HashMap( );

    int defaultLabelId = 0;
    LabelComboBox labelComboBox;
    String labelFilterString = null;
    String newLabelString = null;

    Date defaultAanschafDatumDate;
    JSpinner aanschafDatumSpinner;

    Date defaultVerwijderdDatumDate;

    String defaultOpmerkingenString;
    JTextField opmerkingenTextField;

    EditorsComboBox editorsComboBox;
    String editorsFilterString = null;

    EditorsPersoonTableModel editorsPersoonTableModel;
    JTable editorsPersoonTable;
    String defaultEditorsString;
    int defaultEditorsId = 0;

    int nUpdate = 0;

    final String insertBoekActionCommand = "insertBoek";
    final String updateBoekActionCommand = "updateBoek";

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor for inserting a book
    public EditBoekDialog( Connection connection,
			   Object     parentObject,
			   String     defaultBoekString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.defaultBoekString = defaultBoekString;

	setupBoekDialog( "Insert boek", "Insert", insertBoekActionCommand );
    }

    // Constructor for inserting a book
    public EditBoekDialog( Connection connection,
			   Object     parentObject,
			   String     defaultBoekString,
			   int	      defaultTypeId,
			   int	      defaultUitgeverId,
			   int	      defaultStatusId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.defaultBoekString = defaultBoekString;
	this.defaultTypeId = defaultTypeId;
	this.defaultUitgeverId = defaultUitgeverId;
	this.defaultStatusId = defaultStatusId;

	setupBoekDialog( "Insert boek", "Insert", insertBoekActionCommand );
    }

    // Constructor for updating an existing book
    public EditBoekDialog( Connection connection,
			   Object     parentObject,
			   int        boekId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.boekId = boekId;

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT boek.boek, boek.editors_id, boek.bundel_id, " +
							  "boek.uitgever_id, boek.isbn_3, boek.isbn_4, " +
							  "boek.paginas, boek.kaft_id, boek.status_id, boek.type_id, " +
							  "boek.label_id, boek.datum, boek.verwijderd, boek.opmerkingen, " +
							  "editors.editors FROM boek " +
							  "LEFT JOIN editors ON boek.editors_id = editors.editors_id " +
							  "WHERE boek_id = " + boekId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for boek_id " + boekId + " in boek" );
		return;
	    }

	    defaultBoekString = resultSet.getString( 1 );
	    defaultEditorsId = resultSet.getInt( 2 );
	    defaultBundelId = resultSet.getInt( 3 );
	    defaultUitgeverId = resultSet.getInt( 4 );
	    defaultIsbn3String = resultSet.getString( 5 );
	    defaultIsbn4String = resultSet.getString( 6 );
	    defaultPaginas = resultSet.getInt( 7 );
	    defaultKaftId = resultSet.getInt( 8 );
	    defaultStatusId = resultSet.getInt( 9 );
	    defaultTypeId = resultSet.getInt( 10 );
	    defaultLabelId = resultSet.getInt( 11 );
	    defaultAanschafDatumDate = resultSet.getDate( 12 );
	    defaultVerwijderdDatumDate = resultSet.getDate( 13 );
	    defaultOpmerkingenString = resultSet.getString( 14 );
	    defaultEditorsString = resultSet.getString( 15 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupBoekDialog( "Edit boek", "Update", updateBoekActionCommand );
    }

    // Setup boek dialog
    void setupBoekDialog( String dialogTitle,
			  String editBoekButtonText,
			  String editBoekButtonActionCommand ) {
	// Create modal dialog for editing boek record
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

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Boek titel:" ), constraints );

	boekTextField = new JTextField( defaultBoekString, 45 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( boekTextField, constraints );

	// Setup a JComboBox with the results of the query on uitgever
	uitgeverComboBox = new UitgeverComboBox( connection, dialog,
						 defaultUitgeverId );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Uitgever:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( uitgeverComboBox, constraints );

	class SelectUitgeverActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a uitgever record needs to be inserted
		if ( uitgeverComboBox.newUitgeverSelected( ) ) {
		    // Insert new uitgever record
		    EditUitgeverDialog editUitgeverDialog =
			new EditUitgeverDialog( connection, dialog, uitgeverFilterString );

		    // Check if a new uitgever record has been inserted
		    if ( editUitgeverDialog.uitgeverUpdated( ) ) {
			// Get the id of the new uitgever record
			int selectedUitgeverId = editUitgeverDialog.getUitgeverId( );

			// Setup the uitgever combo box again
			uitgeverComboBox.setupUitgeverComboBox( selectedUitgeverId );
		    }
		}

		// Show the ISBN-1 and ISBN-2
		isbn1Label.setText( uitgeverComboBox.getSelectedUitgeverIsbn1String( ) );
		isbn2Label.setText( uitgeverComboBox.getSelectedUitgeverIsbn2String( ) );
	    }
	}
	uitgeverComboBox.addActionListener( new SelectUitgeverActionListener( ) );

	JButton filterUitgeverButton = new JButton( "Filter" );
	filterUitgeverButton.setActionCommand( "filterUitgever" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterUitgeverButton, constraints );

	class FilterUitgeverActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		uitgeverFilterString = uitgeverComboBox.filterUitgeverComboBox( );
	    }
	}
	filterUitgeverButton.addActionListener( new FilterUitgeverActionListener( ) );

	JButton editUitgeverButton = new JButton( "Edit" );
	editUitgeverButton.setActionCommand( "editUitgever" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editUitgeverButton, constraints );

	class EditUitgeverActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Uitgever ID
		int selectedUitgeverId = uitgeverComboBox.getSelectedUitgeverId( );

		// Check if uitgever has been selected
		if ( selectedUitgeverId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen uitgever geselecteerd",
						   "Select uitgever error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditUitgeverDialog editUitgeverDialog =
		    new EditUitgeverDialog( connection, dialog, selectedUitgeverId );

		if ( editUitgeverDialog.uitgeverUpdated( ) ) {
		    // Show the ISBN-1 and ISBN-2
		    isbn1Label.setText( editUitgeverDialog.getIsbn1String( ) );
		    isbn2Label.setText( editUitgeverDialog.getIsbn2String( ) );

		    // Setup the uitgever combo box again
		    uitgeverComboBox.setupUitgeverComboBox( );
		}
	    }
	}
	editUitgeverButton.addActionListener( new EditUitgeverActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	container.add( new JLabel( "ISBN:" ), constraints );

	JPanel isbnPanel = new JPanel( );
	isbn1Label = new JLabel( uitgeverComboBox.getSelectedUitgeverIsbn1String( ) );
	isbnPanel.add( isbn1Label );
	isbn2Label = new JLabel( uitgeverComboBox.getSelectedUitgeverIsbn2String( ) );
	isbnPanel.add( isbn2Label );
	isbn3TextField = new JTextField( defaultIsbn3String, 10 );
	isbnPanel.add( isbn3TextField );
	isbn4TextField = new JTextField( defaultIsbn4String, 10 );
	isbnPanel.add( isbn4TextField );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( isbnPanel, constraints );

	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Paginas:" ), constraints );
	SpinnerNumberModel paginasSpinnerNumberModel = new SpinnerNumberModel( defaultPaginas, 0, 10000, 1 );
	paginasSpinner = new JSpinner( paginasSpinnerNumberModel );
	JFormattedTextField paginasSpinnerTextField = ( ( JSpinner.DefaultEditor )paginasSpinner.getEditor( ) ).getTextField( );
	if ( paginasSpinnerTextField != null ) {
	    paginasSpinnerTextField.setColumns( 5 );
	}
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( paginasSpinner, constraints );

	// Setup a JComboBox for kaft
	setupKaftComboBox( );
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Kaft:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( kaftComboBox, constraints );

	// Setup a JComboBox for status
	statusComboBox = new StatusComboBox( connection, defaultStatusId );
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Status:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( statusComboBox, constraints );

	// Setup a JComboBox for type
	typeComboBox = new TypeComboBox( connection, defaultTypeId );
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Type:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( typeComboBox, constraints );

	// Setup a JComboBox for bundel
	setupBundelComboBox( );
	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Bundel:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( bundelComboBox, constraints );

	// Setup a JComboBox with the results of the query on label
	labelComboBox = new LabelComboBox( connection, dialog,
					   defaultLabelId );
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Label:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( labelComboBox, constraints );

	class SelectLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a label record needs to be inserted
		if ( labelComboBox.newLabelSelected( ) ) {
		    // Insert new label record
		    EditLabelDialog editLabelDialog =
			new EditLabelDialog( connection, dialog, labelFilterString );

		    // Check if a new label record has been inserted
		    if ( editLabelDialog.labelUpdated( ) ) {
			// Get the id of the new label records
			int selectedLabelId = editLabelDialog.getLabelId( );

			// Setup the label combo box again
			labelComboBox.setupLabelComboBox( selectedLabelId );
		    }
		}
	    }
	}
	labelComboBox.addActionListener( new SelectLabelActionListener( ) );

	JButton filterLabelButton = new JButton( "Filter" );
	filterLabelButton.setActionCommand( "filterLabel" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterLabelButton, constraints );

	class FilterLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		labelFilterString = labelComboBox.filterLabelComboBox( );
	    }
	}
	filterLabelButton.addActionListener( new FilterLabelActionListener( ) );

	JButton editLabelButton = new JButton( "Edit" );
	editLabelButton.setActionCommand( "editLabel" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editLabelButton, constraints );

	class EditLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Label ID
		int selectedLabelId = labelComboBox.getSelectedLabelId( );

		// Check if label has been selected
		if ( selectedLabelId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen label geselecteerd",
						   "Select label error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditLabelDialog editLabelDialog =
		    new EditLabelDialog( connection, dialog, selectedLabelId );

		if ( editLabelDialog.labelUpdated( ) ) {
		    // Setup the label combo box again
		    labelComboBox.setupLabelComboBox( selectedLabelId );
		}
	    }
	}
	editLabelButton.addActionListener( new EditLabelActionListener( ) );

	// Aanschaf datum
	GregorianCalendar calendar = new GregorianCalendar( );
	if ( defaultAanschafDatumDate == null ) {
	    defaultAanschafDatumDate = calendar.getTime( );
	}
	calendar.add(Calendar.YEAR, -50);
	Date earliestDate = calendar.getTime( );
	calendar.add(Calendar.YEAR, 100);
	Date latestDate = calendar.getTime( );
	SpinnerDateModel aanschafDatumSpinnerDatemodel = new SpinnerDateModel( defaultAanschafDatumDate,
									       earliestDate,
									       latestDate,
									       Calendar.DAY_OF_MONTH );
	aanschafDatumSpinner = new JSpinner( aanschafDatumSpinnerDatemodel );
	aanschafDatumSpinner.setEditor( new JSpinner.DateEditor( aanschafDatumSpinner, "dd-MM-yyyy" ) );
	constraints.gridx = 0;
	constraints.gridy = 9;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Aanschaf datum:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( aanschafDatumSpinner, constraints );

	// Opmerkingen
	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opmerkingen:" ), constraints );

	opmerkingenTextField = new JTextField( defaultOpmerkingenString, 45 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opmerkingenTextField, constraints );


	// Setup editors table with connection
	editorsPersoonTableModel = new EditorsPersoonTableModel( connection );

	// Setup the editors table model with defaults
	editorsPersoonTableModel.showTable( defaultEditorsId );

	// Setup a JComboBox with the results of the query on editors
	editorsComboBox = new EditorsComboBox( connection, dialog,
					       defaultEditorsId );
	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Editors:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editorsComboBox, constraints );

	class SelectEditorsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected editors ID from the combo box
		int selectedEditorsId = editorsComboBox.getSelectedEditorsId( );

		// Check if a editors record needs to be inserted
		if ( editorsComboBox.newEditorsSelected( ) ) {
		    // Insert new editors record
		    EditEditorsDialog editEditorsDialog =
			new EditEditorsDialog( connection, dialog, editorsFilterString );

		    // Check if a new editors record has been inserted
		    if ( editEditorsDialog.editorsUpdated( ) ) {
			// Get the id of the new editors record
			selectedEditorsId = editEditorsDialog.getEditorsId( );

			// Setup the editors combo box again
			editorsComboBox.setupEditorsComboBox( selectedEditorsId );
		    }
		}

		// Show the selected editors
		editorsPersoonTableModel.showTable( selectedEditorsId );
	    }
	}
	editorsComboBox.addActionListener( new SelectEditorsActionListener( ) );

	JButton filterEditorsButton = new JButton( "Filter" );
	filterEditorsButton.setActionCommand( "filterEditors" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterEditorsButton, constraints );

	class FilterEditorsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		editorsFilterString = editorsComboBox.filterEditorsComboBox( );
	    }
	}
	filterEditorsButton.addActionListener( new FilterEditorsActionListener( ) );

	JButton editEditorsButton = new JButton( "Edit" );
	editEditorsButton.setActionCommand( "editEditors" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editEditorsButton, constraints );

	class EditEditorsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Editors ID
		int selectedEditorsId = editorsComboBox.getSelectedEditorsId( );

		// Check if editors has been selected
		if ( selectedEditorsId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen editors geselecteerd",
						   "Edit opus error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditEditorsDialog editEditorsDialog =
		    new EditEditorsDialog( connection, dialog, selectedEditorsId );

		if ( editEditorsDialog.editorsUpdated( ) ) {
		    // Show the selected editors
		    editorsPersoonTableModel.showTable( selectedEditorsId );

		    // Setup the editors combo box again
		    editorsComboBox.setupEditorsComboBox( selectedEditorsId );
		}
	    }
	}
	editEditorsButton.addActionListener( new EditEditorsActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 12;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Editors tabel:" ), constraints );

	// Setup a table with editors
	editorsPersoonTable = new JTable( editorsPersoonTableModel );
	editorsPersoonTable.setRowSelectionAllowed( false );
	editorsPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	editorsPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 250 );
	// Set vertical size just enough for 3 entries
	editorsPersoonTable.setPreferredScrollableViewportSize( new Dimension( 250, 48 ) );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( new JScrollPane( editorsPersoonTable ), constraints );


	// Insert/cancel buttons
	JPanel buttonPanel = new JPanel( );

	class EditBoekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( insertBoekActionCommand ) ) {
		    if ( !( insertBoek( ) ) ) return;
		} else if ( actionEvent.getActionCommand( ).equals( updateBoekActionCommand ) ) {
		    if ( !( updateBoek( ) ) ) return;
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editBoekButton = new JButton( editBoekButtonText );
	editBoekButton.setActionCommand( editBoekButtonActionCommand );
	editBoekButton.addActionListener( new EditBoekActionListener( ) );
	buttonPanel.add( editBoekButton );

	JButton cancelBoekButton = new JButton( "Cancel" );
	cancelBoekButton.setActionCommand( "cancelBoek" );
	cancelBoekButton.addActionListener( new EditBoekActionListener( ) );
	buttonPanel.add( cancelBoekButton );

	constraints.gridx = 1;
	constraints.gridy = 13;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );


	dialog.setSize( 900, 620 );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible(true);
    }


    ////////////////////////////////////////////////
    // Kaft
    ////////////////////////////////////////////////

    void setupKaftComboBox( ) {
	// Add first empty item
	kaftComboBox.addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT kaft_id, kaft FROM kaft" );
	    while ( resultSet.next( ) ) {
		String kaftString = resultSet.getString( 2 );

		// Store the kaft_id in the map indexed by kaft
		kaftMap.put( kaftString, resultSet.getObject( 1 ) );

		// Add the item string to the combo box
		kaftComboBox.addItem( kaftString );

		// Check if this is the selected kaft
		if ( resultSet.getInt( 1 ) == defaultKaftId ) {
		    // Select this kaft
		    kaftComboBox.setSelectedItem( kaftString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    int getSelectedKaftId( ) {
	String kaftString = ( String )kaftComboBox.getSelectedItem( );

	if ( kaftString == null ) return 0;

	// Check if empty string is selected
	if ( kaftString.length( ) == 0 ) return 0;

	// Get the kaft_id from the map
	if ( kaftMap.containsKey( kaftString ) ) {
	    return ( ( Integer )kaftMap.get( kaftString ) ).intValue( );
	}

	return 0;
    }


    ////////////////////////////////////////////////
    // Bundel
    ////////////////////////////////////////////////

    void setupBundelComboBox( ) {
	// Add first empty item
	bundelComboBox.addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT bundel_id, bundel FROM bundel ORDER BY bundel" );
	    while ( resultSet.next( ) ) {
		String bundelString = resultSet.getString( 2 );
		// Store the bundel_id in the map indexed by bundel
		bundelMap.put( bundelString, resultSet.getObject( 1 ) );

		// Add the item string to the combo box
		bundelComboBox.addItem( bundelString );

		// Check if this is the selected bundelString
		if ( resultSet.getInt( 1 ) == defaultBundelId ) {
		    // Select this bundel
		    bundelComboBox.setSelectedItem( bundelString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    int getSelectedBundelId( ) {
	String bundelString = ( String )bundelComboBox.getSelectedItem( );

	if ( bundelString == null ) return 0;

	// Check if empty string is selected
	if ( bundelString.length( ) == 0 ) return 0;

	// Get the bundel_id from the map
	if ( bundelMap.containsKey( bundelString ) ) {
	    return ( ( Integer )bundelMap.get( bundelString ) ).intValue( );
	}

	return 0;
    }


    /////////////////////////////////
    // Insert new boek record
    /////////////////////////////////

    private boolean insertBoek( ) {
	String boekString = boekTextField.getText( );
	if ( boekString == null || boekString.length( ) == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Boek titel niet ingevuld",
					   "Insert Boek error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}

	int paginas = ( ( Integer )paginasSpinner.getValue( ) ).intValue( );
	int statusId = statusComboBox.getSelectedStatusId( );
	if ( ( paginas == 0 ) && ( statusId != 0 ) && ( statusId != 10 ) ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Aantal paginas niet ingevuld",
					   "Insert Boek error",
					   JOptionPane.ERROR_MESSAGE);
	    return false;
	}

	// Matcher to find single quotes in boekString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	Matcher quoteMatcher = quotePattern.matcher( boekString );
	String insertString =
	    "INSERT INTO boek SET boek = '" + quoteMatcher.replaceAll( "\\\\'" ) +
	    "', paginas = " + paginas;

	int bundelId = getSelectedBundelId( );
	if ( bundelId != 0 ) insertString += ", bundel_id = " + bundelId;

	int selectedUitgeverId = uitgeverComboBox.getSelectedUitgeverId( );
	if ( selectedUitgeverId != 0 ) insertString += ", uitgever_id = " + selectedUitgeverId;

	String isbn3String = isbn3TextField.getText( );
	if ( isbn3String != null ) {
	    if ( isbn3String.length( ) > 0 ) {
		insertString += ", isbn_3 = '" + isbn3String + "'";
	    }
	}

	String isbn4String = isbn4TextField.getText( );
	if ( isbn4String != null ) {
	    if ( isbn4String.length( ) > 0 ) {
		insertString += ", isbn_4 = '" + isbn4String + "'";
	    }
	}

	int kaftId = getSelectedKaftId( );
	if ( kaftId != 0 ) insertString += ", kaft_id = " + kaftId;

	if ( statusId != 0 ) insertString += ", status_id = " + statusId;

 	int typeId = typeComboBox.getSelectedTypeId( );
	if ( typeId != 0 ) insertString += ", type_id = " + typeId;

	int selectedLabelId = labelComboBox.getSelectedLabelId( );
	if ( selectedLabelId != 0 ) insertString += ", label_id = " + selectedLabelId;

	SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	String aanschafDatumString = dateFormat.format( ( Date )aanschafDatumSpinner.getValue( ) );
	if ( aanschafDatumString != null ) {
	    if ( aanschafDatumString.length( ) > 0 ) {
		insertString += ", datum = '" + aanschafDatumString + "'";
	    }
	}

	String opmerkingenString = opmerkingenTextField.getText( );
	if ( opmerkingenString != null ) {
	    if ( opmerkingenString.length( ) > 0 ) {
		// Matcher to find single quotes in opmerkingenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( opmerkingenString );
		insertString += ", opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	    }
	}

	int selectedEditorsId = editorsComboBox.getSelectedEditorsId( );
	if ( selectedEditorsId != 0 ) insertString += ", editors_id = " + selectedEditorsId;

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( boek_id ) FROM boek" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for boek_id in boek" );
		dialog.setVisible( false );
		return false;
	    }
	    boekId = resultSet.getInt( 1 ) + 1;
	    insertString += ", boek_id = " + boekId;

	    logger.fine( "insertString: " + insertString );
	    nUpdate = statement.executeUpdate( insertString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not insert in boek" );
	    	dialog.setVisible( false );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	return true;
    }

    String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
	if ( updateString == null ) {
	    updateString = additionalUpdateString;
	} else {
	    updateString += ", " + additionalUpdateString;
	}
    }

    private boolean updateBoek( ) {
	// Initialise string holding the update query
	updateString = null;

	String boekString = boekTextField.getText( );
	// Check if boek changed
	if ( !boekString.equals( defaultBoekString ) ) {
	    if ( boekString == null || boekString.length( ) == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Boek titel niet ingevuld",
					       "Insert Boek error",
					       JOptionPane.ERROR_MESSAGE );
		return false;
	    }
	    // Matcher to find single quotes in boekString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( boekString );
	    addToUpdateString( "boek = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	int paginas = ( ( Integer )paginasSpinner.getValue( ) ).intValue( );
	int statusId = statusComboBox.getSelectedStatusId( );
	// Check if paginas changed
	if ( paginas != defaultPaginas ) {
	    if ( ( paginas == 0 ) && ( statusId != 0 ) && ( statusId != 10 ) ) {
		JOptionPane.showMessageDialog( dialog,
					       "Aantal paginas niet ingevuld",
					       "Insert Boek error",
					       JOptionPane.ERROR_MESSAGE);
		return false;
	    }
	    addToUpdateString( "paginas = " + paginas );
	}

	int bundelId = getSelectedBundelId( );
	// Check if bundelId changed (allow for a zero value)
	if ( bundelId != defaultBundelId ) {
	    if ( bundelId == 0 ) {
		addToUpdateString( "bundel_id = NULL" );
	    } else {
		addToUpdateString( "bundel_id = " + bundelId );
	    }
	}

	int uitgeverId = uitgeverComboBox.getSelectedUitgeverId( );
	// Check if uitgeverId changed (allow for a zero value)
	if ( uitgeverId != defaultUitgeverId ) {
	    if ( uitgeverId == 0 ) {
		addToUpdateString( "uitgever_id = NULL" );
	    } else {
		addToUpdateString( "uitgever_id = " + uitgeverId );
	    }
	}

	String isbn3String = isbn3TextField.getText( );
	// Check if ISBN-3 changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultIsbn3String != null ) || ( isbn3String.length( ) > 0 ) ) &&
	     ( !isbn3String.equals( defaultIsbn3String ) ) ) {
	    if ( isbn3String.length( ) == 0 ) {
		addToUpdateString( "isbn_3 = NULL" );
	    } else {
		addToUpdateString( "isbn_3 = '" + isbn3String + "'" );
	    }
	}

	String isbn4String = isbn4TextField.getText( );
	// Check if ISBN-4 changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultIsbn4String != null ) || ( isbn4String.length( ) > 0 ) ) &&
	     ( !isbn4String.equals( defaultIsbn4String ) ) ) {
	    if ( isbn4String.length( ) == 0 ) {
		addToUpdateString( "isbn_4 = NULL" );
	    } else {
		addToUpdateString( "isbn_4 = '" + isbn4String + "'" );
	    }
	}

	int kaftId = getSelectedKaftId( );
	// Check if kaftId changed (allow for a zero value)
	if ( kaftId != defaultKaftId ) {
	    if ( kaftId == 0 ) {
		addToUpdateString( "kaft_id = NULL" );
	    } else {
		addToUpdateString( "kaft_id = " + kaftId );
	    }
	}

	// Check if statusId changed (allow for a zero value)
	if ( statusId != defaultStatusId ) {
	    if ( statusId == 0 ) {
		addToUpdateString( "status_id = NULL" );
	    } else {
		addToUpdateString( "status_id = " + statusId );
	    }
	}

 	int typeId = typeComboBox.getSelectedTypeId( );
	// Check if typeId changed (allow for a zero value)
	if ( typeId != defaultTypeId ) {
	    if ( typeId == 0 ) {
		addToUpdateString( "type_id = NULL" );
	    } else {
		addToUpdateString( "type_id = " + typeId );
	    }
	}

	int selectedLabelId = labelComboBox.getSelectedLabelId( );
	// Check if selectedLabelId changed (allow for a zero value)
	if ( selectedLabelId != defaultLabelId ) {
	    if ( selectedLabelId == 0 ) {
		addToUpdateString( "label_id = NULL" );
	    } else {
		addToUpdateString( "label_id = " + selectedLabelId );
	    }
	}

	Date aanschafDatumDate = ( Date )aanschafDatumSpinner.getValue( );
	// Check if aanschafDatumDate changed
	if ( !aanschafDatumDate.equals( defaultAanschafDatumDate ) ) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	    String aanschafDatumString = dateFormat.format( ( Date )aanschafDatumSpinner.getValue( ) );
	    if ( aanschafDatumString != null ) {
		if ( aanschafDatumString.length( ) > 0 ) {
		    addToUpdateString( "datum = '" + aanschafDatumString + "'" );
		}
	    }
	}

	// Check if status is set to verwijderd
	if ( ( statusId == 100 ) && ( defaultVerwijderdDatumDate == null ) ) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
            GregorianCalendar calendar = new GregorianCalendar( );
	    String verwijderdDatumString = dateFormat.format( calendar.getTime( ) );
	    addToUpdateString( "verwijderd = '" + verwijderdDatumString + "'" );
	}
	else if ( ( statusId != 100 ) && ( defaultVerwijderdDatumDate != null ) ) {
	    addToUpdateString( "verwijderd = NULL" );
	}

	String opmerkingenString = opmerkingenTextField.getText( );
	// Check if opmerkingen changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultOpmerkingenString != null ) || ( opmerkingenString.length( ) > 0 ) ) &&
	     ( !opmerkingenString.equals( defaultOpmerkingenString ) ) ) {
	    if ( opmerkingenString.length( ) == 0 ) {
		addToUpdateString( "opmerkingen = NULL" );
	    } else {
		// Matcher to find single quotes in opmerkingenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		final Matcher quoteMatcher = quotePattern.matcher( opmerkingenString );
		addToUpdateString( "opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    }
	}

	int selectedEditorsId = editorsComboBox.getSelectedEditorsId( );
	// Check if editorsId changed to a non-zero value
	if ( ( selectedEditorsId != defaultEditorsId ) && ( selectedEditorsId != 0 ) ) {
	    addToUpdateString( "editors_id = " + selectedEditorsId );
	}


	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "No update necessary" );
	    return true;
	}

	updateString = "UPDATE boek SET " + updateString;
	updateString += " WHERE boek_id = " + boekId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update in boek" );
	    	dialog.setVisible( false );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	return true;
    }

    public boolean boekUpdated( ) { return nUpdate > 0; }

    public String getBoekString( ) { return boekTextField.getText( ); }

    public int getBoekId( ) { return boekId; }
}

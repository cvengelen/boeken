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
import java.util.regex.*;


public class EditUitgeverDialog {
    final private Logger logger = Logger.getLogger( "boeken.gui.EditUitgeverDialog" );

    private Connection connection;
    Object parentObject;
    JDialog dialog;
    int uitgeverId;
    String uitgeverString;
    String isbn1String;
    String isbn2String;
    JTextField uitgeverTextField;
    JTextField isbn1TextField;
    JTextField isbn2TextField;
    int nUpdate = 0;
    final String insertUitgeverActionCommand = "insertUitgever";
    final String updateUitgeverActionCommand = "updateUitgever";

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );

    // Constructor
    public EditUitgeverDialog( Connection connection,
			       Object     parentObject,
			       String     uitgeverString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.uitgeverString = uitgeverString;
	isbn1String = "";
	isbn2String = "";
	setupUitgeverDialog( "Insert uitgever", "Insert",
			     insertUitgeverActionCommand );
    }

    // Constructor
    public EditUitgeverDialog( Connection connection,
			       Object parentObject,
			       int uitgeverId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.uitgeverId = uitgeverId;

	try {
	    Statement statement = connection.createStatement(); 
	    ResultSet resultSet = statement.executeQuery( "SELECT uitgever, isbn_1, isbn_2 " +
							  "FROM uitgever WHERE uitgever_id = " +
							  uitgeverId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for uitgever_id " +
			       uitgeverId + " in uitgever" );
		return;
	    }

	    uitgeverString = resultSet.getString( 1 );
	    isbn1String    = resultSet.getString( 2 );
	    isbn2String    = resultSet.getString( 3 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupUitgeverDialog( "Edit uitgever", "Update", updateUitgeverActionCommand );
    }

    // Setup uitgever dialog
    void setupUitgeverDialog( String dialogTitle,
			      String editUitgeverButtonText,
			      String editUitgeverButtonActionCommand ) {
	// Create modal dialog for editing uitgever
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
	Container container = dialog.getContentPane();
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Uitgever:" ), constraints );

	uitgeverTextField = new JTextField( uitgeverString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( uitgeverTextField, constraints );

	constraints.gridx = 0;
	constraints.gridy = 1;
	container.add( new JLabel( "ISBN 1:" ), constraints );

	isbn1TextField = new JTextField( isbn1String, 10 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( isbn1TextField, constraints );

	constraints.gridx = 0;
	constraints.gridy = 2;
	container.add( new JLabel( "ISBN 2:" ), constraints );

	isbn2TextField = new JTextField( isbn2String, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( isbn2TextField, constraints );

	JPanel buttonPanel = new JPanel( );
 
	class EditUitgeverActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertUitgeverActionCommand ) ) {
		    insertUitgever( );
		} else if ( ae.getActionCommand( ).equals( updateUitgeverActionCommand ) ) {
		    updateUitgever( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editUitgeverButton = new JButton( editUitgeverButtonText );
	editUitgeverButton.setActionCommand( editUitgeverButtonActionCommand );
	editUitgeverButton.addActionListener( new EditUitgeverActionListener( ) );
	buttonPanel.add( editUitgeverButton );

	JButton cancelUitgeverButton = new JButton( "Cancel" );
	cancelUitgeverButton.setActionCommand( "cancelUitgever" );
	cancelUitgeverButton.addActionListener( new EditUitgeverActionListener( ) );
	buttonPanel.add( cancelUitgeverButton );

	constraints.gridx = 1;
	constraints.gridy = 3;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize(700, 200);
	dialog.setVisible(true);
    }

    void insertUitgever( ) {
	// Matcher to find single quotes in uitgever, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( uitgeverTextField.getText( ) );
	uitgeverString = quoteMatcher.replaceAll( "\\\\'" );

	isbn1String    = isbn1TextField.getText( );
	isbn2String    = isbn2TextField.getText( );

	try {
	    Statement statement = connection.createStatement();
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( uitgever_id ) FROM uitgever" ); 
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for uitgever_id in uitgever" );
		dialog.setVisible( false );
		return;
	    }
	    uitgeverId = resultSet.getInt( 1 ) + 1;

	    nUpdate = statement.executeUpdate( "INSERT INTO uitgever SET " +
					       "uitgever_id = " + uitgeverId +
					       ",  uitgever = '" + uitgeverString +
					       "', isbn_1 = '" + isbn1String +
					       "', isbn_2 = '" + isbn2String + "'" ); 
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in uitgever" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateUitgever( ) {
	// Matcher to find single quotes in uitgever, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( uitgeverTextField.getText( ) );
	uitgeverString = quoteMatcher.replaceAll( "\\\\'" );

	isbn1String    = isbn1TextField.getText( );
	isbn2String    = isbn2TextField.getText( );

	try {
	    Statement statement = connection.createStatement(); 
	    nUpdate = statement.executeUpdate( "UPDATE uitgever SET uitgever = '" + uitgeverString +
					       "' , isbn_1 = '" + isbn1String +
					       "' , isbn_2 = '" + isbn2String +
					       "' WHERE uitgever_id = " + uitgeverId ); 
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in uitgever" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public boolean uitgeverUpdated( ) { return nUpdate > 0; }

    public String getUitgeverString( ) {
	return uitgeverString;
    }

    public String getIsbn1String( ) {
	return isbn1String;
    }

    public String getIsbn2String( ) {
	return isbn2String;
    }

    public int getUitgeverId( ) { return uitgeverId; }
}

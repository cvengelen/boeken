// Dialog for inserting or updating a record in persoon

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class EditPersoonDialog {
    final private Logger logger = Logger.getLogger( "boeken.gui.EditPersoonDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;
    int persoonId;
    String persoonString;
    JTextField persoonTextField;
    int nUpdate = 0;

    final String insertPersoonActionCommand = "insertPersoon";
    final String updatePersoonActionCommand = "updatePersoon";

    // Pattern to find a single quote in persoon, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final private Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditPersoonDialog( Connection conn,
			      Object parentObject,
			      String persoonString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.persoonString = persoonString;
	setupPersoonDialog( "Insert persoon", "Insert",
			     insertPersoonActionCommand );
    }

    // Constructor
    public EditPersoonDialog( Connection conn,
			      Object parentObject,
			      int persoonId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.persoonId = persoonId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT persoon FROM persoon WHERE persoon_id = " +
							  persoonId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for persoon_id " +
			       persoonId + " in persoon" );
		return;
	    }

	    persoonString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupPersoonDialog( "Edit persoon", "Update",
			    updatePersoonActionCommand );
    }

    // Setup persoon dialog
    void setupPersoonDialog( String dialogTitle,
			     String editPersoonButtonText,
			     String editPersoonButtonActionCommand ) {
	// Create modal dialog for editing persoon
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
	container.add( new JLabel( "Persoon:" ), constraints );

	persoonTextField = new JTextField( persoonString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( persoonTextField, constraints );

	class EditPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertPersoonActionCommand ) ) {
		    insertPersoon( );
		} else if ( ae.getActionCommand( ).equals( updatePersoonActionCommand ) ) {
		    updatePersoon( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editPersoonButton = new JButton( editPersoonButtonText );
	editPersoonButton.setActionCommand( editPersoonButtonActionCommand );
	editPersoonButton.addActionListener( new EditPersoonActionListener( ) );
	buttonPanel.add( editPersoonButton );

	JButton cancelPersoonButton = new JButton( "Cancel" );
	cancelPersoonButton.setActionCommand( "cancelPersoon" );
	cancelPersoonButton.addActionListener( new EditPersoonActionListener( ) );
	buttonPanel.add( cancelPersoonButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertPersoon( ) {
	persoonString = persoonTextField.getText( );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( persoon_id ) FROM persoon" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for persoon_id in persoon" );
		dialog.setVisible( false );
		return;
	    }
	    persoonId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in persoonString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( persoonString );

	    nUpdate = statement.executeUpdate( "INSERT INTO persoon SET " +
					       "persoon_id = " + persoonId +
					       ",  persoon = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in persoon" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updatePersoon( ) {
	persoonString = persoonTextField.getText( );

	// Matcher to find single quotes in persoonString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( persoonString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE persoon SET persoon = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE persoon_id = " + persoonId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in persoon" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public boolean persoonUpdated( ) { return nUpdate > 0; }

    public String getPersoonString( ) { return persoonString; }

    public int getPersoonId( ) { return persoonId; }
}

// Dialog for inserting or updating a record in label

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

public class EditLabelDialog {
    final private Logger logger = Logger.getLogger( "boeken.gui.EditLabelDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;
    int labelId;
    String labelString;
    JTextField labelTextField;
    int nUpdate = 0;

    final String insertLabelActionCommand = "insertLabel";
    final String updateLabelActionCommand = "updateLabel";

    // Pattern to find a single quote in persoon, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final private Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditLabelDialog( Connection conn,
			    Object parentObject,
			    String labelString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.labelString = labelString;
	setupLabelDialog( "Insert label", "Insert",
			  insertLabelActionCommand );
    }

    // Constructor
    public EditLabelDialog( Connection conn,
			    Object parentObject,
			    int labelId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.labelId = labelId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT label FROM label WHERE label_id = " +
							  labelId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for label_id " +
			       labelId + " in label" );
		return;
	    }

	    labelString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupLabelDialog( "Edit label", "Update",
			  updateLabelActionCommand );
    }

    // Setup label dialog
    void setupLabelDialog( String dialogTitle,
			   String editLabelButtonText,
			   String editLabelButtonActionCommand ) {
	// Create modal dialog for editing label
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
	container.add( new JLabel( "Label:" ), constraints );

	labelTextField = new JTextField( labelString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( labelTextField, constraints );

	class EditLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertLabelActionCommand ) ) {
		    insertLabel( );
		} else if ( ae.getActionCommand( ).equals( updateLabelActionCommand ) ) {
		    updateLabel( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editLabelButton = new JButton( editLabelButtonText );
	editLabelButton.setActionCommand( editLabelButtonActionCommand );
	editLabelButton.addActionListener( new EditLabelActionListener( ) );
	buttonPanel.add( editLabelButton );

	JButton cancelLabelButton = new JButton( "Cancel" );
	cancelLabelButton.setActionCommand( "cancelLabel" );
	cancelLabelButton.addActionListener( new EditLabelActionListener( ) );
	buttonPanel.add( cancelLabelButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertLabel( ) {
	labelString = labelTextField.getText( );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( label_id ) FROM label" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for label_id in label" );
		dialog.setVisible( false );
		return;
	    }
	    labelId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in labelString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( labelString );

	    nUpdate = statement.executeUpdate( "INSERT INTO label SET " +
					       "label_id = " + labelId +
					       ",  label = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in label" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateLabel( ) {
	labelString = labelTextField.getText( );

	// Matcher to find single quotes in labelString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( labelString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE label SET label = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE label_id = " + labelId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in label" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public boolean labelUpdated( ) { return nUpdate > 0; }

    public String getLabelString( ) { return labelString; }

    public int getLabelId( ) { return labelId; }
}

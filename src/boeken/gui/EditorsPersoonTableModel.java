// Class to setup a TableModel for editors_persoon

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.*;


public class EditorsPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.EditorsPersoonTableModel" );

    private Connection connection;
    private String[ ] headings = { "Editors-Persoon" };

    class EditorRecord {
	int	persoonId;
	String  persoonString;

	public EditorRecord( int    persoonId,
			     String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList editorRecordList = new ArrayList( 10 );
    private int editorsId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditorsPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public EditorsPersoonTableModel( Connection connection, int editorsId ) {
	this.connection = connection;
	showTable( editorsId );
    }

    public int getRowCount( ) { return editorRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= editorRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( EditorRecord )editorRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= editorRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final EditorRecord editorRecord =
	    ( EditorRecord )editorRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( editorRecord.persoonString != null ) &&
		     ( editorRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    editorRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( editorRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    editorRecord.persoonString = persoonString;
		}

		break;

	    default:
		logger.severe( "Invalid column: " + column );
		return;
	    }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " +
			   object + " for column " + column + " in row " + row );
	    return;
	}

	// Check if update is not necessary
	if ( updateString == null ) return;

	updateString = ( "UPDATE persoon SET " + updateString +
			 " WHERE persoon_id = " + editorRecord.persoonId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + editorRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	editorRecordList.set( row, editorRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int editorsId ) {
	this.editorsId = editorsId;
	showTable( );
    }

    public void showTable( ) {
	try {
	    Statement editorsStatement = connection.createStatement( );
	    ResultSet editorsResultSet = editorsStatement.executeQuery( "SELECT persoon.persoon_id, persoon.persoon " +
									"FROM editors_persoon " +
									"LEFT JOIN persoon ON persoon.persoon_id = editors_persoon.persoon_id " +
									"WHERE editors_persoon.editors_id = " + editorsId );

	    while ( editorsResultSet.next( ) ) {
		editorRecordList.add( new EditorRecord( editorsResultSet.getInt( 1 ),
							editorsResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= editorRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return 0;
	}

	return ( ( EditorRecord )editorRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	editorRecordList.add( new EditorRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= editorRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	editorRecordList.set( row,
			      new EditorRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= editorRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	editorRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new editors record, the editorsId still must be set:
    // only allow inserting in the table for other classes when the editors ID is specified.
    public void insertTable( int editorsId ) {
	this.editorsId = editorsId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid editors ID
	if ( editorsId == 0 ) {
	    logger.severe( "Editors not selected" );
	    return;
	}

	try {
	    // Insert new rows in the editors_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int editorIndex = 0; editorIndex < editorRecordList.size( ); editorIndex++ ) {
		int persoonId = ( ( EditorRecord )editorRecordList.get( editorIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO editors_persoon SET " +
						       "editors_id = " + editorsId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in editors_persoon for index " + editorIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid editors ID
	if ( editorsId == 0 ) {
	    logger.severe( "Editors not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current editors ID from the editors_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM editors_persoon WHERE editors_id = " + editorsId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in editors_persoon
	insertTable( );
    }
}

// Class to setup a TableModel for vertalers_persoon

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


public class VertalersPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.EditorsPersoonTableModel" );

    private Connection connection;
    private String[ ] headings = { "Vertalers-Persoon" };

    class VertalerRecord {
	int	persoonId;
	String  persoonString;

	public VertalerRecord( int    persoonId,
			     String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList vertalerRecordList = new ArrayList( 10 );
    private int vertalersId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public VertalersPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public VertalersPersoonTableModel( Connection connection, int vertalersId ) {
	this.connection = connection;
	showTable( vertalersId );
    }

    public int getRowCount( ) { return vertalerRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= vertalerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( VertalerRecord )vertalerRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= vertalerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final VertalerRecord vertalerRecord =
	    ( VertalerRecord )vertalerRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( vertalerRecord.persoonString != null ) &&
		     ( vertalerRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    vertalerRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( vertalerRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    vertalerRecord.persoonString = persoonString;
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
			 " WHERE persoon_id = " + vertalerRecord.persoonId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + vertalerRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	vertalerRecordList.set( row, vertalerRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int vertalersId ) {
	this.vertalersId = vertalersId;
	showTable( );
    }

    public void showTable( ) {
	try {
	    Statement vertalersStatement = connection.createStatement( );
	    ResultSet vertalersResultSet = vertalersStatement.executeQuery( "SELECT persoon.persoon_id, persoon.persoon " +
									    "FROM vertalers_persoon " +
									    "LEFT JOIN persoon ON persoon.persoon_id = vertalers_persoon.persoon_id " +
									    "WHERE vertalers_persoon.vertalers_id = " + vertalersId );

	    while ( vertalersResultSet.next( ) ) {
		vertalerRecordList.add( new VertalerRecord( vertalersResultSet.getInt( 1 ),
							    vertalersResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= vertalerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return 0;
	}

	return ( ( VertalerRecord )vertalerRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	vertalerRecordList.add( new VertalerRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= vertalerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	vertalerRecordList.set( row,
			      new VertalerRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= vertalerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	vertalerRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new vertalers record, the vertalersId still must be set:
    // only allow inserting in the table for other classes when the vertalers ID is specified.
    public void insertTable( int vertalersId ) {
	this.vertalersId = vertalersId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid vertalers ID
	if ( vertalersId == 0 ) {
	    logger.severe( "Vertalers not selected" );
	    return;
	}

	try {
	    // Insert new rows in the vertalers_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int vertalerIndex = 0; vertalerIndex < vertalerRecordList.size( ); vertalerIndex++ ) {
		int persoonId = ( ( VertalerRecord )vertalerRecordList.get( vertalerIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO vertalers_persoon SET " +
						       "vertalers_id = " + vertalersId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in vertalers_persoon for index " + vertalerIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid vertalers ID
	if ( vertalersId == 0 ) {
	    logger.severe( "Vertalers not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current vertalers ID from the vertalers_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM vertalers_persoon WHERE vertalers_id = " + vertalersId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in vertalers_persoon
	insertTable( );
    }
}

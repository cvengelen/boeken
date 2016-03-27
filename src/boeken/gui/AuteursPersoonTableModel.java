// Class to setup a TableModel for auteurs_persoon

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


public class AuteursPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.AuteursPersoonTableModel" );

    private Connection connection;
    private String[ ] headings = { "Auteurs-Persoon" };

    class AuteurRecord {
	int	persoonId;
	String  persoonString;

	public AuteurRecord( int    persoonId,
			     String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList auteurRecordList = new ArrayList( 10 );
    private int auteursId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public AuteursPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public AuteursPersoonTableModel( Connection connection, int auteursId ) {
	this.connection = connection;
	showTable( auteursId );
    }

    public int getRowCount( ) { return auteurRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= auteurRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( AuteurRecord )auteurRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= auteurRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final AuteurRecord auteurRecord =
	    ( AuteurRecord )auteurRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( auteurRecord.persoonString != null ) &&
		     ( auteurRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    auteurRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( auteurRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    auteurRecord.persoonString = persoonString;
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
			 " WHERE persoon_id = " + auteurRecord.persoonId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + auteurRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	auteurRecordList.set( row, auteurRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int auteursId ) {
	this.auteursId = auteursId;
	showTable( );
    }

    public void showTable( ) {
	try {
	    String auteursPersoonQueryString =
		"SELECT persoon.persoon_id, persoon.persoon " +
		"FROM auteurs_persoon " +
		"LEFT JOIN persoon ON persoon.persoon_id = auteurs_persoon.persoon_id " +
		"WHERE auteurs_persoon.auteurs_id = " + auteursId;

	    Statement auteursStatement = connection.createStatement( );
	    ResultSet auteursResultSet = auteursStatement.executeQuery( auteursPersoonQueryString );

	    while ( auteursResultSet.next( ) ) {
		auteurRecordList.add( new AuteurRecord( auteursResultSet.getInt( 1 ),
							auteursResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= auteurRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return 0;
	}

	return ( ( AuteurRecord )auteurRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	auteurRecordList.add( new AuteurRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= auteurRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	auteurRecordList.set( row,
			      new AuteurRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= auteurRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	auteurRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new auteurs record, the auteursId still must be set:
    // only allow inserting in the table for other classes when the auteurs ID is specified.
    public void insertTable( int auteursId ) {
	this.auteursId = auteursId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid auteurs ID
	if ( auteursId == 0 ) {
	    logger.severe( "auteurs not selected" );
	    return;
	}

	try {
	    // Insert new rows in the auteurs_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int auteurIndex = 0; auteurIndex < auteurRecordList.size( ); auteurIndex++ ) {
		int persoonId = ( ( AuteurRecord )auteurRecordList.get( auteurIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO auteurs_persoon SET " +
						       "auteurs_id = " + auteursId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in auteurs_persoon for index " + auteurIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid auteurs ID
	if ( auteursId == 0 ) {
	    logger.severe( "auteurs not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current auteurs ID from the auteurs_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM auteurs_persoon WHERE auteurs_id = " + auteursId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in auteurs_persoon
	insertTable( );
    }
}

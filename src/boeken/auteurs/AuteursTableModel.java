// Class to setup a TableModel for records in auteurs

package boeken.auteurs;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


class AuteursTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( AuteursTableModel.class.getCanonicalName() );

    private Connection connection;
    private final String[ ] headings = { "Id", "Auteurs", "Persoon" };

    private class AuteursRecord {
	String  auteursString;
	String  persoonString;
	int	auteursId;
	int	persoonId;

	AuteursRecord( String auteursString,
                       String persoonString,
                       int    auteursId,
                       int    persoonId ) {
	    this.auteursString = auteursString;
	    this.persoonString = persoonString;
	    this.auteursId = auteursId;
	    this.persoonId = persoonId;
	}
    }

    private final ArrayList<AuteursRecord> auteursRecordList = new ArrayList<>( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    AuteursTableModel( Connection connection ) {
	this.connection = connection;

	setupAuteursTableModel( null );
    }

    void setupAuteursTableModel( String auteursFilterString ) {

	// Setup the table
	try {
	    String auteursQueryString =
		"SELECT auteurs.auteurs, persoon.persoon, " +
		"auteurs.auteurs_id, auteurs_persoon.persoon_id " +
		"FROM auteurs " +
		"LEFT JOIN auteurs_persoon ON auteurs_persoon.auteurs_id = auteurs.auteurs_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = auteurs_persoon.persoon_id ";

	    if ( ( auteursFilterString != null ) && ( auteursFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in auteursFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( auteursFilterString );
		auteursQueryString +=
		    "WHERE auteurs.auteurs LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    auteursQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( auteursQueryString );

	    // Clear the list
	    auteursRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		auteursRecordList.add( new AuteursRecord( resultSet.getString( 1 ),
							  resultSet.getString( 2 ),
							  resultSet.getInt( 3 ),
							  resultSet.getInt( 4 ) ) );
	    }

	    auteursRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return auteursRecordList.size( ); }

    public int getColumnCount( ) { return 3; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public Class getColumnClass( int column ) {
	switch ( column ) {
	case 0: // Id
	    return Integer.class;
	}
	return String.class;
    }

    public boolean isCellEditable( int row, int column ) {
	switch ( column ) {
	case 0: // Id
	case 2: // Persoon
	    // Do not allow editing
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= auteursRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final AuteursRecord auteursRecord = auteursRecordList.get( row );

	if ( column == 0 ) return auteursRecord.auteursId;
	if ( column == 1 ) return auteursRecord.auteursString;
	if ( column == 2 ) return auteursRecord.persoonString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= auteursRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final AuteursRecord auteursRecord = auteursRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String auteursString = ( String )object;
		if ( ( ( auteursString == null ) || ( auteursString.length( ) == 0 ) ) &&
		     ( auteursRecord.auteursString != null ) &&
		     ( auteursRecord.auteursString.length( ) != 0 ) ) {
		    updateString = "auteurs = NULL ";
		    auteursRecord.auteursString = auteursString;
		} else if ( ( auteursString != null ) &&
			    ( !auteursString.equals( auteursRecord.auteursString ) ) ) {
		    // Matcher to find single quotes in auteurs, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( auteursString );
		    updateString = "auteurs = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    auteursRecord.auteursString = auteursString;
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

	updateString = ( "UPDATE auteurs SET " + updateString +
			 " WHERE auteurs_id = " + auteursRecord.auteursId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with auteurs_id " + auteursRecord.auteursId +
			       " in auteurs, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	auteursRecordList.set( row, auteursRecord );

	fireTableCellUpdated( row, column );
    }

    int getAuteursId( int row ) {
        if ( ( row < 0 ) || ( row >= auteursRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

        return (auteursRecordList.get( row )).auteursId;
    }

    String getAuteursString( int row ) {
	if ( ( row < 0 ) || ( row >= auteursRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( auteursRecordList.get( row ) ).auteursString;
    }
}

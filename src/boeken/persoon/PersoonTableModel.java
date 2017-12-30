// Class to setup a TableModel for records in persoon

package boeken.persoon;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


class PersoonTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( PersoonTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Id", "Persoon" };

    private class PersoonRecord {
	int	persoonId;
	String  persoonString;

	PersoonRecord( int     persoonId,
                       String  persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    private final ArrayList<PersoonRecord> persoonRecordList = new ArrayList<>( 200 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    PersoonTableModel( Connection connection, final Component  parentComponent) {
        this.connection = connection;
        this.parentComponent = parentComponent;

	setupPersoonTableModel( null );
    }

    void setupPersoonTableModel( String persoonFilterString ) {

	// Setup the table
	try {
	    String persoonQueryString =
		"SELECT persoon.persoon_id, persoon.persoon " +
		"FROM persoon ";

	    if ( ( persoonFilterString != null ) && ( persoonFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in persoonFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( persoonFilterString );
		persoonQueryString += "WHERE persoon.persoon LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    persoonQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( persoonQueryString );

	    // Clear the list
	    persoonRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		persoonRecordList.add( new PersoonRecord( resultSet.getInt( 1 ),
							  resultSet.getString( 2 ) ) );
	    }

	    persoonRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "PersoonTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return persoonRecordList.size( ); }

    public int getColumnCount( ) { return 2; }

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
	    // Do not allow editing
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= persoonRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final PersoonRecord persoonRecord = persoonRecordList.get( row );

	if ( column == 0 ) return persoonRecord.persoonId;
	if ( column == 1 ) return persoonRecord.persoonString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= persoonRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final PersoonRecord persoonRecord = persoonRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( persoonRecord.persoonString != null ) &&
		     ( persoonRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( persoonRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		persoonRecord.persoonString = persoonString;
		break;

	    default:
		logger.severe( "Invalid column: " + column );
		return;
	    }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " + object + " for column " + column + " in row " + row );
	    return;
	}

	// Check if update is not necessary
	if ( updateString == null ) return;

	// Store record in list
	persoonRecordList.set( row, persoonRecord );

	updateString = "UPDATE persoon SET " + updateString + " WHERE persoon_id = " + persoonRecord.persoonId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with persoon_id " + persoonRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "PersoonTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return persoonRecordList.size( ); }

    int getPersoonId( int row ) {
        if ( ( row < 0 ) || ( row >= persoonRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return persoonRecordList.get( row ).persoonId;
    }

    String getPersoonString( int row ) {
	if ( ( row < 0 ) || ( row >= persoonRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return persoonRecordList.get( row ).persoonString;
    }
}

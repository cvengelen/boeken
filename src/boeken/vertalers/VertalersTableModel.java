// Class to setup a TableModel for records in vertalers

package boeken.vertalers;

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


class VertalersTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( VertalersTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Id", "Vertalers", "Persoon" };

    private class VertalersRecord {
	String  vertalersString;
	String  persoonString;
	int	vertalersId;
	int	persoonId;

	VertalersRecord( String vertalersString,
                         String persoonString,
                         int    vertalersId,
                         int    persoonId ) {
	    this.vertalersString = vertalersString;
	    this.persoonString = persoonString;
	    this.vertalersId = vertalersId;
	    this.persoonId = persoonId;
	}
    }

    private final ArrayList<VertalersRecord> vertalersRecordList = new ArrayList<>( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    VertalersTableModel( Connection connection, final Component  parentComponent) {
        this.connection = connection;
        this.parentComponent = parentComponent;

	setupVertalersTableModel( null );
    }

    void setupVertalersTableModel( String vertalersFilterString ) {

	// Setup the table
	try {
	    String vertalersQueryString =
		"SELECT vertalers.vertalers, persoon.persoon, " +
		"vertalers.vertalers_id, vertalers_persoon.persoon_id " +
		"FROM vertalers " +
		"LEFT JOIN vertalers_persoon ON vertalers_persoon.vertalers_id = vertalers.vertalers_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = vertalers_persoon.persoon_id ";

	    if ( ( vertalersFilterString != null ) && ( vertalersFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in vertalersFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( vertalersFilterString );
		vertalersQueryString +=
		    "WHERE vertalers.vertalers LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    vertalersQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( vertalersQueryString );

	    // Clear the list
	    vertalersRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		vertalersRecordList.add( new VertalersRecord( resultSet.getString( 1 ),
							      resultSet.getString( 2 ),
							      resultSet.getInt( 3 ),
							      resultSet.getInt( 4 ) ) );
	    }

	    vertalersRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "VertalersTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return vertalersRecordList.size( ); }

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
	if ( ( row < 0 ) || ( row >= vertalersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final VertalersRecord vertalersRecord = vertalersRecordList.get( row );

	if ( column == 0 ) return vertalersRecord.vertalersId;
	if ( column == 1 ) return vertalersRecord.vertalersString;
	if ( column == 2 ) return vertalersRecord.persoonString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= vertalersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final VertalersRecord vertalersRecord = vertalersRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String vertalersString = ( String )object;
		if ( ( ( vertalersString == null ) || ( vertalersString.length( ) == 0 ) ) &&
		     ( vertalersRecord.vertalersString != null ) &&
		     ( vertalersRecord.vertalersString.length( ) != 0 ) ) {
		    updateString = "vertalers = NULL ";
		    vertalersRecord.vertalersString = vertalersString;
		} else if ( ( vertalersString != null ) &&
			    ( !vertalersString.equals( vertalersRecord.vertalersString ) ) ) {
		    // Matcher to find single quotes in vertalers, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( vertalersString );
		    updateString = "vertalers = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    vertalersRecord.vertalersString = vertalersString;
		}

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

	updateString = "UPDATE vertalers SET " + updateString + " WHERE vertalers_id = " + vertalersRecord.vertalersId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with vertalers_id " + vertalersRecord.vertalersId + " in vertalers, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "VertalersTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	vertalersRecordList.set( row, vertalersRecord );

	fireTableCellUpdated( row, column );
    }

    int getVertalersId( int row ) {
        if ( ( row < 0 ) || ( row >= vertalersRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return vertalersRecordList.get( row ).vertalersId;
    }

    String getVertalersString( int row ) {
	if ( ( row < 0 ) || ( row >= vertalersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return vertalersRecordList.get( row ).vertalersString;
    }
}

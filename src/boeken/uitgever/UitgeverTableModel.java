// Class to setup a TableModel for records in uitgever

package boeken.uitgever;

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


class UitgeverTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( UitgeverTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Id", "Uitgever", "ISBN 1", "ISBN 2" };

    private class UitgeverRecord {
	String  uitgeverString;
	String  isbn1String;
	String  isbn2String;
	int	uitgeverId;

	UitgeverRecord( String  uitgeverString,
                        String  isbn1String,
                        String  isbn2String,
                        int     uitgeverId ) {
	    this.uitgeverString = uitgeverString;
	    this.isbn1String = isbn1String;
	    this.isbn2String = isbn2String;
	    this.uitgeverId = uitgeverId;
	}
    }

    private final ArrayList<UitgeverRecord> uitgeverRecordList = new ArrayList<>( 200 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    UitgeverTableModel( Connection connection, final Component  parentComponent) {
        this.connection = connection;
        this.parentComponent = parentComponent;

	setupUitgeverTableModel( null );
    }

    void setupUitgeverTableModel( String uitgeverFilterString ) {
	// Setup the table
	try {
	    String uitgeverQueryString = "SELECT uitgever.uitgever, uitgever.isbn_1, uitgever.isbn_2, uitgever.uitgever_id FROM uitgever ";

	    if ( ( uitgeverFilterString != null ) && ( uitgeverFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in uitgeverFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( uitgeverFilterString );
		uitgeverQueryString += "WHERE uitgever.uitgever LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    uitgeverQueryString += "ORDER BY uitgever.uitgever";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( uitgeverQueryString );

	    // Clear the list
	    uitgeverRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		uitgeverRecordList.add( new UitgeverRecord( resultSet.getString( 1 ),
							    resultSet.getString( 2 ),
							    resultSet.getString( 3 ),
							    resultSet.getInt( 4 ) ) );
	    }

	    uitgeverRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "UitgeverTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return uitgeverRecordList.size( ); }

    public int getColumnCount( ) { return 4; }

    // Indicate the class for each column for setting the correct default renderer
    // see file:///home/cvengelen/java/tutorial/uiswing/components/table.html
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
	if ( ( row < 0 ) || ( row >= uitgeverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final UitgeverRecord uitgeverRecord = uitgeverRecordList.get( row );

	if ( column == 0 ) return uitgeverRecord.uitgeverId;
	if ( column == 1 ) return uitgeverRecord.uitgeverString;
	if ( column == 2 ) return uitgeverRecord.isbn1String;
	if ( column == 3 ) return uitgeverRecord.isbn2String;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= uitgeverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final UitgeverRecord uitgeverRecord = uitgeverRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String uitgeverString = ( String )object;
		if ( ( ( uitgeverString == null ) || ( uitgeverString.length( ) == 0 ) ) &&
		     ( uitgeverRecord.uitgeverString != null ) &&
		     ( uitgeverRecord.uitgeverString.length( ) != 0 ) ) {
		    updateString = "uitgever = NULL ";
		} else if ( ( uitgeverString != null ) &&
			    ( !uitgeverString.equals( uitgeverRecord.uitgeverString ) ) ) {
		    // Matcher to find single quotes in uitgever, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( uitgeverString );
		    updateString = "uitgever = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		uitgeverRecord.uitgeverString = uitgeverString;
		break;

	    case 2:
		String isbn1String = ( String )object;
		if ( isbn1String.length( ) > 2 ) isbn1String = isbn1String.substring( 0, 2 );
		if ( ( ( isbn1String == null ) || ( isbn1String.length( ) == 0 ) ) &&
		     ( uitgeverRecord.isbn1String != null ) &&
		     ( uitgeverRecord.isbn1String.length( ) != 0 ) ) {
		    updateString = "isbn_1 = NULL ";
		} else if ( ( isbn1String != null ) &&
			    ( !isbn1String.equals( uitgeverRecord.isbn1String ) ) ) {
		    // Matcher to find single quotes in isbn1, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( isbn1String );
		    updateString = "isbn_1 = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		uitgeverRecord.isbn1String = isbn1String;
		break;

	    case 3:
		String isbn2String = ( String )object;
		if ( isbn2String.length( ) > 5 ) isbn2String = isbn2String.substring( 0, 5 );
		if ( ( ( isbn2String == null ) || ( isbn2String.length( ) == 0 ) ) &&
		     ( uitgeverRecord.isbn2String != null ) &&
		     ( uitgeverRecord.isbn2String.length( ) != 0 ) ) {
		    updateString = "isbn_2 = NULL ";
		} else if ( ( isbn2String != null ) &&
			    ( !isbn2String.equals( uitgeverRecord.isbn2String ) ) ) {
		    // Matcher to find single quotes in isbn2, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( isbn2String );
		    updateString = "isbn_2 = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		uitgeverRecord.isbn2String = isbn2String;
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
	uitgeverRecordList.set( row, uitgeverRecord );

	updateString = "UPDATE uitgever SET " + updateString + " WHERE uitgever_id = " + uitgeverRecord.uitgeverId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with uitgever_id " + uitgeverRecord.uitgeverId +
			       " in uitgever, nUpdate = " + nUpdate );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "UitgeverTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return uitgeverRecordList.size( ); }

    int getUitgeverId( int row ) {
        if ( ( row < 0 ) || ( row >= uitgeverRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return uitgeverRecordList.get( row ).uitgeverId;
    }

    String getUitgeverString( int row ) {
	if ( ( row < 0 ) || ( row >= uitgeverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return uitgeverRecordList.get( row ).uitgeverString;
    }
}

// Class to setup a TableModel for records in label

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


public class LabelTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.LabelTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Label" };

    class LabelRecord {
	int	labelId;
	String  labelString;

	public LabelRecord( int     labelId,
			    String  labelString ) {
	    this.labelId = labelId;
	    this.labelString = labelString;
	}
    }

    ArrayList labelRecordList = new ArrayList( 200 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public LabelTableModel( Connection connection ) {
	this.connection = connection;

	setupLabelTableModel( null );
    }

    public void setupLabelTableModel( String labelFilterString ) {

	// Setup the table
	try {
	    String labelQueryString =
		"SELECT label.label_id, label.label " +
		"FROM label ";

	    if ( ( labelFilterString != null ) && ( labelFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in labelFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( labelFilterString );
		labelQueryString += "WHERE label.label LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    labelQueryString += "ORDER BY label.label";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( labelQueryString );

	    // Clear the list
	    labelRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		labelRecordList.add( new LabelRecord( resultSet.getInt( 1 ),
						      resultSet.getString( 2 ) ) );
	    }

	    labelRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return labelRecordList.size( ); }

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
	if ( ( row < 0 ) || ( row >= labelRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final LabelRecord labelRecord =
	    ( LabelRecord )labelRecordList.get( row );

	if ( column == 0 ) return new Integer( labelRecord.labelId );
	if ( column == 1 ) return labelRecord.labelString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= labelRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final LabelRecord labelRecord =
	    ( LabelRecord )labelRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String labelString = ( String )object;
		if ( ( ( labelString == null ) || ( labelString.length( ) == 0 ) ) &&
		     ( labelRecord.labelString != null ) &&
		     ( labelRecord.labelString.length( ) != 0 ) ) {
		    updateString = "label = NULL ";
		} else if ( ( labelString != null ) &&
			    ( !labelString.equals( labelRecord.labelString ) ) ) {
		    // Matcher to find single quotes in label, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( labelString );
		    updateString = "label = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		labelRecord.labelString = labelString;
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

	// Store record in list
	labelRecordList.set( row, labelRecord );

	updateString = ( "UPDATE label SET " + updateString +
			 " WHERE label_id = " + labelRecord.labelId );

	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with label_id " + labelRecord.labelId +
			       " in label, nUpdate = " + nUpdate );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public int getNumberOfRecords( ) { return labelRecordList.size( ); }

    public int getLabelId( int row ) {
	final LabelRecord labelRecord =
	    ( LabelRecord )labelRecordList.get( row );

	return labelRecord.labelId;
    }

    public String getLabelString( int row ) {
	if ( ( row < 0 ) || ( row >= labelRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( LabelRecord )labelRecordList.get( row ) ).labelString;
    }
}

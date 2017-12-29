// Class to setup a TableModel for records in editors

package boeken.editors;

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


class EditorsTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( "boeken.editors.EditorsTableModel" );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Id", "Editors", "Persoon" };

    private class EditorsRecord {
	String  editorsString;
	String  persoonString;
	int	editorsId;
	int	persoonId;

	EditorsRecord( String  editorsString,
                       String  persoonString,
                       int     editorsId,
                       int    persoonId ) {
	    this.editorsString = editorsString;
	    this.persoonString = persoonString;
	    this.editorsId = editorsId;
	    this.persoonId = persoonId;
	}
    }

    private final ArrayList<EditorsRecord> editorsRecordList = new ArrayList<>( 20 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    EditorsTableModel( Connection connection, final Component  parentComponent) {
        this.connection = connection;
        this.parentComponent = parentComponent;

	setupEditorsTableModel( null );
    }

    void setupEditorsTableModel( String editorsFilterString ) {

	// Setup the table
	try {
	    String editorsQueryString =
		"SELECT editors.editors, persoon.persoon, " +
		"editors.editors_id, editors_persoon.persoon_id " +
		"FROM editors " +
		"LEFT JOIN editors_persoon ON editors_persoon.editors_id = editors.editors_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = editors_persoon.persoon_id ";

	    if ( ( editorsFilterString != null ) && ( editorsFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in editorsFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( editorsFilterString );
		editorsQueryString +=
		    "WHERE editors.editors LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" )  + "%\" ";
	    }

	    editorsQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( editorsQueryString );

	    // Clear the list
	    editorsRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		editorsRecordList.add( new EditorsRecord( resultSet.getString( 1 ),
							  resultSet.getString( 2 ),
							  resultSet.getInt( 3 ),
							  resultSet.getInt( 4 ) ) );
	    }

	    editorsRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "EditorsTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return editorsRecordList.size( ); }

    public int getColumnCount( ) { return 3; }

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
	if ( ( row < 0 ) || ( row >= editorsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final EditorsRecord editorsRecord = editorsRecordList.get( row );

	if ( column == 0 ) return editorsRecord.editorsId;
	if ( column == 1 ) return editorsRecord.editorsString;
	if ( column == 2 ) return editorsRecord.persoonString;

	return "";
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= editorsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final EditorsRecord editorsRecord = editorsRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String editorsString = ( String )object;
		if ( ( ( editorsString == null ) || ( editorsString.length( ) == 0 ) ) &&
		     ( editorsRecord.editorsString != null ) &&
		     ( editorsRecord.editorsString.length( ) != 0 ) ) {
		    updateString = "editors = NULL ";
		    editorsRecord.editorsString = editorsString;
		} else if ( ( editorsString != null ) &&
			    ( !editorsString.equals( editorsRecord.editorsString ) ) ) {
		    // Matcher to find single quotes in editors, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( editorsString );
		    updateString = "editors = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    editorsRecord.editorsString = editorsString;
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

	updateString = "UPDATE editors SET " + updateString + " WHERE editors_id = " + editorsRecord.editorsId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with editors_id " + editorsRecord.editorsId +
			       " in editors, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "EditorsTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	editorsRecordList.set( row, editorsRecord );

	fireTableCellUpdated( row, column );
    }

    int getEditorsId( int row ) {
        if ( ( row < 0 ) || ( row >= editorsRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return (editorsRecordList.get( row )).editorsId;
    }

    String getEditorsString( int row ) {
	if ( ( row < 0 ) || ( row >= editorsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( editorsRecordList.get( row ) ).editorsString;
    }
}

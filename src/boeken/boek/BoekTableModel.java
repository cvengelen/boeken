// Class to setup a TableModel for records in boek

package boeken.boek;

import boeken.gui.LabelComboBox;
import boeken.gui.StatusComboBox;
import boeken.gui.TypeComboBox;
import boeken.gui.UitgeverComboBox;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


class BoekTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( "boeken.boek.BoekTableModel" );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Id", "Boek", "Type",
                                         "Uitgever", "ISBN-3", "ISBN-4",
                                         "Status", "Label", "Aanschafdatum", "Verwijderd" };

    private final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

    private class BoekRecord {
	int	boekId;
	String	boekString;
	int	typeId;
	String	typeString;
	int	uitgeverId;
	String	uitgeverIsbn1Isbn2String;
	String	isbn3String;
	String	isbn4String;
	int	statusId;
	String	statusString;
	int	labelId;
	String	labelString;
	Date	aanschafDate;
	Date    verwijderdDate;

	BoekRecord( int     boekId,
                    String  boekString,
                    int     typeId,
                    String  typeString,
                    int     uitgeverId,
                    String  uitgeverIsbn1Isbn2String,
                    String  isbn3String,
                    String  isbn4String,
                    int     statusId,
                    String  statusString,
                    int     labelId,
                    String  labelString,
                    Date    aanschafDate,
                    Date    verwijderdDate ) {
	    this.boekId = boekId;
	    this.boekString = boekString;
	    this.typeId = typeId;
	    this.typeString = typeString;
	    this.uitgeverId = uitgeverId;
	    this.uitgeverIsbn1Isbn2String = uitgeverIsbn1Isbn2String;
	    this.isbn3String = isbn3String;
	    this.isbn4String = isbn4String;
	    this.statusId = statusId;
	    this.statusString = statusString;
	    this.labelId = labelId;
	    this.labelString = labelString;
	    this.aanschafDate = aanschafDate;
	    this.verwijderdDate = verwijderdDate;
	}

	// Copy constructor
	BoekRecord( BoekRecord boekRecord ) {
	    this.boekId = boekRecord.boekId;
	    this.boekString = boekRecord.boekString;
	    this.typeId = boekRecord.typeId;
	    this.typeString = boekRecord.typeString;
	    this.uitgeverId = boekRecord.uitgeverId;
	    this.uitgeverIsbn1Isbn2String = boekRecord.uitgeverIsbn1Isbn2String;
	    this.isbn3String = boekRecord.isbn3String;
	    this.isbn4String = boekRecord.isbn4String;
	    this.statusId = boekRecord.statusId;
	    this.statusString = boekRecord.statusString;
	    this.labelId = boekRecord.labelId;
	    this.labelString = boekRecord.labelString;
	    this.aanschafDate = boekRecord.aanschafDate;
	    this.verwijderdDate = boekRecord.verwijderdDate;
	}
    }

    private final ArrayList<BoekRecord> boekRecordList = new ArrayList<>( 800 );

    private TypeComboBox typeComboBox;
    private UitgeverComboBox uitgeverComboBox;
    private StatusComboBox statusComboBox;
    private LabelComboBox labelComboBox;

    private JButton cancelBoekButton;
    private JButton saveBoekButton;

    private boolean	rowModified = false;
    private int		editRow = -1;
    private BoekRecord	boekRecord = null;
    private BoekRecord	originalBoekRecord = null;

    // Pattern to find a single quote, to be replaced with
    // escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    BoekTableModel( final Connection connection,
                    final Component  parentComponent,
                    final JButton    cancelBoekButton,
                    final JButton    saveBoekButton ) {
	this.connection = connection;
	this.parentComponent = parentComponent;
	this.cancelBoekButton = cancelBoekButton;
	this.saveBoekButton = saveBoekButton;

	// Create the combo boxes
	typeComboBox = new TypeComboBox( connection );
	uitgeverComboBox = new UitgeverComboBox( connection, parentComponent, false );
	statusComboBox = new StatusComboBox( connection );
	labelComboBox = new LabelComboBox( connection, parentComponent, false );

	setupBoekTableModel( null, 0, 0, 0 );
    }

    void setupBoekTableModel( String boekFilterString,
                              int    typeId,
                              int    uitgeverId,
                              int    statusId ) {
	// Setup the table
	try {
	    String boekQueryString =
		"SELECT boek.boek_id, boek.boek, boek.type_id, type.type, " +
		"boek.uitgever_id, uitgever.uitgever, uitgever.isbn_1, uitgever.isbn_2, " +
		"boek.isbn_3, boek.isbn_4, " +
		"boek.status_id, status.status, boek.label_id, label.label, boek.datum, boek.verwijderd " +
		"FROM boek " +
		"LEFT JOIN type ON type.type_id = boek.type_id " +
		"LEFT JOIN uitgever ON uitgever.uitgever_id = boek.uitgever_id " +
		"LEFT JOIN status ON status.status_id = boek.status_id " +
		"LEFT JOIN label ON label.label_id = boek.label_id ";

	    if ( ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) ||
		 ( uitgeverId != 0 ) || ( statusId != 0 ) || ( typeId != 0 ) ) {
		boekQueryString += "WHERE ";

		if ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) {
		    // Matcher to find single quotes in boekFilterString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( boekFilterString );
		    boekQueryString += "boek.boek LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
		    if ( ( uitgeverId != 0 ) || ( statusId != 0 ) || ( typeId != 0 ) ) {
			boekQueryString += "AND ";
		    }
		}

		if ( uitgeverId != 0 ) {
		    boekQueryString += "boek.uitgever_id = " + uitgeverId + " ";
		    if ( ( statusId != 0 ) || ( typeId != 0 ) ) {
			boekQueryString += "AND ";
		    }
		}

		if ( statusId != 0 ) {
		    boekQueryString += "boek.status_id = " + statusId + " ";
		    if (  typeId != 0 ) {
			boekQueryString += "AND ";
		    }
		}

		if ( typeId != 0 ) {
		    boekQueryString += "boek.type_id = " + typeId + " ";
		}
	    }

	    boekQueryString += "ORDER BY type.type, label.label, boek.boek";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( boekQueryString );

	    // Clear the list
	    boekRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		try {
		    String aanschafDatumString = resultSet.getString( 15 );
		    Date aanschafDate = null;
		    if ( ( aanschafDatumString != null ) && ( aanschafDatumString.length( ) > 0 ) ) {
			aanschafDate = dateFormat.parse( aanschafDatumString );
		    }
		    String verwijderdDatumString = resultSet.getString( 16 );
		    Date verwijderdDate = null;
		    if ( ( verwijderdDatumString != null ) && ( verwijderdDatumString.length( ) > 0 ) ) {
			verwijderdDate = dateFormat.parse( verwijderdDatumString );
		    }

		    String uitgeverIsbn1Isbn2String = null;
		    String uitgeverString = resultSet.getString( 6 );
		    if ( ( uitgeverString != null ) && ( uitgeverString.length( ) > 0 ) ) {
			uitgeverIsbn1Isbn2String = uitgeverString;

			String isbn1String = resultSet.getString( 7 );
			String isbn2String = resultSet.getString( 8 );
			if ( ( ( isbn1String != null ) && ( isbn1String.length( ) > 0 ) ) ||
			     ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) ) {
			    uitgeverIsbn1Isbn2String += " (";

			    if ( ( isbn1String != null ) && ( isbn1String.length( ) > 0 ) ) {
				uitgeverIsbn1Isbn2String += isbn1String;
				if ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) {
				    uitgeverIsbn1Isbn2String += ", ";
				}
			    }

			    if ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) {
				uitgeverIsbn1Isbn2String += isbn2String;
			    }

			    uitgeverIsbn1Isbn2String += ")";
			}
		    }

		    boekRecordList.add( new BoekRecord( resultSet.getInt( 1 ),
							resultSet.getString( 2 ),
							resultSet.getInt( 3 ),
							resultSet.getString( 4 ),
							resultSet.getInt( 5 ),
							uitgeverIsbn1Isbn2String,
							resultSet.getString( 9 ),
							resultSet.getString( 10 ),
							resultSet.getInt( 11 ),
							resultSet.getString( 12 ),
							resultSet.getInt( 13 ),
							resultSet.getString( 14 ),
							aanschafDate,
							verwijderdDate ) );
		} catch( ParseException parseException ) {
		    logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
		}
	    }

	    boekRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "BoekTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
            logger.severe("SQLException: " + sqlException.getMessage());
        }
    }

    public int getRowCount( ) { return boekRecordList.size( ); }

    public int getColumnCount( ) { return 10; }

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
	// Only allow editing for the selected edit row
	if ( row != editRow ) return false;

	switch ( column ) {
	case 0: // Id
	    // Do not allow editing
	    return false;
	}

	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= boekRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final BoekRecord boekRecord = boekRecordList.get( row );

	if ( column == 0 ) return boekRecord.boekId;
	if ( column == 1 ) return boekRecord.boekString;
	if ( column == 2 ) return boekRecord.typeString;
	if ( column == 3 ) return boekRecord.uitgeverIsbn1Isbn2String;
	if ( column == 4 ) return boekRecord.isbn3String;
	if ( column == 5 ) return boekRecord.isbn4String;
	if ( column == 6 ) return boekRecord.statusString;
	if ( column == 7 ) return boekRecord.labelString;
	if ( column == 8 ) {
	    // Check if aanschafDate is present
	    if ( boekRecord.aanschafDate == null ) return "";
	    // Convert the Date object to a string
	    return dateFormat.format( boekRecord.aanschafDate );
	}
	if ( column == 9 ) {
	    // Check if verwijderdDate is present
	    if ( boekRecord.verwijderdDate == null ) return "";
	    // Convert the Date object to a string
	    return dateFormat.format( boekRecord.verwijderdDate );
	}

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= boekRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	boolean verwijderdDateModified = false;

	try {
	    switch ( column ) {
	    case 1:
		String boekString = ( String )object;
		if ( ( ( boekString == null ) || ( boekString.length( ) == 0 ) ) &&
		     ( boekRecord.boekString != null ) ) {
		    boekRecord.boekString = null;
		    rowModified = true;
		} else if ( ( boekString != null ) &&
			    ( !boekString.equals( boekRecord.boekString ) ) ) {
		    boekRecord.boekString = boekString;
		    rowModified = true;
		}
		break;

	    case 2:
		int typeId = typeComboBox.getTypeId( ( String )object );
		if ( typeId != boekRecord.typeId ) {
		    boekRecord.typeId = typeId;
		    boekRecord.typeString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 3:
		int uitgeverId = uitgeverComboBox.getUitgeverId( ( String )object );
		if ( uitgeverId != boekRecord.uitgeverId ) {
		    boekRecord.uitgeverId = uitgeverId;
		    boekRecord.uitgeverIsbn1Isbn2String = ( String )object;
		    rowModified = true;
		}
		break;

	    case 4:
		String isbn3String = ( String )object;
		if ( isbn3String.length( ) > 6 ) isbn3String = isbn3String.substring( 0, 6 );
		if ( ( ( isbn3String == null ) || ( isbn3String.length( ) == 0 ) ) &&
		     ( boekRecord.isbn3String != null ) ) {
		    boekRecord.isbn3String = null;
		    rowModified = true;
		} else if ( ( isbn3String != null ) &&
			    ( !isbn3String.equals( boekRecord.isbn3String ) ) ) {
		    boekRecord.isbn3String = isbn3String;
		    rowModified = true;
		}
		break;

	    case 5:
		String isbn4String = ( String )object;
		if ( isbn4String.length( ) > 1 ) isbn4String = isbn4String.substring( 0, 1 );
		if ( ( ( isbn4String == null ) || ( isbn4String.length( ) == 0 ) ) &&
		     ( boekRecord.isbn4String != null ) ) {
		    boekRecord.isbn4String = null;
		    rowModified = true;
		} else if ( ( isbn4String != null ) &&
			    ( !isbn4String.equals( boekRecord.isbn4String ) ) ) {
		    boekRecord.isbn4String = isbn4String;
		    rowModified = true;
		}
		break;

	    case 6:
		int statusId = statusComboBox.getStatusId( ( String )object );
		if ( statusId != boekRecord.statusId ) {
                    boekRecord.statusId = statusId;
		    boekRecord.statusString = ( String )object;
		    rowModified = true;

	            // Check if status is set to verwijderd
	            if ( ( statusId == 100 ) && ( boekRecord.verwijderdDate == null ) ) {
                        GregorianCalendar calendar = new GregorianCalendar( );
	                boekRecord.verwijderdDate = calendar.getTime( );
	                verwijderdDateModified = true;
	            }
	            else if ( ( statusId != 100 ) && ( boekRecord.verwijderdDate != null ) ) {
	                boekRecord.verwijderdDate = null;
	                verwijderdDateModified = true;
        	    }
		}
		break;

	    case 7:
		int labelId = labelComboBox.getLabelId( ( String )object );
		if ( labelId != boekRecord.labelId ) {
		    boekRecord.labelId = labelId;
		    boekRecord.labelString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 8:
		String aanschafDatumString = ( String )object;

		// Check if string in table is null or empty, and date in record is not null
		if ( ( ( aanschafDatumString == null ) || ( aanschafDatumString.length( ) == 0 ) ) &&
		     ( boekRecord.aanschafDate != null ) ) {
		    // Set the aanschaf datum to null in the record
		    boekRecord.aanschafDate = null;
		    rowModified = true;
		} else {
		    // Convert the string to a Date object
		    try {
			Date aanschafDate = dateFormat.parse( aanschafDatumString );
			logger.info( "Date: " + aanschafDate );

			// Check if aanschafDate is valid, and unequal to aanschafDate in record
			if ( ( aanschafDate != null ) &&
			     ( !aanschafDate.equals( boekRecord.aanschafDate ) ) ) {
			    // Modified aanschafDate: update record
			    boekRecord.aanschafDate = aanschafDate;
			    rowModified = true;
			}
		    } catch( ParseException parseException ) {
			logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
		    }
		}
		break;

	    case 9:
		String verwijderdDatumString = ( String )object;

		// Check if string in table is null or empty, and date in record is not null
		if ( ( ( verwijderdDatumString == null ) || ( verwijderdDatumString.length( ) == 0 ) ) &&
		     ( boekRecord.verwijderdDate != null ) ) {
		    // Set the verwijderd datum to null in the record
		    boekRecord.verwijderdDate = null;
		    rowModified = true;
		} else {
		    // Convert the string to a Date object
		    try {
			Date verwijderdDate = dateFormat.parse( verwijderdDatumString );
			logger.info( "Date: " + verwijderdDate );

			// Check if verwijderdDate is valid, and unequal to verwijderdDate in record
			if ( ( verwijderdDate != null ) &&
			     ( !verwijderdDate.equals( boekRecord.verwijderdDate ) ) ) {
			    // Modified verwijderdDate: update record
			    boekRecord.verwijderdDate = verwijderdDate;
			    rowModified = true;
			}
		    } catch( ParseException parseException ) {
			logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
		    }
		}
		break;

	    default:
		logger.info( "Editing not yet supported for column: " + column );
		// logger.severe( "Invalid column: " + column );
		return;
	    }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " +
			   object + " for column " + column + " in row " + row );
	    return;
	}

	// Store record in list
	boekRecordList.set( row, boekRecord );

	if ( rowModified ) {
	    // Enable cancel and save buttons
	    cancelBoekButton.setEnabled( true );
	    saveBoekButton.setEnabled( true );
	}

	fireTableCellUpdated( row, column );
	if ( verwijderdDateModified ) fireTableCellUpdated( row, 9 );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return boekRecordList.size( ); }

    int getBoekId( int row ) {
        if ( ( row < 0 ) || ( row >= boekRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return (boekRecordList.get( row )).boekId;
    }

    String getBoekString( int row ) {
	if ( ( row < 0 ) || ( row >= boekRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( boekRecordList.get( row ) ).boekString;
    }

    void setEditRow( int editRow ) {
	// Initialize record to be edited
	boekRecord = boekRecordList.get( editRow );

	// Copy record to use as key in table update
	originalBoekRecord = new BoekRecord( boekRecord );

	// Initialize row modified status
	rowModified = false;

	// Allow editing for the selected row
	this.editRow = editRow;
    }

    void unsetEditRow( ) {
	this.editRow = -1;
    }

    void cancelEditRow( int row ) {
	// Check if row being canceled equals the row currently being edited
	if ( row != editRow ) return;

	// Check if row was modified
	if ( !rowModified ) return;

	// Initialize row modified status
	rowModified = false;

	// Store original record in list
	boekRecordList.set( row, originalBoekRecord );

	// Trigger update of table row data
	fireTableRowUpdated( row );
    }

    private String addToUpdateString( String updateString, String additionalUpdateString ) {
	if ( updateString != null ) {
	    return updateString + ", " + additionalUpdateString;
	}
	return additionalUpdateString;
    }

    boolean saveEditRow( int row ) {
	String updateString = null;
	boolean updateId = false;

	// Compare each field with the value in the original record
	// If modified, add entry to update query string

	String boekString = boekRecord.boekString;
	if ( ( boekString == null ) && ( originalBoekRecord.boekString != null ) ) {
	    updateString = addToUpdateString( updateString, "boek = NULL " );
	} else if ( ( boekString != null ) &&
		    ( !boekString.equals( originalBoekRecord.boekString ) ) ) {
	    // Matcher to find single quotes in boek, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( boekString );
	    updateString = addToUpdateString( updateString, "boek = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	int typeId = boekRecord.typeId;
	if ( typeId != originalBoekRecord.typeId ) {
	    updateString = addToUpdateString( updateString, "type_id = " + typeId );
	}

	int uitgeverId = boekRecord.uitgeverId;
	if ( uitgeverId != originalBoekRecord.uitgeverId ) {
	    updateString = addToUpdateString( updateString, "uitgever_id = " + uitgeverId );
	}

	String isbn3String = boekRecord.isbn3String;
	if ( ( isbn3String == null ) && ( originalBoekRecord.isbn3String != null ) ) {
	    updateString = addToUpdateString( updateString, "isbn_3 = NULL " );
	} else if ( ( isbn3String != null ) &&
		    ( !isbn3String.equals( originalBoekRecord.isbn3String ) ) ) {
	    updateString = addToUpdateString( updateString, "isbn_3 = '" + isbn3String + "'" );
	}

	String isbn4String = boekRecord.isbn4String;
	if ( ( isbn4String == null ) && ( originalBoekRecord.isbn4String != null ) ) {
	    updateString = "isbn_4 = NULL ";
	} else if ( ( isbn4String != null ) &&
		    ( !isbn4String.equals( originalBoekRecord.isbn4String ) ) ) {
	    updateString = addToUpdateString( updateString, "isbn_4 = '" + isbn4String + "'" );
	}

	int statusId = boekRecord.statusId;
	if ( statusId != originalBoekRecord.statusId ) {
	    updateString = addToUpdateString( updateString, "status_id = " + statusId );
	}

	int labelId = boekRecord.labelId;
	if ( labelId != originalBoekRecord.labelId ) {
	    updateString = addToUpdateString( updateString, "label_id = " + labelId );
	}

	Date aanschafDate = boekRecord.aanschafDate;
	if ( ( aanschafDate == null ) && ( originalBoekRecord.aanschafDate != null ) ) {
	    updateString = addToUpdateString( updateString, "datum = NULL " );
	} else if ( ( aanschafDate != null ) &&
		    ( !aanschafDate.equals( originalBoekRecord.aanschafDate ) ) ) {
	    // Convert the date object to a string
	    String aanschafDatumString  = dateFormat.format( boekRecord.aanschafDate );
	    updateString = addToUpdateString( updateString, "datum = '" + aanschafDatumString + "'" );
	}

	Date verwijderdDate = boekRecord.verwijderdDate;
	if ( ( verwijderdDate == null ) && ( originalBoekRecord.verwijderdDate != null ) ) {
	    updateString = addToUpdateString( updateString, "verwijderd = NULL " );
	} else if ( ( verwijderdDate != null ) &&
		    ( !verwijderdDate.equals( originalBoekRecord.verwijderdDate ) ) ) {
	    // Convert the date object to a string
	    String verwijderdDatumString  = dateFormat.format( boekRecord.verwijderdDate );
	    updateString = addToUpdateString( updateString, "verwijderd = '" + verwijderdDatumString + "'" );
	}

	// Check if update is not necessary
	if ( updateString == null ) return true;

	updateString = "UPDATE boek SET " + updateString + " WHERE boek_id = " + originalBoekRecord.boekId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with boek_id " + originalBoekRecord.boekId +
			       " in boek, nUpdate = " + nUpdate );
		return false;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "BoekTableModel exception",
                                          JOptionPane.ERROR_MESSAGE);
            logger.severe("SQLException: " + sqlException.getMessage());
            return false;
        }

	// Store record in list
	// logger.info( "storing record at row " + row );
	boekRecordList.set( row, boekRecord );

	// Initialize row modified status
	rowModified = false;

	// Trigger update of table row data
	fireTableRowUpdated( row );

	// Successful completion
	return true;
    }

    private void fireTableRowUpdated( int row ) {
	for ( int column = 0; column < getColumnCount( ); column++ ) {
	    fireTableCellUpdated( row, column );
	}
    }

    boolean getRowModified( ) { return rowModified; }
}

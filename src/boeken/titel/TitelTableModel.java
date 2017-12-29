// Class to setup a TableModel for records in titel

package boeken.titel;

import boeken.gui.AuteursComboBox;
import boeken.gui.OnderwerpComboBox;
import boeken.gui.TaalComboBox;
import boeken.gui.TitelKey;
import boeken.gui.VormComboBox;

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


class TitelTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( "boeken.titel.TitelTableModel" );

    private final Connection connection;
    private final Component  parentComponent;

    private final String[ ] headings = { "Titel", "Auteur", "Jaar copyright", "Onderwerp",
                                         "Vorm", "Taal", "Opmerkingen", "Boek" };

    private class TitelRecord {
	String  titelString;
	String  persoonString;
	int	copyrightJaar;
	int	onderwerpId;
	String  onderwerpString;
	int	vormId;
	String  vormString;
	int	taalId;
	String  taalString;
	String	opmerkingenString;
	String  boekString;
	int	auteursId;
	int	boekId;

	TitelRecord( String  titelString,
                     String  persoonString,
                     int     copyrightJaar,
                     int     onderwerpId,
                     String  onderwerpString,
                     int     vormId,
                     String  vormString,
                     int     taalId,
                     String  taalString,
                     String  opmerkingenString,
                     String  boekString,
                     int     auteursId,
                     int     boekId ) {
	    this.titelString = titelString;
	    this.persoonString = persoonString;
	    this.copyrightJaar = copyrightJaar;
	    this.onderwerpId = onderwerpId;
	    this.onderwerpString = onderwerpString;
	    this.vormId = vormId;
	    this.vormString = vormString;
	    this.taalId = taalId;
	    this.taalString = taalString;
	    this.opmerkingenString = opmerkingenString;
	    this.boekString = boekString;
	    this.auteursId = auteursId;
	    this.boekId = boekId;
	}

	// Copy constructor
	TitelRecord( TitelRecord titelRecord ) {
	    this.titelString = titelRecord.titelString;
	    this.persoonString = titelRecord.persoonString;
	    this.copyrightJaar = titelRecord.copyrightJaar;
	    this.onderwerpId = titelRecord.onderwerpId;
	    this.onderwerpString = titelRecord.onderwerpString;
	    this.vormId = titelRecord.vormId;
	    this.vormString = titelRecord.vormString;
	    this.taalId = titelRecord.taalId;
	    this.taalString = titelRecord.taalString;
	    this.opmerkingenString = titelRecord.opmerkingenString;
	    this.boekString = titelRecord.boekString;
	    this.auteursId = titelRecord.auteursId;
	    this.boekId = titelRecord.boekId;
	}
    }

    private final ArrayList<TitelRecord> titelRecordList = new ArrayList<>( 500 );

    private AuteursComboBox auteursComboBox;
    private OnderwerpComboBox onderwerpComboBox;
    private VormComboBox vormComboBox;
    private TaalComboBox taalComboBox;

    private JButton cancelTitelButton;
    private JButton saveTitelButton;

    private boolean	rowModified = false;
    private int		editRow = -1;
    private TitelRecord	titelRecord = null;
    private TitelRecord	originalTitelRecord = null;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    TitelTableModel( final Connection connection,
                     final Component  parentComponent,
                     final JButton    cancelTitelButton,
                     final JButton    saveTitelButton ) {
	this.connection = connection;
	this.parentComponent = parentComponent;
	this.cancelTitelButton = cancelTitelButton;
	this.saveTitelButton = saveTitelButton;

	// Create the combo boxes
        auteursComboBox = new AuteursComboBox(connection, parentComponent, false);
	onderwerpComboBox = new OnderwerpComboBox( connection );
	vormComboBox = new VormComboBox( connection );
	taalComboBox = new TaalComboBox( connection );

	setupTitelTableModel( null, null, null, 0, 0, 0, 0 );
    }

    // Setup the table
    void setupTitelTableModel( String boekFilterString,
                               String titelFilterString,
                               String opmerkingenFilterString,
                               int    auteursId,
                               int    onderwerpId,
                               int    vormId,
                               int    taalId ) {
	try {
	    String titelQueryString =
		"SELECT titel.titel, persoon.persoon, " +
		"titel.jaar, titel.onderwerp_id, onderwerp.onderwerp, " +
		"titel.vorm_id, vorm.vorm, " +
		"titel.taal_id, taal.taal, titel.opmerkingen, " +
		"boek.boek, titel.auteurs_id, titel.boek_id " +
		"FROM titel " +
		"LEFT JOIN auteurs_persoon ON auteurs_persoon.auteurs_id = titel.auteurs_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = auteurs_persoon.persoon_id " +
		"LEFT JOIN onderwerp ON onderwerp.onderwerp_id = titel.onderwerp_id " +
		"LEFT JOIN vorm ON vorm.vorm_id = titel.vorm_id " +
		"LEFT JOIN taal ON taal.taal_id = titel.taal_id " +
		"LEFT JOIN boek ON boek.boek_id = titel.boek_id ";

	    if ( ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) ||
		 ( ( titelFilterString != null ) && ( titelFilterString.length( ) > 0 ) ) ||
		 ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
		 ( auteursId != 0 ) ||
		 ( onderwerpId != 0 ) ||
		 ( vormId != 0 ) ||
		 ( taalId != 0 ) ) {
		titelQueryString += "WHERE ";

		if ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) {
		    titelQueryString += "boek.boek LIKE \"%" + boekFilterString + "%\" ";
		    if ( ( ( titelFilterString != null ) && ( titelFilterString.length( ) > 0 ) ) ||
			 ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
			 ( auteursId != 0 ) ||
			 ( onderwerpId != 0 ) ||
			 ( vormId != 0 ) ||
			 ( taalId != 0 ) ) {
			titelQueryString += "AND ";
		    }
		}

		if ( ( titelFilterString != null ) && ( titelFilterString.length( ) > 0 ) ) {
		    titelQueryString += "titel.titel LIKE \"%" + titelFilterString + "%\" ";
		    if ( ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
			 ( auteursId != 0 ) ||
			 ( onderwerpId != 0 ) ||
			 ( vormId != 0 ) ||
			 ( taalId != 0 ) ) {
			titelQueryString += "AND ";
		    }
		}

		if ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) {
		    titelQueryString += "titel.opmerkingen LIKE \"%" + opmerkingenFilterString + "%\" ";
		    if ( ( auteursId != 0 ) ||
			 ( onderwerpId != 0 ) ||
			 ( vormId != 0 ) ||
			 ( taalId != 0 ) ) {
			titelQueryString += "AND ";
		    }
		}

		if ( auteursId != 0 ) {
		    titelQueryString += "titel.auteurs_id = " + auteursId + " ";
		    if ( ( onderwerpId != 0 ) ||
			 ( vormId != 0 ) ||
			 ( taalId != 0 ) ) {
			titelQueryString += "AND ";
		    }
		}

		if ( onderwerpId != 0 ) {
		    titelQueryString += "titel.onderwerp_id = " + onderwerpId + " ";
		    if ( ( vormId != 0 ) ||
			 ( taalId != 0 ) ) {
			titelQueryString += "AND ";
		    }
		}

		if ( vormId != 0 ) {
		    titelQueryString += "titel.vorm_id = " + vormId + " ";
		    if ( taalId != 0 ) {
			titelQueryString += "AND ";
		    }
		}

		if ( taalId != 0 ) {
		    titelQueryString += "titel.taal_id = " + taalId + " ";
		}
	    }

	    titelQueryString += "ORDER BY persoon.persoon, titel.titel";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( titelQueryString );

	    // Clear the list
	    titelRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		titelRecordList.add( new TitelRecord( resultSet.getString( 1 ),
						      resultSet.getString( 2 ),
						      resultSet.getInt( 3 ),
						      resultSet.getInt( 4 ),
						      resultSet.getString( 5 ),
						      resultSet.getInt( 6 ),
						      resultSet.getString( 7 ),
						      resultSet.getInt( 8 ),
						      resultSet.getString( 9 ),
						      resultSet.getString( 10 ),
						      resultSet.getString( 11 ),
						      resultSet.getInt( 12 ),
						      resultSet.getInt( 13 ) ) );
	    }

	    titelRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in select: " + sqlException.getMessage(),
                                          "TitelTableModel SQL exception",
                                          JOptionPane.ERROR_MESSAGE);
            logger.severe("SQLException: " + sqlException.getMessage());
        }
    }

    public int getRowCount( ) { return titelRecordList.size( ); }

    public int getColumnCount( ) { return 8; }

    // Indicate the class for each column for setting the correct default renderer
    // see file:///home/cvengelen/java/tutorial/uiswing/components/table.html
    public Class getColumnClass( int column ) {
	switch ( column ) {
	case 2: // jaar
	    return Integer.class;
	}

	return String.class;
    }

    public boolean isCellEditable( int row, int column ) {
	// Only allow editing for the selected edit row
	if ( row != editRow ) return false;

	switch ( column ) {
	case 7: // boek
	    // Do not allow editing
	    return false;
	}

	// Allow editing
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= titelRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final TitelRecord titelRecord = titelRecordList.get( row );

	if ( column == 0 ) return titelRecord.titelString;
	if ( column == 1 ) return titelRecord.persoonString;
	if ( column == 2 ) return titelRecord.copyrightJaar;
	if ( column == 3 ) return titelRecord.onderwerpString;
	if ( column == 4 ) return titelRecord.vormString;
	if ( column == 5 ) return titelRecord.taalString;
	if ( column == 6 ) return titelRecord.opmerkingenString;
	if ( column == 7 ) return titelRecord.boekString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= titelRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	try {
	    switch ( column ) {
            case 0:
                String titelString = (String)object;
                if (((titelString == null) || (titelString.length() == 0)) &&
                        (titelRecord.titelString != null)) {
                    titelRecord.titelString = null;
                    rowModified = true;
                } else if ((titelString != null) &&
                        (!titelString.equals(titelRecord.titelString))) {
                    titelRecord.titelString = titelString;
                    rowModified = true;
                }
                break;

            case 1:
                int auteursId = auteursComboBox.getAuteursId((String)object);
                if (auteursId != titelRecord.auteursId) {
                    titelRecord.auteursId = auteursId;
                    titelRecord.persoonString = (String)object;
                    rowModified = true;
                }
                break;

            case 2:
                int copyrightJaar = 0;
                if (object != null) copyrightJaar = (Integer)object;
                if ((copyrightJaar == 0) && (titelRecord.copyrightJaar != 0)) {
                    titelRecord.copyrightJaar = 0;
                    rowModified = true;
                } else if (copyrightJaar != titelRecord.copyrightJaar) {
                    titelRecord.copyrightJaar = copyrightJaar;
                    rowModified = true;
                }
                break;

            case 3:
                int onderwerpId = onderwerpComboBox.getOnderwerpId((String)object);
                if (onderwerpId != titelRecord.onderwerpId) {
                    titelRecord.onderwerpId = onderwerpId;
                    titelRecord.onderwerpString = (String)object;
                    rowModified = true;
                }
                break;

            case 4:
                int vormId = vormComboBox.getVormId((String)object);
                if (vormId != titelRecord.vormId) {
                    titelRecord.vormId = vormId;
                    titelRecord.vormString = (String)object;
                    rowModified = true;
                }
                break;

            case 5:
                int taalId = taalComboBox.getTaalId((String)object);
                if (taalId != titelRecord.taalId) {
                    titelRecord.taalId = taalId;
                    titelRecord.taalString = (String)object;
                    rowModified = true;
                }
                break;

            case 6:
                String opmerkingenString = (String)object;
                if (((opmerkingenString == null) || (opmerkingenString.length() == 0)) &&
                        (titelRecord.opmerkingenString != null)) {
                    titelRecord.opmerkingenString = null;
                    rowModified = true;
                } else if ((opmerkingenString != null) &&
                        (!opmerkingenString.equals(titelRecord.opmerkingenString))) {
                    titelRecord.opmerkingenString = opmerkingenString;
                    rowModified = true;
                }
                break;

            default:
                logger.info("Editing not yet supported for column: " + column);
                // logger.severe( "Invalid column: " + column );
                return;
            }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " +
			   object + " for column " + column + " in row " + row );
	    return;
	}

	// Store record in list
	titelRecordList.set( row, titelRecord );

	if ( rowModified ) {
	    // Enable cancel and save buttons
	    cancelTitelButton.setEnabled( true );
	    saveTitelButton.setEnabled( true );
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return titelRecordList.size( ); }

    TitelKey getTitelKey( int row ) {
	final TitelKey nullTitelKey = new TitelKey( );

	final TitelRecord titelRecord = titelRecordList.get( row );

	String titelString = titelRecord.titelString;

	if ( titelString == null ) return nullTitelKey;
	if ( titelString.length( ) == 0 ) return nullTitelKey;

	int auteursId = titelRecord.auteursId;
	int boekId = titelRecord.boekId;

	return new TitelKey( boekId, auteursId, titelString );
    }


    void setEditRow( int editRow ) {
	// Initialize record to be edited
	titelRecord = titelRecordList.get( editRow );

	// Copy record to use as key in table update
	originalTitelRecord = new TitelRecord( titelRecord );

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
	titelRecordList.set( row, originalTitelRecord );

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

	// Compare each field with the value in the original record
	// If modified, add entry to update query string

	Matcher quoteMatcher = null;  // Matcher to find single quotes in a string

	String titelString = titelRecord.titelString;
	if ( ( ( titelString == null ) || ( titelString.length( ) == 0 ) ) &&
	     ( originalTitelRecord.titelString != null ) &&
	     ( originalTitelRecord.titelString.length( ) != 0 ) ) {
	    // titel is declared "NOT NULL", and therefore cannot be set to NULL
	    // See the MySQL reference manual section 13.2.10
	    updateString = addToUpdateString( updateString, "titel = '' " );
	} else if ( ( titelString != null ) &&
		    ( !titelString.equals( originalTitelRecord.titelString ) ) ) {
	    // Matcher to find single quotes in titel, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    quoteMatcher = quotePattern.matcher( titelString );
	    updateString = addToUpdateString( updateString,
					      "titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	int copyrightJaar = titelRecord.copyrightJaar;
	if ( ( copyrightJaar == 0 ) && ( originalTitelRecord.copyrightJaar != 0 ) ) {
	    updateString = addToUpdateString( updateString, " jaar = NULL " );
	} else if ( copyrightJaar != titelRecord.copyrightJaar ) {
	    updateString = addToUpdateString( updateString, "jaar = " + copyrightJaar );
	}

	int onderwerpId = titelRecord.onderwerpId;
	if ( onderwerpId != originalTitelRecord.onderwerpId ) {
	    updateString = addToUpdateString( updateString, "onderwerp_id = " + onderwerpId );
	}

	int vormId = titelRecord.vormId;
	if ( vormId != originalTitelRecord.vormId ) {
	    updateString = addToUpdateString( updateString, "vorm_id = " + vormId );
	}

	int taalId = titelRecord.taalId;
	if ( taalId != originalTitelRecord.taalId ) {
	    updateString = addToUpdateString( updateString, "taal_id = " + taalId );
	}

	String opmerkingenString = titelRecord.opmerkingenString;
	if ( ( ( opmerkingenString == null ) || ( opmerkingenString.length( ) == 0 ) ) &&
	     ( originalTitelRecord.opmerkingenString != null ) &&
	     ( originalTitelRecord.opmerkingenString.length( ) != 0 ) ) {
	    updateString = addToUpdateString( updateString, "opmerkingen = null " );
	} else if ( ( opmerkingenString != null ) &&
		    ( !opmerkingenString.equals( originalTitelRecord.opmerkingenString ) ) ) {
	    // Matcher to find single quotes in opmerkingen, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    quoteMatcher = quotePattern.matcher( opmerkingenString );
	    updateString = addToUpdateString( updateString,
					      "opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

        int auteursId = titelRecord.auteursId;
        if ( auteursId != originalTitelRecord.auteursId ) {
            updateString = addToUpdateString(updateString, "auteurs_id = " + auteursId);
        }

        // Check if update is not necessary
	if ( updateString == null ) return true;

	updateString = "UPDATE titel SET " + updateString;

	final TitelKey titelKey	= new TitelKey( originalTitelRecord.boekId,
						originalTitelRecord.auteursId,
						originalTitelRecord.titelString );

	// Use the original key for selection of the existing record
	// Matcher to find single quotes in titel, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	quoteMatcher = quotePattern.matcher( titelKey.getTitelString( ) );
	updateString += " WHERE titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	updateString += " AND auteurs_id = " + titelKey.getAuteursId( );
	updateString += " AND boek_id = " + titelKey.getBoekId( );

	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with titel " + titelKey.getTitelString( ) +
			       " in titel, nUpdate = " + nUpdate );
		return false;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog(parentComponent,
                                          "SQL exception in update: " + sqlException.getMessage(),
                                          "TitelTableModel SQL exception",
                                          JOptionPane.ERROR_MESSAGE);
            logger.severe("SQLException: " + sqlException.getMessage());
            return false;
        }

	// Store record in list
	// logger.info( "storing record at row " + row );
	titelRecordList.set( row, titelRecord );

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

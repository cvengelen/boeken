// Class to setup a ComboBox for selection of status

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class StatusComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.StatusComboBox" );

    private Connection connection;

    private Map< String, Integer > statusMap = new HashMap< String, Integer >( );


    public StatusComboBox( Connection connection ) {
        this.connection = connection;

        // Setup the status combo box
        setupStatusComboBox( 0 );
    }

    public StatusComboBox( Connection connection,
                           int selectedStatusId ) {
        this.connection = connection;

        // Setup the status combo box
        setupStatusComboBox( selectedStatusId );
    }

    void setupStatusComboBox( int selectedStatusId ) {
        // Remove all existing items in the status combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        try {
            // Fill the combo box and hash table
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT status_id, status FROM status ORDER BY status" );
            while ( resultSet.next( ) ) {
                String statusString = resultSet.getString( 2 );

                // Store the status_id in the map indexed by statusString
                statusMap.put( statusString, resultSet.getInt( 1 ) );

                // Add the item string to the combo box
                addItem( statusString );

                // Check if this is the selected statusString
                if ( resultSet.getInt( 1 ) == selectedStatusId ) {
                    // Select this status
                    setSelectedItem( statusString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String getSelectedStatusString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedStatusId( ) {
        return getStatusId( ( String ) getSelectedItem( ) );
    }


    public int getStatusId( String statusString ) {
        if ( statusString == null ) return 0;

        // Check if empty string is selected
        if ( statusString.length( ) == 0 ) return 0;

        // Get the status_id from the map
        if ( statusMap.containsKey( statusString ) ) {
            return statusMap.get( statusString );
        }

        return 0;
    }
}

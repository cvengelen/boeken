// Class to setup a ComboBox for selection of taal

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class TaalComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.TaalComboBox" );

    private Connection connection;

    private Map< String, Integer > taalMap = new HashMap< String, Integer >( );


    public TaalComboBox( Connection connection ) {
        this.connection = connection;

        // Setup the taal combo box
        setupTaalComboBox( 0 );
    }

    public TaalComboBox( Connection connection,
                         int selectedTaalId ) {
        this.connection = connection;

        // Setup the taal combo box
        setupTaalComboBox( selectedTaalId );
    }

    void setupTaalComboBox( int selectedTaalId ) {
        // Remove all existing items in the taal combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );


        try {
            // Fill the combo box and hash table
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT taal_id, taal FROM taal ORDER BY taal" );
            while ( resultSet.next( ) ) {
                String taalString = resultSet.getString( 2 );

                // Store the taal_id in the map indexed by taalString
                taalMap.put( taalString, resultSet.getInt( 1 ) );

                // Add the item string to the combo box
                addItem( taalString );

                // Check if this is the selected taalString
                if ( resultSet.getInt( 1 ) == selectedTaalId ) {
                    // Select this taal
                    setSelectedItem( taalString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String getSelectedTaalString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedTaalId( ) {
        return getTaalId( ( String ) getSelectedItem( ) );
    }


    public int getTaalId( String taalString ) {
        if ( taalString == null ) return 0;

        // Check if empty string is selected
        if ( taalString.length( ) == 0 ) return 0;

        // Get the taal_id from the map
        if ( taalMap.containsKey( taalString ) ) {
            return taalMap.get( taalString );
        }

        return 0;
    }
}

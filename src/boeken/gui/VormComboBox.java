// Class to setup a ComboBox for selection of vorm

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class VormComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.VormComboBox" );

    private Connection connection;

    private Map< String, Integer > vormMap = new HashMap< String, Integer >( );


    public VormComboBox( Connection connection ) {
        this.connection = connection;

        // Setup the vorm combo box
        setupVormComboBox( 0 );
    }

    public VormComboBox( Connection connection,
                         int selectedVormId ) {
        this.connection = connection;

        // Setup the vorm combo box
        setupVormComboBox( selectedVormId );
    }

    void setupVormComboBox( int selectedVormId ) {
        // Remove all existing items in the vorm combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );


        try {
            // Fill the combo box and hash table
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT vorm_id, vorm FROM vorm ORDER BY vorm" );
            while ( resultSet.next( ) ) {
                String vormString = resultSet.getString( 2 );

                // Store the vorm_id in the map indexed by vormString
                vormMap.put( vormString, resultSet.getInt( 1 ) );

                // Add the item string to the combo box
                addItem( vormString );

                // Check if this is the selected vormString
                if ( resultSet.getInt( 1 ) == selectedVormId ) {
                    // Select this vorm
                    setSelectedItem( vormString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String getSelectedVormString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedVormId( ) {
        return getVormId( ( String ) getSelectedItem( ) );
    }


    public int getVormId( String vormString ) {
        if ( vormString == null ) return 0;

        // Check if empty string is selected
        if ( vormString.length( ) == 0 ) return 0;

        // Get the vorm_id from the map
        if ( vormMap.containsKey( vormString ) ) {
            return vormMap.get( vormString );
        }

        return 0;
    }
}

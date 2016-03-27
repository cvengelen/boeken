// Class to setup a ComboBox for selection of onderwerp

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class OnderwerpComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.OnderwerpComboBox" );

    private Connection connection;

    private Map< String, Integer > onderwerpMap = new HashMap< String, Integer >( );


    public OnderwerpComboBox( Connection connection ) {
        this.connection = connection;

        // Setup the onderwerp combo box
        setupOnderwerpComboBox( 0 );
    }

    public OnderwerpComboBox( Connection connection,
                              int selectedOnderwerpId ) {
        this.connection = connection;

        // Setup the onderwerp combo box
        setupOnderwerpComboBox( selectedOnderwerpId );
    }

    void setupOnderwerpComboBox( int selectedOnderwerpId ) {
        // Remove all existing items in the onderwerp combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );


        try {
            // Fill the combo box and hash table
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT onderwerp_id, onderwerp FROM onderwerp ORDER BY onderwerp" );
            while ( resultSet.next( ) ) {
                String onderwerpString = resultSet.getString( 2 );

                // Store the onderwerp_id in the map indexed by onderwerpString
                onderwerpMap.put( onderwerpString, resultSet.getInt( 1 ) );

                // Add the item string to the combo box
                addItem( onderwerpString );

                // Check if this is the selected onderwerpString
                if ( resultSet.getInt( 1 ) == selectedOnderwerpId ) {
                    // Select this onderwerp
                    setSelectedItem( onderwerpString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String getSelectedOnderwerpString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedOnderwerpId( ) {
        return getOnderwerpId( ( String ) getSelectedItem( ) );
    }


    public int getOnderwerpId( String onderwerpString ) {
        if ( onderwerpString == null ) return 0;

        // Check if empty string is selected
        if ( onderwerpString.length( ) == 0 ) return 0;

        // Get the onderwerp_id from the map
        if ( onderwerpMap.containsKey( onderwerpString ) ) {
            return onderwerpMap.get( onderwerpString );
        }

        return 0;
    }
}

// Class to setup a ComboBox for selection of type

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class TypeComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.TypeComboBox" );

    private Connection connection;

    private Map< String, Integer > typeMap = new HashMap< String, Integer >( );


    public TypeComboBox( Connection connection ) {
        this.connection = connection;

        // Setup the type combo box
        setupTypeComboBox( 0 );
    }

    public TypeComboBox( Connection connection,
                         int selectedTypeId ) {
        this.connection = connection;

        // Setup the type combo box
        setupTypeComboBox( selectedTypeId );
    }

    void setupTypeComboBox( int selectedTypeId ) {
        // Remove all existing items in the type combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );


        try {
            // Fill the combo box and hash table
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT type_id, type FROM type ORDER BY type" );
            while ( resultSet.next( ) ) {
                String typeString = resultSet.getString( 2 );

                // Store the type_id in the map indexed by typeString
                typeMap.put( typeString, resultSet.getInt( 1 ) );

                // Add the item string to the combo box
                addItem( typeString );

                // Check if this is the selected typeString
                if ( resultSet.getInt( 1 ) == selectedTypeId ) {
                    // Select this type
                    setSelectedItem( typeString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }

    public String getSelectedTypeString( ) {
        return ( String ) getSelectedItem( );
    }

    public int getSelectedTypeId( ) {
        return getTypeId( ( String ) getSelectedItem( ) );
    }

    public int getTypeId( String typeString ) {
        if ( typeString == null ) return 0;

        // Check if empty string is selected
        if ( typeString.length( ) == 0 ) return 0;

        // Get the type_id from the map
        if ( typeMap.containsKey( typeString ) ) {
            return typeMap.get( typeString );
        }

        return 0;
    }
}

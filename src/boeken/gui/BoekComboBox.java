// Class to setup a ComboBox for selection of a boek record

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;

public class BoekComboBox extends JComboBox< String > {
    final private Logger logger = Logger.getLogger( "boeken.gui.BoekComboBox" );

    private Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map< String, Integer > boekMap = new HashMap< String, Integer >( );
    private int selectedBoekId = 0;
    private String boekFilterString = null;
    private String newBoekString = null;


    public BoekComboBox( Connection connection,
                         Object parentObject,
                         String boekFilterString ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.boekFilterString = boekFilterString;

        // Setup the boek combo box
        setupBoekComboBox( );
    }


    public BoekComboBox( Connection connection,
                         Object parentObject,
                         int selectedBoekId ) {
        this.connection = connection;
        this.parentObject = parentObject;

        // Setup the boek combo box
        setupBoekComboBox( selectedBoekId );
    }


    public void setupBoekComboBox( int selectedBoekId ) {
        this.selectedBoekId = selectedBoekId;

        // Setup the boek combo box
        setupBoekComboBox( );
    }


    void setupBoekComboBox( ) {
        // Remove all existing items in the boek combo box
        removeAllItems( );

        // Add first empty item
        addItem( "" );

        // Add special item to insert new boek
        newBoekString = "Nieuw boek ";
        if ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) {
            newBoekString += boekFilterString + " ";
        }
        newBoekString += "toevoegen";
        addItem( newBoekString );

        if ( !boekMap.isEmpty( ) ) {
            // Remove all items in the boek hash table
            boekMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String boekQueryString =
                    "SELECT boek_id, boek, type, label FROM boek " +
                            "LEFT JOIN type ON boek.type_id = type.type_id " +
                            "LEFT JOIN label ON boek.label_id = label.label_id ";

            // Check for a boek filter
            if ( ( boekFilterString != null ) && ( boekFilterString.length( ) > 0 ) ) {
                // Add filter to query
                boekQueryString += "WHERE boek LIKE '%" + boekFilterString + "%' ";
            }

            // Add order to query
            boekQueryString += "ORDER BY type, label, boek";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( boekQueryString );

            while ( resultSet.next( ) ) {
                String labelString = resultSet.getString( 4 );
                if ( labelString == null ) {
                    labelString = "Geen label";
                }

                String boekTitelString = "(" + labelString + ") " + resultSet.getString( 2 );
                if ( boekTitelString.length( ) > 90 ) {
                    boekTitelString = boekTitelString.substring( 0, 90 );
                }

                // Store the boek_id in the map indexed by the boekTitelString
                boekMap.put( boekTitelString, resultSet.getInt( 1 ) );

                // Add the boekTitelString to the combo box
                addItem( boekTitelString );

                // Check if this is the default boek
                if ( resultSet.getInt( 1 ) == selectedBoekId ) {
                    // Select this boek
                    setSelectedItem( boekTitelString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterBoekComboBox( ) {
        String newBoekFilterString = null;

        // Prompt for the boek filter, using the current value as default
        if ( parentObject instanceof JFrame ) {
            newBoekFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JFrame ) parentObject,
                            "Boek filter:",
                            "Boek filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            boekFilterString );
        } else if ( parentObject instanceof JDialog ) {
            newBoekFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JDialog ) parentObject,
                            "Boek filter:",
                            "Boek filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            boekFilterString );
        }

        // Check if dialog was completed successfully (i.e., not canceled)
        if ( newBoekFilterString != null ) {
            // Store the new boek filter
            boekFilterString = newBoekFilterString;

            // Setup the boek combo box with the boek filter
            // Reset the selected boek ID in order to avoid immediate selection
            setupBoekComboBox( 0 );
        }

        // Return current boek filter string, also when dialog has been canceled
        return boekFilterString;
    }


    public String getSelectedBoekString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedBoekId( ) {
        String boekString = ( String ) getSelectedItem( );

        if ( boekString == null ) return 0;

        // Check if empty string is selected
        if ( boekString.length( ) == 0 ) return 0;

        // Get the boek_id from the map
        if ( boekMap.containsKey( boekString ) ) {
            return boekMap.get( boekString );
        }

        return 0;
    }


    public boolean newBoekSelected( ) {
        String boekString = ( String ) getSelectedItem( );

        // Check if empty string is selected
        if ( boekString == null ) return false;

        return boekString.equals( newBoekString );
    }
}

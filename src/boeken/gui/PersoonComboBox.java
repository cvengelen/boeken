// Class to setup a ComboBox for selection of a persoon record

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import javax.swing.*;

import java.util.logging.Logger;

public class PersoonComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.PersoonComboBox" );

    private Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map< String, Integer > persoonMap = new HashMap< String, Integer >( );
    private int selectedPersoonId = 0;
    private String persoonFilterString = null;
    private String newPersoonString = null;


    public PersoonComboBox( Connection connection,
                            Object parentObject ) {
        this.connection = connection;
        this.parentObject = parentObject;

        // Setup the persoon combo box
        setupPersoonComboBox( );
    }


    public PersoonComboBox( Connection connection,
                            Object parentObject,
                            int selectedPersoonId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.selectedPersoonId = selectedPersoonId;

        // Setup the persoon combo box
        setupPersoonComboBox( );
    }


    public void setupPersoonComboBox( int selectedPersoonId ) {
        this.selectedPersoonId = selectedPersoonId;

        // Setup the persoon combo box
        setupPersoonComboBox( );
    }


    void setupPersoonComboBox( ) {
        // Remove all existing items in the persoon combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        // Add special item to insert new persoon
        newPersoonString = "Nieuwe persoon ";
        if ( ( persoonFilterString != null ) && ( persoonFilterString.length( ) > 0 ) ) {
            newPersoonString += persoonFilterString + " ";
        }
        newPersoonString += "toevoegen";
        addItem( newPersoonString );

        if ( !persoonMap.isEmpty( ) ) {
            // Remove all items in the persoon hash table
            persoonMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String persoonQueryString = "SELECT persoon_id, persoon FROM persoon ";

            // Check for a persoon filter
            if ( ( persoonFilterString != null ) && ( persoonFilterString.length( ) > 0 ) ) {
                // Add filter to query
                persoonQueryString += "WHERE persoon LIKE '%" + persoonFilterString + "%' ";
            }

            // Add order to query
            persoonQueryString += "ORDER BY persoon";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( persoonQueryString );

            while ( resultSet.next( ) ) {
                String persoonString = resultSet.getString( 2 );

                // Store the persoon_id in the map indexed by the persoonString
                persoonMap.put( persoonString, resultSet.getInt( 1 ) );

                // Add the persoonString to the combo box
                addItem( persoonString );

                // Check if this is the selected persoon
                if ( resultSet.getInt( 1 ) == selectedPersoonId ) {
                    // Select this persoon
                    setSelectedItem( persoonString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterPersoonComboBox( ) {
        String newPersoonFilterString = null;

        // Prompt for the persoon filter, using the current value as default
        if ( parentObject instanceof JFrame ) {
            newPersoonFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JFrame ) parentObject,
                            "Persoon filter:",
                            "Persoon filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            persoonFilterString );
        } else if ( parentObject instanceof JDialog ) {
            newPersoonFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JDialog ) parentObject,
                            "Persoon filter:",
                            "Persoon filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            persoonFilterString );
        }

        // Check if dialog was completed successfully (i.e., not canceled)
        if ( newPersoonFilterString != null ) {
            // Store the new persoon filter
            persoonFilterString = newPersoonFilterString;

            // Setup the persoon combo box with the persoon filter
            // Reset the selected persoon ID in order to avoid immediate selection
            setupPersoonComboBox( 0 );
        }

        // Return current persoon filter string, also when dialog has been canceled
        return persoonFilterString;
    }


    public String getSelectedPersoonString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedPersoonId( ) {
        String persoonString = ( String ) getSelectedItem( );

        if ( persoonString == null ) return 0;

        // Check if empty string is selected
        if ( persoonString.length( ) == 0 ) return 0;

        // Get the persoon_id from the map
        if ( persoonMap.containsKey( persoonString ) ) {
            return persoonMap.get( persoonString );
        }

        return 0;
    }


    public boolean newPersoonSelected( ) {
        String persoonString = ( String ) getSelectedItem( );

        // Check if empty string is selected
        if ( persoonString == null ) return false;

        return persoonString.equals( newPersoonString );
    }
}

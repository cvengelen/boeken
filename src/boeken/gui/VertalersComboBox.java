// Class to setup a ComboBox for selection of a vertalers record

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import javax.swing.*;

import java.util.logging.Logger;


public class VertalersComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.VertalersComboBox" );

    private Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map< String, Integer > vertalersMap = new HashMap< String, Integer >( );
    int selectedVertalersId = 0;
    private String vertalersFilterString = null;
    private String newVertalersString = null;


    public VertalersComboBox( Connection connection,
                              Object parentObject ) {
        this.connection = connection;
        this.parentObject = parentObject;

        // Setup the vertalers combo box
        setupVertalersComboBox( );
    }


    public VertalersComboBox( Connection connection,
                              Object parentObject,
                              int defaultVertalersId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.selectedVertalersId = defaultVertalersId;

        // Setup the vertalers combo box
        setupVertalersComboBox( );
    }


    public void setupVertalersComboBox( int selectedVertalersId ) {
        this.selectedVertalersId = selectedVertalersId;

        // Setup the vertalers combo box
        setupVertalersComboBox( );
    }


    public void setupVertalersComboBox( ) {
        // Remove all existing items in the vertalers combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        // Add special item to insert new vertalers
        newVertalersString = "Nieuwe vertalers ";
        if ( ( vertalersFilterString != null ) && ( vertalersFilterString.length( ) > 0 ) ) {
            newVertalersString += vertalersFilterString + " ";
        }
        newVertalersString += "toevoegen";
        addItem( newVertalersString );

        if ( !vertalersMap.isEmpty( ) ) {
            // Remove all items in the vertalers hash table
            vertalersMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            // Fill the combo box and hash table
            String vertalersQueryString =
                    "SELECT vertalers.vertalers_id, vertalers.vertalers, persoon.persoon FROM vertalers " +
                            "LEFT JOIN vertalers_persoon ON vertalers.vertalers_id = vertalers_persoon.vertalers_id " +
                            "LEFT JOIN persoon ON vertalers_persoon.persoon_id = persoon.persoon_id ";

            // Check for a vertalers filter
            if ( ( vertalersFilterString != null ) && ( vertalersFilterString.length( ) > 0 ) ) {
                // Add filter to query
                vertalersQueryString += "WHERE persoon.persoon LIKE '%" + vertalersFilterString + "%' ";
            }

            // Add order to query
            vertalersQueryString += "ORDER BY persoon, vertalers.vertalers";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( vertalersQueryString );

            while ( resultSet.next( ) ) {
                String vertalerString = resultSet.getString( 3 );
                String vertalersString = resultSet.getString( 2 );

                if ( vertalerString != null ) {
                    vertalersString = vertalerString + " (" + vertalersString + ")";
                }

                if ( vertalersString.length( ) > 80 ) {
                    vertalersString = vertalersString.substring( 0, 80 );
                }

                // Store the vertalers_id in the map indexed by the vertalersString
                vertalersMap.put( vertalersString, resultSet.getInt( 1 ) );

                // Add the vertalersString to the combo box
                addItem( vertalersString );

                // Check if this is the selected vertalers
                if ( resultSet.getInt( 1 ) == selectedVertalersId ) {
                    // Select this vertalers
                    setSelectedItem( vertalersString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterVertalersComboBox( ) {
        String newVertalersFilterString = null;

        // Prompt for the vertalers filter, using the current value as default
        if ( parentObject instanceof JFrame ) {
            newVertalersFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JFrame ) parentObject,
                            "Vertalers filter:",
                            "Vertalers filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            vertalersFilterString );
        } else if ( parentObject instanceof JDialog ) {
            newVertalersFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JDialog ) parentObject,
                            "Vertalers filter:",
                            "Vertalers filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            vertalersFilterString );
        }

        // Check if dialog was completed successfully (i.e., not canceled)
        if ( newVertalersFilterString != null ) {
            // Store the new vertalers filter
            vertalersFilterString = newVertalersFilterString;

            // Setup the vertalers combo box with the vertalers filter
            // Reset the selected vertalers ID in order to avoid immediate selection
            setupVertalersComboBox( 0 );
        }

        // Return current vertalers filter string, also when dialog has been canceled
        return vertalersFilterString;
    }


    public String getSelectedVertalersString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedVertalersId( ) {
        String vertalersString = ( String ) getSelectedItem( );

        if ( vertalersString == null ) return 0;

        // Check if empty string is selected
        if ( vertalersString.length( ) == 0 ) return 0;

        // Get the vertalers_id from the map
        if ( vertalersMap.containsKey( vertalersString ) ) {
            return vertalersMap.get( vertalersString );
        }

        return 0;
    }


    public boolean newVertalersSelected( ) {
        String vertalersString = ( String ) getSelectedItem( );

        // Check if empty string is selected
        if ( vertalersString == null ) return false;

        return vertalersString.equals( newVertalersString );
    }
}

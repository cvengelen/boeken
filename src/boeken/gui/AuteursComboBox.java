// Class to setup a ComboBox for selection of an auteurs record

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class AuteursComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( AuteursComboBox.class.getCanonicalName( ) );

    private Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map< String, Integer > auteursIdMap = new HashMap< >( );
    private int selectedAuteursId = 0;
    private String auteursFilterString = null;
    private String newAuteursString = null;

    // Extra map for auteurs
    private Map< String, String > auteursMap = new HashMap< >( );

    private boolean allowNewAuteurs = true;


    public AuteursComboBox( Connection connection,
                            Object parentObject,
                            boolean allowNewAuteurs ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.allowNewAuteurs = allowNewAuteurs;

        // Setup the auteurs combo box
        setupAuteursComboBox( );
    }


    public AuteursComboBox( Connection connection,
                            Object parentObject,
                            int selectedAuteursId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.selectedAuteursId = selectedAuteursId;

        // Setup the auteurs combo box
        setupAuteursComboBox( );
    }


    void setupAuteursComboBox( int selectedAuteursId ) {
        this.selectedAuteursId = selectedAuteursId;

        // Setup the auteurs combo box
        setupAuteursComboBox( );
    }


    private void setupAuteursComboBox( ) {
        // Remove all existing items in the auteurs combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        if ( allowNewAuteurs ) {
            // Add special item to insert new auteurs
            newAuteursString = "Nieuwe auteurs ";
            if ( ( auteursFilterString != null ) && ( auteursFilterString.length( ) > 0 ) ) {
                newAuteursString += auteursFilterString + " ";
            }
            newAuteursString += "toevoegen";
            addItem( newAuteursString );
        }

        if ( !auteursIdMap.isEmpty( ) ) {
            // Remove all items in the auteurs hash table
            auteursIdMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String auteursQueryString =
                    "SELECT auteurs.auteurs_id, auteurs.auteurs, persoon.persoon FROM auteurs " +
                            "LEFT JOIN auteurs_persoon ON auteurs.auteurs_id = auteurs_persoon.auteurs_id " +
                            "LEFT JOIN persoon ON auteurs_persoon.persoon_id = persoon.persoon_id ";

            // Check for a auteurs filter
            if ( ( auteursFilterString != null ) && ( auteursFilterString.length( ) > 0 ) ) {
                // Add filter to query
                auteursQueryString += "WHERE persoon.persoon LIKE '%" + auteursFilterString + "%' ";
            }

            // Add order to query
            auteursQueryString += "ORDER BY persoon.persoon, auteurs.auteurs";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( auteursQueryString );

            while ( resultSet.next( ) ) {
                String auteurString = resultSet.getString( 3 );
                String auteursComboBoxItemString = resultSet.getString( 2 );

                if ( auteurString != null ) {
                    auteursComboBoxItemString = auteurString + " (" + auteursComboBoxItemString + ")";
                }

                // Store the auteurs_id in the map indexed by auteursComboBoxItemString
                auteursIdMap.put( auteursComboBoxItemString, resultSet.getInt( 1 ) );

                // Store the auteurs in the map indexed by auteursComboBoxItemString
                auteursMap.put( auteursComboBoxItemString, resultSet.getString( 2 ) );

                // Add the auteursComboBoxItemString to the combo box
                addItem( auteursComboBoxItemString );

                // Check if this is the selected auteurs
                if ( resultSet.getInt( 1 ) == selectedAuteursId ) {
                    // Select this auteurs
                    setSelectedItem( auteursComboBoxItemString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterAuteursComboBox( ) {
        String newAuteursFilterString = null;

        // Prompt for the auteurs filter, using the current value as default
        if ( parentObject instanceof JFrame ) {
            newAuteursFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JFrame ) parentObject,
                            "Auteurs filter:",
                            "Auteurs filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            auteursFilterString );
        } else if ( parentObject instanceof JDialog ) {
            newAuteursFilterString =
                    ( String ) JOptionPane.showInputDialog( ( JDialog ) parentObject,
                            "Auteurs filter:",
                            "Auteurs filter dialog",
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            null,
                            auteursFilterString );
        }

        // Check if dialog was completed successfully (i.e., not canceled)
        if ( newAuteursFilterString != null ) {
            // Store the new auteurs filter
            auteursFilterString = newAuteursFilterString;

            // Setup the auteurs combo box with the auteurs filter
            // Reset the selected auteurs ID in order to avoid immediate selection
            setupAuteursComboBox( 0 );
        }

        // Return current auteurs filter string, also when dialog has been canceled
        return auteursFilterString;
    }


    String getSelectedAuteursString( ) {
        final String selectedAuteursString = ( String )getSelectedItem( );

        // Get the auteurs from the map
        if ( auteursMap.containsKey( selectedAuteursString ) ) {
            return auteursMap.get( selectedAuteursString );
        }

        // Search in map failed, fall back on selected item
        return selectedAuteursString;
    }


    public int getSelectedAuteursId( ) {
        String auteursString = ( String )getSelectedItem( );

        if ( auteursString == null ) return 0;

        // Check if empty string is selected
        if ( auteursString.length( ) == 0 ) return 0;

        // Get the auteurs_id from the map
        if ( auteursIdMap.containsKey( auteursString ) ) {
            return auteursIdMap.get( auteursString );
        }

        return 0;
    }


    boolean newAuteursSelected( ) {
        String auteursString = ( String )getSelectedItem( );

        // Check if empty string is selected
        if ( auteursString == null ) return false;

        return auteursString.equals( newAuteursString );
    }
}

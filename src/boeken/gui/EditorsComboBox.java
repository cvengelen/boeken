// Class to setup a ComboBox for selection of a editors record

package boeken.gui;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import javax.swing.*;

import java.util.logging.Logger;

public class EditorsComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.EditorsComboBox" );

    private Connection connection;
    private Component  parentComponent;

    private Map< String, Integer > editorsMap = new HashMap< String, Integer >( );
    private int selectedEditorsId = 0;
    private String editorsFilterString = null;
    private String newEditorsString = null;


    public EditorsComboBox( Connection connection,
                            Component parentComponent ) {
        this.connection = connection;
        this.parentComponent = parentComponent;

        // Setup the editors combo box
        setupEditorsComboBox( );
    }


    public EditorsComboBox( Connection connection,
                            Component parentComponent,
                            int defaultEditorsId ) {
        this.connection = connection;
        this.parentComponent = parentComponent;
        this.selectedEditorsId = defaultEditorsId;

        // Setup the editors combo box
        setupEditorsComboBox( );
    }


    public void setupEditorsComboBox( int selectedEditorsId ) {
        this.selectedEditorsId = selectedEditorsId;

        // Setup the editors combo box
        setupEditorsComboBox( );
    }


    public void setupEditorsComboBox( ) {
        // Remove all existing items in the editors combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        // Add special item to insert new editors
        newEditorsString = "Nieuwe editors ";
        if ( ( editorsFilterString != null ) && ( editorsFilterString.length( ) > 0 ) ) {
            newEditorsString += editorsFilterString + " ";
        }
        newEditorsString += "toevoegen";
        addItem( newEditorsString );

        if ( !editorsMap.isEmpty( ) ) {
            // Remove all items in the editors hash table
            editorsMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String editorsQueryString =
                    "SELECT editors.editors_id, editors.editors, persoon.persoon FROM editors " +
                            "LEFT JOIN editors_persoon ON editors.editors_id = editors_persoon.editors_id " +
                            "LEFT JOIN persoon ON editors_persoon.persoon_id = persoon.persoon_id ";

            // Check for a editors filter
            if ( ( editorsFilterString != null ) && ( editorsFilterString.length( ) > 0 ) ) {
                // Add filter to query
                editorsQueryString += "WHERE persoon.persoon LIKE '%" + editorsFilterString + "%' ";
            }

            // Add order to query
            editorsQueryString += "ORDER BY persoon.persoon, editors.editors";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( editorsQueryString );

            while ( resultSet.next( ) ) {
                String editorString = resultSet.getString( 3 );
                String editorsString = resultSet.getString( 2 );

                if ( editorString != null ) {
                    editorsString = editorString + " (" + editorsString + ")";
                }

                // Store the editors_id in the map indexed by the editorsString
                editorsMap.put( editorsString, resultSet.getInt( 1 ) );

                // Add the editorsString to the combo box
                addItem( editorsString );

                // Check if this is the selected editors
                if ( resultSet.getInt( 1 ) == selectedEditorsId ) {
                    // Select this editors
                    setSelectedItem( editorsString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterEditorsComboBox( ) {
        String newEditorsFilterString = null;

        // Prompt for the editors filter, using the current value as default
        newEditorsFilterString = (String)JOptionPane.showInputDialog(parentComponent,
                                                                     "Editors filter:",
                                                                     "Editors filter dialog",
                                                                     JOptionPane.QUESTION_MESSAGE,
                                                                     null,
                                                                     null,
                                                                     editorsFilterString);

        // Check if dialog was completed successfully (i.e., not canceled)
        if (newEditorsFilterString != null) {
            // Store the new editors filter
            editorsFilterString = newEditorsFilterString;

            // Setup the editors combo box with the editors filter
            // Reset the selected editors ID in order to avoid immediate selection
            setupEditorsComboBox(0);
        }

        // Return current editors filter string, also when dialog has been canceled
        return editorsFilterString;
    }


    public String getSelectedEditorsString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedEditorsId( ) {
        String editorsString = ( String ) getSelectedItem( );

        if ( editorsString == null ) return 0;

        // Check if empty string is selected
        if ( editorsString.length( ) == 0 ) return 0;

        // Get the editors_id from the map
        if ( editorsMap.containsKey( editorsString ) ) {
            return editorsMap.get( editorsString );
        }

        return 0;
    }


    public boolean newEditorsSelected( ) {
        String editorsString = ( String ) getSelectedItem( );

        // Check if empty string is selected
        if ( editorsString == null ) return false;

        return editorsString.equals( newEditorsString );
    }
}

// Class to setup a ComboBox for selection of label

package boeken.gui;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class LabelComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.LabelComboBox" );

    private Connection connection;
    private Component  parentComponent;

    private Map< String, Integer > labelMap = new HashMap< String, Integer >( );
    private int selectedLabelId = 0;
    private String labelFilterString = null;
    private String newLabelString = null;

    private boolean allowNewLabel = true;


    public LabelComboBox( Connection connection,
                          Component parentComponent,
                          boolean allowNewLabel ) {
        this.connection = connection;
        this.parentComponent = parentComponent;
        this.allowNewLabel = allowNewLabel;

        // Setup the label combo box
        setupLabelComboBox( );
    }


    public LabelComboBox( Connection connection,
                          Component parentComponent,
                          int selectedLabelId ) {
        this.connection = connection;
        this.parentComponent = parentComponent;
        this.selectedLabelId = selectedLabelId;

        // Setup the label combo box
        setupLabelComboBox( );
    }


    public void setupLabelComboBox( int selectedLabelId ) {
        this.selectedLabelId = selectedLabelId;

        // Setup the label combo box
        setupLabelComboBox( );
    }


    void setupLabelComboBox( ) {
        // Remove all existing items in the label combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        if ( allowNewLabel ) {
            // Add special item to insert new label
            newLabelString = "Nieuw label ";
            if ( ( labelFilterString != null ) && ( labelFilterString.length( ) > 0 ) ) {
                newLabelString += labelFilterString + " ";
            }
            newLabelString += "toevoegen";
            addItem( newLabelString );
        }

        if ( !labelMap.isEmpty( ) ) {
            // Remove all items in the label hash table
            labelMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String labelQueryString = "SELECT label_id, label FROM label ";

            // Check for a label filter
            if ( ( labelFilterString != null ) && ( labelFilterString.length( ) > 0 ) ) {
                // Add filter to query
                labelQueryString += "WHERE label LIKE '%" + labelFilterString + "%' ";
            }

            // Add order to query
            labelQueryString += "ORDER BY label";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( labelQueryString );

            while ( resultSet.next( ) ) {
                String labelString = resultSet.getString( 2 );

                // Store the label_id in the map indexed by the labelString
                labelMap.put( labelString, resultSet.getInt( 1 ) );

                // Add the labelString string to the combo box
                addItem( labelString );

                // Check if this is the selected label
                if ( resultSet.getInt( 1 ) == selectedLabelId ) {
                    // Select this label
                    setSelectedItem( labelString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterLabelComboBox( ) {
        String newLabelFilterString = null;

        // Prompt for the label filter, using the current value as default
        newLabelFilterString = (String)JOptionPane.showInputDialog(parentComponent,
                                                                   "Label filter:",
                                                                   "Label filter dialog",
                                                                   JOptionPane.QUESTION_MESSAGE,
                                                                   null,
                                                                   null,
                                                                   labelFilterString);

        // Check if dialog was completed successfully (i.e., not canceled)
        if (newLabelFilterString != null) {
            // Store the new label filter
            labelFilterString = newLabelFilterString;

            // Setup the label combo box with the label filter
            // Reset the selected label ID in order to avoid immediate selection
            setupLabelComboBox(0);
        }

        // Return current label filter string, also when dialog has been canceled
        return labelFilterString;
    }


    public String getSelectedLabelString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedLabelId( ) {
        return getLabelId( ( String ) getSelectedItem( ) );
    }


    public int getLabelId( String labelString ) {
        if ( labelString == null ) return 0;

        // Check if empty string is selected
        if ( labelString.length( ) == 0 ) return 0;

        // Get the label_id from the map
        if ( labelMap.containsKey( labelString ) ) {
            return labelMap.get( labelString );
        }

        return 0;
    }


    public boolean newLabelSelected( ) {
        String labelString = ( String ) ( getSelectedItem( ) );

        // Check if empty string is selected
        if ( labelString == null ) return false;

        return labelString.equals( newLabelString );
    }
}

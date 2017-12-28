// Class to setup a ComboBox for selection of a uitgever record

package boeken.gui;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class UitgeverComboBox extends JComboBox< String > {
    final Logger logger = Logger.getLogger( "boeken.gui.UitgeverComboBox" );

    private Connection connection;
    private Component  parentComponent;

    private int selectedUitgeverId = 0;
    private String uitgeverFilterString = null;
    private String newUitgeverString = null;

    private boolean allowNewUitgever = true;

    private class UitgeverData {
        public int uitgeverId;
        public String isbn1String;
        public String isbn2String;

        public UitgeverData( int uitgeverId,
                             String isbn1String,
                             String isbn2String ) {
            this.uitgeverId = uitgeverId;
            this.isbn1String = isbn1String;
            this.isbn2String = isbn2String;
        }
    }

    private Map< String, UitgeverData > uitgeverMap = new HashMap< String, UitgeverData >( );

    public UitgeverComboBox( Connection connection,
                             Component parentComponent,
                             boolean allowNewUitgever ) {
        this.connection = connection;
        this.parentComponent = parentComponent;
        this.allowNewUitgever = allowNewUitgever;

        // Setup the uitgever combo box
        setupUitgeverComboBox( );
    }

    public UitgeverComboBox( Connection connection,
                             Component parentComponent,
                             int defaultUitgeverId ) {
        this.connection = connection;
        this.parentComponent = parentComponent;
        selectedUitgeverId = defaultUitgeverId;

        // Setup the uitgever combo box
        setupUitgeverComboBox( );
    }


    public void setupUitgeverComboBox( int selectedUitgeverId ) {
        this.selectedUitgeverId = selectedUitgeverId;

        // Setup the uitgever combo box
        setupUitgeverComboBox( );
    }


    void setupUitgeverComboBox( ) {
        // Remove all existing items in the uitgever combo box
        removeAllItems( );

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        if ( allowNewUitgever ) {
            // Add special item to insert new uitgever
            newUitgeverString = "Nieuwe uitgever ";
            if ( ( uitgeverFilterString != null ) && ( uitgeverFilterString.length( ) > 0 ) ) {
                newUitgeverString += uitgeverFilterString + " ";
            }
            newUitgeverString += "toevoegen";
            addItem( newUitgeverString );
        }

        if ( !uitgeverMap.isEmpty( ) ) {
            // Remove all items in the uitgever hash table
            uitgeverMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String uitgeverQueryString = "SELECT uitgever_id, uitgever, isbn_1, isbn_2 FROM uitgever ";

            // Check for a uitgever filter
            if ( ( uitgeverFilterString != null ) && ( uitgeverFilterString.length( ) > 0 ) ) {
                // Add filter to query
                uitgeverQueryString += "WHERE uitgever LIKE '%" + uitgeverFilterString + "%' ";
            }

            // Add order to query
            uitgeverQueryString += "ORDER BY uitgever";

            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( uitgeverQueryString );

            while ( resultSet.next( ) ) {
                String uitgeverString = resultSet.getString( 2 );
                String isbn1String = resultSet.getString( 3 );
                String isbn2String = resultSet.getString( 4 );
                if ( ( ( isbn1String != null ) && ( isbn1String.length( ) > 0 ) ) ||
                        ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) ) {
                    uitgeverString += " (";

                    if ( ( isbn1String != null ) && ( isbn1String.length( ) > 0 ) ) {
                        uitgeverString += isbn1String;
                        if ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) {
                            uitgeverString += ", ";
                        }
                    }

                    if ( ( isbn2String != null ) && ( isbn2String.length( ) > 0 ) ) {
                        uitgeverString += isbn2String;
                    }

                    uitgeverString += ")";
                }

                // UitgeverData uitgeverData = new UitgeverData( ( ( Integer )resultSet.getObject( 1 ) ).intValue( ),
                UitgeverData uitgeverData = new UitgeverData( resultSet.getInt( 1 ),
                        resultSet.getString( 3 ),
                        resultSet.getString( 4 ) );

                // Store the uitgever data in the map indexed by the uitgeverString
                uitgeverMap.put( uitgeverString, uitgeverData );

                // Add the uitgeverString to the combo box
                addItem( uitgeverString );

                // Check if this is the selected uitgever
                if ( resultSet.getInt( 1 ) == selectedUitgeverId ) {
                    // Select this uitgever
                    setSelectedItem( uitgeverString );
                }
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 20 );
    }


    public String filterUitgeverComboBox( ) {
        String newUitgeverFilterString = null;

        // Prompt for the uitgever filter, using the current value as default
        newUitgeverFilterString = (String)JOptionPane.showInputDialog(parentComponent,
                                                                      "Uitgever filter:",
                                                                      "Uitgever filter dialog",
                                                                      JOptionPane.QUESTION_MESSAGE,
                                                                      null,
                                                                      null,
                                                                      uitgeverFilterString);

        // Check if dialog was completed successfully (i.e., not canceled)
        if (newUitgeverFilterString != null) {
            // Store the new uitgever filter
            uitgeverFilterString = newUitgeverFilterString;

            // Setup the uitgever combo box with the uitgever filter
            // Reset the selected uitgever ID in order to avoid immediate selection
            setupUitgeverComboBox(0);
        }

        // Return current uitgever filter string, also when dialog has been canceled
        return uitgeverFilterString;
    }


    public String getSelectedUitgeverString( ) {
        return ( String ) getSelectedItem( );
    }


    public int getSelectedUitgeverId( ) {
        return getUitgeverId( ( String ) getSelectedItem( ) );
    }


    public int getUitgeverId( String uitgeverString ) {
        if ( uitgeverString == null ) return 0;

        // Check if empty string is selected
        if ( uitgeverString.length( ) == 0 ) return 0;

        // Get the uitgever_id from the UitgeverData stored in the map
        if ( uitgeverMap.containsKey( uitgeverString ) ) {
            return ( uitgeverMap.get( uitgeverString ) ).uitgeverId;
        }

        return 0;
    }


    String getSelectedUitgeverIsbn1String( ) {
        String uitgeverString = ( String ) getSelectedItem( );

        if ( uitgeverString == null ) return "";

        // Check if empty string is selected
        if ( uitgeverString.length( ) == 0 ) return "";

        // Get the uitgever ISBN-1 from the UitgeverData stored in the map
        if ( uitgeverMap.containsKey( uitgeverString ) ) {
            return ( uitgeverMap.get( uitgeverString ) ).isbn1String;
        }

        return "";
    }


    String getSelectedUitgeverIsbn2String( ) {
        String uitgeverString = ( String ) getSelectedItem( );

        if ( uitgeverString == null ) return "";

        // Check if empty string is selected
        if ( uitgeverString.length( ) == 0 ) return "";

        // Get the uitgever ISBN-2 from the UitgeverData stored in the map
        if ( uitgeverMap.containsKey( uitgeverString ) ) {
            return ( uitgeverMap.get( uitgeverString ) ).isbn2String;
        }

        return "";
    }


    public boolean newUitgeverSelected( ) {
        String uitgeverString = ( String ) getSelectedItem( );

        // Check if empty string is selected
        if ( uitgeverString == null ) return false;

        return uitgeverString.equals( newUitgeverString );
    }
}

// frame to show records in titel table

package boeken.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.logging.*;
import java.util.regex.*;

import table.*;


public class TitelFrame {
    final Logger logger = Logger.getLogger( "boeken.gui.TitelFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Titel" );

    JTextField boekFilterTextField;
    JTextField titelFilterTextField;
    JTextField opmerkingenFilterTextField;

    AuteursComboBox auteursComboBox;
    int selectedAuteursId = 0;

    OnderwerpComboBox onderwerpComboBox;
    int selectedOnderwerpId = 0;

    VormComboBox vormComboBox;
    int selectedVormId = 0;

    TaalComboBox taalComboBox;
    int selectedTaalId = 0;

    TitelTableModel titelTableModel;
    TableSorter titelTableSorter;
    JTable titelTable;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    public TitelFrame( final Connection connection ) {
        this.connection = connection;

        // put the controls the content pane
        Container container = frame.getContentPane( );

        // Set grid bag layout manager
        container.setLayout( new GridBagLayout( ) );
        GridBagConstraints constraints = new GridBagConstraints( );
        constraints.insets = new Insets( 5, 10, 5, 10 );


        /////////////////////////////////
        // Text filter action listener
        /////////////////////////////////

        class TextFilterActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Setup the titel table
                titelTableSorter.clearSortingState( );
                titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                        titelFilterTextField.getText( ),
                        opmerkingenFilterTextField.getText( ),
                        selectedAuteursId,
                        selectedOnderwerpId,
                        selectedVormId,
                        selectedTaalId );
            }
        }
        final TextFilterActionListener textFilterActionListener = new TextFilterActionListener( );


        /////////////////////////////////
        // Boek filter string
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Boek Filter:" ), constraints );

        boekFilterTextField = new JTextField( 50 );
        boekFilterTextField.addActionListener( textFilterActionListener );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( boekFilterTextField, constraints );


        /////////////////////////////////
        // Titel filter string
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Titel Filter:" ), constraints );

        titelFilterTextField = new JTextField( 50 );
        titelFilterTextField.addActionListener( textFilterActionListener );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( titelFilterTextField, constraints );


        /////////////////////////////////
        // Opmerkingen filter string
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Opmerkingen Filter:" ), constraints );

        opmerkingenFilterTextField = new JTextField( 50 );
        opmerkingenFilterTextField.addActionListener( textFilterActionListener );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( opmerkingenFilterTextField, constraints );


        /////////////////////////////////
        // Auteurs Combo Box
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Auteurs:" ), constraints );

        final JPanel auteursPanel = new JPanel( );
        final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );
        auteursPanel.setBorder( emptyBorder );

        // Setup a JComboBox with the results of the query on auteurs
        auteursComboBox = new AuteursComboBox( connection, frame, false );
        auteursPanel.add( auteursComboBox );

        class SelectAuteursActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Get the selected auteurs ID from the combo box
                selectedAuteursId = auteursComboBox.getSelectedAuteursId( );

                // Setup the titel table for the selected auteurs
                titelTableSorter.clearSortingState( );
                titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                        titelFilterTextField.getText( ),
                        opmerkingenFilterTextField.getText( ),
                        selectedAuteursId,
                        selectedOnderwerpId,
                        selectedVormId,
                        selectedTaalId );
            }
        }
        auteursComboBox.addActionListener( new SelectAuteursActionListener( ) );

        JButton filterAuteursButton = new JButton( "Filter" );
        filterAuteursButton.setActionCommand( "filterAuteurs" );
        auteursPanel.add( filterAuteursButton );

        class FilterAuteursActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                auteursComboBox.filterAuteursComboBox( );
            }
        }
        filterAuteursButton.addActionListener( new FilterAuteursActionListener( ) );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( auteursPanel, constraints );


        /////////////////////////////////
        // Onderwerp Combo Box
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Onderwerp:" ), constraints );

        // Setup a JComboBox for onderwerp
        onderwerpComboBox = new OnderwerpComboBox( connection );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( onderwerpComboBox, constraints );

        class SelectOnderwerpActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Get the selected onderwerp ID from the combo box
                selectedOnderwerpId = onderwerpComboBox.getSelectedOnderwerpId( );

                // Setup the titel table for the selected onderwerp
                titelTableSorter.clearSortingState( );
                titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                        titelFilterTextField.getText( ),
                        opmerkingenFilterTextField.getText( ),
                        selectedAuteursId,
                        selectedOnderwerpId,
                        selectedVormId,
                        selectedTaalId );
            }
        }
        onderwerpComboBox.addActionListener( new SelectOnderwerpActionListener( ) );


        /////////////////////////////////
        // Vorm Combo Box
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Vorm:" ), constraints );

        // Setup a JComboBox for vorm
        vormComboBox = new VormComboBox( connection );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( vormComboBox, constraints );

        class SelectVormActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Get the selected vorm ID from the combo box
                selectedVormId = vormComboBox.getSelectedVormId( );

                // Setup the titel table for the selected vorm
                titelTableSorter.clearSortingState( );
                titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                        titelFilterTextField.getText( ),
                        opmerkingenFilterTextField.getText( ),
                        selectedAuteursId,
                        selectedOnderwerpId,
                        selectedVormId,
                        selectedTaalId );
            }
        }
        vormComboBox.addActionListener( new SelectVormActionListener( ) );


        /////////////////////////////////
        // Taal Combo Box
        /////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Taal:" ), constraints );

        // Setup a JComboBox for taal
        taalComboBox = new TaalComboBox( connection );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( taalComboBox, constraints );

        class SelectTaalActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Get the selected taal ID from the combo box
                selectedTaalId = taalComboBox.getSelectedTaalId( );

                // Setup the titel table for the selected taal
                titelTableSorter.clearSortingState( );
                titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                        titelFilterTextField.getText( ),
                        opmerkingenFilterTextField.getText( ),
                        selectedAuteursId,
                        selectedOnderwerpId,
                        selectedVormId,
                        selectedTaalId );
            }
        }
        taalComboBox.addActionListener( new SelectTaalActionListener( ) );


        // Define the edit, cancel, save and delete buttons
        // These are enabled/disabled by the table model and the list selection listener.
        final JButton openTitelDialogButton = new JButton( "Open Dialog" );
        final JButton editTitelButton = new JButton( "Edit" );
        final JButton cancelTitelButton = new JButton( "Cancel" );
        final JButton saveTitelButton = new JButton( "Save" );
        final JButton deleteTitelButton = new JButton( "Delete" );

        // Create titel table from title table model
        titelTableModel = new TitelTableModel( connection,
                cancelTitelButton,
                saveTitelButton );
        titelTableSorter = new TableSorter( titelTableModel );
        titelTable = new JTable( titelTableSorter );
        titelTableSorter.setTableHeader( titelTable.getTableHeader( ) );
        // titelTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

        titelTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

        titelTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 300 );  // titel
        titelTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 150 );  // auteur
        titelTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 50 );  // copyright jaar
        titelTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 100 );  // onderwerp
        titelTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth( 80 );  // vorm
        titelTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 80 );  // Taal
        titelTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth( 300 );  // Opmerkingen
        titelTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 300 );  // Boek

        final DefaultCellEditor onderwerpDefaultCellEditor =
                new DefaultCellEditor( new OnderwerpComboBox( connection ) );
        titelTable.getColumnModel( ).getColumn( 3 ).setCellEditor( onderwerpDefaultCellEditor );

        final DefaultCellEditor vormDefaultCellEditor =
                new DefaultCellEditor( new VormComboBox( connection ) );
        titelTable.getColumnModel( ).getColumn( 4 ).setCellEditor( vormDefaultCellEditor );

        final DefaultCellEditor taalDefaultCellEditor =
                new DefaultCellEditor( new TaalComboBox( connection ) );
        titelTable.getColumnModel( ).getColumn( 5 ).setCellEditor( taalDefaultCellEditor );

        // Set vertical size just enough for 20 entries
        titelTable.setPreferredScrollableViewportSize( new Dimension( 900, 320 ) );


        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        // constraints.insets = new Insets( 10, 5, 5, 5 );
        // Setting weighty and fill is necessary for proper filling the frame when resized.
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        constraints.fill = GridBagConstraints.BOTH;
        container.add( new JScrollPane( titelTable ), constraints );


        // Get the selection model related to the rekening_mutatie table
        final ListSelectionModel titelListSelectionModel = titelTable.getSelectionModel( );

        class TitelListSelectionListener implements ListSelectionListener {
            int selectedRow = -1;

            public void valueChanged( ListSelectionEvent listSelectionEvent ) {
                // Ignore extra messages.
                if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

                // Check if current row has modified values
                if ( titelTableModel.getRowModified( ) ) {
                    if ( selectedRow == -1 ) {
                        logger.severe( "Invalid selected row" );
                    } else {
                        int result =
                                JOptionPane.showConfirmDialog( frame,
                                        "Data zijn gewijzigd: modificaties opslaan?",
                                        "Record is gewijzigd",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null );

                        if ( result == JOptionPane.YES_OPTION ) {
                            // Save the changes in the table model, and in the database
                            if ( !( titelTableModel.saveEditRow( selectedRow ) ) ) {
                                JOptionPane.showMessageDialog( frame,
                                        "Error: row not saved",
                                        "Save titel record error",
                                        JOptionPane.ERROR_MESSAGE );
                                return;
                            }
                        } else {
                            // Cancel any edits in the selected row
                            titelTableModel.cancelEditRow( selectedRow );
                        }
                    }
                }

                // Ignore if nothing is selected
                if ( titelListSelectionModel.isSelectionEmpty( ) ) {
                    selectedRow = -1;

                    openTitelDialogButton.setEnabled( false );
                    editTitelButton.setEnabled( false );
                    cancelTitelButton.setEnabled( false );
                    saveTitelButton.setEnabled( false );
                    deleteTitelButton.setEnabled( false );

                    return;
                }

                // Remove the capability to edit the row
                titelTableModel.unsetEditRow( );

                // Get the selected row
                int viewRow = titelListSelectionModel.getMinSelectionIndex( );
                selectedRow = titelTableSorter.modelIndex( viewRow );

                // Enable the open-dialog and edit buttons
                openTitelDialogButton.setEnabled( true );
                editTitelButton.setEnabled( true );

                // Disable the cancel and save buttons (these will be enabled
                // when any data in the row is actually modified)
                cancelTitelButton.setEnabled( false );
                saveTitelButton.setEnabled( false );

                // Enable the delete button
                deleteTitelButton.setEnabled( true );
            }

            public int getSelectedRow( ) {
                return selectedRow;
            }
        }

        // Add titelListSelectionListener object to the selection model of the musici table
        final TitelListSelectionListener titelListSelectionListener = new TitelListSelectionListener( );
        titelListSelectionModel.addListSelectionListener( titelListSelectionListener );


        ////////////////////////////////////////////////
        // Add, Edit, Cancel, Save, Delete, Close Buttons
        ////////////////////////////////////////////////

        // Class to handle button actions: uses titelListSelectionListener
        class ButtonActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
                    frame.setVisible( false );
                    System.exit( 0 );
                } else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
                    // Insert new titel record
                    EditTitelDialog editTitelDialog =
                            new EditTitelDialog( connection, frame,
                                    titelFilterTextField.getText( ),
                                    boekFilterTextField.getText( ),
                                    selectedAuteursId,
                                    selectedOnderwerpId,
                                    selectedVormId,
                                    selectedTaalId );

                    // Records may have been modified: setup the table model again
                    titelTableSorter.clearSortingState( );
                    titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                            titelFilterTextField.getText( ),
                            opmerkingenFilterTextField.getText( ),
                            selectedAuteursId,
                            selectedOnderwerpId,
                            selectedVormId,
                            selectedTaalId );
                } else {
                    int selectedRow = titelListSelectionListener.getSelectedRow( );
                    if ( selectedRow < 0 ) {
                        JOptionPane.showMessageDialog( frame,
                                "Geen titel geselecteerd",
                                "Titel frame error",
                                JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    // Get the selected titel key
                    TitelKey selectedTitelKey = titelTableModel.getTitelKey( selectedRow );

                    // Check if titel has been selected
                    if ( selectedTitelKey == new TitelKey( ) ) {
                        JOptionPane.showMessageDialog( frame,
                                "Geen titel geselecteerd",
                                "Titel frame error",
                                JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    if ( actionEvent.getActionCommand( ).equals( "openDialog" ) ) {
                        // Open dialog
                        EditTitelDialog editTitelDialog =
                                new EditTitelDialog( connection, frame, selectedTitelKey );

                        // Records may have been modified: setup the table model again
                        titelTableSorter.clearSortingState( );
                        titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                                titelFilterTextField.getText( ),
                                opmerkingenFilterTextField.getText( ),
                                selectedAuteursId,
                                selectedOnderwerpId,
                                selectedVormId,
                                selectedTaalId );
                    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
                        String titelString = selectedTitelKey.getTitelString( );
                        // Replace null or empty string by single space for messages
                        if ( ( titelString == null ) || ( titelString.length( ) == 0 ) ) {
                            titelString = " ";
                        }
                        int result =
                                JOptionPane.showConfirmDialog( frame,
                                        "Delete '" + titelString + "' ?",
                                        "Delete Boek record",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE,
                                        null );

                        if ( result != JOptionPane.YES_OPTION ) return;

                        String deleteString = "DELETE FROM titel";

                        // Use the key for selection of the record to be deleted
                        if ( selectedTitelKey.getTitelString( ) == null ) {
                            deleteString += " WHERE titel = ''";
                        } else {
                            // Matcher to find single quotes in titel, in order to replace these
                            // with escaped quotes (the quadruple slashes are really necessary)
                            final Matcher quoteMatcher = quotePattern.matcher( selectedTitelKey.getTitelString( ) );
                            deleteString += " WHERE titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
                        }
                        deleteString += " AND auteurs_id = " + selectedTitelKey.getAuteursId( );
                        deleteString += " AND boek_id = " + selectedTitelKey.getBoekId( );

                        logger.info( "deleteString: " + deleteString );

                        try {
                            Statement statement = connection.createStatement( );
                            int nUpdate = statement.executeUpdate( deleteString );
                            if ( nUpdate != 1 ) {
                                String errorString = ( "Could not delete record with titel '" +
                                        selectedTitelKey.getTitelString( ) + "' in titel" );
                                JOptionPane.showMessageDialog( frame,
                                        errorString,
                                        "Delete titel record",
                                        JOptionPane.ERROR_MESSAGE );
                                logger.severe( errorString );
                                return;
                            }
                        } catch ( SQLException sqlException ) {
                            logger.severe( "SQLException: " + sqlException.getMessage( ) );
                            return;
                        }

                        // Records may have been modified: setup the table model again
                        titelTableSorter.clearSortingState( );
                        titelTableModel.setupTitelTableModel( boekFilterTextField.getText( ),
                                titelFilterTextField.getText( ),
                                opmerkingenFilterTextField.getText( ),
                                selectedAuteursId,
                                selectedOnderwerpId,
                                selectedVormId,
                                selectedTaalId );
                    } else if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
                        // Allow to edit the selected row
                        titelTableModel.setEditRow( selectedRow );

                        // Disable the edit button
                        editTitelButton.setEnabled( false );
                    } else if ( actionEvent.getActionCommand( ).equals( "cancel" ) ) {
                        // Cancel any edits in the selected row
                        titelTableModel.cancelEditRow( selectedRow );

                        // Remove the capability to edit the row
                        titelTableModel.unsetEditRow( );

                        // Enable the edit button, so that the user can select edit again
                        editTitelButton.setEnabled( true );

                        // Disable the cancel and save buttons
                        cancelTitelButton.setEnabled( false );
                        saveTitelButton.setEnabled( false );
                    } else if ( actionEvent.getActionCommand( ).equals( "save" ) ) {
                        // Save the changes in the table model, and in the database
                        if ( !( titelTableModel.saveEditRow( selectedRow ) ) ) {
                            JOptionPane.showMessageDialog( frame,
                                    "Error: row not saved",
                                    "Save titel record error",
                                    JOptionPane.ERROR_MESSAGE );
                            return;
                        }

                        // Remove the capability to edit the row
                        titelTableModel.unsetEditRow( );

                        // Enable the edit button, so that the user can select edit again
                        editTitelButton.setEnabled( true );

                        // Disable the cancel and save buttons
                        cancelTitelButton.setEnabled( false );
                        saveTitelButton.setEnabled( false );
                    }
                }
            }
        }
        final ButtonActionListener buttonActionListener = new ButtonActionListener( );

        JPanel buttonPanel = new JPanel( );

        final JButton insertTitelButton = new JButton( "Insert" );
        insertTitelButton.setActionCommand( "insert" );
        insertTitelButton.addActionListener( buttonActionListener );
        buttonPanel.add( insertTitelButton );

        openTitelDialogButton.setActionCommand( "openDialog" );
        openTitelDialogButton.setEnabled( false );
        openTitelDialogButton.addActionListener( buttonActionListener );
        buttonPanel.add( openTitelDialogButton );

        editTitelButton.setActionCommand( "edit" );
        editTitelButton.setEnabled( false );
        editTitelButton.addActionListener( buttonActionListener );
        buttonPanel.add( editTitelButton );

        cancelTitelButton.setActionCommand( "cancel" );
        cancelTitelButton.setEnabled( false );
        cancelTitelButton.addActionListener( buttonActionListener );
        buttonPanel.add( cancelTitelButton );

        saveTitelButton.setActionCommand( "save" );
        saveTitelButton.setEnabled( false );
        saveTitelButton.addActionListener( buttonActionListener );
        buttonPanel.add( saveTitelButton );

        deleteTitelButton.setActionCommand( "delete" );
        deleteTitelButton.setEnabled( false );
        deleteTitelButton.addActionListener( buttonActionListener );
        buttonPanel.add( deleteTitelButton );

        final JButton closeButton = new JButton( "Close" );
        closeButton.setActionCommand( "close" );
        closeButton.addActionListener( buttonActionListener );
        buttonPanel.add( closeButton );

        constraints.gridx = 0;
        constraints.gridy = 8;
        container.add( buttonPanel, constraints );

        frame.setSize( 980, 720 );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setVisible( true );
    }
}

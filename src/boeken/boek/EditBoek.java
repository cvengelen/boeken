package boeken.boek;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import boeken.gui.*;
import table.*;

/**
 * Frame to show, insert and update records in the boek table in schema boeken.
 */
public class EditBoek extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditBoek.class.getCanonicalName() );

    private JTextField boekFilterTextField;

    private int selectedTypeId = 0;
    private TypeComboBox typeComboBox;

    private int selectedUitgeverId = 0;
    private UitgeverComboBox uitgeverComboBox;

    private int selectedStatusId = 0;
    private StatusComboBox statusComboBox;

    private BoekTableModel boekTableModel;
    private TableSorter boekTableSorter;


    public EditBoek( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit boek", true, true, true, true);

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.weightx = 0d;
	constraints.weighty = 0d;


	/////////////////////////////////
	// Boek Filter
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Boek Filter:" ), constraints );
	boekFilterTextField = new JTextField( 20 );

        constraints.insets = new Insets( 20, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( boekFilterTextField, constraints );

        /////////////////////////////////
        // Boek filter action listener
        /////////////////////////////////
	boekFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the boek table
            boekTableSorter.clearSortingState( );
            boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
                    selectedTypeId,
                    selectedUitgeverId,
                    selectedStatusId );
        } );

        /////////////////////////////////
        // Boek filter focus listener
        /////////////////////////////////
        boekFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the boek table
                boekTableSorter.clearSortingState( );
                boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
                                                    selectedTypeId,
                                                    selectedUitgeverId,
                                                    selectedStatusId );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );


	/////////////////////////////////
	// Type Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Type:" ), constraints );

	// Setup a JComboBox for type
	typeComboBox = new TypeComboBox( connection );
        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( typeComboBox, constraints );

	typeComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected type ID from the combo box
            selectedTypeId = typeComboBox.getSelectedTypeId( );

            // Setup the boek table for the selected type
            boekTableSorter.clearSortingState( );
            boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
                                                selectedTypeId,
                                                selectedUitgeverId,
                                                selectedStatusId  );
        } );


        /////////////////////////////////
	// Uitgever Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 2;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Uitgever:" ), constraints );

	// Setup a JComboBox with the results of the query on uitgever
	uitgeverComboBox = new UitgeverComboBox( connection, EditBoek.this, false );
        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( uitgeverComboBox, constraints );

	uitgeverComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected uitgever ID from the combo box
            selectedUitgeverId = uitgeverComboBox.getSelectedUitgeverId( );

            // Setup the boek table for the selected uitgever
            boekTableSorter.clearSortingState( );
            boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
                    selectedTypeId,
                    selectedUitgeverId,
                    selectedStatusId );
        } );


	/////////////////////////////////
	// Status Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Status:" ), constraints );

	// Setup a JComboBox for status
	statusComboBox = new StatusComboBox( connection );
        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( statusComboBox, constraints );

	statusComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected status ID from the combo box
            selectedStatusId = statusComboBox.getSelectedStatusId( );

            // Setup the boek table for the selected status
            boekTableSorter.clearSortingState( );
            boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
                    selectedTypeId,
                    selectedUitgeverId,
                    selectedStatusId );
        } );


	// Define the edit, cancel, save and delete buttons
	// These are enabled/disabled by the table model and the list selection listener.
	final JButton openBoekDialogButton = new JButton( "Open Dialog" );
	final JButton editBoekButton = new JButton( "Edit" );
	final JButton cancelBoekButton = new JButton( "Cancel" );
	final JButton saveBoekButton = new JButton( "Save" );
	final JButton deleteBoekButton = new JButton( "Delete" );

	// Create boek table from boek table model
	boekTableModel = new BoekTableModel( connection, EditBoek.this, cancelBoekButton, saveBoekButton );
	boekTableSorter = new TableSorter( boekTableModel );
	final JTable boekTable = new JTable( boekTableSorter );
	boekTableSorter.setTableHeader( boekTable.getTableHeader( ) );
	// boekTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	boekTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	boekTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	boekTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 220 );  // boek
	boekTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 120 );  // type
	boekTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 150 );  // uitgever, isbn1, isbn2
	boekTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth(  60 );  // isbn3
	boekTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth(  40 );  // isbn4
	boekTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth(  80 );  // status
	boekTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 100 );  // label
	boekTable.getColumnModel( ).getColumn( 8 ).setPreferredWidth( 100 );  // aanschaf datum
	boekTable.getColumnModel( ).getColumn( 9 ).setPreferredWidth( 100 );  // verwijderd datum

	final DefaultCellEditor typeDefaultCellEditor = new DefaultCellEditor( new TypeComboBox( connection ) );
	boekTable.getColumnModel( ).getColumn( 2 ).setCellEditor( typeDefaultCellEditor );

	final DefaultCellEditor uitgeverDefaultCellEditor = new DefaultCellEditor( new UitgeverComboBox( connection, EditBoek.this, false ) );
	boekTable.getColumnModel( ).getColumn( 3 ).setCellEditor( uitgeverDefaultCellEditor );

	final DefaultCellEditor statusDefaultCellEditor = new DefaultCellEditor( new StatusComboBox( connection ) );
	boekTable.getColumnModel( ).getColumn( 6 ).setCellEditor( statusDefaultCellEditor );

	final DefaultCellEditor labelDefaultCellEditor = new DefaultCellEditor( new LabelComboBox( connection, EditBoek.this, false ) );
	boekTable.getColumnModel( ).getColumn( 7 ).setCellEditor( labelDefaultCellEditor );

	// Set vertical size just enough for 20 entries
	boekTable.setPreferredScrollableViewportSize( new Dimension( 1020, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
	// Setting weighty and fill is necessary for proper filling the frame when resized.
        constraints.weightx = 1d;
	constraints.weighty = 1d;
	constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( boekTable ), constraints );


	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel boekListSelectionModel = boekTable.getSelectionModel( );

	class BoekListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Check if current row has modified values
		if ( boekTableModel.getRowModified( ) ) {
		    if ( selectedRow == -1 ) {
			logger.severe( "Invalid selected row" );
		    } else {
			int result =
			    JOptionPane.showConfirmDialog( EditBoek.this,
							   "Data zijn gewijzigd: modificaties opslaan?",
							   "Record is gewijzigd",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result == JOptionPane.YES_OPTION ) {
			    // Save the changes in the table model, and in the database
			    if ( !( boekTableModel.saveEditRow( selectedRow ) ) ) {
				JOptionPane.showMessageDialog( EditBoek.this,
							       "Error: row not saved",
							       "Save boek record error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} else {
			    // Cancel any edits in the selected row
			    boekTableModel.cancelEditRow( selectedRow );
			}
		    }
		}

		// Ignore if nothing is selected
		if ( boekListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;

		    openBoekDialogButton.setEnabled( false );
		    editBoekButton.setEnabled( false );
		    cancelBoekButton.setEnabled( false );
		    saveBoekButton.setEnabled( false );
		    deleteBoekButton.setEnabled( false );

		    return;
		}

		// Remove the capability to edit the row
		boekTableModel.unsetEditRow( );

		// Get the selected row
		int viewRow = boekListSelectionModel.getMinSelectionIndex( );
		selectedRow = boekTableSorter.modelIndex( viewRow );

		// Enable the open-dialog and edit buttons
		openBoekDialogButton.setEnabled( true );
		editBoekButton.setEnabled( true );

		// Disable the cancel and save buttons (these will be enabled
		// when any data in the row is actually modified)
		cancelBoekButton.setEnabled( false );
		saveBoekButton.setEnabled( false );

		// Enable the delete button
		deleteBoekButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add boekListSelectionListener object to the selection model of the musici table
	final BoekListSelectionListener boekListSelectionListener = new BoekListSelectionListener( );
	boekListSelectionModel.addListSelectionListener( boekListSelectionListener );

	// Class to handle button actions: uses boekListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
		    dispose( );
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new boek record
		    new EditBoekDialog( connection, parentFrame,
                                        boekFilterTextField.getText( ),
                                        selectedTypeId,
                                        selectedUitgeverId,
                                        selectedStatusId );

		    // Records may have been modified: setup the table model again
		    boekTableSorter.clearSortingState( );
		    boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
							selectedTypeId,
							selectedUitgeverId,
							selectedStatusId );
		} else {
		    int selectedRow = boekListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( EditBoek.this,
						       "Geen boek geselecteerd",
						       "Edit boek error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected boek id
		    final int selectedBoekId = boekTableModel.getBoekId( selectedRow );

		    // Check if boek has been selected
		    if ( selectedBoekId == 0 ) {
			JOptionPane.showMessageDialog( EditBoek.this,
						       "Geen boek geselecteerd",
						       "Edit boek error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "openDialog" ) ) {
			// Do dialog
			new EditBoekDialog( connection, parentFrame, selectedBoekId );

			// Records may have been modified: setup the table model again
			boekTableSorter.clearSortingState( );
			boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
							    selectedTypeId,
							    selectedUitgeverId,
							    selectedStatusId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			String selectedBoekString = boekTableModel.getBoekString( selectedRow );

			// Replace null or empty string by single space for messages
			if ( ( selectedBoekString == null ) || ( selectedBoekString.length( ) == 0  ) ) {
			    selectedBoekString = " ";
			}

			// Check if selectedBoekId is present in titel table
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT boek_id FROM titel WHERE boek_id = " +
							selectedBoekId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( EditBoek.this,
							       "Tabel titel heeft nog verwijzing naar '" +
							       selectedBoekString + "'",
							       "Edit boek error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( EditBoek.this,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditBoek SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( EditBoek.this,
							   "Delete '" + selectedBoekString + "' ?",
							   "Delete Boek record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM boek WHERE boek_id = " + selectedBoekId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				final String errorString = "Could not delete record with boek_id  = " + selectedBoekId + " in boek";
				JOptionPane.showMessageDialog( EditBoek.this,
							       errorString,
							       "Edit boek rerror",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( EditBoek.this,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditBoek SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			// Records may have been modified: setup the table model again
			boekTableSorter.clearSortingState( );
			boekTableModel.setupBoekTableModel( boekFilterTextField.getText( ),
							    selectedTypeId,
							    selectedUitgeverId,
							    selectedStatusId );
		    } else if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Allow to edit the selected row
			boekTableModel.setEditRow( selectedRow );

			// Disable the edit button
			editBoekButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "cancel" ) ) {
			// Cancel any edits in the selected row
			boekTableModel.cancelEditRow( selectedRow );

			// Remove the capability to edit the row
			boekTableModel.unsetEditRow( );

			// Enable the edit button, so that the user can select edit again
			editBoekButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelBoekButton.setEnabled( false );
			saveBoekButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "save" ) ) {
			// Save the changes in the table model, and in the database
			if ( !( boekTableModel.saveEditRow( selectedRow ) ) ) {
			    JOptionPane.showMessageDialog( EditBoek.this,
							   "Error: row not saved",
							   "Save boek record error",
							   JOptionPane.ERROR_MESSAGE );
			    return;
			}

			// Remove the capability to edit the row
			boekTableModel.unsetEditRow( );

			// Enable the edit button, so that the user can select edit again
			editBoekButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelBoekButton.setEnabled( false );
			saveBoekButton.setEnabled( false );
		    }
		}
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertBoekButton = new JButton( "Insert" );
	insertBoekButton.setActionCommand( "insert" );
	insertBoekButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertBoekButton );

	openBoekDialogButton.setActionCommand( "openDialog" );
	openBoekDialogButton.setEnabled( false );
	openBoekDialogButton.addActionListener( buttonActionListener );
	buttonPanel.add( openBoekDialogButton );

	editBoekButton.setActionCommand( "edit" );
	editBoekButton.setEnabled( false );
	editBoekButton.addActionListener( buttonActionListener );
	buttonPanel.add( editBoekButton );

	cancelBoekButton.setActionCommand( "cancel" );
	cancelBoekButton.setEnabled( false );
	cancelBoekButton.addActionListener( buttonActionListener );
	buttonPanel.add( cancelBoekButton );

	saveBoekButton.setActionCommand( "save" );
	saveBoekButton.setEnabled( false );
	saveBoekButton.addActionListener( buttonActionListener );
	buttonPanel.add( saveBoekButton );

	deleteBoekButton.setActionCommand( "delete" );
	deleteBoekButton.setEnabled( false );
	deleteBoekButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteBoekButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 5;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	setSize( 1140, 700 );
	setLocation(x, y);
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}

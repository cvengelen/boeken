package boeken.editors;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import boeken.gui.EditEditorsDialog;
import table.*;

/**
 * Frame to show, insert and update records in the editors table in schema boeken.
 * An instance of EditorsFrame is created by class boeken.Main.
 */
public class EditorsFrame {
    private final Logger logger = Logger.getLogger( "boeken.editors.EditorsFrame" );

    private final Connection connection;
    private final JFrame frame = new JFrame( "Editors" );

    private JTextField editorsFilterTextField;

    private EditorsTableModel editorsTableModel;
    private TableSorter editorsTableSorter;

    private class Editors {
	int	id;
	String  string;

	Editors( int    id,
                 String string ) {
	    this.id = id;
	    this.string = string;
	}

	boolean presentInTable( String tableString ) {
	    // Check if editorsId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT editors_id FROM " + tableString +
							      " WHERE editors_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( frame,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + string + "'",
						   "Editors frame error",
						   JOptionPane.ERROR_MESSAGE );
		    return true;
		}
	    } catch ( SQLException sqlException ) {
		logger.severe( "SQLException: " + sqlException.getMessage( ) );
		return true;
	    }
	    return false;
	}
    }

    public EditorsFrame( final Connection connection ) {
	this.connection = connection;

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Editors Filter:" ), constraints );

	editorsFilterTextField = new JTextField( 20 );
        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( editorsFilterTextField, constraints );

	editorsFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            editorsTableSorter.clearSortingState();
            // Setup the editors table
            editorsTableModel.setupEditorsTableModel( editorsFilterTextField.getText() );
        } );

	// Create editors table from editors table model
	editorsTableModel = new EditorsTableModel( connection );
	editorsTableSorter = new TableSorter( editorsTableModel );
	final JTable editorsTable = new JTable( editorsTableSorter );
	editorsTableSorter.setTableHeader( editorsTable.getTableHeader( ) );
	// editorsTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	editorsTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	editorsTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	editorsTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 230 );  // editors
	editorsTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 160 );  // persoon

	// Set vertical size just enough for 20 entries
	editorsTable.setPreferredScrollableViewportSize( new Dimension( 440, 300 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( editorsTable ), constraints );


	// Define the edit button because it is used by the list selection listener
	final JButton editEditorsButton = new JButton( "Edit" );
	final JButton deleteEditorsButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel editorsListSelectionModel = editorsTable.getSelectionModel( );

	class EditorsListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( editorsListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    editEditorsButton.setEnabled( false );
		    deleteEditorsButton.setEnabled( false );
		    return;
		}

		int viewRow = editorsListSelectionModel.getMinSelectionIndex( );
		selectedRow = editorsTableSorter.modelIndex( viewRow );
		editEditorsButton.setEnabled( true );
		deleteEditorsButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add editorsListSelectionListener object to the selection model of the musici table
	final EditorsListSelectionListener editorsListSelectionListener = new EditorsListSelectionListener( );
	editorsListSelectionModel.addListSelectionListener( editorsListSelectionListener );

	// Class to handle button actions: uses editorsListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new editors record
		    new EditEditorsDialog( connection, frame,
                                           editorsFilterTextField.getText( ) );
		} else {
		    int selectedRow = editorsListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen editors geselecteerd",
						       "Editors frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected editors id
		    int selectedEditorsId = editorsTableModel.getEditorsId( selectedRow );

		    // Check if editors has been selected
		    if ( selectedEditorsId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen editors geselecteerd",
						       "Editors frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
                        new EditEditorsDialog( connection, frame, selectedEditorsId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Editors editors = new Editors( editorsTableModel.getEditorsId( selectedRow ),
							     editorsTableModel.getEditorsString( selectedRow ) );

			// Check if editors ID is still used
			if ( editors.presentInTable( "editors_persoon" ) ) return;
			if ( editors.presentInTable( "boek" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( editors.string == null ) || ( editors.string.length( ) == 0  ) ) {
			    editors.string = " ";
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + editors.string + "' ?",
							   "Delete Editors record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM editors";
			deleteString += " WHERE editors_id = " + editors.id;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with editors_id  = " +
						       editors.id + " in editors" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Editors record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the table model again
		editorsTableSorter.clearSortingState( );
		editorsTableModel.setupEditorsTableModel( editorsFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertEditorsButton = new JButton( "Insert" );
	insertEditorsButton.setActionCommand( "insert" );
	insertEditorsButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertEditorsButton );


	editEditorsButton.setActionCommand( "edit" );
	editEditorsButton.setEnabled( false );
	editEditorsButton.addActionListener( buttonActionListener );
	buttonPanel.add( editEditorsButton );


	deleteEditorsButton.setActionCommand( "delete" );
	deleteEditorsButton.setEnabled( false );
	deleteEditorsButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteEditorsButton );


	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	frame.setSize( 500, 460 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}

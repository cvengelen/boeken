package boeken.auteurs;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import boeken.gui.EditAuteursDialog;
import table.*;

/**
 * Frame to show, insert and update records in the auteurs table in schema boeken.
 * An instance of AuteursFrame is created by class boeken.Main.
 */
public class AuteursFrame {
    private final Logger logger = Logger.getLogger(AuteursFrame.class.getCanonicalName());

    private final Connection connection;
    private final JFrame frame = new JFrame( "Auteurs" );

    private JTextField auteursFilterTextField;

    private AuteursTableModel auteursTableModel;
    private TableSorter auteursTableSorter;

    private class Auteurs {
	int	id;
	String  string;

	Auteurs( int    id,
                 String string ) {
	    this.id = id;
	    this.string = string;
	}

	boolean presentInTable( String tableString ) {
	    // Check if auteursId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT auteurs_id FROM " + tableString +
							      " WHERE auteurs_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( frame,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + string + "'",
						   "Auteurs frame error",
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

    public AuteursFrame( final Connection connection ) {
	this.connection = connection;

        // put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

	constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
	constraints.gridy = 0;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Auteurs Filter:" ), constraints );
	auteursFilterTextField = new JTextField( 20 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( auteursFilterTextField, constraints );

        auteursFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            auteursTableSorter.clearSortingState();
            // Setup the auteurs table when the auteurs field is modified
            auteursTableModel.setupAuteursTableModel( auteursFilterTextField.getText() );
        } );

	// Create auteurs table from auteurs table model
	auteursTableModel = new AuteursTableModel( connection );
	auteursTableSorter = new TableSorter( auteursTableModel );
	JTable auteursTable = new JTable( auteursTableSorter );
	auteursTableSorter.setTableHeader( auteursTable.getTableHeader( ) );
	// auteursTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	auteursTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	auteursTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	auteursTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 250 );  // auteurs
	auteursTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // persoon

	// Set vertical size just enough for 20 entries
	auteursTable.setPreferredScrollableViewportSize( new Dimension( 500, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        // Setting weighty and fill is necessary for proper filling the frame when resized.
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( auteursTable ), constraints );


	// Define the edit, delete button because it is used by the list selection listener
	final JButton editAuteursButton = new JButton( "Edit" );
	final JButton deleteAuteursButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel auteursListSelectionModel = auteursTable.getSelectionModel( );

	class AuteursListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( auteursListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    editAuteursButton.setEnabled( false );
		    deleteAuteursButton.setEnabled( false );
		    return;
		}

		int viewRow = auteursListSelectionModel.getMinSelectionIndex( );
		selectedRow = auteursTableSorter.modelIndex( viewRow );
		editAuteursButton.setEnabled( true );
		deleteAuteursButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add auteursListSelectionListener object to the selection model of the musici table
	final AuteursListSelectionListener auteursListSelectionListener = new AuteursListSelectionListener( );
	auteursListSelectionModel.addListSelectionListener( auteursListSelectionListener );

	// Class to handle button actions: uses auteursListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new auteurs record
		    new EditAuteursDialog( connection, frame,
                                           auteursFilterTextField.getText( ) );
		} else {
		    int selectedRow = auteursListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen auteurs geselecteerd",
						       "Auteurs frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected auteurs id
		    int selectedAuteursId = auteursTableModel.getAuteursId( selectedRow );

		    // Check if auteurs has been selected
		    if ( selectedAuteursId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen auteurs geselecteerd",
						       "Auteurs frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
			new EditAuteursDialog( connection, frame, selectedAuteursId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Auteurs auteurs = new Auteurs( auteursTableModel.getAuteursId( selectedRow ),
							     auteursTableModel.getAuteursString( selectedRow ) );

			// Check if auteurs ID is still used
			if ( auteurs.presentInTable( "auteurs_persoon" ) ) return;
			if ( auteurs.presentInTable( "titel" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( auteurs.string == null ) || ( auteurs.string.length( ) == 0  ) ) {
			    auteurs.string = " ";
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + auteurs.string + "' ?",
							   "Delete Auteurs record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM auteurs";
			deleteString += " WHERE auteurs_id = " + auteurs.id;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with auteurs_id  = " +
						       auteurs.id + " in auteurs" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Auteurs record",
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
		auteursTableSorter.clearSortingState( );
		auteursTableModel.setupAuteursTableModel( auteursFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertAuteursButton = new JButton( "Insert" );
	insertAuteursButton.setActionCommand( "insert" );
	insertAuteursButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertAuteursButton );


	editAuteursButton.setActionCommand( "edit" );
	editAuteursButton.setEnabled( false );
	editAuteursButton.addActionListener( buttonActionListener );
	buttonPanel.add( editAuteursButton );


	deleteAuteursButton.setActionCommand( "delete" );
	deleteAuteursButton.setEnabled( false );
	deleteAuteursButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteAuteursButton );


	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 2;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	frame.setSize( 560, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}

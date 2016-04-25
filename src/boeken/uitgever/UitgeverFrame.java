package boeken.uitgever;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import table.*;

/**
 * Frame to show, insert and update records in the uitgever table in schema boeken.
 * An instance of UitgeverFrame is created by class boeken.Main.
 */
public class UitgeverFrame {
    private final Logger logger = Logger.getLogger( UitgeverFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Uitgever" );

    private JTextField uitgeverFilterTextField;

    private UitgeverTableModel uitgeverTableModel;
    private TableSorter uitgeverTableSorter;

    public UitgeverFrame( final Connection connection ) {

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
	container.add( new JLabel( "Uitgever Filter:" ), constraints );

	uitgeverFilterTextField = new JTextField( 20 );
        constraints.insets = new Insets( 20, 5, 5, 40 );
        constraints.gridx  = GridBagConstraints.RELATIVE;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( uitgeverFilterTextField, constraints );

	uitgeverFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            uitgeverTableSorter.clearSortingState();
            // Setup the uitgever table
            uitgeverTableModel.setupUitgeverTableModel( uitgeverFilterTextField.getText( ) );

        } );

        // Create uitgever table from uitgever table model
	uitgeverTableModel = new UitgeverTableModel( connection );
	uitgeverTableSorter = new TableSorter( uitgeverTableModel );
	final JTable uitgeverTable = new JTable( uitgeverTableSorter );
	uitgeverTableSorter.setTableHeader( uitgeverTable.getTableHeader( ) );
	// uitgeverTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	uitgeverTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	uitgeverTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	uitgeverTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 370 );  // uitgever
	uitgeverTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth(  50 );  // ISBN prefix 1
	uitgeverTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth(  80 );  // ISBN prefix 2

	// Set vertical size just enough for 20 entries
	uitgeverTable.setPreferredScrollableViewportSize( new Dimension( 550, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill    = GridBagConstraints.BOTH;
	container.add( new JScrollPane( uitgeverTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deleteUitgeverButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel uitgeverListSelectionModel = uitgeverTable.getSelectionModel( );

	class UitgeverListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( uitgeverListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteUitgeverButton.setEnabled( false );
		    return;
		}

		int viewRow = uitgeverListSelectionModel.getMinSelectionIndex( );
		selectedRow = uitgeverTableSorter.modelIndex( viewRow );
		deleteUitgeverButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add uitgeverListSelectionListener object to the selection model of the musici table
	final UitgeverListSelectionListener uitgeverListSelectionListener = new UitgeverListSelectionListener( );
	uitgeverListSelectionModel.addListSelectionListener( uitgeverListSelectionListener );

	// Class to handle button actions: uses uitgeverListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( uitgever_id ) FROM uitgever" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for uitgever_id in uitgever" );
			    return;
			}
			int uitgeverId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO uitgever SET uitgever_id = " + uitgeverId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in uitgever" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = uitgeverListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen uitgever geselecteerd",
						       "Uitgever frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected uitgever id
		    int selectedUitgeverId = uitgeverTableModel.getUitgeverId( selectedRow );

		    // Check if uitgever has been selected
		    if ( selectedUitgeverId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen uitgever geselecteerd",
						       "Uitgever frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    String selectedUitgeverString = uitgeverTableModel.getUitgeverString( selectedRow );
		    // Replace null or empty string by single space for messages
		    if ( ( selectedUitgeverString == null ) || ( selectedUitgeverString.length( ) == 0  ) ) {
			selectedUitgeverString = " ";
		    }

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if selected uitgever Id is present in boek table
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT uitgever_id FROM boek WHERE uitgever_id = " +
							selectedUitgeverId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel boek heeft nog verwijzing naar '" +
							       selectedUitgeverString + "'",
							       "Uitgever frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + selectedUitgeverString + "' ?",
							   "Delete Uitgever record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM uitgever";
			deleteString += " WHERE uitgever_id = " + selectedUitgeverId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with uitgever_id  = " +
						       selectedUitgeverId + " in uitgever" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Uitgever record",
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
		uitgeverTableSorter.clearSortingState( );
		uitgeverTableModel.setupUitgeverTableModel( uitgeverFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertUitgeverButton = new JButton( "Insert" );
	insertUitgeverButton.setActionCommand( "insert" );
	insertUitgeverButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertUitgeverButton );

	deleteUitgeverButton.setActionCommand( "delete" );
	deleteUitgeverButton.setEnabled( false );
	deleteUitgeverButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteUitgeverButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill    = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	frame.setSize( 610, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}

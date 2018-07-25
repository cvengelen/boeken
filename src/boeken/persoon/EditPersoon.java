package boeken.persoon;

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
 * Frame to show, insert and update records in the persoon table in schema boeken.
 */
public class EditPersoon extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditPersoon.class.getCanonicalName() );

    private final Connection connection;

    private PersoonTableModel persoonTableModel;
    private TableSorter persoonTableSorter;

    private class Persoon {
	int	id;
	String  name;

	Persoon( int    id,
                 String name ) {
	    this.id = id;
	    this.name = name;
	}

	boolean presentInTable( String tableString ) {
	    // Check if persoonId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT persoon_id FROM " + tableString +
							      " WHERE persoon_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( EditPersoon.this,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + name + "'",
						   "Edit persoon error",
						   JOptionPane.ERROR_MESSAGE );
		    return true;
		}
	    } catch ( SQLException sqlException ) {
	        JOptionPane.showMessageDialog(EditPersoon.this,
                                              "SQL exception in select: " + sqlException.getMessage(),
                                              "EditPersoon SQL exception",
                                              JOptionPane.ERROR_MESSAGE);
		logger.severe( "SQLException: " + sqlException.getMessage( ) );
		return true;
	    }
	    return false;
	}
    }

    public EditPersoon(final Connection connection, int x, int y ) {
        super("Edit persoon", true, true, true, true);

        this.connection = connection;

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.insets = new Insets( 0, 0, 10, 10 );

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Persoon Filter:" ), constraints );
	final JTextField persoonFilterTextField = new JTextField( 15 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( persoonFilterTextField, constraints );

	persoonFilterTextField.addActionListener(  ( ActionEvent actionEvent ) -> {
            persoonTableSorter.clearSortingState();
            // Setup the persoon table
            persoonTableModel.setupPersoonTableModel( persoonFilterTextField.getText() );
        } );

        persoonFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                persoonTableSorter.clearSortingState();
                // Setup the persoon table
                persoonTableModel.setupPersoonTableModel( persoonFilterTextField.getText() );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

        // Create persoon table from persoon table model
	persoonTableModel = new PersoonTableModel( connection, EditPersoon.this );
	persoonTableSorter = new TableSorter( persoonTableModel );
	final JTable persoonTable = new JTable( persoonTableSorter );
	persoonTableSorter.setTableHeader( persoonTable.getTableHeader( ) );
	// persoonTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	persoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	persoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	persoonTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 250 );  // persoon

	// Set vertical size just enough for 20 entries
	persoonTable.setPreferredScrollableViewportSize( new Dimension( 300, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( persoonTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deletePersoonButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel persoonListSelectionModel = persoonTable.getSelectionModel( );

	class PersoonListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( persoonListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deletePersoonButton.setEnabled( false );
		    return;
		}

		int viewRow = persoonListSelectionModel.getMinSelectionIndex( );
		selectedRow = persoonTableSorter.modelIndex( viewRow );
		deletePersoonButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add persoonListSelectionListener object to the selection model of the musici table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );

	// Class to handle button actions: uses persoonListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( persoon_id ) FROM persoon" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for persoon_id in persoon" );
			    return;
			}
			int persoonId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO persoon SET persoon_id = " + persoonId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in persoon" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog(EditPersoon.this,
                                                      "SQL exception: " + sqlException.getMessage(),
                                                      "EditPersoon SQL exception",
                                                      JOptionPane.ERROR_MESSAGE);
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = persoonListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( EditPersoon.this,
						       "Geen Persoon geselecteerd",
						       "Edit persoon error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected persoon id
		    int selectedPersoonId = persoonTableModel.getPersoonId( selectedRow );

		    // Check if persoon has been selected
		    if ( selectedPersoonId == 0 ) {
			JOptionPane.showMessageDialog( EditPersoon.this,
						       "Geen persoon geselecteerd",
						       "Edit persoon error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Persoon persoon = new Persoon( selectedPersoonId,
							     persoonTableModel.getPersoonString( selectedRow ) );

			// Check if persoon ID is still used
			if ( persoon.presentInTable( "auteurs_persoon" ) ) return;
			if ( persoon.presentInTable( "editors_persoon" ) ) return;
			if ( persoon.presentInTable( "vertalers_persoon" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( persoon.name == null ) || ( persoon.name.length( ) == 0  ) ) {
			    persoon.name = " ";
			}

			int result = JOptionPane.showConfirmDialog( EditPersoon.this,
                                                                    "Delete '" + persoon.name + "' ?",
                                                                    "Delete Persoon record",
                                                                    JOptionPane.YES_NO_OPTION,
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null );
			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM persoon WHERE persoon_id = " + persoon.id;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with persoon_id  = " +
						       persoon.id + " in persoon" );
				JOptionPane.showMessageDialog( EditPersoon.this,
							       errorString,
							       "Delete Persoon record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog(EditPersoon.this,
                                                          "SQL exception in delete: " + sqlException.getMessage(),
                                                          "EditPersoon SQL exception",
                                                          JOptionPane.ERROR_MESSAGE);
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the table model again
		persoonTableSorter.clearSortingState( );
		persoonTableModel.setupPersoonTableModel( persoonFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertPersoonButton = new JButton( "Insert" );
	insertPersoonButton.setActionCommand( "insert" );
	insertPersoonButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertPersoonButton );

	deletePersoonButton.setActionCommand( "delete" );
	deletePersoonButton.setEnabled( false );
	deletePersoonButton.addActionListener( buttonActionListener );
	buttonPanel.add( deletePersoonButton );

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

	setSize( 360, 500 );
	setLocation(x, y);
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}

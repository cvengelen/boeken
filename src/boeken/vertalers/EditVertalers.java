package boeken.vertalers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import boeken.gui.EditVertalersDialog;
import table.*;

/**
 * Frame to show, insert and update records in the vertalers table in schema boeken.
 * An instance of EditVertalers is created by class boeken.Main.
 */
public class EditVertalers extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditVertalers.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private JTextField vertalersFilterTextField;

    private VertalersTableModel vertalersTableModel;
    private TableSorter vertalersTableSorter;

    private class Vertalers {
	int	id;
	String  string;

	public Vertalers( int    id,
			  String string ) {
	    this.id = id;
	    this.string = string;
	}

	boolean presentInTable( String tableString ) {
	    // Check if vertalersId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT vertalers_id FROM " + tableString + " WHERE vertalers_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog(EditVertalers.this,
                                                  "Tabel " + tableString + " heeft nog verwijzing naar '" + string +"'",
                                                  "Edit vertalers error",
						   JOptionPane.ERROR_MESSAGE );
		    return true;
		}
	    } catch ( SQLException sqlException ) {
                JOptionPane.showMessageDialog(EditVertalers.this,
                                              "SQL exception in select: " + sqlException.getMessage(),
                                              "EditVertalers SQL exception",
                                              JOptionPane.ERROR_MESSAGE );
		logger.severe( "SQLException: " + sqlException.getMessage( ) );
		return true;
	    }
	    return false;
	}
    }

    public EditVertalers(final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit vertalers", true, true, true, true);

        this.connection = connection;
        this.parentFrame = parentFrame;

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

	constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Vertalers Filter:" ), constraints );
	vertalersFilterTextField = new JTextField( 20 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( vertalersFilterTextField, constraints );

	vertalersFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            vertalersTableSorter.clearSortingState();
            // Setup the vertalers table
            vertalersTableModel.setupVertalersTableModel( vertalersFilterTextField.getText() );
        } );


	// Create vertalers table from vertalers table model
	vertalersTableModel = new VertalersTableModel( connection, this );
	vertalersTableSorter = new TableSorter( vertalersTableModel );
	final JTable vertalersTable = new JTable( vertalersTableSorter );
	vertalersTableSorter.setTableHeader( vertalersTable.getTableHeader( ) );
	// vertalersTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	vertalersTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	vertalersTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	vertalersTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // vertalers
	vertalersTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 150 );  // persoon

	// Set vertical size just enough for 20 entries
	vertalersTable.setPreferredScrollableViewportSize( new Dimension( 400, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( vertalersTable ), constraints );


	// Define the edit, delete button because it is used by the list selection listener
	final JButton editVertalersButton = new JButton( "Edit" );
	final JButton deleteVertalersButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel vertalersListSelectionModel = vertalersTable.getSelectionModel( );

	class VertalersListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( vertalersListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    editVertalersButton.setEnabled( false );
		    deleteVertalersButton.setEnabled( false );
		    return;
		}

		int viewRow = vertalersListSelectionModel.getMinSelectionIndex( );
		selectedRow = vertalersTableSorter.modelIndex( viewRow );
		editVertalersButton.setEnabled( true );
		deleteVertalersButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add vertalersListSelectionListener object to the selection model of the musici table
	final VertalersListSelectionListener vertalersListSelectionListener = new VertalersListSelectionListener( );
	vertalersListSelectionModel.addListSelectionListener( vertalersListSelectionListener );

	// Class to handle button actions: uses vertalersListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new vertalers record
		    new EditVertalersDialog( connection, parentFrame,
                                             vertalersFilterTextField.getText( ) );
		} else {
		    int selectedRow = vertalersListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( EditVertalers.this,
						       "Geen vertalers geselecteerd",
						       "Edit vertalers error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected vertalers id
		    int selectedVertalersId = vertalersTableModel.getVertalersId( selectedRow );

		    // Check if vertalers has been selected
		    if ( selectedVertalersId == 0 ) {
			JOptionPane.showMessageDialog( EditVertalers.this,
						       "Geen vertalers geselecteerd",
						       "Edit vertalers error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
			new EditVertalersDialog( connection, parentFrame, selectedVertalersId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Vertalers vertalers = new Vertalers( vertalersTableModel.getVertalersId( selectedRow ),
								   vertalersTableModel.getVertalersString( selectedRow ) );

			// Check if vertalers ID is still used
			if ( vertalers.presentInTable( "vertalers_persoon" ) ) return;
			if ( vertalers.presentInTable( "titel" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( vertalers.string == null ) || ( vertalers.string.length( ) == 0  ) ) {
			    vertalers.string = " ";
			}

			int result = JOptionPane.showConfirmDialog( EditVertalers.this,
                                                                    "Delete '" + vertalers.string + "' ?",
                                                                    "Delete vertalers record",
                                                                    JOptionPane.YES_NO_OPTION,
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null );
			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString  = "DELETE FROM vertalers WHERE vertalers_id = " + vertalers.id;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				final String errorString = "Could not delete record with vertalers_id  = " + vertalers.id + " in vertalers";
				JOptionPane.showMessageDialog( EditVertalers.this,
							       errorString,
							       "Edit vertalers error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog(EditVertalers.this,
                                                          "SQL exception in delete: " + sqlException.getMessage(),
                                                          "EditVertalers SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the table model again
		vertalersTableSorter.clearSortingState( );
		vertalersTableModel.setupVertalersTableModel( vertalersFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertVertalersButton = new JButton( "Insert" );
	insertVertalersButton.setActionCommand( "insert" );
	insertVertalersButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertVertalersButton );


	editVertalersButton.setActionCommand( "edit" );
	editVertalersButton.setEnabled( false );
	editVertalersButton.addActionListener( buttonActionListener );
	buttonPanel.add( editVertalersButton );


	deleteVertalersButton.setActionCommand( "delete" );
	deleteVertalersButton.setEnabled( false );
	deleteVertalersButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteVertalersButton );


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
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	setSize( 460, 500 );
	setLocation(x, y);
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}

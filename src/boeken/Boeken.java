// Package:	boeken
// File:	Boeken.java
// Description:	Boeken main program
// Author:	Chris van Engelen
// History:	2017/12/26: Initial version
//              2021/4/19: update JDBC driver

package boeken;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Boeken main program
 * @author Chris van Engelen
 */
public class Boeken extends JFrame implements ActionListener {
    private final static Logger logger = Logger.getLogger( boeken.Main.class.getCanonicalName() );

    private final JDesktopPane desktopPane;
    private Connection connection;

    private int openFrameCount = 0;
    private static final int xOffset = 30, yOffset = 30;

    private Boeken() {
        super("Boeken");

        final int inset = 100;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width  - (3 * inset), screenSize.height - (2 * inset));

        // Set up the GUI.
        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);
        setJMenuBar(createMenuBar());

        // Make dragging a little faster but perhaps uglier.
        desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.cj.jdbc.Driver" );
        } catch ( ClassNotFoundException classNotFoundException ) {
            logger.severe( "ClassNotFoundException: " + classNotFoundException.getMessage() );
            System.exit( 1 );
        }

        try {
            // Get the password for the boeken account, which gives access to schema boeken.
            final boeken.gui.PasswordPanel passwordPanel = new boeken.gui.PasswordPanel();
            final String password = passwordPanel.getPassword();
            if (password == null) {
                logger.info("No password");
                System.err.println("Geen password gegeven");
                System.exit( 1 );
            }

            // Get the connection to the boeken schema in the MySQL database
            connection = DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=boeken&password=" + password );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit( 1 );
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit( 1 );
        }

        // Add a window listener to close the connection when the frame is disposed
        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    // Close the connection to the MySQL database
                    connection.close( );
                } catch (SQLException sqlException) {
                    logger.severe( "SQL exception closing connection: " + sqlException.getMessage() );
                }
            }
        } );
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        Dimension maximumSize = new Dimension(120, 40);

        // Edit titel
        JMenuItem menuItem = new JMenuItem("Titel", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editTitel");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit auteurs
        menuItem = new JMenuItem("Auteurs", KeyEvent.VK_A);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editAuteurs");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit boek
        menuItem = new JMenuItem("Boek", KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editBoek");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit editors
        menuItem = new JMenuItem("Editors", KeyEvent.VK_E);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editEditors");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit label
        menuItem = new JMenuItem("Label", KeyEvent.VK_L);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editLabel");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit persoon
        menuItem = new JMenuItem("Persoon", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editPersoon");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit uitgever
        menuItem = new JMenuItem("Uitgever", KeyEvent.VK_U);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editUitgever");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        // Edit vertalers
        menuItem = new JMenuItem("Vertalers", KeyEvent.VK_V);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editVertalers");
        menuItem.setMaximumSize(maximumSize);
        menuItem.addActionListener(this);
        menuBar.add(menuItem);

        return menuBar;
    }

    // React to menu selections.
    public void actionPerformed(ActionEvent actionEvent) {
        JInternalFrame internalFrame = null;
        switch (actionEvent.getActionCommand()) {
        case "editTitel":
            internalFrame = new boeken.titel.EditTitel(connection, this, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editAuteurs":
            internalFrame = new boeken.auteurs.EditAuteurs(connection, this, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editBoek":
            internalFrame = new boeken.boek.EditBoek(connection, this, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editEditors":
            internalFrame = new boeken.editors.EditEditors(connection, this, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editLabel":
            internalFrame = new boeken.label.EditLabel(connection, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editPersoon":
            internalFrame = new boeken.persoon.EditPersoon(connection, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editUitgever":
            internalFrame = new boeken.uitgever.EditUitgever(connection, xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        case "editVertalers":
            internalFrame = new boeken.vertalers.EditVertalers(connection, this,xOffset * openFrameCount, yOffset * openFrameCount);
            break;
        }

        if (internalFrame == null) {
            logger.severe( "Invalid action command: " + actionEvent.getActionCommand() );
            return;
        }

        internalFrame.setVisible( true );
        desktopPane.add( internalFrame );
        try {
            internalFrame.setSelected( true );
            openFrameCount++;
        } catch ( java.beans.PropertyVetoException propertyVetoException ) {
            JOptionPane.showMessageDialog( this, propertyVetoException.getMessage( ),
                    "The internal frame could not be displayed",
                    JOptionPane.ERROR_MESSAGE);
            logger.severe( propertyVetoException.getMessage() );
        }
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread, creating and showing this application's GUI.
        // See: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
        // and: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
        try {
            javax.swing.SwingUtilities.invokeAndWait( () -> {
                // Use the default window decorations.
                JFrame.setDefaultLookAndFeelDecorated( true );

                // Create and set up the window.
                Boeken boeken = new Boeken();
                boeken.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

                // Display the window.
                boeken.setVisible( true );
            } );
        }
        catch (InvocationTargetException | InterruptedException exc) {
            System.err.print("Exception: " + exc.getMessage());
            System.exit(1);
        }
    }
}

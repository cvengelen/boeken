// Main program to show and select records from editors
// Start with boeken.editors.Editors

package boeken.editors;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Editors {
    private final static Logger logger = Logger.getLogger( Editors.class.getCanonicalName() );

    public static void main( String[ ] args ) {
        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );

            logger.info( "Opening db connection" );
            new EditorsFrame( DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=cvengelen&password=cve123" ) );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit(1);
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit(1);
        }
    }
}

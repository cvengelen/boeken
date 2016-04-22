// Main program to show and select records from label
// Start with boeken.label.Label

package boeken.label;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Label {
    private final static Logger logger = Logger.getLogger( Label.class.getCanonicalName() );

    public static void main( String[ ] args ) {
        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );

            logger.info( "Opening db connection" );
            new LabelFrame( DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=cvengelen&password=cve123" ) );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit(1);
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit(1);
        }
    }
}

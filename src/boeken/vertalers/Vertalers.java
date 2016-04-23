// Main program to show and select records from vertalers
// Start with boeken.vertalers.Vertalers

package boeken.vertalers;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

public class Vertalers {
    private final static Logger logger = Logger.getLogger( Vertalers.class.getCanonicalName() );

    public static void main( String[ ] args ) {
        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );

            logger.info( "Opening db connection" );
            new VertalersFrame( DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=cvengelen&password=cve123" ) );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit(1);
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit(1);
        }
    }
}

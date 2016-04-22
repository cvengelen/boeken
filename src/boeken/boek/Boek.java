// Main program to insert or update in boek
// Start with boeken.boek.Boek

package boeken.boek;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.*;

public class Boek {
    private final static Logger logger = Logger.getLogger( Boek.class.getCanonicalName() );

    public static void main( String[ ] args ) {
        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );

            logger.info( "Opening db connection" );
            new BoekFrame(DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=cvengelen&password=cve123"));
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit(1);
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit(1);
        }
    }
}

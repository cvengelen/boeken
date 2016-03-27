// Main program to show and select records from auteurs
// Start with boeken.Auteurs

package boeken;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 
import java.util.logging.*;

import boeken.gui.*;


public class Auteurs {
    final static Logger logger = Logger.getLogger( "boeken.Auteurs" );

    public static void main( String[ ] args ) {
        try {
            // The newInstance() call is a work around for some broken Java implementations
            // Class.forName("com.mysql.jdbc.Driver").newInstance();
            Class.forName( "com.mysql.jdbc.Driver" );
        } catch ( Exception exception ) {
	    logger.severe( "Exception: " + exception.getMessage( ) );
	    return;
        }

	final Connection connection;
	try {
            logger.info( "Opening db connection" );
            connection = DriverManager.getConnection( "jdbc:mysql://localhost/boeken?user=cvengelen&password=cve123" );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
        }

	AuteursFrame auteursFrame = new AuteursFrame( connection );
    }
}

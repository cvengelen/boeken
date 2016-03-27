// Class to hold key of record in titel

package boeken.gui;

public class TitelKey {
    private int boekId = 0;
    private int auteursId = 0;
    private String titelString = null;

    public TitelKey( ) {
	this.boekId = 0;
	this.auteursId = 0;
	this.titelString = null;
    }

    public TitelKey( int boekId,
		     int auteursId,
		     String titelString ) {
	this.boekId = boekId;
	this.auteursId = auteursId;
	this.titelString = titelString;
    }

    public int getBoekId( ) { return boekId; }

    public int getAuteursId( ) { return auteursId; }

    public String getTitelString( ) { return titelString; }

    public boolean equals( Object object ) {
	if ( object == null ) return false;
	if ( ! ( object instanceof TitelKey ) ) return false;
	if ( ( ( ( TitelKey )object ).getBoekId( )    != boekId     ) ||
	     ( ( ( TitelKey )object ).getAuteursId( ) != auteursId  ) ||
	     ( ! ( titelString.equals( ( ( TitelKey )object ).getTitelString( ) ) ) ) ) return false;
	return true;
    }

    public String toString( ) {
	return ( "boek_id: " + boekId +
		 ", auteurs_id: " + auteursId +
		 ", titel: " + titelString );
    }
}

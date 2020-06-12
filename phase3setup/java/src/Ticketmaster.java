/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.math.BigInteger;  
import java.nio.charset.StandardCharsets; 
import java.security.MessageDigest;  
import java.security.NoSuchAlgorithmException;  

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class Ticketmaster{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public Ticketmaster(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while (rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + Ticketmaster.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		Ticketmaster esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new Ticketmaster (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add User");
				System.out.println("2. Add Booking");
				System.out.println("3. Add Movie Showing for an Existing Theater");
				System.out.println("4. Cancel Pending Bookings");
				System.out.println("5. Change Seats Reserved for a Booking");
				System.out.println("6. Remove a Payment");
				System.out.println("7. Clear Cancelled Bookings");
				System.out.println("8. Remove Shows on a Given Date");
				System.out.println("9. List all Theaters in a Cinema Playing a Given Show");
				System.out.println("10. List all Shows that Start at a Given Time and Date");
				System.out.println("11. List Movie Titles Containing \"love\" Released After 2010");
				System.out.println("12. List the First Name, Last Name, and Email of Users with a Pending Booking");
				System.out.println("13. List the Title, Duration, Date, and Time of Shows Playing a Given Movie at a Given Cinema During a Date Range");
				System.out.println("14. List the Movie Title, Show Date & Start Time, Theater Name, and Cinema Seat Number for all Bookings of a Given User");
				System.out.println("15. EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddUser(esql); break;
					case 2: AddBooking(esql); break;
					case 3: AddMovieShowingToTheater(esql); break;
					case 4: CancelPendingBookings(esql); break;
					case 5: ChangeSeatsForBooking(esql); break;
					case 6: RemovePayment(esql); break;
					case 7: ClearCancelledBookings(esql); break;
					case 8: RemoveShowsOnDate(esql); break;
					case 9: ListTheatersPlayingShow(esql); break;
					case 10: ListShowsStartingOnTimeAndDate(esql); break;
					case 11: ListMovieTitlesContainingLoveReleasedAfter2010(esql); break;
					case 12: ListUsersWithPendingBooking(esql); break;
					case 13: ListMovieAndShowInfoAtCinemaInDateRange(esql); break;
					case 14: ListBookingInfoForUser(esql); break;
					case 15: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static byte[] getSHA(String input) throws NoSuchAlgorithmException 
    {  
        // Static getInstance method is called with hashing SHA  
        MessageDigest md = MessageDigest.getInstance("SHA-256");  
  
        // digest() method called  
        // to calculate message digest of an input  
        // and return array of byte 
        return md.digest(input.getBytes(StandardCharsets.UTF_8));  
    } 
    
    public static String toHexString(byte[] hash) 
    { 
        // Convert byte array into signum representation  
        BigInteger number = new BigInteger(1, hash);  
  
        // Convert message digest into hex value  
        StringBuilder hexString = new StringBuilder(number.toString(16));  
  
        // Pad with leading zeros 
        while (hexString.length() < 32)  
        {  
            hexString.insert(0, '0');  
        }  
  
        return hexString.toString();  
    }
	
	public static void AddUser(Ticketmaster esql){//1
		try {
			String email;
			String first;
			String last;
			long phone_number;
			String password;

			System.out.print("Enter email address: ");
			email = in.readLine();
			System.out.print("Enter first name: ");
			first = in.readLine();
			System.out.print("Enter last name: ");
			last = in.readLine();
			System.out.print("Enter phone number: ");
			phone_number = Long.parseLong(in.readLine());
			System.out.print("Enter new password: ");
			password = toHexString(getSHA(in.readLine()));
		
			String sql_stmt = String.format("INSERT INTO Users (email, lname, fname, phone, pwd) VALUES ('%s', '%s', '%s', '%d', '%s');", email, last, first, phone_number, password);
			esql.executeUpdate(sql_stmt);

			System.out.println("Successfully added new user!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void AddBooking(Ticketmaster esql){//2
		try {
			String email;
			String first;
			String last;
			long phone_number;
			String password;

			System.out.print("Enter email address: ");
			email = in.readLine();
			System.out.print("Enter first name: ");
			first = in.readLine();
			System.out.print("Enter last name: ");
			last = in.readLine();
			System.out.print("Enter phone number: ");
			phone_number = Long.parseLong(in.readLine());
			System.out.print("Enter new password: ");
			password = toHexString(getSHA(in.readLine()));
		
			String sql_stmt = String.format("INSERT INTO Users (email, lname, fname, phone, pwd) VALUES ('%s', '%s', '%s', '%d', '%s');", email, last, first, phone_number, password);
			esql.executeUpdate(sql_stmt);

			System.out.println("Successfully added new user!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		try {
			String email;
			String first;
			String last;
			long phone_number;
			String password;

			System.out.print("Enter email address: ");
			email = in.readLine();
			System.out.print("Enter first name: ");
			first = in.readLine();
			System.out.print("Enter last name: ");
			last = in.readLine();
			System.out.print("Enter phone number: ");
			phone_number = Long.parseLong(in.readLine());
			System.out.print("Enter new password: ");
			password = toHexString(getSHA(in.readLine()));
		
			String sql_stmt = String.format("INSERT INTO Users (email, lname, fname, phone, pwd) VALUES ('%s', '%s', '%s', '%d', '%s');", email, last, first, phone_number, password);
			esql.executeUpdate(sql_stmt);

			System.out.println("Successfully added new user!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void CancelPendingBookings(Ticketmaster esql){//4
		String sql_stmt = "UPDATE Bookings SET status = 'Cancelled' WHERE status = 'Pending';";
		esql.executeUpdate(sql_stmt);
		System.out.println("All pending bookings have been marked as cancelled\n");
	}
	
	public static void ChangeSeatsForBooking(Ticketmaster esql) throws Exception{//5
 		try {
			String bookingID;
			String seatID;
			String new_seatID;
			String sql_stmt;

			System.out.print("Enter the booking ID of the seat you want to change ");
			bookingID = in.readLine();
	
			System.out.print("What is the seat ID that you want to change?");
			seatID = in.readLine();
			System.out.print("What is your new seat ID?");
			new_seatID = in.readLine();

 			/* TODO:
			if (new_seatID = ssid AND bid = null) {
				if (price = new_seatID price) {
					sql_stmt "UPDATE ShowSeats SET bid = null WHERE ssid = " + seatID;
					esql.executeUpdate(sql_stmt);
					sql_stmt = "UPDATE ShowSeats SET bid = " + bid + " WHERE ssid = " + new_seatID;
					esql.executeUpdate(sql_stmt);
					System.out.println("Successfully replaced your seat!\n");
				} else {
					System.out.println("Sorry! The seat you want to switch is different in price. \n")
				}
			} else {
				System.out.println("Sorry! We couldn't find that \n");
			}*/
			System.out.println("Successfully replaced your seat!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void RemovePayment(Ticketmaster esql){//6
		try {
			String paymentID;
			String sql_stmt;
			String bid;
			String seatID;

			System.out.print("Enter the payment ID you want to remove: ");
			paymentID = in.readLine();
			
			// find matching bid to the bid in payment
			sql_stmt = "SELECT bid FROM Payments WHERE pid = " + paymentID;
			bid = esql.executeUpdate(sql_stmt).get(0).get(0);

			sql_stmt = "SELECT sid FROM ShowSeats, Bookings WHERE bid = " + bid;
			seatID = esql.executeUpdate(sql_stmt).get(0).get(0);
			
			// change booking status from that bid to cancelled
			sql_stmt "UPDATE ShowSeats SET bid = \"Cancelled\" WHERE ssid = " + seatID + "AND bid = " + bid;
			esql.executeUpdate(sql_stmt);
			
			// remove the payment instance
			sql_stmt = "DELETE FROM Payments WHERE pid = " + paymentID;
			esql.executeUpdate(sql_stmt)
			System.out.println("Successfully removed the payment!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}	
	}
	
	public static void ClearCancelledBookings(Ticketmaster esql){//7
		String sql_stmt = "DELETE FROM Bookings WHERE status = 'Cancelled'";
		esql.executeUpdate(sql_stmt);
		System.out.println("All cancelled bookings have been removed\n");
	}

	public static void RemoveShowsOnDate(Ticketmaster esql){//8
		try {
			String theatreName;
			String showDate;

			System.out.print("What is the name of the cinema?\n");
			theatreName = in.readLine();
			
			System.out.print("What is the date that you want to cancel?\n");
			showDate = in.readLine();
			
			// TODO:
			// "remove" bookings using remove payment above
			
			System.out.println("Successfully removed all shows on that date!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}	
	}
	
	public static void ListTheatersPlayingShow(Ticketmaster esql){//9
		//
		
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		//
		
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		//
		
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		//
		
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		//
		
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		//
		
	}
}
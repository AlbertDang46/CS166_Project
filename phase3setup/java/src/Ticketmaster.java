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
import java.util.Random;
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
			String status;
			String bdatetime;
			int seats;
			long sid;
			String email;

			String movie_title;
			List<Long> ssids = new ArrayList<Long>();

			System.out.print("Enter booking status: ");
			status = in.readLine();
			System.out.print("Enter booking datetime in format YYYY-MM-DD HH:MM:SS: ");
			bdatetime = in.readLine() + "-08";
			System.out.print("Enter user account email: ");
			email = in.readLine();

			System.out.print("Enter title of movie to book: ");
			movie_title = in.readLine();
			List<List<String>> movie_list = esql.executeQueryAndReturnResult(String.format("SELECT mvid FROM Movies WHERE title = '%s';", movie_title));
			long mvid = Long.parseLong(movie_list.get(0).get(0));

			System.out.print(String.format("Here are a list of showings for %s\n", movie_title));
			esql.executeQueryAndPrintResult(String.format("SELECT S.sid, S.sdate, S.sttime, S.edtime, T.tname, T.tseats FROM Shows S, Theaters T, Plays P WHERE S.sid = P.sid AND T.tid = P.tid AND S.mvid = %d AND (S.sdate > CAST('%s' AS DATE) OR (S.sdate = CAST('%s' AS DATE) AND S.edtime > CAST('%s' AS TIME)));", mvid, bdatetime.substring(0, 10), bdatetime.substring(0, 10), bdatetime.substring(11, 19)));
			System.out.print("Enter in SID of showing that you would like to book: ");
			sid = Long.parseLong(in.readLine());

			System.out.print("Here are a list of available seats for your selected movie showing\n");
			esql.executeQueryAndPrintResult(String.format("SELECT ssid, price FROM ShowSeats WHERE sid = %d AND bid IS NULL;", sid));
			System.out.print("Enter in ssids of seats you would like to book on each line or enter in 'q' to stop selecting seats:\n");
			seats = 0;
			boolean selectSeats = true;
			while(selectSeats) {
				String selection = in.readLine();
				if(selection.equals("q")) {
					break;
				}
				ssids.add(Long.parseLong(selection));
				seats++;
			}

			esql.executeUpdate(String.format("INSERT INTO Bookings (bid, status, bdatetime, seats, sid, email) VALUES (nextval('Booking_Seq'), '%s', '%s', %d, %d, '%s');", status, bdatetime, seats, sid, email));

			for(long ssid : ssids) {
				esql.executeUpdate(String.format("UPDATE ShowSeats SET bid = %d WHERE ssid = %d", esql.getCurrSeqVal("Booking_Seq"), ssid));
			}

			System.out.println("Successfully added new booking!\n");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void AddMovieShowingToTheater(Ticketmaster esql){//3
		try {
			String title;
			String rdate;
			String country;
			String description;
			int duration;
			String lang;
			String genre;

			String city;
			long cid;
			long tid;

			String sdate;
			String sttime;
			String edtime;
			
			System.out.print("Enter title of new movie: ");
			title = in.readLine();
			System.out.print("Enter release date of new movie in format MM/DD/YYYY: ");
			rdate = in.readLine();
			System.out.print("Enter country of new movie: ");
			country = in.readLine();
			System.out.print("Enter description of new movie: ");
			description = in.readLine();
			System.out.print("Enter duration of new movie: ");
			duration = Integer.parseInt(in.readLine());
			System.out.print("Enter language code of new movie: ");
			lang = in.readLine();
			System.out.print("Enter genre of new movie: ");
			genre = in.readLine();

			esql.executeUpdate(String.format("INSERT INTO Movies (mvid, title, rdate, country, description, duration, lang, genre) VALUES (nextval('Movie_Seq'), '%s', '%s', '%s', '%s', %d, '%s', '%s');", title, rdate, country, description, duration, lang, genre));

			System.out.print("Enter in city of cinema where showing will be: ");
			city = in.readLine();
			System.out.print("Here are a list of cinemas in this city\n");
			esql.executeQueryAndPrintResult(String.format("SELECT C2.cid, C2.cname, C2.tnum FROM Cities C1, Cinemas C2 WHERE C1.city_id = C2.city_id AND C1.city_name = '%s';", city));

			System.out.print("Enter in cid of cinema where showing will be: ");
			cid = Long.parseLong(in.readLine());
			System.out.print("Here are a list of theaters in the selected cinema\n");
			esql.executeQueryAndPrintResult(String.format("SELECT tid, tname, tseats FROM Theaters WHERE cid = %d;", cid));

			System.out.print("Enter in tid of theater where showing will be: ");
			tid = Long.parseLong(in.readLine());
			System.out.print("Enter in date of showing in format MM/DD/YYYY: ");
			sdate = in.readLine();
			System.out.print("Enter in start time of showing in format HH:MM: ");
			sttime = in.readLine();
			System.out.print("Enter in end time of showing in format HH:MM: ");
			edtime = in.readLine();

			esql.executeUpdate(String.format("INSERT INTO Shows (sid, mvid, sdate, sttime, edtime) VALUES (nextval('Show_Seq'), %d, '%s', '%s', '%s');", esql.getCurrSeqVal("Movie_Seq"), sdate, sttime, edtime));
			esql.executeUpdate(String.format("INSERT INTO Plays (sid, tid) VALUES (%d, %d);", esql.getCurrSeqVal("Show_Seq"), tid));

			List<List<String>> cinemaSeats = esql.executeQueryAndReturnResult(String.format("SELECT * FROM CinemaSeats WHERE tid = %d;", tid));

			for(List<String> cs: cinemaSeats) {
				esql.executeUpdate(String.format("INSERT INTO ShowSeats (ssid, sid, csid, price) VALUES (nextVal('ShowSeat_Seq'), %d, %d, %d);", esql.getCurrSeqVal("Show_Seq"), Long.parseLong(cs.get(0)), new Random().nextInt(7) + 6));
			}

			System.out.println("Successfully added new movie showing!\n");
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
		try {
			String sdate;
			long sid;

			System.out.print("Enter in start date of show in format MM/DD/YYYY: ");
			sdate = in.readLine();

			System.out.print("Here are all the shows playing on this date\n");
			esql.executeQueryAndPrintResult(String.format("SELECT * FROM Shows WHERE sdate = '%s';", sdate));
			
			System.out.print("Enter in sid of show: ");
			sid = Long.parseLong(in.readLine());

			System.out.print("Here are all the theaters playing this show\n");
			esql.executeQueryAndPrintResult(String.format("SELECT C1.city_name, C2.cname, T.tname, T.tseats FROM Cities C1, Cinemas C2, Theaters T, Plays P WHERE P.sid = %d AND P.tid = T.tid AND T.cid = C2.cid AND C2.city_id = C1.city_id;", sid));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
	
	public static void ListShowsStartingOnTimeAndDate(Ticketmaster esql){//10
		try {
			String sdate;
			String sttime;

			System.out.print("Enter in start date of show in format MM/DD/YYYY: ");
			sdate = in.readLine();
			System.out.print("Enter in start time of show in format HH:MM: ");
			sttime = in.readLine();

			System.out.print("Here are all the shows that start on this date and time\n");
			esql.executeQueryAndPrintResult(String.format("SELECT * FROM Shows WHERE sdate = '%s' AND sttime = '%s';", sdate, sttime));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}

	public static void ListMovieTitlesContainingLoveReleasedAfter2010(Ticketmaster esql){//11
		try {
			System.out.print("Here are all movies containing the word 'love' released after 2010\n");
			esql.executeQueryAndPrintResult("SELECT * FROM Movies WHERE title ILIKE '%love%' AND rdate > CAST('12/31/2010' AS DATE);");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}

	public static void ListUsersWithPendingBooking(Ticketmaster esql){//12
		try {
			System.out.print("Here are all the users who have a booking with a status of pending\n");
			esql.executeQueryAndPrintResult("SELECT U.fname, U.lname, U.email FROM Users U, Bookings B WHERE B.status = 'Pending' AND B.email = U.email;");
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}

	public static void ListMovieAndShowInfoAtCinemaInDateRange(Ticketmaster esql){//13
		try {
			String title;
			long mvid;
			String startDate;
			String endDate;

			System.out.print("Enter movie title: ");
			title = in.readLine();
			mvid = Long.parseLong(esql.executeQueryAndReturnResult(String.format("SELECT mvid FROM Movies WHERE title = '%s';", title)).get(0).get(0));

			System.out.print("Enter start date of date range in format MM/DD/YYYY: ");
			startDate = in.readLine();
			System.out.print("Enter end date of date range in format MM/DD/YYYY: ");
			endDate = in.readLine();

			System.out.print("Here are all the shows playing this movie in this date range\n");
			esql.executeQueryAndPrintResult(String.format("SELECT M.title, M.duration, S.sdate, S.sttime FROM Movies M, Shows S WHERE M.mvid = %d AND M.mvid = S.mvid AND S.sdate >= CAST('%s' AS DATE) AND S.sdate <= CAST('%s' AS DATE)", mvid, startDate, endDate));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}

	public static void ListBookingInfoForUser(Ticketmaster esql){//14
		try {
			String email;

			System.out.print("Enter in user email: ");
			email = in.readLine();

			System.out.print("Here are all the bookings for this user\n");
			esql.executeQueryAndPrintResult(String.format("SELECT M.title, S1.sdate, S1.sttime, T.tname, C.sno FROM Movies M, Shows S1, Theaters T, ShowSeats S2, CinemaSeats C, Plays P, Bookings B WHERE B.email = '%s' AND B.sid = S1.sid AND S1.mvid = M.mvid AND S1.sid = P.sid AND P.tid = T.tid AND B.bid = S2.bid AND S2.csid = C.csid", email));
		} catch (Exception e) {
			System.out.println(e.getMessage() + "\n");
		}
	}
}
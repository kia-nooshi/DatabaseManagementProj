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
import java.lang.Math;
// import java.security.Timestamp;
import java.sql.Timestamp;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;




/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Hotel {

   //global variables
   static String managerID = "";
   static String customerID = "";

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of Hotel 
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Hotel(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Hotel

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
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
   }//end executeQuery

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
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
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
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
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
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   public int getNewUserID(String sql) throws SQLException {
      Statement stmt = this._connection.createStatement ();
      ResultSet rs = stmt.executeQuery (sql);
      if (rs.next())
         return rs.getInt(1);
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
            "Usage: " +
            "java [-classpath <classpath>] " +
            Hotel.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Hotel esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Hotel object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Hotel (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
                System.out.println("MAIN MENU");
                System.out.println("---------");
                System.out.println("1. View Hotels within 30 units");
                System.out.println("2. View Rooms");
                System.out.println("3. Book a Room");
                System.out.println("4. View recent booking history");

                //the following functionalities basically used by managers
                System.out.println("5. Update Room Information");
                System.out.println("6. View 5 recent Room Updates Info");
                System.out.println("7. View booking history of the hotel");
                System.out.println("8. View 5 regular Customers");
                System.out.println("9. Place room repair Request to a company");
                System.out.println("10. View room repair Requests history");

                System.out.println(".........................");
                System.out.println("20. Log out");
                switch (readChoice()){
                   case 1: viewHotels(esql); break;
                   case 2: viewRooms(esql); break;
                   case 3: bookRooms(esql); break;
                   case 4: viewRecentBookingsfromCustomer(esql); break;
                   case 5: updateRoomInfo(esql); break;
                   case 6: viewRecentUpdates(esql); break;
                   case 7: viewBookingHistoryofHotel(esql); break;
                   case 8: viewRegularCustomers(esql); break;
                   case 9: placeRoomRepairRequests(esql); break;
                   case 10: viewRoomRepairHistory(esql); break;
                   case 20: usermenu = false; break;
                   default : System.out.println("Unrecognized choice!"); break;
                }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
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

   /*
    * Creates a new user
    **/
   public static void CreateUser(Hotel esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine(); 
         String type="Customer";
			String query = String.format("INSERT INTO USERS (name, password, userType) VALUES ('%s','%s', '%s')", name, password, type);
         esql.executeUpdate(query);
         System.out.println ("User successfully created with userID = " + esql.getNewUserID("SELECT last_value FROM users_userID_seq"));
         
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Hotel esql){
      try{
         System.out.print("\tEnter userID: ");
         String userID = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         managerID = userID;
         String query = String.format("SELECT * FROM USERS WHERE userID = '%s' AND password = '%s'", userID, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return userID;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

public static void viewHotels(Hotel esql) {
   try {
       System.out.print("\tEnter longitude: ");
       String longi = in.readLine();
       System.out.print("\tEnter latitude: ");
       String lat = in.readLine();

       double long2 = Double.parseDouble(longi);
       double lat2 = Double.parseDouble(lat);

         

       String query = "SELECT hotelID, hotelName FROM Hotel WHERE calculate_distance(latitude, longitude, ";
       query += lat2;
       query += ", ";
       query += long2;
       query += ") <= 30";
       List<List<String>> viewHotels = esql.executeQueryAndReturnResult(query);
       System.out.println ("Hotels within 30 units of distance: " + viewHotels);
   } catch(Exception e) {
       System.err.println (e.getMessage ());
   }
}


   public static void viewRooms(Hotel esql) {

      try{
         System.out.println("These are the list of available hotels");
         String validHotels = "SELECT hotelName, hotelID FROM Hotel";
         System.out.println();
         esql.executeQueryAndPrintResult(validHotels);   
         List<List<String>> getHotels = esql.executeQueryAndReturnResult(validHotels);   

         System.out.print("\tEnter HotelID: ");
         String hotelID = in.readLine();
         boolean hotelChecker = true;
         
         while (hotelChecker){
               for (int i = 0; i < 20; i++ ){
                  if(hotelID.equals(getHotels.get(i).get(1))){
                     hotelChecker = false;
                  }
               } if(hotelChecker) {
                  System.out.println();
                  System.out.print("DNE! Enter a Valid HotelID: ");
                  hotelID = in.readLine();
               }

         }
         
         boolean validDate = true;
         System.out.print("to view rooms, enter your desired date in format (yyyy-mm-dd): ");
         String date = in.readLine();

         while (validDate){
            
            String formatDate = "\\d{4}-\\d{2}-\\d{2}";

            if(date.matches(formatDate)){
               validDate = false;
               
            }else{
               System.out.println();
               System.out.print("Date was formatted invalidly. Enter date in correct format.");
               System.out.println("\n");
            }

             
         }

         String query = "SELECT r.roomNumber, r.price, rs.bookingDate ";
               query+= "FROM Rooms r "; 
               query += "INNER JOIN RoomBookings rs ";
               query += "ON r.roomNumber = rs.roomNumber ";
               query+= "WHERE r.hotelID = ";
                     query+= hotelID;
                     query += " AND rs.bookingDate = ";
                     query+= date; 

         esql.executeQueryAndPrintResult(query);

         

      }  catch(Exception e){
         System.err.println (e.getMessage ());
      }
      
      

   }


   public static void bookRooms(Hotel esql) {
      try {
         System.out.println("Enter hotelID: ");
         String hotelID = in.readLine();
         System.out.println("Enter room number: ");
         String roomNum = in.readLine();
         System.out.println("Enter date (yyyy-mm-dd) you want to book: ");
         String date = "";
         date = in.readLine();
         String dateFormat = "\\d{4}-\\d{2}-\\d{2}";
         System.out.println("Date user inputted is: " + date);
         if (date.matches(dateFormat) == false) {
            System.out.println("Dateformat is false");
            throw new RuntimeException("Date must be formatted in yyyy-mm-dd");
         }
         if (hotelID == null || roomNum == null || hotelID.isEmpty() || roomNum.isEmpty()) {
            throw new RuntimeException("Hotel ID and Room Number cannot be null or empty.");
        }
         String query = "SELECT rb.hotelID, rb.roomNumber, rb.bookingDate ";
                query += "FROM RoomBookings rb ";
                query += "WHERE rb.hotelID = ";
                query +=  hotelID;
                query += " AND rb.roomNumber = ";
                query += roomNum;
                query += " AND rb.bookingDate = '";
                query += date;
                query += "'";
         System.out.println(query);
         String query2 = String.format("INSERT INTO RoomBookings (customerID, hotelID, roomNumber, bookingDate) VALUES ('%s','%s','%s','%s')", managerID, hotelID, roomNum, date);  
         System.out.println(query2);
                int bookRoomsCheck = esql.executeQuery(query);
                System.out.println(query2);
                esql.executeUpdate(query2);
                System.out.println(query2);
                if (bookRoomsCheck > 0) {
                  System.out.println("Room is booked.");
                }else {
                  System.out.println("Room is not booked.");
                }
         String query3 = String.format("SELECT r.price FROM Rooms r, RoomBookings rb WHERE r.hotelID = ");
               query3 += hotelID;
               query3 += " AND r.roomNumber = ";
               query3+= roomNum;
               List<List<String>> bookRoomsPrices = esql.executeQueryAndReturnResult(query3);
                System.out.println ("The room has been booked successfully and is priced at : " + bookRoomsPrices);   
                System.out.println(query3);             
   } catch(Exception e) {
          System.err.println (e.getMessage ());
      }
   }


   public static void viewRecentBookingsfromCustomer(Hotel esql) {
      try {
         System.out.println("Here are the 5 Most Recent Bookings");
         String recentBookings = "SELECT b.hotelID, b.roomNumber, b.bookingDate, r.price FROM Rooms r, RoomBookings b WHERE b.hotelID = r.hotelID AND b.roomNumber = r.roomNumber AND b.customerID = ";
         recentBookings += customerID;
         recentBookings += " ORDER BY b.bookingDate";
         List<List<String>> getBookings = esql.executeQueryAndReturnResult(recentBookings);
   } catch(Exception e) {
          System.err.println (e.getMessage ());
      }
   }


   public static void updateRoomInfo(Hotel esql) {

      try{
      
         boolean valid = true;
         boolean update = true;

         while (update){
            System.out.println("you have edit access to these hotels");

            String rightID = "SELECT DISTINCT h.hotelName, h.hotelID ";
                  rightID += "FROM Hotel h ";
                  rightID += "INNER JOIN Rooms r ON h.hotelID = r.hotelID ";
                  rightID += "WHERE h.hotelID = r.hotelID AND managerUserID = ";
                  rightID += managerID;
                  rightID += " ORDER BY h.hotelID";

               List<List<String>> managedHotels = esql.executeQueryAndReturnResult(rightID);


            for (int s = 0; s < managedHotels.size(); s++){
               System.out.println("hotel name");
               System.out.println(managedHotels.get(s).get(0));
               System.out.println("hotel ID");
               System.out.println(managedHotels.get(s).get(1));
            }
                  
            System.out.println("type in hotel ID to view room information");
            String hotel = in.readLine();

            while (valid){
               for (int k = 0; k < managedHotels.size(); k++){
                  if (hotel.equals(managedHotels.get(k).get(1))){
                        valid = false;
                  }
                 
               }
               if (valid){
                  System.out.println("\n hotelID DNE! enter valid ID: ");
                  hotel = in.readLine();
               }
            }

            System.out.println(" ");
            System.out.println(" ");

            String validRooms = "SELECT roomNumber, price, imageUrl FROM Rooms r WHERE hotelID = ";
                   validRooms += hotel;

               List<List<String>> possibleRooms = esql.executeQueryAndReturnResult(validRooms);
 
               System.out.println(" \n rooms of hotel you chose \n ");
               for (int k = 0; k < possibleRooms.size(); k++){

                  System.out.println("Room # ");
                  System.out.println(possibleRooms.get(k).get(0));

               }

               boolean check = false;

               System.out.println("enter number of room you want to update: ");
               String number = in.readLine();

                     while (!check){
                        for (int s = 0; s < possibleRooms.size(); s++){
                           if(number.equals(possibleRooms.get(s).get(0))){
                        
                              check = true;
                           }
                        }
                           if (!check){
                              System.out.println("room number DNE! type a valid room number: ");
                              number = in.readLine();
                           
                        }
                     }

                     System.out.println("\n enter updated room price in $: ");
                     String updatedPrice = in.readLine();
                     System.out.println("\n");
                     
                     String newPrice = "UPDATE Rooms SET Price = ";
                            newPrice += updatedPrice;
                            newPrice += " WHERE hotelID = ";
                            newPrice += hotel;
                            newPrice += " AND roomNumber = ";
                            newPrice += number;

                   
                     esql.executeUpdate(newPrice);

                     Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
                     String priceTime = currentTimestamp.toString();
                     java.util.Date pricedate = new java.util.Date();

                     String updateNew = String.format("INSERT INTO RoomUpdatesLog(managerID, hotelID, roomNumber, updatedOn) VALUES ('%s', '%s', '%s', '%s' );", managerID, hotel, number, pricedate);

                     esql.executeUpdate(updateNew);

                     System.out.println("update image URl");
                     String image = in.readLine();
                     System.out.println("\n");

                     String imageUpdate = "UPDATE Rooms SET imageURL = '";
                            imageUpdate += image;
                            imageUpdate += "' WHERE hotelID = ";
                            imageUpdate += hotel;
                            imageUpdate += " AND roomNumber = ";
                            imageUpdate += number;

                            System.out.println("step 1");
                     esql.executeUpdate(imageUpdate);
                     System.out.println("step 2");
                     Timestamp curTimestamp = new Timestamp(System.currentTimeMillis());
                     String newTime = curTimestamp.toString();
                     java.util.Date date = new java.util.Date();
                     

                     String secondUpdate = String.format("INSERT INTO RoomUpdatesLog (managerID, hotelID, roomNumber, updatedOn) VALUES ('%s', '%s', '%s' , '%s' );" , managerID, hotel, number, date );
                     esql.executeUpdate(secondUpdate);
                     System.out.println("step 3");


                     System.out.println("\n updates successful");

                  update = false;

         }

            
                     



      }  catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }


   public static void viewRecentUpdates(Hotel esql) {
      try {
         String userID = managerID;
         String isManager = String.format("SELECT h.managerUserID FROM Hotel h WHERE h.managerUserID = %s", userID);
         
         if(esql.executeQueryAndPrintResult(isManager) > 0) {
            String recentLogs = String.format("SELECT u.hotelID, u.roomNumber FROM RoomUpdatesLog WHERE managerID = %s ORDER BY updatedOn DESC LIMIT 5", userID);
            esql.executeQueryAndPrintResult(recentLogs);
         }else{
            System.out.println("You aren't authorized to view the Recent Updates History ");
         }


   } catch(Exception e) {
          System.err.println (e.getMessage ());
      }
   }



   public static void viewBookingHistoryofHotel(Hotel esql) {
   try{
   }  catch(Exception e){
      System.err.println (e.getMessage ());
   }

   }


   public static void viewRegularCustomers(Hotel esql) {
      try{

         System.out.println("These are the list of hotels you manage ");
         String validHotels = "SELECT DISTINCT hotelName, hotelID FROM Hotel h, Rooms r";
                validHotels += "WHERE h.hotelID = r.hotelID AND managerUserID = ";
                validHotels += managerID;
                validHotels += "ORDER BY h.hotelID"; 
         List<List<String>> getHotels = esql.executeQueryAndReturnResult(validHotels);

        for (int k = 0; k < getHotels.size(); k++) {

         System.out.print(" hotel name: ");
         System.out.print(getHotels.get(k).get(0));
         System.out.print("\n hotel ID: ");
         System.out.print(getHotels.get(k).get(1));
         System.out.println();

        }

        

 
      }  catch(Exception e){
         System.err.println (e.getMessage ());
      }

   }


   public static void placeRoomRepairRequests(Hotel esql) {
      try{
         System.out.println("These are the list of available hotels");
         String validHotels = "SELECT hotelName, hotelID FROM Hotel";
         System.out.println();
         esql.executeQueryAndPrintResult(validHotels);
         List<List<String>> getHotels = esql.executeQueryAndReturnResult(validHotels);
         System.out.print("\tEnter HotelID: ");
         String hotelID = in.readLine();
         boolean hotelChecker = true;
         
         while (hotelChecker){
               for (int i = 0; i < 20; i++ ){
                  if(hotelID.equals(getHotels.get(i).get(1))){
                     hotelChecker = false;
                  }
               } if(hotelChecker) {
                  System.out.println();
                  System.out.print("DNE! Enter a Valid HotelID: ");
                  hotelID = in.readLine();
               }

         }

         System.out.println("These are the list of available rooms");
         String validRooms = "SELECT roomNumber FROM Rooms WHERE hotelID = ";
                validRooms+= hotelID;
         System.out.println();
         System.out.print("\tEnter room: ");
         esql.executeQueryAndPrintResult(validRooms);
         List<List<String>> getRooms = esql.executeQueryAndReturnResult(validRooms);
         String roomNumber = in.readLine();
         boolean roomChecker = true;
         
         while (roomChecker){
               for (int i = 0; i < 10; i++ ){
                  if(roomNumber.equals(getRooms.get(i).get(0))){
                     roomChecker = false;
                  }
               } if(roomChecker) {
                  System.out.println();
                  System.out.print("DNE! Enter a Valid room: ");
                  roomNumber = in.readLine();
               }

         }
         System.out.println("These are the list of available maintenance companies");
         String validMaintComp = "SELECT companyID FROM MaintenanceCompany";
         System.out.println();
         System.out.print("\tEnter Maint Comp: ");
         esql.executeQueryAndPrintResult(validMaintComp);
         List<List<String>> getMaint = esql.executeQueryAndReturnResult(validMaintComp);
         String maintComp = in.readLine();
         boolean maintCompChecker = true;
         
         while (maintCompChecker){
               for (int i = 0; i < 5; i++ ){
                  if(maintComp.equals(getMaint.get(i).get(0))){
                     maintCompChecker = false;
                  }
               } if(maintCompChecker) {
                  System.out.println();
                  System.out.print("DNE! Enter a Valid maint comp: ");
                  maintComp = in.readLine();
               }

         }


         boolean validDate = true;
         System.out.print(" enter your repair date in format (yyyy-mm-dd): ");
         String date = in.readLine();

         while (validDate){
            
            String formatDate = "\\d{4}-\\d{2}-\\d{2}";

            if(date.matches(formatDate)){
               validDate = false;
               
            }else{
               System.out.println();
               System.out.print("Date was formatted invalidly. Enter date in correct format.");
               System.out.println("\n");
            }
             
         }

         String insertRepair = String.format("INSERT INTO RoomRepairs (companyID, hotelID, roomNumber, repairDate) VALUES ('%s', '%s', '%s', '%s')", maintComp, hotelID, roomNumber, date);

         esql.executeUpdate(insertRepair);

         String repairIdGet = "SELECT repairID FROM RoomRepairs";

         List<List<String>> repairsGotten = esql.executeQueryAndReturnResult(repairIdGet);

         String repairsID =  repairsGotten.get(repairsGotten.size()- 1).get(0);

         String repairIdInsert = String.format("INSERT INTO RoomRepairRequests (managerID, repairID) VALUES ('%s', '%s')", managerID, repairsID);

         esql.executeUpdate(repairIdInsert);
         
   
      } catch(Exception e){
         System.err.println (e.getMessage ());
      }

   }


   public static void viewRoomRepairHistory(Hotel esql) {
      try{

         //get hotel id from previous function
         //System.out.println("These are the list of hotels you manage ");
         String validHotels = "SELECT DISTINCT h.hotelName, h.hotelID ";
                validHotels += "FROM Hotel h, Rooms r ";
                validHotels += "WHERE h.hotelID = r.hotelID AND managerUserID = ";
                validHotels += managerID;
                validHotels += "ORDER BY h.hotelID";
        // System.out.println();
        //esql.executeQueryAndPrintResult(validHotels);

         List<List<String>> getHotels = esql.executeQueryAndReturnResult(validHotels);
        // System.out.print("\tEnter HotelID: ");

        for (int k = 0; k < getHotels.size(); k++) {

         System.out.print(" hotel name: ");
         System.out.print(getHotels.get(k).get(0));
         System.out.print("\n hotel ID: ");
         System.out.print(getHotels.get(k).get(1));
         System.out.println();

        }
    
         System.out.println("list of all room repair requests history");
         String viewRepair = "SELECT * FROM RoomRepairs rr, RoomRepairRequests rq "; 
                viewRepair += "WHERE rr.repairID = rq.repairID AND rq.managerID = ";
                viewRepair+= managerID;

                List<List<String>> retrieve = esql.executeQueryAndReturnResult(viewRepair);

         for (int i =0; i< retrieve.size(); i++){
            //company  id 
            System.out.println("company ID: ");
            System.out.println(retrieve.get(i).get(0));
            System.out.println("\n");

            //hotel id
            System.out.println("hotel ID: ");
            System.out.println(retrieve.get(i).get(1));
            System.out.println("\n");

            //room number
            System.out.println("room number: ");
            System.out.println(retrieve.get(i).get(2));
            System.out.println("\n");

            //repair date
            System.out.println("repair date: ");
            System.out.println(retrieve.get(i).get(3));
            System.out.println("\n");


         }
      }catch(Exception e){
         System.err.println (e.getMessage ());
      } 


   }

}//end Hotel


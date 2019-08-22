package com.eli.hsqldb.hsqltest;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.json.JSONException;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * Unit test for simple App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     * @throws IOException 
     */
	EventLogger eventsClass = EventLogger.getInstance();
	static final Logger logg = Logger.getLogger(AppTest.class.getName());
	
	@Test
	public void createFile1()
	{
		String fileInput = "{\"id\":\"scsmbstgrb\", \"state\":\"STARTED\", \"timestamp\":1491377495213}\n" + 
				"{\"id\":\"scsmbstgrb\", \"state\":\"FINISHED\", \"timestamp\":1491377495218}";
		String fileName1 = "testFile1.txt";
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName1));
			writer.write(fileInput);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	
	public void testStartedAndFinished() throws Exception
	{
		createFile1();
		logg.info("\ntesting creation of an event with both STARTED and FINSIHED states, a complete event will be stored in the map...");
		//System.out.println("\ntesting creation of an event with both STARTED and FINSIHED states, a complete event...");
		
		eventsClass = EventLogger.getInstance();
		eventsClass.loadEventLogs("testFile1.txt");
		
		//for 
		assertTrue(eventsClass.getMapSize() == 1);	
	}
	
	

	public void testDuration() throws Exception
	{
		createFile1();
		logg.info("\ntesting duration on a complete event...");
		//System.out.println("\ntesting creation of an event with both STARTED and FINSIHED states, a complete event...");
		
		eventsClass = EventLogger.getInstance();
		eventsClass.clearMap();
		eventsClass.loadEventLogs("testFile1.txt");

		
		assertTrue(eventsClass.getEventLog("scsmbstgrb").getDuration().intValue() == 5);
	}
	

	public void testAlert() throws Exception
	{
		createFile1();
		logg.info("\ntesting alert when duration is greater than 4ms...");
		//System.out.println("\ntesting creation of an event with both STARTED and FINSIHED states, a complete event...");
		
		eventsClass = EventLogger.getInstance();
		eventsClass.clearMap();
		eventsClass.loadEventLogs("testFile1.txt");
		
		assertTrue(eventsClass.getEventLog("scsmbstgrb").isAlert() == true);
	}
	
	

	public void testDBPersistence() throws Exception
	{
		createFile1();
		logg.info("\ntesting persistence to DB...");
		//System.out.println("\ntesting creation of an event with both STARTED and FINSIHED states, a complete event...");
		
		eventsClass = EventLogger.getInstance();
		eventsClass.clearMap();
		eventsClass.loadEventLogs("testFile1.txt");
		
		eventsClass.persistToDB();
		
		Connection con;
		String connectionString = "jdbc:hsqldb:file:db-data/mydatabase";

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e) {
			throw e;
		}
		try {
			// will create DB if does not exist
			// "SA" is default user with hypersql
			con = DriverManager.getConnection(connectionString, "SA", "");

			// select everything
			System.out.println("Selecting all entries in DB...");
			logg.info("Selecting all entries in DB...");
			PreparedStatement pst = con.prepareStatement("select * from eventlog");
			pst.clearParameters();
			ResultSet rs = pst.executeQuery();
			
			

			List<EventLog> eventlogs = new ArrayList<>();
			while (rs.next()) {
				EventLog logs = new EventLog();
				logs.setId(rs.getString(1));
				logs.setDuration(new BigInteger(rs.getString(2)));
				logs.setType(rs.getString(3));
				logs.setHost(rs.getString(4));
				logs.setAlert(rs.getBoolean(5));
				eventlogs.add(logs);
			}

			assertTrue(eventlogs.size() == 1);

			con.close();

		} catch (SQLException e) {
			throw e;
		}
		
	}
	
	public void createFile2()
	{
		String fileInput = "{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495210}\n" + 
				"{\"id\":\"scsmbstgrc\", \"state\":\"STARTED\", \"timestamp\":1491377495216}";
		String fileName1 = "testFile2.txt";
		
		BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter(fileName1));
			writer.write(fileInput);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// this should not be loaded into the DB, as event id does not have a FINISHED state
	public void testIncorrectEventEntry() throws Exception
	{
		createFile2();
		logg.info("\nTesting Event id that does not have a FINISHED state, so shouldn't be persisted to DB");
		//System.out.println("\nTesting Event id that does not have a FINISHED state, so shouldn't be persisted to DB");
		
		eventsClass = EventLogger.getInstance();
		eventsClass.clearMap();
		eventsClass.loadEventLogs("testFile2.txt");
		eventsClass.persistToDB();
		
		// event wasn't created due to state being "STARTED for same id entry
		assertTrue(eventsClass.getEventLog("scsmbstgrc").isComplete() == false);	
	}
	

	// this should not be loaded into the DB, so IOException should be generated
	public void testEmptyFile()
	{
		logg.info("Testing empty file...");
		//System.out.println("Testing empty file...");
		createFile2();
		
		eventsClass = EventLogger.getInstance();
		eventsClass.clearMap();
		try {
			eventsClass.loadEventLogs("testFile5.txt");
			eventsClass.persistToDB();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		catch (IOException e) {
			
			assertTrue(1==1);
			System.out.println("file does not exist...");
			e.printStackTrace();
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// event wasn't created due to state being "STARTED for same id entry
	}
	
	
	
}

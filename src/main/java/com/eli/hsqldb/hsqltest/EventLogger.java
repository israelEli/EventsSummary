package com.eli.hsqldb.hsqltest;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;


public class EventLogger {
	
	static final Logger logg = Logger.getLogger(EventLogger.class.getName());

	static Connection con;
	static String connectionString = "jdbc:hsqldb:file:db-data/mydatabase";
	private static HashMap<String, EventLog> map = new HashMap<>();
	private static Object mapLock = new Object();
	private static EventLogger logger  = new EventLogger();

	public static EventLogger getInstance()
	{
		if (logger == null)
			logger = new EventLogger();
		return logger;
	}

	// only persist to DB events with complete status - both STARTED and FINISHED states recorded
	public static void persistToDB() throws Exception {
		//String createEvents = readToString("sql/events.sql");
		String dropTable = "drop table eventlog";
		String createEvents = "create table if not exists eventlog (id varchar(15),duration smallint ,type varchar(45) NULL, host varchar(10) NULL, alert boolean default FALSE)";
		logg.info("Attempting to create contacts DB ... ");

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
		} catch (ClassNotFoundException e) {
			throw e;
		}
		try {
			// will create DB if does not exist
			// "SA" is default user with hypersql
			con = DriverManager.getConnection(connectionString, "SA", "");
			con.createStatement().executeUpdate(dropTable);
			// create table
			con.createStatement().executeUpdate(createEvents);

			for (HashMap.Entry<String, EventLog> entry : map.entrySet()) {
				EventLog evLog = entry.getValue();
				if (evLog.isComplete()) {
					String insertStatement = "insert into eventlog values (?,?,?,?,?)";
					PreparedStatement pst = con.prepareStatement(insertStatement);
					pst.setString(1, evLog.getId());
					pst.setInt(2, evLog.getDuration().intValue());
					pst.setString(3, evLog.getType());
					pst.setString(4, evLog.getHost());
					pst.setBoolean(5, evLog.isAlert());

					// pst.clearParameters();
					int updateStatus = pst.executeUpdate();
				}
			}
			

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

			for (EventLog e : eventlogs) {
				//log.info(e.toString());
				System.out.println(e);
			}


		} catch (SQLException e) {
			throw e;
		} finally {
			con.close();
		}
	}

	public static void main(String[] args) throws JSONException, Exception {

		loadEventLogs(args[0]);
		persistToDB();

	}
	
	public static void loadEventLogs(String fileInput) throws JSONException, IOException
	{
		
		FileInputStream inputStream = new FileInputStream(fileInput);
		Scanner sc = new Scanner(inputStream, "UTF-8");
		while (sc.hasNextLine()) {

			JSONObject obj = new JSONObject(sc.nextLine());
			if (map.get(obj.get("id")) == null) {
				EventLog log = new EventLog();

				if (obj.length() == 5) {

					log.setId(obj.getString("id"));
					log.setState(obj.getString("state"));
					log.setDuration(obj.getBigInteger("timestamp"));
					if (obj.getString("type") != null)
						log.setType(obj.getString("type"));
					if (obj.getString("host") != null)
						log.setType(obj.getString("host"));
					log.setHost(obj.getString("host"));
				} 
				else {

					log.setId(obj.getString("id"));
					log.setState(obj.getString("state"));
					log.setDuration(obj.getBigInteger("timestamp"));

				}
				
				// thread-safe hashmap inserts
				synchronized(mapLock) {
					map.put(log.getId(), log);
				}
			}
			// id already exist, so let us find elapsed time
			else if (!(map.get(obj.get("id")).getState().equals(obj.get("state")))) {
				EventLog evLog = map.get(obj.get("id"));
				evLog.setTimestamp(evLog.getDuration(), obj.getBigInteger("timestamp"));
				
				// thread safe hash map operations
				synchronized(mapLock) {
					map.put(evLog.getId(), evLog);
				}
			}
		}

		for (HashMap.Entry<String, EventLog> entry : map.entrySet()) {
			logg.info(entry.getValue()+"");
			//System.out.println(entry.getValue());
		}
	}
	
	
	public EventLog getEventLog(String key)
	{
		return map.get(key);
	}
	
	public int getMapSize()
	{
		return map.size();
	}
	
	public void clearMap()
	{
		map.clear();
	}
	

}

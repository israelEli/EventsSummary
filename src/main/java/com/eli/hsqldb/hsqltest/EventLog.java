package com.eli.hsqldb.hsqltest;

import java.math.BigInteger;
import java.util.logging.Logger;


public class EventLog {

	static Logger log = Logger.getLogger(EventLog.class.getName());
	private String id;
	private String state;
	private String type;
	private String host;
	// test for completeness of both 'STARTED' and 'FINISHED' states
	private boolean complete;
	// stores the diffrence between both timestamp entries
	private BigInteger duration;
	public BigInteger getDuration() {
		return duration;
	}

	public void setDuration(BigInteger duration) {
		this.duration = duration;
	}


	private boolean alert;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public boolean isAlert() {
		return alert;
	}

	public void setAlert(boolean alert) {
		this.alert = alert;
	}

	@Override
	public String toString() {
		return "EventLog [id=" + id + ", state=" + state + ", type=" + type + ", host=" + host + ", duration="
				+ duration + ", alert=" + alert + "]";
	}


	public void setTimestamp(BigInteger timestamp, BigInteger timestamp2) {
		BigInteger diff = timestamp.subtract(timestamp2);
		setComplete(true);
		this.duration = diff.abs();
		if (this.duration.intValue() > 4)
			this.setAlert(true);
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

}

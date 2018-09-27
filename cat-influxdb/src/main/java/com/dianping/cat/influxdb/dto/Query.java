package com.dianping.cat.influxdb.dto;

/**
 * Represents a Query against Influxdb.
 *
 * @author stefan.majer [at] gmail.com
 *
 */
public class Query {

	private final String command;

	private final String database;

	private final String method;

	/**
	 * @param command
	 * @param database
	 */
	public Query(final String command, final String database) {
		super();
		this.command = command;
		this.database = database;
		int index = command.indexOf(" ");

		if (index > -1) {
			this.method = command.substring(0, index);
		} else {
			this.method = command;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Query other = (Query) obj;
		if (command == null) {
			if (other.command != null)
				return false;
		} else if (!command.equals(other.command))
			return false;
		if (database == null) {
			if (other.database != null)
				return false;
		} else if (!database.equals(other.database))
			return false;
		return true;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return this.command;
	}

	/**
	 * @return the database
	 */
	public String getDatabase() {
		return this.database;
	}

	public String getMethod() {
		return this.method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((command == null) ? 0 : command.hashCode());
		result = prime * result + ((database == null) ? 0 : database.hashCode());
		return result;
	}
}

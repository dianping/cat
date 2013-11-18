package com.dianping.cat.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;

/**
 * @goal install
 */
public class CatInstallMojo extends AbstractMojo {

	private final String m_datasourceUrl = "JDBC-URL";

	private final String m_datasourceUser = "JDBC-USER";

	private final String m_datasourcePassword = "JDBC-PASSWORD";

	private String m_path = "/data/appdatas/cat";

	private String m_clientPath = m_path + File.separator + "client.xml";

	private String m_serverPath = m_path + File.separator + "server.xml";

	private String m_datasourcePath = m_path + File.separator + "datasources.xml";

	/**
	 * @parameter expression="${jdbc.url}"
	 */
	private String m_jdbcUrl;

	/**
	 * @parameter expression="${jdbc.user}"
	 */
	private String m_user;

	/**
	 * @parameter expression="${jdbc.password}"
	 */
	private String m_password;

	private void createDatabase(Statement stmt) throws SQLException {
		try {
			stmt.executeUpdate("create database cat");

		} catch (SQLException e) {
			if (e.getErrorCode() == 1007) {
				getLog().info("Database 'cat' already exists, drop it first...");
				stmt.executeUpdate("drop database cat");

				getLog().info("Database 'cat' has dropped.");
				stmt.executeUpdate("create database cat");
			} else {
				throw e;
			}
		}
	}

	private void createTables(Statement stmt) throws IOException, SQLException {
		String sqlTable = Files.forIO().readFrom(getClass().getResourceAsStream("Cat.sql"), "utf-8");
		String[] tables = sqlTable.split(";");

		sqlTable = sqlTable.replace("\n", " ");

		for (String table : tables) {
			if (table != null && table.trim().length() > 0) {
				stmt.execute(table.trim() + ";");
			}
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Delopying the Cat environment...");

		validate();

		setupDatabase();

		setupConfigurationFiles();
	}

	private Connection getConnection(String jdbcUrl) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(jdbcUrl, m_user, m_password);

		return conn;
	}

	private void setupConfigurationFiles() {
		File path = new File(m_path);

		if (!path.exists()) {
			path.mkdirs();
		}

		if (!path.canRead() || !path.canWrite()) {
			getLog()
			      .warn(m_path
			            + " doesn't have enough privilege to read or write this pathname, please add read and write privileges to the current user.");
		}

		getLog().info("Generating the configuration files to " + m_path + "...");

		try {
			Files.forIO().copy(getClass().getResourceAsStream("client.xml"), new FileOutputStream(m_clientPath),
			      AutoClose.INPUT_OUTPUT);
			getLog().info("generate client.xml .");

			Files.forIO().copy(getClass().getResourceAsStream("server.xml"), new FileOutputStream(m_serverPath),
			      AutoClose.INPUT_OUTPUT);
			getLog().info("generate server.xml .");

			String datasources = Files.forIO().readFrom(getClass().getResourceAsStream("datasources.xml"), "utf-8");
			datasources = datasources.replaceAll(m_datasourceUrl, m_jdbcUrl + "/cat");
			datasources = datasources.replaceAll(m_datasourceUser, m_user);
			datasources = datasources.replaceAll(m_datasourcePassword, m_password);

			Files.forIO().writeTo(new File(m_datasourcePath), datasources);
			getLog().info("generate datasources.xml .");
		} catch (Exception e) {
			getLog().error(e);
		}
	}

	private void setupDatabase() {
		Connection conn = null;
		Statement stmt = null;

		try {
			getLog().info("Connect the mysql database : " + m_jdbcUrl);
			conn = getConnection(m_jdbcUrl);
			stmt = conn.createStatement();

			getLog().info("Creating database...");
			createDatabase(stmt);
			getLog().info("Database 'cat' created successfully.");

			getLog().info("Create tables...");
			createTables(stmt);
			getLog().info("Create tables successfully.");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (stmt != null) {
					stmt.close();
				}

				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				// ignore it
			}
		}
	}

	private void validate() {
		Reader inputStream = null;
		BufferedReader reader = null;

		try {
			inputStream = new InputStreamReader(System.in);
			reader = new BufferedReader(inputStream);

			if (m_jdbcUrl == null || m_jdbcUrl.length() == 0) {
				System.out.print("Please input the mysql jdbc url(jdbc:mysql://192.168.1.1:3306):");
				m_jdbcUrl = reader.readLine();
			}

			if (m_user == null || m_user.length() == 0) {
				System.out.print("Please input the mysql user:");
				m_user = reader.readLine();
			}

			if (m_password == null || m_password.length() == 0) {
				System.out.print("Please input the mysql password:");
				m_password = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				// ignore it
			}
		}
	}
}
package com.dianping.cat.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Pattern;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.unidal.helper.Files;
import org.unidal.helper.Files.AutoClose;
import org.unidal.maven.plugin.common.PropertyProviders;
import org.unidal.maven.plugin.common.PropertyProviders.IValidator;

/**
 * @goal install
 * @aggregator true
 */
public class InstallMojo extends AbstractMojo {

	private String m_path = "/data/appdatas/cat";

	private String m_logPath = "/data/applogs/cat";

	private String m_clientPath = m_path + File.separator + "client.xml";

	private String m_serverPath = m_path + File.separator + "server.xml";

	private String m_datasourcePath = m_path + File.separator + "datasources.xml";

	/**
	 * @parameter property="jdbc.url"
	 * @readonly
	 */
	private String m_jdbcUrl;

	/**
	 * @parameter property="jdbc.user" 
	 * @readonly
	 */
	private String m_user;

	/**
	 * @parameter property="jdbc.password"
	 * @readonly
	 */
	private String m_password;

	/**
	 * @parameter property="verbose"
	 * @readonly
	 */
	private boolean m_verbose = false;

	private void createDatabase(Statement stmt) throws SQLException {
		ResultSet result = null;
		try {
			result = stmt.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = 'cat'");
			if (!result.next()) {
				stmt.executeUpdate("create database cat");
			} else {
                getLog().info("Database 'cat' already exists, drop it first...");
                stmt.executeUpdate("drop database cat");
                getLog().info("Database 'cat' has dropped");
                stmt.executeUpdate("create database cat");
            }
		} catch (SQLException e) {
			if (e.getErrorCode() == 1007) {
				getLog().info("Database 'cat' already exists, drop it first...");
				stmt.executeUpdate("drop database cat");

				getLog().info("Database 'cat' has dropped");
				stmt.executeUpdate("create database cat");
			} else {
				throw e;
			}
		} finally {
			result.close();
		}
	}

	private void createTables(Statement stmt) throws IOException, SQLException {
		String sqlTable = Files.forIO().readFrom(getClass().getResourceAsStream("Cat.sql"), "utf-8");
		String[] tables = sqlTable.split(";");

		for (String table : tables) {
			if (table != null && table.trim().length() > 0) {
				stmt.execute(table.trim() + ";");
			}
		}
	}

	private void debug(String info) {
		if (m_verbose) {
			getLog().debug(info);
		}
	}

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		getLog().info("Preparing Cat environment...");

		validate();

		if (setupDatabase() && setupConfigurationFiles()) {
			getLog().info("Preparing Cat environment ... DONE");
			getLog().info("Use following command line to start local Cat server:");
			getLog().info("   cd cat-home; mvn jetty:run");
			getLog().info("Please open http://localhost:2281/cat in your browser");
		}
	}

	private Connection getConnection(String jdbcUrl) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection conn = DriverManager.getConnection(jdbcUrl, m_user, m_password);

		return conn;
	}

	private boolean setupConfigurationFiles() throws MojoFailureException {
		File path = new File(m_path);
		File logPath = new File(m_logPath);

		File temp = null;
		try {
			temp = File.createTempFile("test", "test");
		} catch (IOException e1) {
			getLog()
			      .error(
			            "Don't have privilege to read/write temp dir, please manually promote read/write privileges to this directory.");
			throw new MojoFailureException("Don't have privilege to read/write temp dir");
		}

		if (!path.exists()) {
			boolean result = logPath.mkdirs() && path.mkdirs();

			if (!result || temp.exists()) {
				getLog().error("Don't have privilege to read/write " + m_path + ", please  manually make this directory");
				throw new MojoFailureException("Don't have privilege to read/write " + m_path);
			}
		}

		getLog().info("Generating the configuration files to " + m_path + " ...");

		boolean isSuccess = false;
		try {
			debug("Generating client.xml ...");

			Files.forIO().copy(getClass().getResourceAsStream("client.xml"), new FileOutputStream(m_clientPath),
			      AutoClose.INPUT_OUTPUT);

			debug("Generating server.xml ...");

			Files.forIO().copy(getClass().getResourceAsStream("server.xml"), new FileOutputStream(m_serverPath),
			      AutoClose.INPUT_OUTPUT);

			debug("Generating datasources.xml .");

			String datasources = Files.forIO().readFrom(getClass().getResourceAsStream("datasources.xml"), "utf-8");

			datasources = datasources.replaceAll(Pattern.quote("${jdbc.url}"), m_jdbcUrl + "/cat");
			datasources = datasources.replaceAll(Pattern.quote("${jdbc.user}"), m_user);
			datasources = datasources.replaceAll(Pattern.quote("${jdbc.password}"), m_password);

			Files.forIO().writeTo(new File(m_datasourcePath), datasources);

			getLog().info("Configuration files are generated successfully");

			isSuccess = true;
		} catch (Exception e) {
			getLog().error(e);
		}

		return isSuccess;
	}

	private boolean setupDatabase() throws MojoFailureException {
		Connection conn = null;
		Statement stmt = null;
		boolean isSuccess = false;

		try {
			getLog().info("Connecting to database(" + m_jdbcUrl + ") ...");
			conn = getConnection(m_jdbcUrl);
			getLog().info("Connected to database(" + m_jdbcUrl + ")");

			getLog().info("Creating database(cat) ...");
			stmt = conn.createStatement();
			createDatabase(stmt);
			getLog().info("Database(cat) is created successfully");

			getLog().info("Creating tables ...");
			createTables(stmt);
			getLog().info("Tables are created successfully");

			isSuccess = true;
		} catch (Exception e) {
			getLog().error(e);
			throw new MojoFailureException(e.getMessage());
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

		return isSuccess;
	}

	private void validate() {
		m_jdbcUrl = System.getenv("mysql_jdbcUrl");
		m_user = System.getenv("mysql_username");
		m_password = System.getenv("mysql_password");

		if (m_jdbcUrl != null && m_user != null && m_password != null) {
			// ignore it
		} else {
			m_jdbcUrl = PropertyProviders.fromConsole().forString("jdbc.url", "Please input jdbc url:", null,
			      "jdbc:mysql://127.0.0.1:3306", new IValidator<String>() {
				      @Override
				      public boolean validate(String url) {
					      if (url.startsWith("jdbc:mysql://")) {
						      return true;
					      } else {
						      return false;
					      }
				      }
			      });
			m_user = PropertyProviders.fromConsole().forString("jdbc.user", "Please input username:", null, null, null);
			m_password = PropertyProviders.fromConsole().forString("jdbc.password", "Please input password:", null, "",
			      null);

		}

		getLog().info("jdbc.url: " + m_jdbcUrl);
		getLog().info("jdbc.user: " + m_user);
		getLog().info("jdbc.password: " + m_password);
	}
}
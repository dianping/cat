package com.dianping.bee.engine.spi.handler;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.alibaba.cobar.ErrorCode;
import com.alibaba.cobar.net.handler.FrontendPrivileges;
import com.alibaba.cobar.server.ServerConnection;

public class UseHandler extends AbstractHandler {

	private static final Logger LOGGER = Logger.getLogger(UseHandler.class);

	@Override
	protected void handle(ServerConnection c, List<String> parts) {
		String schema = unescape(parts.get(0));
		LOGGER.info("use : " + schema);
		// 检查schema的有效性
		FrontendPrivileges privileges = c.getPrivileges();

		if (schema == null || !privileges.schemaExists(schema)) {
			error(c, ErrorCode.ER_BAD_DB_ERROR, "Unknown database '%s'", schema);
			return;
		}

		String user = c.getUser();

		if (!privileges.userExists(user, c.getHost())) {
			error(c, ErrorCode.ER_ACCESS_DENIED_ERROR, "Access denied for user '%s'", c.getUser());
			return;
		}

		Set<String> schemas = privileges.getUserSchemas(user);

		if (schemas == null || schemas.size() == 0 || schemas.contains(schema)) {
			CommandContext ctx = new CommandContext(c);

			c.setSchema(schema);
			ctx.writeOk();
			ctx.complete();
		} else {
			error(c, ErrorCode.ER_DBACCESS_DENIED_ERROR, "Access denied for user '%s' to database '%s'", c.getUser(),
			      schema);
		}
	}
}

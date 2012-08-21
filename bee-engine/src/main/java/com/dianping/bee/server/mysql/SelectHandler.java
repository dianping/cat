/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-14
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.server.mysql;

import com.alibaba.cobar.parser.util.ParseUtil;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.server.parser.ServerParseSelect;
import com.alibaba.cobar.server.response.SelectDatabase;
import com.alibaba.cobar.server.response.SelectIdentity;
import com.alibaba.cobar.server.response.SelectLastInsertId;
import com.alibaba.cobar.server.response.SelectUser;
import com.alibaba.cobar.server.response.SelectVersion;
import com.alibaba.cobar.server.response.SelectVersionComment;
import com.dianping.whale.cobar.response.SelectResponse;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class SelectHandler {

	public static void handle(String stmt, ServerConnection c, int offs) {
		int offset = offs;
		switch (ServerParseSelect.parse(stmt, offs)) {
		case ServerParseSelect.VERSION_COMMENT:
			SelectVersionComment.response(c);
			break;
		case ServerParseSelect.DATABASE:
			SelectDatabase.response(c);
			break;
		case ServerParseSelect.USER:
			SelectUser.response(c);
			break;
		case ServerParseSelect.VERSION:
			SelectVersion.response(c);
			break;
		case ServerParseSelect.LAST_INSERT_ID:
			// offset = ParseUtil.move(stmt, 0, "select".length());
			loop: for (; offset < stmt.length(); ++offset) {
				switch (stmt.charAt(offset)) {
				case ' ':
					continue;
				case '/':
				case '#':
					offset = ParseUtil.comment(stmt, offset);
					continue;
				case 'L':
				case 'l':
					break loop;
				}
			}
			offset = ServerParseSelect.indexAfterLastInsertIdFunc(stmt, offset);
			offset = ServerParseSelect.skipAs(stmt, offset);
			SelectLastInsertId.response(c, stmt, offset);
			break;
		case ServerParseSelect.IDENTITY:
			// offset = ParseUtil.move(stmt, 0, "select".length());
			loop: for (; offset < stmt.length(); ++offset) {
				switch (stmt.charAt(offset)) {
				case ' ':
					continue;
				case '/':
				case '#':
					offset = ParseUtil.comment(stmt, offset);
					continue;
				case '@':
					break loop;
				}
			}
			int indexOfAtAt = offset;
			offset += 2;
			offset = ServerParseSelect.indexAfterIdentity(stmt, offset);
			String orgName = stmt.substring(indexOfAtAt, offset);
			offset = ServerParseSelect.skipAs(stmt, offset);
			SelectIdentity.response(c, stmt, offset, orgName);
			break;
		default:
			SelectResponse.response(c, stmt);
		}
	}
}

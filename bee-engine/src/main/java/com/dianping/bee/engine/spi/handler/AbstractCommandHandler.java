package com.dianping.bee.engine.spi.handler;

import java.nio.ByteBuffer;
import java.util.List;

import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.protocol.MySQLPacket;
import com.alibaba.cobar.protocol.mysql.EOFPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.protocol.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.util.StringUtil;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;

public abstract class AbstractCommandHandler extends ContainerHolder implements CommandHandler {
	protected void error(ServerConnection c, int errorCode, String pattern, Object... args) {
		if (args.length == 0) {
			c.writeErrMessage(errorCode, pattern);
		} else {
			c.writeErrMessage(errorCode, String.format(pattern, args));
		}
	}

	protected abstract void handle(ServerConnection c, List<String> parts);

	@Override
	public void handle(String sql, ServerConnection c, int offset) {
		List<String> parts = Splitters.by(' ').noEmptyItem().trim().split(sql.substring(offset + 1));

		handle(c, parts);
	}

	protected String unescape(String str) {
		if (str == null || str.length() < 2) {
			return str;
		}

		int length = str.length();

		if (str.charAt(0) == '`' && str.charAt(length - 1) == '`') {
			return str.substring(1, length - 1);
		} else {
			return str;
		}
	}

	protected ByteBuffer writeHeader(ServerConnection c, ByteBuffer buffer, MySQLPacket packet) {
		return packet.write(buffer, c);
	}

	protected static class CommandContext {
		private ServerConnection m_conn;

		private ByteBuffer m_buffer;

		private String m_charset;

		private byte m_packetId;

		public CommandContext(ServerConnection c) {
			m_conn = c;
			m_buffer = c.allocate();
			m_charset = m_conn.getCharset();
			m_packetId = 1;
		}

		public void complete() {
			m_conn.write(m_buffer);
		}

		public void write(MySQLPacket packet) {
			m_buffer = packet.write(m_buffer, m_conn);
		}

		public void writeEOF() {
			EOFPacket eof = new EOFPacket();

			eof.packetId = m_packetId++;
			write(eof);
		}

		public void writeField(String name, int fieldType) {
			FieldPacket field = PacketUtil.getField(name, fieldType);

			field.packetId = m_packetId++;
			write(field);
		}

		public void writeHeader(int fieldCount) {
			ResultSetHeaderPacket header = PacketUtil.getHeader(fieldCount);

			header.packetId = m_packetId++;
			write(header);
		}

		public void writeOk() {
			m_buffer = m_conn.writeToBuffer(OkPacket.OK, m_buffer);
		}

		public void writeRow(String... values) {
			int cols = values.length;
			RowDataPacket row = new RowDataPacket(cols);

			for (int i = 0; i < cols; i++) {
				row.add(StringUtil.encode(values[i], m_charset));
			}

			row.packetId = m_packetId++;
			write(row);
		}
	}
}

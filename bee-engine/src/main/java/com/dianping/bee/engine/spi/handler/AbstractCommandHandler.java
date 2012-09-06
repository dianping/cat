package com.dianping.bee.engine.spi.handler;

import java.nio.ByteBuffer;
import java.util.List;

import com.alibaba.cobar.Fields;
import com.alibaba.cobar.net.util.PacketUtil;
import com.alibaba.cobar.protocol.MySQLPacket;
import com.alibaba.cobar.protocol.mysql.EOFPacket;
import com.alibaba.cobar.protocol.mysql.FieldPacket;
import com.alibaba.cobar.protocol.mysql.OkPacket;
import com.alibaba.cobar.protocol.mysql.ResultSetHeaderPacket;
import com.alibaba.cobar.protocol.mysql.RowDataPacket;
import com.alibaba.cobar.server.ServerConnection;
import com.alibaba.cobar.util.IntegerUtil;
import com.alibaba.cobar.util.StringUtil;
import com.dianping.bee.engine.spi.handler.internal.BinaryRowDataPacket;
import com.dianping.bee.engine.spi.meta.Cell;
import com.dianping.bee.engine.spi.meta.ColumnMeta;
import com.dianping.bee.engine.spi.meta.Row;
import com.dianping.bee.engine.spi.meta.internal.TypeUtils;
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
			packet.packetId = m_packetId++;
			m_buffer = packet.write(m_buffer, m_conn);
		}

		public void writeEOF() {
			EOFPacket eof = new EOFPacket();

			write(eof);
		}

		public void writeField(String name, int fieldType) {
			FieldPacket field = PacketUtil.getField(name, fieldType);

			write(field);
		}

		public void writeHeader(int fieldCount) {
			ResultSetHeaderPacket header = PacketUtil.getHeader(fieldCount);

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

			write(row);
		}

		public void writeRow(Row row) {
			int len = row.getColumnSize();
			RowDataPacket packet = new RowDataPacket(len);

			for (int cellIndex = 0; cellIndex < len; cellIndex++) {
				Cell cell = row.getCell(cellIndex);
				ColumnMeta column = cell.getMeta();
				String value = cell.getValue() == null ? null : String.valueOf(cell.getValue());

				try {
					switch (TypeUtils.convertJavaTypeToFieldType(column.getType())) {
					case Fields.FIELD_TYPE_STRING:
						packet.add(StringUtil.encode(value, m_charset));
						break;
					case Fields.FIELD_TYPE_INT24:
						packet.add(value == null ? null : IntegerUtil.toBytes(Integer.parseInt(value)));
						break;
					case Fields.FIELD_TYPE_DECIMAL:
					case Fields.FIELD_TYPE_TINY:
					case Fields.FIELD_TYPE_SHORT:
					case Fields.FIELD_TYPE_LONG:
					case Fields.FIELD_TYPE_FLOAT:
					case Fields.FIELD_TYPE_DOUBLE:
					case Fields.FIELD_TYPE_NULL:
					case Fields.FIELD_TYPE_TIMESTAMP:
					case Fields.FIELD_TYPE_LONGLONG:
					case Fields.FIELD_TYPE_DATE:
					case Fields.FIELD_TYPE_TIME:
					case Fields.FIELD_TYPE_DATETIME:
					case Fields.FIELD_TYPE_YEAR:
					case Fields.FIELD_TYPE_NEWDATE:
					case Fields.FIELD_TYPE_VARCHAR:
					case Fields.FIELD_TYPE_BIT:
					case Fields.FIELD_TYPE_NEW_DECIMAL:
					case Fields.FIELD_TYPE_ENUM:
					case Fields.FIELD_TYPE_SET:
					case Fields.FIELD_TYPE_TINY_BLOB:
					case Fields.FIELD_TYPE_MEDIUM_BLOB:
					case Fields.FIELD_TYPE_LONG_BLOB:
					case Fields.FIELD_TYPE_BLOB:
					case Fields.FIELD_TYPE_VAR_STRING:
					case Fields.FIELD_TYPE_GEOMETRY:
					default:
						packet.add(StringUtil.encode(value, m_charset));
						break;
					}
				} catch (Exception e) {
					throw new RuntimeException(String.format("Error when writing row for column(%s) with value(%s)!",
					      column.getName(), value), e);
				}
			}

			write(packet);
		}

		/**
		 * @param row
		 */
		public void writeBinaryRow(Row row) {
			int len = row.getColumnSize();
			BinaryRowDataPacket packet = new BinaryRowDataPacket(len);

			for (int cellIndex = 0; cellIndex < len; cellIndex++) {
				Cell cell = row.getCell(cellIndex);
				ColumnMeta column = cell.getMeta();
				String value = cell.getValue() == null ? null : String.valueOf(cell.getValue());

				try {
					switch (TypeUtils.convertJavaTypeToFieldType(column.getType())) {
					case Fields.FIELD_TYPE_STRING:
						packet.add(StringUtil.encode(value, m_charset));
						break;
					case Fields.FIELD_TYPE_INT24:
						packet.add(value == null ? null : convertIntToBytes(Integer.parseInt(value)));
						break;
					case Fields.FIELD_TYPE_DECIMAL:
					case Fields.FIELD_TYPE_TINY:
					case Fields.FIELD_TYPE_SHORT:
					case Fields.FIELD_TYPE_LONG:
					case Fields.FIELD_TYPE_FLOAT:
					case Fields.FIELD_TYPE_DOUBLE:
					case Fields.FIELD_TYPE_NULL:
					case Fields.FIELD_TYPE_TIMESTAMP:
					case Fields.FIELD_TYPE_LONGLONG:
					case Fields.FIELD_TYPE_DATE:
					case Fields.FIELD_TYPE_TIME:
					case Fields.FIELD_TYPE_DATETIME:
					case Fields.FIELD_TYPE_YEAR:
					case Fields.FIELD_TYPE_NEWDATE:
					case Fields.FIELD_TYPE_VARCHAR:
					case Fields.FIELD_TYPE_BIT:
					case Fields.FIELD_TYPE_NEW_DECIMAL:
					case Fields.FIELD_TYPE_ENUM:
					case Fields.FIELD_TYPE_SET:
					case Fields.FIELD_TYPE_TINY_BLOB:
					case Fields.FIELD_TYPE_MEDIUM_BLOB:
					case Fields.FIELD_TYPE_LONG_BLOB:
					case Fields.FIELD_TYPE_BLOB:
					case Fields.FIELD_TYPE_VAR_STRING:
					case Fields.FIELD_TYPE_GEOMETRY:
					default:
						packet.add(StringUtil.encode(value, m_charset));
						break;
					}
				} catch (Exception e) {
					throw new RuntimeException(String.format("Error when writing row for column(%s) with value(%s)!",
					      column.getName(), value), e);
				}
			}

			write(packet);
		}
	}

	private static byte[] convertIntToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) (i & 0xff);
		result[1] = (byte) (i >>> 8);
		result[2] = (byte) (i >>> 16);
		result[3] = (byte) (i >>> 24);
		return result;
	}
}

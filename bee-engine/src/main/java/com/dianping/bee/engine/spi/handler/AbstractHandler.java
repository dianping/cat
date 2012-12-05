package com.dianping.bee.engine.spi.handler;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.alibaba.cobar.Fields;
import com.alibaba.cobar.net.FrontendConnection;
import com.alibaba.cobar.net.util.BufferUtil;
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
import com.dianping.bee.engine.Cell;
import com.dianping.bee.engine.Row;
import com.dianping.bee.engine.helper.TypeUtils;
import com.dianping.bee.engine.spi.ColumnMeta;
import com.site.helper.Splitters;
import com.site.lookup.ContainerHolder;

public abstract class AbstractHandler extends ContainerHolder implements Handler {

	private static final Logger LOGGER = Logger.getLogger(AbstractHandler.class);

	private static byte[] convertIntToBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte) (i & 0xff);
		result[1] = (byte) (i >>> 8);
		result[2] = (byte) (i >>> 16);
		result[3] = (byte) (i >>> 24);
		return result;
	}

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
		LOGGER.info("handle : " + sql);
		List<String> parts = Splitters.by(' ').noEmptyItem().trim().split(sql.substring(offset + 1));

		handle(c, parts);
	}

	static class BinaryRowDataPacket extends MySQLPacket {
		private final byte header = 0;

		private final int fieldCount;

		private final List<byte[]> fieldValues;

		private final byte[] fieldsLength;

		private byte[] bitMap;

		public BinaryRowDataPacket(int fieldCount) {
			this.fieldCount = fieldCount;
			this.fieldValues = new ArrayList<byte[]>(fieldCount);
			this.fieldsLength = new byte[fieldCount];
			this.bitMap = new byte[(fieldCount + 7 + 2) / 8];
		}

		public void add(byte[] value, byte fieldLength) {
			fieldValues.add(value);
			this.fieldsLength[fieldValues.size() - 1] = fieldLength;
		}

		@Override
		public int calcPacketSize() {
			int size = 1 + bitMap.length;
			for (int i = 0; i < fieldCount; i++) {
				if (fieldsLength[i] > 0) {
					size += fieldsLength[i];
				} else {
					size += fieldValues.get(i).length + 1;
				}
			}
			return size;
		}

		@Override
		protected String getPacketInfo() {
			return "MySQL BinaryRowData Packet";
		}

		@Override
		public ByteBuffer write(ByteBuffer bb, FrontendConnection c) {
			bb = c.checkWriteBuffer(bb, c.getPacketHeaderSize());
			BufferUtil.writeUB3(bb, calcPacketSize());
			bb.put(packetId);
			bb.put(header);
			bb.put(bitMap);
			for (int i = 0; i < fieldCount; i++) {
				byte[] fv = fieldValues.get(i);
				bb = c.checkWriteBuffer(bb, BufferUtil.getLength(fv.length));
				if (fieldsLength[i] < 0) {
					BufferUtil.writeLength(bb, fieldValues.get(i).length);
				}
				bb = c.writeToBuffer(fv, bb);
			}

			return bb;
		}
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
						byte[] encodeStr = StringUtil.encode(value, m_charset);
						packet.add(encodeStr, (byte) -1);
						break;
					case Fields.FIELD_TYPE_INT24:
						packet.add(value == null ? null : convertIntToBytes(Integer.parseInt(value)), (byte) 4);
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
						encodeStr = StringUtil.encode(value, m_charset);
						packet.add(encodeStr, (byte) -1);
						break;
					}
				} catch (Exception e) {
					throw new RuntimeException(String.format("Error when writing row for column(%s) with value(%s)!", column.getName(),
					      value), e);
				}
			}

			write(packet);
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
					throw new RuntimeException(String.format("Error when writing row for column(%s) with value(%s)!", column.getName(),
					      value), e);
				}
			}

			write(packet);
		}

		public void writeRow(String... values) {
			int cols = values.length;
			RowDataPacket row = new RowDataPacket(cols);

			for (int i = 0; i < cols; i++) {
				row.add(StringUtil.encode(values[i], m_charset));
			}

			write(row);
		}
	}

}

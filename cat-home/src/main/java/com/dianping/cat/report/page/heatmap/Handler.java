package com.dianping.cat.report.page.heatmap;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;

import com.dianping.cat.job.sql.dal.LocationRecord;
import com.dianping.cat.job.sql.dal.LocationRecordDao;
import com.dianping.cat.job.sql.dal.LocationRecordEntity;
import com.dianping.cat.report.ReportPage;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;
import com.site.web.mvc.PageHandler;
import com.site.web.mvc.annotation.InboundActionMeta;
import com.site.web.mvc.annotation.OutboundActionMeta;
import com.site.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;

	@Inject
	private LocationRecordDao m_dao;

	private Result aggregate(List<LocationRecord> records, Payload payload) {
		int unit = payload.getUnit();
		int width = payload.getWidth();
		int height = payload.getHeight();
		double lat1 = payload.getLat1();
		double lat2 = payload.getLat2();
		double lng1 = payload.getLng1();
		double lng2 = payload.getLng2();
		int yc = (int) Math.ceil(height * 1.0 / unit);
		int xc = (int) Math.ceil(width * 1.0 / unit);
		double latUnit = Math.abs(lat1 - lat2) / yc;
		double lngUnit = Math.abs(lng1 - lng2) / xc;
		int[][] matrix = new int[yc][xc];

		if (xc > 0 && yc > 0) {
			for (LocationRecord record : records) {
				int latIndex = (int) Math.floor((record.getLat() - lat1) / latUnit);
				int lngIndex = (int) Math.floor((record.getLng() - lng1) / lngUnit);

				matrix[latIndex][lngIndex] += record.getTotal();
			}
		}

		return new Result(matrix, lat1, lng1, latUnit, lngUnit);
	}

	private void handleAjax(Model model, Payload payload) throws DalException {
		double lat1 = payload.getLat1();
		double lat2 = payload.getLat2();
		double lng1 = payload.getLng1();
		double lng2 = payload.getLng2();
		Date start = model.getReport().getStartTime();
		Date end = model.getReport().getEndTime();

		List<LocationRecord> locations = m_dao.findAllByTransactionDateLatLngRange(start, end, lat1, lat2, lng1, lng2,
		      LocationRecordEntity.READSET_LAT_LNG_TOTAL);
		Result result = aggregate(locations, payload);

		model.setLocationData(result.toString());
		model.setMax(result.getMax());
		model.setCb(payload.getCb());
	}

	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "heatmap")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "heatmap")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();
		Action action = payload.getAction();

		// Last hour is default
		if (payload.getPeriod().isCurrent()) {
			payload.setStep(payload.getStep() - 1);
		}

		model.setDisplay(payload.getDisplay());
		model.setAction(action);
		model.setPage(ReportPage.HEATMAP);

		if (payload.getPeriod().isFuture()) {
			model.setLongDate(payload.getCurrentDate());
		} else {
			model.setLongDate(payload.getDate());
		}

		HeatMapReport report = new HeatMapReport(new Date(payload.getDate()), payload.getFlag());

		model.setReport(report);

		switch (action) {
		case JSONP:
			try {
				handleAjax(model, payload);
			} catch (DalException e) {
				e.printStackTrace();
			}
			break;
		}

		m_jspViewer.view(ctx, model);
	}

	static class Location {
		private double m_lat;

		private double m_lng;

		private int m_count;

		public Location(double lat, double lng) {
			m_lat = lat;
			m_lng = lng;
		}

		public void addCount(int count) {
			m_count += count;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof Location))
				return false;
			Location other = (Location) obj;
			if (Double.doubleToLongBits(m_lat) != Double.doubleToLongBits(other.m_lat))
				return false;
			if (Double.doubleToLongBits(m_lng) != Double.doubleToLongBits(other.m_lng))
				return false;

			return true;
		}

		public int getCount() {
			return m_count;
		}

		public double getLat() {
			return m_lat;
		}

		public double getLng() {
			return m_lng;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			long temp;

			temp = Double.doubleToLongBits(m_lat);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(m_lng);
			result = prime * result + (int) (temp ^ (temp >>> 32));

			return result;
		}

		@Override
		public String toString() {
			return "[" + m_lat + "," + m_lng + "," + m_count + "]";
		}
	}

	static class Result {
		private int[][] m_matrix;

		private double m_latUnit;

		private double m_lngUnit;

		private double m_lat1;

		private double m_lng1;

		public Result(int[][] matrix, double lat1, double lng1, double latUnit, double lngUnit) {
			m_lat1 = lat1;
			m_lng1 = lng1;
			m_latUnit = latUnit;
			m_lngUnit = lngUnit;
			if (matrix == null) {
				m_matrix = new int[0][0];
			} else {
				m_matrix = Arrays.copyOf(matrix, matrix.length);
			}
		}

		public int getMax() {
			int max = 0;

			for (int i = 0; i < m_matrix.length; i++) {
				int[] row = m_matrix[i];

				for (int j = 0; j < row.length; j++) {
					int count = row[j];

					if (count > max) {
						max = count;
					}
				}
			}

			return max;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(8192);
			boolean first = true;
			DecimalFormat format = new DecimalFormat("0.00000");

			sb.append('[');

			for (int i = 0; i < m_matrix.length; i++) {
				int[] row = m_matrix[i];

				for (int j = 0; j < row.length; j++) {
					int count = row[j];

					if (count <= 0) {
						continue;
					}

					// point to center of the block
					double lat = m_lat1 + m_latUnit * (i + 0.5);
					double lng = m_lng1 + m_lngUnit * (j + 0.5);

					if (first) {
						first = false;
					} else {
						sb.append(',');
					}

					sb.append('[').append(format.format(lat));
					sb.append(',').append(format.format(lng));
					sb.append(',').append(count);
					sb.append(']');
				}
			}

			sb.append(']');

			return sb.toString();
		}
	}
}

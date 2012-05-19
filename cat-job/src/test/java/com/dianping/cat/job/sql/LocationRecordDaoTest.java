package com.dianping.cat.job.sql;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.job.sql.dal.LocationRecord;
import com.dianping.cat.job.sql.dal.LocationRecordDao;
import com.dianping.cat.job.sql.dal.LocationRecordEntity;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LocationRecordDaoTest extends ComponentTestCase {
	@Test
	public void testFind() throws Exception {
		LocationRecordDao dao = lookup(LocationRecordDao.class);

		Calendar cal = Calendar.getInstance();

		cal.set(2012, 04, 10, 0, 0, 0);

		Date transactionDate = cal.getTime();

		List<LocationRecord> locations = dao.findAllByTransactionDate(transactionDate, 31.183556726728153,
		      31.286595237657263, 121.37105049658203, 121.57223763037109, LocationRecordEntity.READSET_LAT_LNG_TOTAL);

		System.out.println(locations);
	}

	@Test
	public void testInsert() throws Exception {
		LocationRecordDao dao = lookup(LocationRecordDao.class);

		Calendar cal = Calendar.getInstance();

		cal.set(2012, 04, 17, 13, 14, 38);

		Date transactionDate = cal.getTime();
		// LocationRecord[creation-date: null, from-lat: 0.0, from-lng: 0.0, id:
		// 0, key-id: 0, lat: 81.08641, lng: -103.79883, to-lat: 0.0, to-lng: 0.0,
		// total: 1, transaction-date: Thu May 17 13:14:38 CST 2012], message:
		// java.sql.BatchUpdateException: Data truncation: Out of range value for
		// column 'lat' at row 1
		LocationRecord r = dao.createLocal();
		
		r.setLat(81.08641);
		r.setLng(-103.79883);
		r.setTotal(1);
		r.setTransactionDate(transactionDate);
		
		dao.insert(r);
	}
}

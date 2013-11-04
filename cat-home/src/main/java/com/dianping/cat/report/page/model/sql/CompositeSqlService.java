package com.dianping.cat.report.page.model.sql;

import java.util.List;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.SqlReportMerger;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CompositeSqlService extends BaseCompositeModelService<SqlReport> {
	public CompositeSqlService() {
		super(SqlAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<SqlReport> createRemoteService() {
		return new RemoteSqlService();
	}

	@Override
	protected SqlReport merge(ModelRequest request, List<ModelResponse<SqlReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(request.getDomain()));
		String datebase = request.getProperty("database");
		if (Constants.ALL.equals(datebase)) {
			merger.setAllDatabase(true);
		}
		for (ModelResponse<SqlReport> response : responses) {
			SqlReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getSqlReport();
	}
}

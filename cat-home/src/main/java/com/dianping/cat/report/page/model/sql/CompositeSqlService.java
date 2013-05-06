package com.dianping.cat.report.page.model.sql;

import java.util.List;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeSqlService extends BaseCompositeModelService<SqlReport> {
	public CompositeSqlService() {
		super("sql");
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
		if (CatString.ALL.equals(datebase)) {
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

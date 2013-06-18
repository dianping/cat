package com.dianping.cat.report.page.model.database;

import java.util.List;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeDatabaseService extends BaseCompositeModelService<DatabaseReport> {
	public CompositeDatabaseService() {
		super("database");
	}

	@Override
	protected BaseRemoteModelService<DatabaseReport> createRemoteService() {
		return new RemoteDatabaseService();
	}

	@Override
	protected DatabaseReport merge(ModelRequest request, List<ModelResponse<DatabaseReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(request.getProperty("database")));
		String domain = request.getDomain();

		if (domain.equals(CatString.ALL)) {
			merger.setAllDomain(true);
		}

		for (ModelResponse<DatabaseReport> response : responses) {
			DatabaseReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getDatabaseReport();
	}
}

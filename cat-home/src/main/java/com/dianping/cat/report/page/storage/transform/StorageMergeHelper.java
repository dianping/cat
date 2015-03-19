package com.dianping.cat.report.page.storage.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public class StorageMergeHelper {

	public StorageReport mergeAllDomains(StorageReport storageReport, String domain) {
		if (Constants.ALL.equals(domain)) {
			AllDomainMerger merger = new AllDomainMerger();

			merger.visitStorageReport(storageReport);
			storageReport = merger.getStorageReport();
		}
		return storageReport;
	}

	public StorageReport mergeAllMachines(StorageReport storageReport, String ipAddress) {
		if (Constants.ALL.equals(ipAddress)) {
			AllMachineMerger merger = new AllMachineMerger();

			merger.visitStorageReport(storageReport);
			storageReport = merger.getStorageReport();
		}
		return storageReport;
	}

	public StorageReport mergeReport(StorageReport report, String ipAddress, String domain) {
		StorageReport temp = mergeAllMachines(report, ipAddress);

		return mergeAllDomains(temp, domain);
	}
}

package com.dianping.cat.message.consumer.model.failure;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.transform.DefaultJsonBuilder;
import com.site.helper.Files;

public class FailureReportStore {

	private static final SimpleDateFormat SDF = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm");

	private static final String END = "\n";

	public static void storeToHtml(File file, FailureReport report) {
		try {
			Files.forIO().writeTo(file, getStoreString(report));
		} catch (IOException e) {
			throw new RuntimeException(String.format(
					"Unable to create file %s!", file), e);
		}
	}

	public static String getStoreString(FailureReport report) {
		StringBuilder result = new StringBuilder();
		DefaultJsonBuilder jsonBuilder = new DefaultJsonBuilder();

		jsonBuilder.visitFailureReport(report);

		String jsonString = jsonBuilder.getString();

		result.append("<html>").append(END).append("<head>").append(END)
				.append("<title>").append(END).append("Failure Report ")
				.append("From ").append(SDF.format(report.getStartTime()))
				.append(" To ").append(SDF.format(report.getEndTime()))
				.append(END).append("</title>").append(END).append("<body>")
				.append(END).append(jsonString).append("</body>").append(END)
				.append("</html>").append(END);
		return result.toString();
	}
}

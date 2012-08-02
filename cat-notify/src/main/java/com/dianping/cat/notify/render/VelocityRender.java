package com.dianping.cat.notify.render;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.dianping.cat.notify.config.ConfigContext;

public class VelocityRender implements IRender {

	private VelocityEngine engine = null;

	private ConfigContext configContext;

	@Override
	public boolean init() {
		engine = new VelocityEngine();
		Properties properties = new Properties();
		String templatePath = configContext.getProperty("velocity.template.path");
		templatePath = templatePath == null ? "/" : templatePath;
		String path = templatePath;
		try {
			File file = new File(templatePath).getCanonicalFile();
			path = file.getAbsolutePath();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		properties.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, path);
		try {
			engine.init(properties);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public String fetchAll(String template, Map<String, Object> params) {
		// get the final render string,from template
		StringWriter writer = new StringWriter();
		VelocityContext context = new VelocityContext();
		if (null != params && params.size() > 0) {
			for (Entry<String, Object> entry : params.entrySet()) {
				context.put(entry.getKey(), entry.getValue());
			}
		}
		try {
			Template t = engine.getTemplate(template);
			t.merge(context, writer);
		} catch (ResourceNotFoundException e) {
			return e.toString();
		} catch (ParseErrorException e) {
			return e.toString();
		} catch (MethodInvocationException e) {
			return e.toString();
		} catch (Exception e) {
			return e.toString();
		}
		return writer.toString();
	}

	public void setConfigContext(ConfigContext configContext) {
		this.configContext = configContext;
	}
}

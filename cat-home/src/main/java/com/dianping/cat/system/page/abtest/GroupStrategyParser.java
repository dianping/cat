package com.dianping.cat.system.page.abtest;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.InputStream;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.GsonBuilder;

public class GroupStrategyParser implements Initializable {
	private GsonBuilder m_gsonBuilder;

	public GsonBuilder getGsonBuilder() {
		return m_gsonBuilder;
	}

	@Override
	public void initialize() throws InitializationException {
		m_gsonBuilder = new GsonBuilder();
		m_gsonBuilder.setFieldNamingStrategy(new NonPrexFieldNamingStrategy());
	}

	public GroupstrategyDescriptor parse(InputStream input) throws ParseException {
		final CompilationUnit result = JavaParser.parse(input);

		TypeDeclaration type = (TypeDeclaration) result.getTypes().get(0);

		final GroupstrategyDescriptor descriptor = new GroupstrategyDescriptor();
		String name = type.getName();

		descriptor.setClassName(name);
		descriptor.setFullyQualifiedName(result.getPackage().getName() + "." + name);

		new AnnotationVisitor(descriptor).visit(result, null);
		return descriptor;
	}

	public void setGsonBuilder(GsonBuilder gsonBuilder) {
		m_gsonBuilder = gsonBuilder;
	}

	class AnnotationVisitor extends VoidVisitorAdapter<Object> {

		public GroupstrategyDescriptor m_descriptor;

		public AnnotationVisitor(GroupstrategyDescriptor descriptor) {
			m_descriptor = descriptor;
		}

		public void visit(FieldDeclaration node, Object arg) {
			List<AnnotationExpr> annotations = node.getAnnotations();

			if (annotations != null) {
				for (AnnotationExpr expr : annotations) {
					String annotation = expr.toString();

					int index = annotation.indexOf("@Inject");

					if (index >= 0) {
						int begin = annotation.indexOf('"');
						int end = annotation.lastIndexOf('"');

						String name = annotation.substring(begin + 1, end).trim();
						String type = node.getType().toString();

						Field field = new Field();
						field.setName(name);
						field.setType(type);

						m_descriptor.getFields().add(field);
					}
				}
			}
		}
	}

	public static class NonPrexFieldNamingStrategy implements FieldNamingStrategy {
		@Override
		public String translateName(java.lang.reflect.Field f) {
			String name = f.getName();
			int pos = name.indexOf('_');

			return name.substring(pos + 1);
		}
	}
}

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

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;

public class GroupStrategyParser {

	public static GroupstrategyDescriptor parse(InputStream input) throws ParseException {
		final GroupstrategyDescriptor descriptor = new GroupstrategyDescriptor();
		final CompilationUnit result = JavaParser.parse(input);

		TypeDeclaration type = (TypeDeclaration) result.getTypes().get(0);

		String name = type.getName();
		descriptor.setName(name);
		descriptor.setFullyQualifiedName(result.getPackage().getName() + "." + name);

		new AnnotationVisitor(descriptor).visit(result, null);
		return descriptor;
	}

	static class AnnotationVisitor extends VoidVisitorAdapter<Object> {

		public GroupstrategyDescriptor m_descriptor;

		public AnnotationVisitor(GroupstrategyDescriptor descriptor) {
			m_descriptor = descriptor;
		}

		public void visit(FieldDeclaration node, Object arg) {
			List<AnnotationExpr> annotations = node.getAnnotations();

			for (AnnotationExpr expr : annotations) {
				String annotation = expr.toString();

				int index = annotation.indexOf("@Inject");
				if (index >= 0) {
					int begin = annotation.indexOf('"');
					int end = annotation.lastIndexOf('"');

					Field field = new Field();
					String name = annotation.substring(begin + 1, end).trim();
					String type = node.getType().toString();

					field.setName(name);
					field.setType(type);

					m_descriptor.getFields().add(field);
				}
			}
		}
	}

	public static String toJson(GroupstrategyDescriptor descriptor) {
		StringBuilder sb = new StringBuilder(200);

		sb.append("{");
		sb.append("\"name\":\"").append(descriptor.getName()).append("\"").append(",");
		sb.append("\"fullyQualifiedName\":\"").append(descriptor.getFullyQualifiedName()).append("\",");
		sb.append("\"fields\":");
		sb.append("[");

		boolean isFirst = true;
		for (Field field : descriptor.getFields()) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(",");
			}

			sb.append("{");
			sb.append("\"field-name\":\"").append(field.getName()).append("\"").append(",");
			sb.append("\"field-type\":\"").append(field.getType()).append("\"").append(",");
			sb.append("\"field-value\":\"").append(field.getValue()).append("\"").append(",");
			sb.append("\"field-input-type\":\"").append(field.getInputType()).append("\"");
			sb.append("}");
		}

		sb.append("]");
		sb.append("}");
		return sb.toString();
	}
	
	public static GroupstrategyDescriptor toObject(String json){
		
		//TODO
		return null;
	}
}

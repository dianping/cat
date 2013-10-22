package com.dianping.cat.system.page.abtest;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AnnotationExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.InputStream;
import java.util.List;

import com.dianping.cat.abtest.model.entity.Field;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;

public class GroupStrategyParser {
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

	class AnnotationVisitor extends VoidVisitorAdapter<Object> {
		private GroupstrategyDescriptor m_descriptor;

		private boolean m_isImportInjectClass = false;

		public AnnotationVisitor(GroupstrategyDescriptor descriptor) {
			m_descriptor = descriptor;
		}

		public void visit(FieldDeclaration node, Object arg) {
			if (m_isImportInjectClass) {
				List<AnnotationExpr> annotations = node.getAnnotations();

				if (annotations != null) {
					for (AnnotationExpr expr : annotations) {
						String annotation = expr.toString();

						int pos = annotation.indexOf("@Inject");

						if (pos >= 0) {
							int begin = annotation.indexOf('"');
							int end = annotation.lastIndexOf('"');

							String name = annotation.substring(begin + 1, end).trim();
							String type = node.getType().toString();
							List<VariableDeclarator> modifierName = node.getVariables();
							VariableDeclarator firstVar = modifierName.get(0);

							Field field = new Field();
							field.setName(name);
							field.setType(type);
							field.setModifierName(firstVar.getId().getName());

							if (firstVar.getInit() != null) {
								field.setValue(firstVar.getInit().toString());
							}

							m_descriptor.getFields().add(field);
						}
					}
				}
			}
		}

		public void visit(ImportDeclaration n, Object arg) {
			if (n.getName().toString().equals("org.unidal.lookup.annotation.Inject")) {
				m_isImportInjectClass = true;
			}
		}
	}
}

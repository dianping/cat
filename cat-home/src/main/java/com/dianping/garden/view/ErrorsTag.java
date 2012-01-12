package com.dianping.garden.view;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyContent;

import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xml.sax.InputSource;

import com.site.dal.xml.XmlAdapter;
import com.site.dal.xml.XmlException;
import com.site.dal.xml.annotation.XmlAttribute;
import com.site.dal.xml.annotation.XmlElement;
import com.site.dal.xml.annotation.XmlElements;
import com.site.dal.xml.annotation.XmlRootElement;
import com.site.dal.xml.annotation.XmlValue;
import com.site.dal.xml.registry.XmlRegistry;
import com.site.lookup.ContainerLoader;
import com.site.web.jsp.tag.AbstractBodyTag;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ErrorObject;

public class ErrorsTag extends AbstractBodyTag {
   private static final long serialVersionUID = 1L;

   private XmlAdapter m_xmlAdapter;

   private PageContext m_pageContext;

   public ErrorsTag() {
      PlexusContainer container = ContainerLoader.getDefaultContainer();

      try {
         XmlRegistry registry = (XmlRegistry) container.lookup(XmlRegistry.class);

         registry.register(Root.class);
         m_xmlAdapter = (XmlAdapter) container.lookup(XmlAdapter.class);
      } catch (ComponentLookupException e) {
         throw new RuntimeException("Initializating error.", e);
      }
   }

   @Override
   protected void handleBody() throws JspException {
      ActionContext<?> ctx = (ActionContext<?>) m_pageContext.getAttribute("ctx", PageContext.REQUEST_SCOPE);

      if (ctx != null) {
         List<ErrorObject> errors = ctx.getErrors();

         if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder(1024);
            Root root = parseBody();

            sb.append("<br/><ul>\r\n");

            if (root.getTitle() != null) {
               sb.append("<b>").append(root.getTitle()).append("</b>\r\n");
            }

            for (ErrorObject eo : errors) {
               String id = eo.getId();
               Error error = root.getError(id);

               sb.append("<li>");

               if (error != null) {
                  sb.append("<label style=\"color:red;\">").append(error.getField()).append(": </label>");
                  sb.append(error.getDescription());
               } else {
                  sb.append(id);

                  if (eo.getException() != null) {
                     sb.append(": ").append(eo.getException().getMessage());
                  }
               }

               sb.append("</li>\r\n");
            }

            sb.append("</ul>\r\n");

            try {
               write(sb.toString());
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }

   private Root parseBody() throws JspException {
      BodyContent bodyContent = getBodyContent();
      String content = bodyContent == null ? "<root/>" : "<root>" + bodyContent.getString() + "</root>";

      try {
         InputSource source = new InputSource(new StringReader(content));
         Root root = m_xmlAdapter.unmarshal(source);

         return root;
      } catch (XmlException e) {
         throw new JspException("Invalid body content of <a:Error/>: " + bodyContent, e);
      }
   }

   @Override
   public void setPageContext(PageContext pageContext) {
      m_pageContext = pageContext;

      super.setPageContext(pageContext);
   }

   @XmlElement(name = "error")
   public static final class Error {
      @XmlAttribute(name = "id")
      private String m_id;

      @XmlAttribute(name = "field")
      private String m_field;

      @XmlValue
      private String m_description;

      public String getDescription() {
         return m_description;
      }

      public String getField() {
         return m_field;
      }

      public String getId() {
         return m_id;
      }

      public void setDescription(String description) {
         m_description = description;
      }

      public void setField(String field) {
         m_field = field;
      }

      public void setId(String id) {
         m_id = id;
      }

      @Override
      public String toString() {
         return "Error[id=" + m_id + ", field=" + m_field + ", description=" + m_description + "]";
      }
   }

   @XmlRootElement(name = "root")
   public static final class Root {
      @XmlElements(@XmlElement(name = "error", type = Error.class))
      private List<Error> m_errors;

      @XmlElement(name = "title")
      private String m_title;

      public List<Error> getErrors() {
         return m_errors;
      }

      public Error getError(String id) {
         if (m_errors != null) {
            for (Error error : m_errors) {
               if (id.equals(error.getId())) {
                  return error;
               }
            }
         }

         return null;
      }

      public String getTitle() {
         return m_title;
      }

      public void setErrors(List<Error> errors) {
         m_errors = errors;
      }

      public void setTitle(String title) {
         m_title = title;
      }
   }
}

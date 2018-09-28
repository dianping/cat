package com.dianping.cat.status.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.dianping.cat.status.model.BaseEntity;
import com.dianping.cat.status.model.IVisitor;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.dianping.cat.status.model.Constants.ATTR_ID;
import static com.dianping.cat.status.model.Constants.ENTITY_EXTENSION;

@Data
@EqualsAndHashCode(callSuper = true)
public class Extension extends BaseEntity<Extension> {
    private String id;
    private String description;
    private final Map<String, ExtensionDetail> details = new LinkedHashMap<String, ExtensionDetail>();
    private Map<String, String> dynamicAttributes = new LinkedHashMap<String, String>();

    public Extension() {
    }

    public Extension(String id) {
        this.id = id;
    }

    @Override
    public void accept(IVisitor visitor) {
        visitor.visitExtension(this);
    }

    public Extension addExtensionDetail(ExtensionDetail extensionDetail) {
        details.put(extensionDetail.getId(), extensionDetail);
        return this;
    }

    public ExtensionDetail findExtensionDetail(String id) {
        return details.get(id);
    }

    public ExtensionDetail findOrCreateExtensionDetail(String id) {
        ExtensionDetail extensionDetail = details.get(id);

        if (extensionDetail == null) {
            synchronized (details) {
                extensionDetail = details.get(id);

                if (extensionDetail == null) {
                    extensionDetail = new ExtensionDetail(id);
                    details.put(id, extensionDetail);
                }
            }
        }

        return extensionDetail;
    }

    @Override
    public void mergeAttributes(Extension other) {
        assertAttributeEquals(other, ENTITY_EXTENSION, ATTR_ID, id, other.getId());

        for (Map.Entry<String, String> e : other.getDynamicAttributes().entrySet()) {
            dynamicAttributes.put(e.getKey(), e.getValue());
        }

    }

    public boolean removeExtensionDetail(String id) {
        if (details.containsKey(id)) {
            details.remove(id);
            return true;
        }

        return false;
    }

    public void setDynamicAttribute(String name, String value) {
        dynamicAttributes.put(name, value);
    }

    public Extension setDescription(String description) {
        this.description = description;
        return this;
    }

    public Extension setId(String id) {
        this.id = id;
        return this;
    }

}

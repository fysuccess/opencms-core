/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.contenteditor.shared;

import com.alkacon.acacia.shared.AttributeConfiguration;
import com.alkacon.acacia.shared.ContentDefinition;
import com.alkacon.acacia.shared.Entity;
import com.alkacon.acacia.shared.TabInfo;
import com.alkacon.vie.shared.I_Entity;
import com.alkacon.vie.shared.I_EntityAttribute;
import com.alkacon.vie.shared.I_Type;

import org.opencms.gwt.shared.CmsModelResourceInfo;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Contains all information needed for editing an XMLContent.<p>
 */
public class CmsContentDefinition extends ContentDefinition {

    /** The entity id prefix. */
    private static final String ENTITY_ID_PREFIX = "http://opencms.org/resources/";

    /** The value of the acacia-unlock configuration option. */
    private boolean m_autoUnlock;

    /** The available locales. */
    private Map<String, String> m_availableLocales;

    /** A map from attribute names to complex widget configurations. */
    private Map<String, CmsComplexWidgetData> m_complexWidgetData;

    /** The content locales. */
    private List<String> m_contentLocales;

    /** Flag indicating the resource needs to removed on cancel. */
    private boolean m_deleteOnCancel;

    /** The external widget configurations. */
    private List<CmsExternalWidgetConfiguration> m_externalWidgetConfigurations;

    /** The direct edit flag (set to true for classic direct edit mode). */
    private boolean m_isDirectEdit;

    /** The model file informations. */
    private List<CmsModelResourceInfo> m_modelInfos;

    /** The new link. */
    private String m_newLink;

    /** Flag indicating the current content has an invalid XML structure and was auto corrected. */
    private boolean m_performedAutocorrection;

    /** The reference resource structure id. */
    private CmsUUID m_referenceResourceId;

    /** The resource type name. */
    private String m_resourceType;

    /** The site path. */
    private String m_sitePath;

    /** The content title. */
    private String m_title;

    /**
     * Constructor.<p>
     * 
     * @param entity the entity
     * @param configurations the attribute configurations
     * @param externalWidgetConfigurations the external widget configurations
     * @param complexWidgetData the complex widget configurations 
     * @param types the types
     * @param tabInfos the tab information
     * @param locale the content locale
     * @param contentLocales the content locales
     * @param availableLocales the available locales
     * @param title the content title
     * @param sitePath the site path
     * @param resourceType the resource type name
     * @param performedAutocorrection flag indicating the current content has an invalid XML structure and was auto corrected
     * @param autoUnlock false if the editor should not unlock resources automatically in standalone mode 
     */
    public CmsContentDefinition(
        Entity entity,
        Map<String, AttributeConfiguration> configurations,
        Collection<CmsExternalWidgetConfiguration> externalWidgetConfigurations,
        Map<String, CmsComplexWidgetData> complexWidgetData,
        Map<String, I_Type> types,
        List<TabInfo> tabInfos,
        String locale,
        List<String> contentLocales,
        Map<String, String> availableLocales,
        String title,
        String sitePath,
        String resourceType,
        boolean performedAutocorrection,
        boolean autoUnlock) {

        super(entity, configurations, types, tabInfos, locale);
        m_contentLocales = contentLocales;
        m_availableLocales = availableLocales;
        m_complexWidgetData = complexWidgetData;
        m_title = title;
        m_sitePath = sitePath;
        m_resourceType = resourceType;
        m_externalWidgetConfigurations = new ArrayList<CmsExternalWidgetConfiguration>(externalWidgetConfigurations);
        m_performedAutocorrection = performedAutocorrection;
        m_autoUnlock = autoUnlock;
    }

    /**
     * Constructor for model file informations object.<p>
     * 
     * @param modelInfos the model file informations
     * @param newLink the new link
     * @param referenceId the reference resource structure id
     * @param locale the locale
     */
    public CmsContentDefinition(
        List<CmsModelResourceInfo> modelInfos,
        String newLink,
        CmsUUID referenceId,
        String locale) {

        super(null, null, null, null, locale);
        m_modelInfos = modelInfos;
        m_newLink = newLink;
        m_referenceResourceId = referenceId;
    }

    /**
     * Constructor for serialization only.<p>
     */
    protected CmsContentDefinition() {

        super();
    }

    /**
     * Returns the UUID according to the given entity id.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the entity id
     */
    public static CmsUUID entityIdToUuid(String entityId) {

        if (entityId.startsWith(ENTITY_ID_PREFIX)) {
            entityId = entityId.substring(entityId.lastIndexOf("/") + 1);
        }
        return new CmsUUID(entityId);
    }

    /**
     * Extracts the locale from the entity id.<p>
     * 
     * @param entityId the entity id
     * 
     * @return the locale
     */
    public static String getLocaleFromId(String entityId) {

        if (entityId.startsWith(ENTITY_ID_PREFIX)) {
            return entityId.substring(ENTITY_ID_PREFIX.length(), entityId.lastIndexOf("/"));
        }
        return null;
    }

    /**
     * Returns the value for the given XPath expression.<p>
     *  
     * @param entity the entity
     * @param path the path
     * 
     * @return the value
     */
    public static String getValueForPath(I_Entity entity, String path) {

        String result = null;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String attributeName;
        if (path.contains("/")) {
            attributeName = path.substring(0, path.indexOf("/"));
            path = path.substring(path.indexOf("/"));
        } else {
            attributeName = path;
            path = null;
        }
        int index = ContentDefinition.extractIndex(attributeName);
        if (index > 0) {
            index--;
        }
        attributeName = entity.getTypeName() + "/" + ContentDefinition.removeIndex(attributeName);
        I_EntityAttribute attribute = entity.getAttribute(attributeName);
        if (!((attribute == null) || (attribute.isComplexValue() && (path == null)))) {
            if (attribute.isSimpleValue()) {
                if ((path == null) && (attribute.getValueCount() > 0)) {
                    List<String> values = attribute.getSimpleValues();
                    result = values.get(index);
                }
            } else if (attribute.getValueCount() > (index + 1)) {
                List<I_Entity> values = attribute.getComplexValues();
                result = getValueForPath(values.get(index), path);
            }
        }
        return result;
    }

    /**
     * Returns the entity id according to the given UUID.<p>
     * 
     * @param uuid the UUID
     * @param locale the content locale
     * 
     * @return the entity id
     */
    public static String uuidToEntityId(CmsUUID uuid, String locale) {

        return ENTITY_ID_PREFIX + locale + "/" + uuid.toString();
    }

    /**
     * Returns the available locales.<p>
     *
     * @return the available locales
     */
    public Map<String, String> getAvailableLocales() {

        return m_availableLocales;
    }

    /** 
     * Gets the complex widget configurations.<p>
     * 
     * @return the complex widget configurations 
     */
    public Map<String, CmsComplexWidgetData> getComplexWidgetData() {

        return m_complexWidgetData;
    }

    /**
     * Returns the content locales.<p>
     *
     * @return the content locales
     */
    public List<String> getContentLocales() {

        return m_contentLocales;
    }

    /**
     * Returns the external widget configurations.<p>
     *
     * @return the external widget configurations
     */
    public List<CmsExternalWidgetConfiguration> getExternalWidgetConfigurations() {

        return m_externalWidgetConfigurations;
    }

    /**
     * Returns the model file informations.<p>
     * 
     * @return the model file informations
     */
    public List<CmsModelResourceInfo> getModelInfos() {

        return m_modelInfos;
    }

    /**
     * Returns the new link.<p>
     *
     * @return the new link
     */
    public String getNewLink() {

        return m_newLink;
    }

    /**
     * Returns the reference resource structure id.<p>
     * 
     * @return the reference resource structure id
     */
    public CmsUUID getReferenceResourceId() {

        return m_referenceResourceId;
    }

    /**
     * Returns the resource type.<p>
     *
     * @return the resource type
     */
    public String getResourceType() {

        return m_resourceType;
    }

    /**
     * Returns the site path.<p>
     *
     * @return the site path
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns the title.<p>
     *
     * @return the title
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the value of the acacia-unlock configuration option.<p>
     * 
     * @return the value of the acacia-unlock configuration option 
     */
    public boolean isAutoUnlock() {

        return m_autoUnlock;
    }

    /**
     * Returns if the resource needs to removed on cancel.<p>
     *
     * @return <code>true</code> if the resource needs to removed on cancel
     */
    public boolean isDeleteOnCancel() {

        return m_deleteOnCancel;
    }

    /**
     * Returns true if the direct edit flag is set, which means that the editor was opened from the classic direct edit mode.<p>
     * 
     * @return true if the direct edit flag is set
     */
    public boolean isDirectEdit() {

        return m_isDirectEdit;
    }

    /**
     * Returns if the model file informations are present, in this case no additional data is contained.<p>
     * 
     * @return <code>true</code> if the definition contains the model file informations
     */
    public boolean isModelInfo() {

        return m_modelInfos != null;
    }

    /**
     * Returns if auto correction was performed.<p>
     *
     * @return <code>true</code> if auto correction was performed
     */
    public boolean isPerformedAutocorrection() {

        return m_performedAutocorrection;
    }

    /**
     * Sets if the resource needs to removed on cancel.<p>
     *
     * @param deleteOnCancel <code>true</code> if the resource needs to removed on cancel
     */
    public void setDeleteOnCancel(boolean deleteOnCancel) {

        m_deleteOnCancel = deleteOnCancel;
    }

    /** 
     * Sets the value of the direct edit flag.<p>
     * 
     * @param isDirectEdit the new value for the direct edit flag
     */
    public void setDirectEdit(boolean isDirectEdit) {

        m_isDirectEdit = isDirectEdit;
    }

}

/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/workplace/editors/ade/Attic/CmsADEServer.java,v $
 * Date   : $Date: 2009/10/21 12:11:32 $
 * Version: $Revision: 1.1.2.35 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors.ade;

import org.opencms.db.CmsUserSettings;
import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.flex.CmsFlexController;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.json.JSONArray;
import org.opencms.json.JSONException;
import org.opencms.json.JSONObject;
import org.opencms.jsp.CmsJspActionElement;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.search.CmsSearch;
import org.opencms.search.CmsSearchParameters;
import org.opencms.search.CmsSearchResult;
import org.opencms.util.CmsRequestUtil;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.CmsWorkplaceMessages;
import org.opencms.workplace.explorer.CmsResourceUtil;
import org.opencms.xml.CmsXmlUtils;
import org.opencms.xml.containerpage.CmsADEManager;
import org.opencms.xml.containerpage.CmsContainerBean;
import org.opencms.xml.containerpage.CmsContainerElementBean;
import org.opencms.xml.containerpage.CmsContainerPageBean;
import org.opencms.xml.containerpage.CmsSearchOptions;
import org.opencms.xml.containerpage.CmsXmlContainerPage;
import org.opencms.xml.containerpage.CmsXmlContainerPageFactory;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;

/**
 * ADE server used for client/server communication.<p>
 * 
 * see jsp files under <tt>/system/workplace/editors/ade/</tt>.<p>
 * 
 * @author Michael Moossen 
 * 
 * @version $Revision: 1.1.2.35 $
 * 
 * @since 7.6
 */
public class CmsADEServer extends CmsJspActionElement {

    /** Request parameter action value constant. */
    public static final String ACTION_ALL = "all";

    /** Request parameter action value constant. */
    public static final String ACTION_CNT = "cnt";

    /** Request parameter action value constant. */
    public static final String ACTION_DEL = "del";

    /** Request parameter action value constant. */
    public static final String ACTION_ELEM = "elem";

    /** Request parameter action value constant. */
    public static final String ACTION_ELEM_PROPS = "elemProps";

    /** Request parameter action value constant. */
    public static final String ACTION_FAV = "fav";

    /** Request parameter action value constant. */
    public static final String ACTION_LS = "ls";

    /** Request parameter action value constant. */
    public static final String ACTION_NEW = "new";

    /** Request parameter action value constant. */
    public static final String ACTION_PROPS = "props";

    /** Request parameter action value constant. */
    public static final String ACTION_PUB_LIST = "publish-list";

    /** Request parameter action value constant. */
    public static final String ACTION_PUBLISH = "publish";

    /** Request parameter action value constant. */
    public static final String ACTION_REC = "rec";

    /** Request parameter action value constant. */
    public static final String ACTION_SEARCH = "search";

    /** JSON response state value constant. */
    public static final String CONTAINER_TYPE = "Container";

    /** State Constant for client-side element type 'new element configuration'. */
    public static final String ELEMENT_NEWCONFIG = "NC";

    /** JSON response state value constant. */
    public static final String ELEMENT_TYPE = "Element";

    /** Additional info key constant. */
    public static final String INFO_PUBLISH_LIST = "__INFO_PUBLISH_LIST__";

    /** Mime type constant. */
    public static final String MIMETYPE_APPLICATION_JSON = "application/json";

    /** JSON property constant allowEdit. */
    public static final String P_CNTPAGE_ALLOWEDIT = "allowEdit";

    /** JSON property constant locale. */
    public static final String P_CNTPAGE_LOCALE = "locale";

    /** JSON property constant locaked. */
    public static final String P_CNTPAGE_LOCKED = "locked";

    /** JSON property constant name. */
    public static final String P_CNTPAGE_NAME = "name";

    /** JSON property constant type. */
    public static final String P_CNTPAGE_TYPE = "type";

    /** JSON property constant containers. */
    public static final String P_CONTAINERS = "containers";

    /** JSON property constant elements. */
    public static final String P_ELEMENTS = "elements";

    /** JSON property constant favorites. */
    public static final String P_FAVORITES = "favorites";

    /** JSON property constant formatter. */
    public static final String P_FORMATTER = "formatter";

    /** JSON property constant icon. */
    public static final String P_ICON = "icon";

    /** JSON property constant id. */
    public static final String P_ID = "id";

    /** JSON property constant infoName. */
    public static final String P_INFO_NAME = "infoName";

    /** JSON property constant infoValue. */
    public static final String P_INFO_VALUE = "infoValue";

    /** JSON property constant lockedBy. */
    public static final String P_LOCKED_BY = "lockedBy";

    /** JSON property constant maxElem. */
    public static final String P_MAXELEMENTS = "maxElem";

    /** JSON property constant newOrder. */
    public static final String P_NEWORDER = "newOrder";

    /** JSON property constant objtype. */
    public static final String P_OBJTYPE = "objtype";

    /** JSON property constant reason. */
    public static final String P_REASON = "reason";

    /** JSON property constant recent. */
    public static final String P_RECENT = "recent";

    /** JSON property constant recentListSize. */
    public static final String P_RECENT_LIST_SIZE = "recentListSize";

    /** JSON property constant recent. */
    public static final String P_RESOURCES = "resources";

    /** JSON property constant count. */
    public static final String P_SEARCH_COUNT = "count";

    /** JSON property constant hasmore. */
    public static final String P_SEARCH_HASMORE = "hasmore";

    /** JSON property constant searchOrder. */
    public static final String P_SEARCHORDER = "searchOrder";

    /** JSON property constant state. */
    public static final String P_STATE = "state";

    /** JSON property constant subItems. */
    public static final String P_SUBITEMS = "subItems";

    /** JSON property constant title. */
    public static final String P_TITLE = "title";

    /** JSON property constant uri. */
    public static final String P_URI = "uri";

    /** Request parameter name constant. */
    public static final String PARAMETER_ACTION = "action";

    /** Request parameter name constant. */
    public static final String PARAMETER_CNTPAGE = "cntpage";

    /** Request parameter name constant. */
    public static final String PARAMETER_DATA = "data";

    /** Request parameter name constant. */
    public static final String PARAMETER_ELEM = "elem";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCALE = "locale";

    /** Request parameter name constant. */
    public static final String PARAMETER_LOCATION = "location";

    /** Request parameter name constant. */
    public static final String PARAMETER_PAGE = "page";

    /** Request parameter name constant. */
    public static final String PARAMETER_PROPERTIES = "properties";

    /** Request parameter name constant. */
    public static final String PARAMETER_RELATED = "related";

    /** Request parameter name constant. */
    public static final String PARAMETER_REMOVE_RESOURCES = "resources";

    /** Request parameter name constant. */
    public static final String PARAMETER_RESOURCES = "resources";

    /** Request parameter name constant. */
    public static final String PARAMETER_SIBLINGS = "siblings";

    /** Request parameter name constant. */
    public static final String PARAMETER_TEXT = "text";

    /** Request parameter action value constant. */
    public static final String PARAMETER_TYPE = "type";

    /** Request parameter name constant. */
    public static final String PARAMETER_URI = "uri";

    /** Reason value constant. */
    public static final String REASON_PERMISSIONS = "perm";

    /** Reason value constant. */
    public static final String REASON_PUBLISHED = "pub";

    /** Request path constant. */
    public static final String REQUEST_GET = "/system/workplace/editors/ade/get.jsp";

    /** Request path constant. */
    public static final String REQUEST_SET = "/system/workplace/editors/ade/set.jsp";

    /** JSON response property constant. */
    public static final String RES_ERROR = "error";

    /** JSON response property constant. */
    public static final String RES_STATE = "state";

    /** JSON response state value constant. */
    public static final String RES_STATE_ERROR = "error";

    /** JSON response state value constant. */
    public static final String RES_STATE_OK = "ok";

    /** Session attribute name constant. */
    public static final String SESSION_ATTR_ADE_CACHE = "__OCMS_ADE_CACHE__";

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsADEServer.class);

    /** The ADE manager instance. */
    private CmsADEManager m_manager;

    /** The session cache. */
    private CmsADESessionCache m_sessionCache;

    /**
     * Constructor.<p>
     * 
     * @param context the JSP page context object
     * @param req the JSP request 
     * @param res the JSP response 
     */
    public CmsADEServer(PageContext context, HttpServletRequest req, HttpServletResponse res) {

        super(context, req, res);
        m_manager = OpenCms.getADEManager();
        m_sessionCache = (CmsADESessionCache)req.getSession().getAttribute(SESSION_ATTR_ADE_CACHE);
        if (m_sessionCache == null) {
            m_sessionCache = new CmsADESessionCache(getCmsObject());
            req.getSession().setAttribute(SESSION_ATTR_ADE_CACHE, m_sessionCache);
        }
    }

    /**
     * Main method that handles all requests.<p>
     * 
     * @throws IOException if there is any problem while writing the result to the response 
     * @throws JSONException if there is any problem with JSON
     */
    public void serve() throws JSONException, IOException {

        // set the mime type to application/json
        CmsFlexController controller = CmsFlexController.getController(getRequest());
        controller.getTopResponse().setContentType(MIMETYPE_APPLICATION_JSON);

        JSONObject result = new JSONObject();
        try {
            // handle request depending on the requested jsp
            String action = getRequest().getPathInfo();
            if (action.equals(REQUEST_GET)) {
                result = executeActionGet();
            } else if (action.equals(REQUEST_SET)) {
                result = executeActionSet();
            } else {
                result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_INVALID_ACTION_URL_1, action));
            }
        } catch (Exception e) {
            // a serious error occurred, should not...
            result.put(RES_ERROR, e.getLocalizedMessage() == null ? "NPE" : e.getLocalizedMessage());
            LOG.error(Messages.get().getBundle().key(
                Messages.ERR_SERVER_EXCEPTION_1,
                CmsRequestUtil.appendParameters(
                    getRequest().getRequestURL().toString(),
                    CmsRequestUtil.createParameterMap(getRequest().getQueryString()),
                    false)), e);
        }
        // add state info
        if (result.has(RES_ERROR)) {
            // add state=error in case an error occurred 
            result.put(RES_STATE, RES_STATE_ERROR);
        } else if (!result.has(RES_STATE)) {
            // add state=ok i case no error occurred
            result.put(RES_STATE, RES_STATE_OK);
        }
        // write the result
        result.write(getResponse().getWriter());
    }

    /**
     * Returns a list of container elements from a json array with client ids.<p>
     * 
     * @param array the json array
     * 
     * @return a list of resource ids
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException 
     */
    protected List<CmsContainerElementBean> arrayToElementList(JSONArray array) throws JSONException, CmsException {

        List<CmsContainerElementBean> result = new ArrayList<CmsContainerElementBean>(array.length());
        for (int i = 0; i < array.length(); i++) {
            String id = array.getString(i);
            try {
                result.add(getCachedElement(id));
            } catch (CmsIllegalArgumentException e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        }
        return result;
    }

    /**
     * Checks whether a list of parameters are present as attributes of a request.<p>
     * 
     * If this isn't the case, an error message is written to the JSON result object.
     * 
     * @param request the request which contains the parameters
     * @param result the JSON object which the error message should be written into, can be <code>null</code>
     * @param params the array of parameter names which should be checked
     * 
     * @return true if and only if all parameters are present in the request
     * 
     * @throws JSONException if something goes wrong with JSON
     */
    protected boolean checkParameters(HttpServletRequest request, JSONObject result, String... params)
    throws JSONException {

        for (String param : params) {
            String value = request.getParameter(param);
            if (value == null) {
                if (result != null) {
                    storeErrorMissingParam(result, param);
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Compares two search option objects.<p>
     * 
     * Better than to implement the {@link CmsSearchOptions#equals(Object)} method,
     * since the page number is not considered in this comparison.<p>
     * 
     * @param o1 the first search option object
     * @param o2 the first search option object
     * 
     * @return <code>true</code> if they are equal
     */
    protected boolean compareSearchOptions(CmsSearchOptions o1, CmsSearchOptions o2) {

        if (o1 == o2) {
            return true;
        }
        if ((o1 == null) || (o2 == null)) {
            return false;
        }
        if (!o1.getLocation().equals(o2.getLocation())) {
            return false;
        }
        if (!o1.getText().equals(o2.getText())) {
            return false;
        }
        if (!o1.getType().equals(o2.getType())) {
            return false;
        }
        return true;

    }

    /**
     * Creates a new CmsContainerElementBean for a structure-id and given properties.<p> 
     * 
     * @param structureId the structure-id
     * @param properties the properties-map
     * 
     * @return the element bean
     * 
     * @throws CmsException if something goes wrong reading the element resource
     * @throws JSONException if something goes wrong parsing JSON
     */
    protected CmsContainerElementBean createElement(CmsUUID structureId, JSONObject properties)
    throws CmsException, JSONException {

        Map<String, String> cnfProps = new HashMap<String, String>();
        if (properties != null) {
            Iterator<String> itProperties = properties.keys();
            while (itProperties.hasNext()) {
                String propertyName = itProperties.next();
                cnfProps.put(propertyName, properties.getString(propertyName));
            }
        }
        return new CmsContainerElementBean(getCmsObject().readResource(structureId), null, cnfProps);
    }

    /**
     * Deletes the given elements from server.<p>
     * 
     * @param elems the array of client-side element ids
     * 
     * @throws CmsException if something goes wrong 
     */
    protected void deleteElements(JSONArray elems) throws CmsException {

        CmsObject cms = getCmsObject();
        for (int i = 0; i < elems.length(); i++) {
            CmsResource res = cms.readResource(m_manager.convertToServerId(elems.optString(i)));
            String path = cms.getSitePath(res);
            cms.lockResource(path);
            cms.deleteResource(path, CmsResource.DELETE_PRESERVE_SIBLINGS);
            cms.unlockResource(path);
        }
    }

    /**
     * Handles all ADE get requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionGet() throws CmsException, JSONException {

        JSONObject result = new JSONObject();

        HttpServletRequest request = getRequest();

        if (!checkParameters(request, result, PARAMETER_ACTION)) {
            return result;
        }
        String actionParam = request.getParameter(PARAMETER_ACTION);
        if (actionParam.equals(ACTION_PUB_LIST)) {
            if (checkParameters(request, null, PARAMETER_REMOVE_RESOURCES)) {
                // remove the resources from the user's publish list
                String remResParam = request.getParameter(PARAMETER_REMOVE_RESOURCES);
                JSONArray resourcesToRemove = new JSONArray(remResParam);
                removeResourcesFromPublishList(resourcesToRemove);
            }
            if (checkParameters(request, null, PARAMETER_RELATED, PARAMETER_SIBLINGS)) {
                // get list of resources to publish
                String relatedParam = request.getParameter(PARAMETER_RELATED);
                String siblingsParam = request.getParameter(PARAMETER_SIBLINGS);
                boolean related = Boolean.parseBoolean(relatedParam);
                boolean siblings = Boolean.parseBoolean(siblingsParam);
                JSONArray resourcesToPublish = getPublishList(related, siblings);
                result.put(P_RESOURCES, resourcesToPublish);
            } else {
                // get list of resources that can not be published
                boolean related = true;
                boolean siblings = false;

                JSONArray resources = getResourcesToPublishWithProblems(related, siblings);
                if (resources.length() == 0) {
                    // if no problems just get the list of resources to publish
                    resources = getPublishList(related, siblings);
                }
                result.put(P_RESOURCES, resources);
            }
            return result;
        } else if (actionParam.equals(ACTION_PUBLISH)) {
            if (!checkParameters(request, result, PARAMETER_RESOURCES)) {
                return result;
            }
            // resources to publish
            String resourcesParam = request.getParameter(PARAMETER_RESOURCES);
            JSONArray resourcesToPublish = new JSONArray(resourcesParam);
            // get the resources with link check problems
            JSONArray resources = getResourcesWithLinkCheck(resourcesToPublish);
            if (resources.length() == 0) {
                // publish resources
                publishResources(resourcesToPublish);
            } else {
                // return resources with problems
                result.put(P_RESOURCES, resources);
            }
            return result;
        }
        if (!checkParameters(request, result, PARAMETER_CNTPAGE, PARAMETER_LOCALE, PARAMETER_URI)) {
            return result;
        }
        String cntPageParam = request.getParameter(PARAMETER_CNTPAGE);
        String localeParam = request.getParameter(PARAMETER_LOCALE);
        String uriParam = request.getParameter(PARAMETER_URI);

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        CmsResource cntPageRes = cms.readResource(cntPageParam);
        CmsXmlContainerPage xmlCntPage = CmsXmlContainerPageFactory.unmarshal(cms, cntPageRes, request);
        CmsContainerPageBean cntPage = xmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());

        if (actionParam.equals(ACTION_ALL)) {
            // first load, get everything
            result = getContainerPage(cntPageRes, cntPage, uriParam.equals(cntPageParam) ? null : uriParam);
        } else if (actionParam.equals(ACTION_ELEM)) {
            // get element data
            String elemParam = request.getParameter(PARAMETER_ELEM);
            if (elemParam == null) {
                storeErrorMissingParam(result, PARAMETER_ELEM);
                return result;
            }
            CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
            JSONObject resElements = new JSONObject();
            if (elemParam.startsWith("[")) {
                // element list
                JSONArray elems = new JSONArray(elemParam);
                for (int i = 0; i < elems.length(); i++) {
                    String elemId = elems.getString(i);
                    try {
                        CmsContainerElementBean element = getCachedElement(elemId);
                        resElements.put(element.getClientId(), elemUtil.getElementData(element, cntPage.getTypes()));
                    } catch (Exception e) {
                        // ignore any problems
                        if (!LOG.isDebugEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            } else {
                // single element
                try {
                    CmsContainerElementBean element = getCachedElement(elemParam);
                    resElements.put(element.getClientId(), elemUtil.getElementData(element, cntPage.getTypes()));
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            }
            result.put(P_ELEMENTS, resElements);
        } else if (actionParam.equals(ACTION_ELEM_PROPS)) {

            String elemParam = request.getParameter(PARAMETER_ELEM);
            String propertiesParam = request.getParameter(PARAMETER_PROPERTIES);
            if (elemParam == null) {
                storeErrorMissingParam(result, PARAMETER_ELEM);
                return result;
            }
            if (propertiesParam == null) {
                storeErrorMissingParam(result, PARAMETER_PROPERTIES);
                return result;
            }
            try {
                CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
                JSONObject resElements = new JSONObject();
                JSONObject properties = new JSONObject(propertiesParam);
                CmsContainerElementBean element = createElement(m_manager.convertToServerId(elemParam), properties);
                String clientId = element.getClientId();
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                resElements.put(clientId, elemUtil.getElementData(element, cntPage.getTypes()));
                result.put(P_ELEMENTS, resElements);
            } catch (Exception e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } else if (actionParam.equals(ACTION_FAV)) {
            // get the favorite list
            result.put(P_FAVORITES, getFavoriteList(null, cntPage.getTypes()));
        } else if (actionParam.equals(ACTION_REC)) {
            // get recent list
            result.put(P_RECENT, getRecentList(null, cntPage.getTypes()));
        } else if (actionParam.equals(ACTION_SEARCH)) {
            // new search
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getSearchResult(cntPageParam, searchOptions, cntPage.getTypes());
            result.merge(searchResult, true, false);
        } else if (actionParam.equals(ACTION_LS)) {
            // last search
            CmsSearchOptions searchOptions = new CmsSearchOptions(request);
            JSONObject searchResult = getLastSearchResult(cntPageParam, searchOptions, cntPage.getTypes());

            // we need those on the client side to make scrolling work
            CmsSearchOptions oldOptions = m_sessionCache.getADESearchOptions();
            if (oldOptions != null) {
                result.put(PARAMETER_TYPE, oldOptions.getTypes());
                result.put(PARAMETER_TEXT, oldOptions.getText());
                result.put(PARAMETER_LOCATION, oldOptions.getLocation());
            }
            result.merge(searchResult, true, false);
        } else if (actionParam.equals(ACTION_NEW)) {
            // get a new element
            if (!checkParameters(request, result, PARAMETER_DATA)) {
                return result;
            }
            String dataParam = request.getParameter(PARAMETER_DATA);
            String type = dataParam;

            CmsResource newResource = m_manager.createNewElement(
                cms,
                request.getParameter(PARAMETER_CNTPAGE),
                request,
                type);
            result.put(CmsElementUtil.P_ELEMENT_ID, m_manager.convertToClientId(newResource.getStructureId()));
            result.put(P_URI, cms.getSitePath(newResource));
        } else if (actionParam.equals(ACTION_PROPS)) {
            // get property dialog information
            // get element data
            String elemParam = request.getParameter(PARAMETER_ELEM);
            if (elemParam == null) {
                storeErrorMissingParam(result, PARAMETER_ELEM);
                return result;
            }
            try {
                CmsElementUtil elemUtil = new CmsElementUtil(cms, uriParam, request, getResponse());
                CmsContainerElementBean element = getCachedElement(elemParam);
                result = elemUtil.getElementPropertyInfo(element);
            } catch (Exception e) {
                if (!LOG.isDebugEnabled()) {
                    LOG.warn(e.getLocalizedMessage());
                }
                LOG.debug(e.getLocalizedMessage(), e);
            }
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_ACTION,
                actionParam));
        }
        return result;
    }

    /**
     * Handles all ADE set requests.<p>
     * 
     * @return the result
     * 
     * @throws JSONException if there is any problem with JSON
     * @throws CmsException if there is a problem with the cms context
     */
    protected JSONObject executeActionSet() throws JSONException, CmsException {

        JSONObject result = new JSONObject();
        HttpServletRequest request = getRequest();
        if (!checkParameters(request, result, PARAMETER_ACTION, PARAMETER_LOCALE)) {
            return result;
        }
        String actionParam = request.getParameter(PARAMETER_ACTION);
        String localeParam = request.getParameter(PARAMETER_LOCALE);

        CmsObject cms = getCmsObject();
        cms.getRequestContext().setLocale(CmsLocaleManager.getLocale(localeParam));
        if (actionParam.equals(ACTION_FAV)) {
            // save the favorite list
            String dataParam = request.getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                storeErrorMissingParam(result, PARAMETER_DATA);
                return result;
            }
            JSONArray list = new JSONArray(dataParam);

            m_manager.saveFavoriteList(cms, arrayToElementList(list));
        } else if (actionParam.equals(ACTION_REC)) {
            // save the recent list
            String dataParam = request.getParameter(PARAMETER_DATA);
            if (dataParam == null) {
                storeErrorMissingParam(result, PARAMETER_DATA);
                return result;
            }
            JSONArray list = new JSONArray(dataParam);
            m_sessionCache.setCacheADERecentList(arrayToElementList(list));
        } else if (actionParam.equals(ACTION_CNT)) {
            // save the container page
            if (!checkParameters(request, result, PARAMETER_CNTPAGE, PARAMETER_DATA)) {
                return result;
            }
            String cntPageParam = request.getParameter(PARAMETER_CNTPAGE);
            String dataParam = request.getParameter(PARAMETER_DATA);
            JSONObject cntPage = new JSONObject(dataParam);
            setContainerPage(cntPageParam, cntPage);
        } else if (actionParam.equals(ACTION_DEL)) {
            // delete elements
            if (!checkParameters(request, result, PARAMETER_DATA)) {
                return result;
            }
            String dataParam = request.getParameter(PARAMETER_DATA);
            JSONArray elems = new JSONArray(dataParam);
            deleteElements(elems);
        } else {
            result.put(RES_ERROR, Messages.get().getBundle().key(
                Messages.ERR_JSON_WRONG_PARAMETER_VALUE_2,
                PARAMETER_ACTION,
                actionParam));
        }
        return result;
    }

    /**
     * Reads the cached element-bean for the given client-side-id from cache.<p>
     * 
     * @param clientId the client-side-id
     * @return the CmsContainerElementBean
     * @throws CmsException - if the resource could not be read for any reason
     */
    protected CmsContainerElementBean getCachedElement(String clientId) throws CmsException {

        String id = clientId;
        CmsContainerElementBean element = null;
        element = m_sessionCache.getCacheContainerElement(id);
        if (element != null) {
            return element;
        }
        if (id.contains("#")) {
            id = id.substring(0, id.indexOf("#"));
            element = m_sessionCache.getCacheContainerElement(id);
            if (element != null) {
                return element;
            }
        }
        // this is necessary if the element has not been cached yet
        element = new CmsContainerElementBean(getCmsObject().readResource(m_manager.convertToServerId(id)), null, null);
        m_sessionCache.setCacheContainerElement(id, element);
        return element;
    }

    /**
     * Returns the data for the given container page.<p>
     * 
     * @param resource the container page's resource 
     * @param cntPage the container page to use
     * @param elemUri the current element uri, <code>null</code> if not to be used as template
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getContainerPage(CmsResource resource, CmsContainerPageBean cntPage, String elemUri)
    throws CmsException, JSONException {

        CmsObject cms = getCmsObject();

        // create empty result object
        JSONObject result = new JSONObject();
        JSONObject resElements = new JSONObject();
        JSONObject resContainers = new JSONObject();
        result.put(P_ELEMENTS, resElements);
        result.put(P_CONTAINERS, resContainers);
        result.put(P_CNTPAGE_LOCALE, cms.getRequestContext().getLocale().toString());
        result.put(P_RECENT_LIST_SIZE, m_manager.getRecentListMaxSize(cms));

        // get the container page itself
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        Set<String> types = cntPage.getTypes();

        // collect some basic data
        result.put(CmsADEServer.P_CNTPAGE_ALLOWEDIT, resUtil.getLock().isLockableBy(
            cms.getRequestContext().currentUser())
            && resUtil.isEditable());
        result.put(CmsADEServer.P_CNTPAGE_LOCKED, resUtil.getLockedByName());

        // collect creatable type elements
        resElements.merge(getNewResourceTypes(cms.getSitePath(resource), types), true, false);
        // collect searchable type elements
        resElements.merge(getSearchResourceTypes(cms.getSitePath(resource), types), true, false);

        // collect page elements
        CmsElementUtil elemUtil = new CmsElementUtil(
            cms,
            getRequest().getParameter(PARAMETER_URI),
            getRequest(),
            getResponse());
        Set<String> ids = new HashSet<String>();
        for (Map.Entry<String, CmsContainerBean> entry : cntPage.getContainers().entrySet()) {
            CmsContainerBean container = entry.getValue();

            // set the container data
            JSONObject resContainer = new JSONObject();
            resContainer.put(P_OBJTYPE, CONTAINER_TYPE);
            resContainer.put(P_CNTPAGE_NAME, container.getName());
            resContainer.put(P_CNTPAGE_TYPE, container.getType());
            resContainer.put(P_MAXELEMENTS, container.getMaxElements());
            JSONArray resContainerElems = new JSONArray();
            resContainer.put(P_ELEMENTS, resContainerElems);

            // get the actual number of elements to render
            int renderElems = container.getElements().size();
            if ((container.getMaxElements() > -1) && (renderElems > container.getMaxElements())) {
                renderElems = container.getMaxElements();
            }
            // add the template element
            if ((elemUri != null) && container.getType().equals(CmsContainerPageBean.TYPE_TEMPLATE)) {
                renderElems--;

                CmsResource elemRes = cms.readResource(elemUri);
                CmsContainerElementBean element = new CmsContainerElementBean(elemRes, null, null);
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                // check if the element already exists
                String id = element.getClientId();
                // collect ids
                resContainerElems.put(id);
                if (ids.contains(id)) {
                    continue;
                }
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element, types);
                // store element data
                ids.add(id);
                resElements.put(id, resElement);
            }

            // iterate the elements
            for (CmsContainerElementBean element : container.getElements()) {
                if (renderElems < 1) {
                    // just collect as many elements as allowed in the template
                    break;
                }
                renderElems--;

                // collect ids
                String id = element.getClientId();
                resContainerElems.put(id);
                if (ids.contains(id)) {
                    continue;
                }
                m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                // get the element data
                JSONObject resElement = elemUtil.getElementData(element, types);

                // get subcontainer elements
                if (resElement.has(P_SUBITEMS)) {
                    // this container page should contain exactly one container
                    CmsXmlContainerPage subXmlCntPage = CmsXmlContainerPageFactory.unmarshal(
                        cms,
                        element.getElement(),
                        getRequest());
                    CmsContainerPageBean subCntPage = subXmlCntPage.getCntPage(cms, cms.getRequestContext().getLocale());
                    CmsContainerBean subContainer = subCntPage.getContainers().values().iterator().next();

                    // adding all sub-items to the elements data
                    for (CmsContainerElementBean subElement : subContainer.getElements()) {
                        if (!ids.contains(subElement.getElement().getStructureId())) {
                            String subId = subElement.getClientId();
                            if (ids.contains(subId)) {
                                continue;
                            }
                            JSONObject subItemData = elemUtil.getElementData(subElement, types);
                            ids.add(subId);
                            resElements.put(subId, subItemData);
                        }
                    }
                }

                // store element data
                ids.add(id);
                resElements.put(id, resElement);
            }

            resContainers.put(container.getName(), resContainer);
        }
        // collect the favorites
        JSONArray resFavorites = getFavoriteList(resElements, types);
        result.put(P_FAVORITES, resFavorites);
        // collect the recent list
        JSONArray resRecent = getRecentList(resElements, types);
        result.put(P_RECENT, resRecent);

        return result;
    }

    /**
     * Returns the current user's favorites list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container page types
     * 
     * @return the current user's favorites list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected JSONArray getFavoriteList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest().getParameter(PARAMETER_URI),
            getRequest(),
            getResponse());

        // iterate the list and create the missing elements
        List<CmsContainerElementBean> favList = m_manager.getFavoriteList(getCmsObject());
        for (CmsContainerElementBean element : favList) {
            String id = element.getClientId();
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(element, types));
                    m_sessionCache.setCacheContainerElement(element.getClientId(), element);
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cntPageUri the container page uri
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 2 properties, {@link #P_ELEMENTS} and {@link #P_SEARCH_HASMORE}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected JSONObject getLastSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsSearchOptions lastOptions = m_sessionCache.getADESearchOptions();
        if ((lastOptions == null) || compareSearchOptions(lastOptions, options)) {
            return new JSONObject();
        }
        return getSearchResult(cntPageUri, lastOptions, types);
    }

    /**
     * Returns the data for new elements from the given configuration file.<p>
     * 
     * @param cntPageUri the container page uri
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getNewResourceTypes(String cntPageUri, Set<String> types) throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        resElements.put(P_NEWORDER, new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());

        List<CmsResource> creatableElements = m_manager.getCreatableElements(
            cms,
            request.getParameter(PARAMETER_CNTPAGE),
            request);
        for (CmsResource creatableElement : creatableElements) {
            String type = OpenCms.getResourceManager().getResourceType(creatableElement).getTypeName();
            JSONObject resElement = elemUtil.getElementData(
                new CmsContainerElementBean(creatableElement, null, null),
                types);
            // overwrite some special fields for new elements
            resElement.put(CmsElementUtil.P_ELEMENT_ID, type);
            resElement.put(CmsElementUtil.P_ELEMENT_STATUS, ELEMENT_NEWCONFIG);
            resElement.put(CmsElementUtil.P_ELEMENT_TYPE, type);
            resElement.put(CmsElementUtil.P_ELEMENT_TYPENAME, CmsWorkplaceMessages.getResourceName(
                cms.getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
            // additional array to keep track of the order
            resElements.accumulate(P_NEWORDER, type);
        }
        return resElements;
    }

    /**
     * Returns the list of resources that can be published.<p>
     * 
     * @param related to include related resources
     * @param siblings to include siblings
     * 
     * @return a list of resources that can be published
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getPublishList(boolean related, boolean siblings) throws CmsException, JSONException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/_content/events/event_0003.html"));
        jsonRes.put(P_INFO_NAME, "Unpublished related resource, will be published :");
        jsonRes.put(P_INFO_VALUE, "/demo_t3/images/Rose.jpg");
        resources.put(jsonRes);

        JSONObject jsonRes2 = resourceToJson(getCmsObject().readResource("/demo_t3/images/Rose.jpg"));
        jsonRes2.put(P_INFO_NAME, "Resource related to :");
        jsonRes2.put(P_INFO_VALUE, "/demo_t3/_content/events/event_0003.html");
        resources.put(jsonRes2);

        return resources;
    }

    /**
     * Returns the current user's recent list.<p>
     * 
     * @param resElements the current page's element list
     * @param types the supported container types
     * 
     * @return the current user's recent list
     * 
     * @throws CmsException if something goes wrong 
     */
    protected JSONArray getRecentList(JSONObject resElements, Collection<String> types) throws CmsException {

        JSONArray result = new JSONArray();
        CmsElementUtil elemUtil = new CmsElementUtil(
            getCmsObject(),
            getRequest().getParameter(PARAMETER_URI),
            getRequest(),
            getResponse());

        // get the cached list
        List<CmsContainerElementBean> recentList = m_sessionCache.getADERecentList();
        // iterate the list and create the missing elements
        for (CmsContainerElementBean element : recentList) {
            String id = element.getClientId();
            if ((resElements != null) && !resElements.has(id)) {
                try {
                    resElements.put(id, elemUtil.getElementData(element, types));
                    result.put(id);
                } catch (Exception e) {
                    // ignore any problems
                    if (!LOG.isDebugEnabled()) {
                        LOG.warn(e.getLocalizedMessage());
                    }
                    LOG.debug(e.getLocalizedMessage(), e);
                }
            } else {
                result.put(id);
            }
        }

        return result;
    }

    /**
     * Returns a list of resources that should be published but can not.<p>
     * This can be due to several reasons:<br>
     * <ul>
     *  <li>Locks</li>
     *  <li>Permissions</li>
     *  <li>Already published</li>
     * </ul>
     * 
     * @param related to include related resources
     * @param siblings to include siblings
     * 
     * @return a list of resources that should be published but can not
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getResourcesToPublishWithProblems(boolean related, boolean siblings)
    throws JSONException, CmsException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/_content/events/event_0003.html"));
        jsonRes.put(P_REASON, REASON_PERMISSIONS);
        resources.put(jsonRes);

        JSONObject jsonRes2 = resourceToJson(getCmsObject().readResource("/demo_t3/images/Rose.jpg"));
        jsonRes2.put(P_REASON, REASON_PUBLISHED);
        resources.put(jsonRes2);

        return resources;
    }

    /**
     * Checks for possible broken links when the given list of resources would be published.<p>
     * 
     * @param ids list of structure ids identifying the resources to be published
     * 
     * @return a sublist of JSON resources that would produce broken links when published 
     * 
     * @throws CmsException if something goes wrong 
     * @throws JSONException if something goes wrong 
     */
    protected JSONArray getResourcesWithLinkCheck(JSONArray ids) throws JSONException, CmsException {

        JSONArray resources = new JSONArray();

        // TODO: this is just a skeleton
        JSONObject jsonRes = resourceToJson(getCmsObject().readResource("/demo_t3/index.html"));
        jsonRes.put(P_INFO_NAME, "Broken link sources :");
        jsonRes.put(P_INFO_VALUE, "/demo_t3/_content/articles/intro.html (Html link)");
        resources.put(jsonRes);

        return resources;
    }

    /**
     * Returns the data for searchable resource types from the given configuration file.<p>
     * 
     * @param cntPageUri the container page uri
     * @param types the supported container page types
     * 
     * @return the data for the given container page
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected JSONObject getSearchResourceTypes(String cntPageUri, Set<String> types)
    throws CmsException, JSONException {

        JSONObject resElements = new JSONObject();
        resElements.put(P_SEARCHORDER, new JSONArray());
        HttpServletRequest request = getRequest();
        CmsObject cms = getCmsObject();
        CmsElementUtil elemUtil = new CmsElementUtil(cms, cntPageUri, request, getResponse());

        List<CmsResource> creatableElements = m_manager.getSearchableResourceTypes(
            cms,
            request.getParameter(PARAMETER_CNTPAGE),
            request);
        for (CmsResource searchableElement : creatableElements) {
            String type = OpenCms.getResourceManager().getResourceType(searchableElement).getTypeName();
            JSONObject resElement = elemUtil.getElementData(
                new CmsContainerElementBean(searchableElement, null, null),
                types);
            // overwrite some special fields for new elements
            resElement.put(CmsElementUtil.P_ELEMENT_ID, type);
            resElement.put(CmsElementUtil.P_ELEMENT_STATUS, ELEMENT_NEWCONFIG);
            resElement.put(CmsElementUtil.P_ELEMENT_TYPE, type);
            resElement.put(CmsElementUtil.P_ELEMENT_TYPENAME, CmsWorkplaceMessages.getResourceName(
                cms.getRequestContext().getLocale(),
                type));
            resElements.put(type, resElement);
            // additional array to keep track of the order
            resElements.accumulate(P_SEARCHORDER, type);
        }
        return resElements;
    }

    /**
     * Returns elements for the search result matching the given options.<p>
     * 
     * @param cntPageUri the container page uri
     * @param options the search options
     * @param types the supported container types
     * 
     * @return JSON object with 3 properties, {@link CmsADEServer#P_ELEMENTS}, {@link CmsADEServer#P_SEARCH_HASMORE} and {@link CmsADEServer#P_SEARCH_COUNT}
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected JSONObject getSearchResult(String cntPageUri, CmsSearchOptions options, Set<String> types)
    throws JSONException, CmsException {

        CmsObject cms = getCmsObject();

        JSONObject result = new JSONObject();
        JSONArray elements = new JSONArray();
        result.put(CmsADEServer.P_ELEMENTS, elements);

        CmsUser user = cms.getRequestContext().currentUser();

        // if there is no type or no text to search, no search is needed 
        if (options.isValid()) {
            // get the configured search index 
            String indexName = new CmsUserSettings(user).getWorkplaceSearchIndexName();

            // get the page size
            int pageSize = m_manager.getSearchPageSize(cms);

            // set the search parameters
            CmsSearchParameters params = new CmsSearchParameters(options.getText());
            params.setIndex(indexName);
            params.setMatchesPerPage(pageSize);
            params.setSearchPage(options.getPage() + 1);
            params.setResourceTypes(options.getTypesAsList());

            // search
            CmsSearch searchBean = new CmsSearch();
            searchBean.init(cms);
            searchBean.setParameters(params);
            searchBean.setSearchRoot(options.getLocation());
            List<CmsSearchResult> searchResults = searchBean.getSearchResult();
            if (searchResults != null) {
                // helper
                CmsElementUtil elemUtil = new CmsElementUtil(
                    cms,
                    getRequest().getParameter(PARAMETER_URI),
                    getRequest(),
                    getResponse());

                // iterate result list and generate the elements
                Iterator<CmsSearchResult> it = searchResults.iterator();
                while (it.hasNext()) {
                    CmsSearchResult sr = it.next();
                    // get the element data
                    String uri = cms.getRequestContext().removeSiteRoot(sr.getPath());
                    try {
                        CmsResource resource = cms.readResource(uri);
                        JSONObject resElement = elemUtil.getElementData(new CmsContainerElementBean(
                            resource,
                            null,
                            null), types);
                        // store element data
                        elements.put(resElement);
                    } catch (Exception e) {
                        if (!LOG.isDebugEnabled()) {
                            LOG.warn(e.getLocalizedMessage());
                        }
                        LOG.debug(e.getLocalizedMessage(), e);
                    }
                }
            }
            // check if there are more search pages
            int results = searchBean.getSearchPage() * searchBean.getMatchesPerPage();
            boolean hasMore = (searchBean.getSearchResultCount() > results);
            result.put(CmsADEServer.P_SEARCH_HASMORE, hasMore);
            result.put(CmsADEServer.P_SEARCH_COUNT, searchBean.getSearchResultCount());
        } else {
            // no search
            result.put(CmsADEServer.P_SEARCH_HASMORE, false);
            result.put(CmsADEServer.P_SEARCH_COUNT, 0);
        }

        m_sessionCache.setCacheADESearchOptions(options);

        return result;
    }

    /**
     * Publishes the given list of resources.<p>
     * 
     * @param ids list of structure ids identifying the resources to publish
     * 
     * @throws CmsException if something goes wrong
     * @throws JSONException if something goes wrong
     */
    protected void publishResources(JSONArray ids) throws CmsException, JSONException {

        List<CmsResource> resources = new ArrayList<CmsResource>(ids.length());
        CmsObject cms = getCmsObject();
        for (int i = 0; i < ids.length(); i++) {
            resources.add(cms.readResource(new CmsUUID(ids.optString(i))));
        }
        // TODO: publish the resources
        // OpenCms.getPublishManager().publishProject(cms, report, publishList);
        removeResourcesFromPublishList(ids);
    }

    /**
     * Removes the given resources from the user's publish list.<p>
     * 
     * @param ids list of structure ids identifying the resources to be removed
     * 
     * @throws JSONException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    protected void removeResourcesFromPublishList(JSONArray ids) throws JSONException, CmsException {

        CmsObject cms = getCmsObject();
        CmsUser user = cms.readUser(cms.getRequestContext().currentUser().getId());
        String info = (String)user.getAdditionalInfo(INFO_PUBLISH_LIST);
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(info)) {
            // publish list is empty, nothing to do
            return;
        }
        JSONArray userPubList = new JSONArray(info);
        JSONArray newPubList = new JSONArray();
        for (int i = 0; i < userPubList.length(); i++) {
            String id = userPubList.optString(i);
            if (!ids.containsString(id)) {
                newPubList.put(id);
            }
        }
        cms.getRequestContext().currentUser().setAdditionalInfo(INFO_PUBLISH_LIST, newPubList.toString());
        cms.writeUser(user);
    }

    /**
     * Converts a resource to a JSON object.<p>
     * 
     * @param resource the resource to convert
     * 
     * @return a JSON object representing the resource
     * 
     * @throws JSONException if something goes wrong
     */
    protected JSONObject resourceToJson(CmsResource resource) throws JSONException {

        CmsObject cms = getCmsObject();
        JSONObject jsonRes = new JSONObject();
        CmsResourceUtil resUtil = new CmsResourceUtil(cms, resource);
        jsonRes.put(P_ID, resource.getStructureId());
        jsonRes.put(P_URI, resUtil.getFullPath());
        jsonRes.put(P_TITLE, resUtil.getTitle());
        jsonRes.put(P_ICON, resUtil.getIconPathExplorer());
        jsonRes.put(P_STATE, resUtil.getStateAbbreviation());
        jsonRes.put(P_LOCKED_BY, resUtil.getLockedByName());
        return jsonRes;
    }

    /**
     * Saves the new state of the container page.<p>
     * 
     * @param uri the uri of the container page to save
     * @param cntPage the container page data
     * 
     * @throws CmsException if something goes wrong with the cms context
     * @throws JSONException if something goes wrong with the JSON manipulation
     */
    protected void setContainerPage(String uri, JSONObject cntPage) throws CmsException, JSONException {

        CmsObject cms = getCmsObject();
        String paramUri = getRequest().getParameter(PARAMETER_URI);

        cms.lockResourceTemporary(uri);
        CmsFile containerPage = cms.readFile(uri);
        CmsXmlContent xmlCnt = CmsXmlContentFactory.unmarshal(cms, containerPage);
        Locale locale = CmsLocaleManager.getLocale(cntPage.getString(P_CNTPAGE_LOCALE));
        if (xmlCnt.hasLocale(locale)) {
            // remove the locale 
            xmlCnt.removeLocale(locale);
        }
        xmlCnt.addLocale(cms, locale);

        JSONObject cnts = cntPage.getJSONObject(P_CONTAINERS);
        int cntCount = 0;
        Iterator<String> itCnt = cnts.keys();
        while (itCnt.hasNext()) {
            String cntKey = itCnt.next();
            JSONObject cnt = cnts.getJSONObject(cntKey);

            I_CmsXmlContentValue cntValue = xmlCnt.getValue(CmsXmlContainerPage.N_CONTAINER, locale, cntCount);
            if (cntValue == null) {
                cntValue = xmlCnt.addValue(cms, CmsXmlContainerPage.N_CONTAINER, locale, cntCount);
            }

            String name = cnt.getString(P_CNTPAGE_NAME);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsXmlContainerPage.N_NAME), locale, 0).setStringValue(
                cms,
                name);

            String type = cnt.getString(P_CNTPAGE_TYPE);
            xmlCnt.getValue(CmsXmlUtils.concatXpath(cntValue.getPath(), CmsXmlContainerPage.N_TYPE), locale, 0).setStringValue(
                cms,
                type);

            JSONArray elems = cnt.getJSONArray(P_ELEMENTS);
            for (int i = 0; i < elems.length(); i++) {
                JSONObject elem = cnt.getJSONArray(P_ELEMENTS).getJSONObject(i);

                String formatter = elem.getString(P_FORMATTER);
                String elemUri = elem.getString(P_URI);
                if (type.equals(CmsContainerPageBean.TYPE_TEMPLATE) && elemUri.equals(paramUri)) {
                    // skip main-content if acting as template
                    continue;
                }
                String clientId = elem.getString(P_ID);
                I_CmsXmlContentValue elemValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                    cntValue.getPath(),
                    CmsXmlContainerPage.N_ELEMENT), locale, i);
                xmlCnt.getValue(CmsXmlUtils.concatXpath(elemValue.getPath(), CmsXmlContainerPage.N_URI), locale, 0).setStringValue(
                    cms,
                    elemUri);
                xmlCnt.getValue(
                    CmsXmlUtils.concatXpath(elemValue.getPath(), CmsXmlContainerPage.N_FORMATTER),
                    locale,
                    0).setStringValue(cms, formatter);

                // checking if there are any properties to set
                if (clientId.contains("#")) {
                    CmsContainerElementBean element = getCachedElement(clientId);
                    Map<String, CmsProperty> properties = m_manager.getElementProperties(element);
                    Map<String, CmsXmlContentProperty> propertiesConf = m_manager.getElementPropertyConfiguration(element.getElement());
                    Iterator<String> itProps = properties.keySet().iterator();

                    // index of the property
                    int j = 0;

                    // iterating all properties
                    while (itProps.hasNext()) {
                        String propertyName = itProps.next();

                        // only if there is a value set and the property is configured in the schema we will save it to the container-page 
                        if ((properties.get(propertyName).getStructureValue() != null)
                            && propertiesConf.containsKey(propertyName)) {
                            I_CmsXmlContentValue propValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                                elemValue.getPath(),
                                CmsXmlContainerPage.N_PROPERTIES), locale, j);
                            xmlCnt.getValue(
                                CmsXmlUtils.concatXpath(propValue.getPath(), CmsXmlContainerPage.N_NAME),
                                locale,
                                0).setStringValue(cms, propertyName);
                            I_CmsXmlContentValue valValue = xmlCnt.addValue(cms, CmsXmlUtils.concatXpath(
                                propValue.getPath(),
                                CmsXmlContainerPage.N_VALUE), locale, 0);
                            if (propertiesConf.get(propertyName).getPropertyType() == CmsXmlContentProperty.T_URI) {
                                xmlCnt.addValue(
                                    cms,
                                    CmsXmlUtils.concatXpath(valValue.getPath(), CmsXmlContainerPage.N_URI),
                                    locale,
                                    0).setStringValue(cms, properties.get(propertyName).getStructureValue());
                            } else {
                                xmlCnt.addValue(
                                    cms,
                                    CmsXmlUtils.concatXpath(valValue.getPath(), CmsXmlContainerPage.N_STRING),
                                    locale,
                                    0).setStringValue(cms, properties.get(propertyName).getStructureValue());
                            }
                            j++;
                        }
                    }
                }

            }
            cntCount++;
        }
        containerPage.setContents(xmlCnt.marshal());
        cms.writeFile(containerPage);
    }

    /**
     * Stores an error message for a missing parameter in a JSON object.
     * 
     * @param result the JSON object in which the error message should be stored
     * @param parameterName the name of the missing parameter
     * @throws JSONException if something goes wrong.
     */
    protected void storeErrorMissingParam(JSONObject result, String parameterName) throws JSONException {

        result.put(RES_ERROR, Messages.get().getBundle().key(Messages.ERR_JSON_MISSING_PARAMETER_1, parameterName));
    }
}

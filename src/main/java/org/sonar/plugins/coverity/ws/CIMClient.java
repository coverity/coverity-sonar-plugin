/*
 * Coverity Sonar Plugin
 * Copyright (c) 2019 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */

package org.sonar.plugins.coverity.ws;

import com.coverity.ws.v9.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

/**
 * Represents one Coverity Integrity Manager server. Abstracts functions like getting streams and defects.
 */
public class CIMClient {
    public static final String COVERITY_WS_VERSION = "v9";
    public static final String COVERITY_NAMESPACE = "http://ws.coverity.com/" + COVERITY_WS_VERSION;
    public static final String CONFIGURATION_SERVICE_WSDL = "/ws/" + COVERITY_WS_VERSION + "/configurationservice?wsdl";
    public static final String DEFECT_SERVICE_WSDL = "/ws/" + COVERITY_WS_VERSION + "/defectservice?wsdl";

    private static final int GET_STREAM_DEFECTS_MAX_CIDS = 100;

    private static final Logger LOG = LoggerFactory.getLogger(CIMClient.class);

    /**
     * The host name for the CIM server
     */
    private final String host;
    /**
     * The port for the CIM server (this is the HTTP port and not the data port)
     */
    private final int port;
    /**
     * Username for connecting to the CIM server
     */
    private final String user;
    /**
     * Password for connecting to the CIM server
     */
    private final String password;
    /**
     * Use SSL
     */
    private final boolean useSSL;
    /**
     * cached webservice port for Defect service
     */
    private transient DefectServiceService defectServiceService;
    /**
     * cached webservice port for Configuration service
     */
    private transient ConfigurationServiceService configurationServiceService;
    private transient Map<String, Long> projectKeys;

    public CIMClient(String host, int port, String user, String password, boolean ssl) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.useSSL = ssl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public boolean isUseSSL() {
        return useSSL;
    }

    /**
     * The root URL for the CIM instance
     *
     * @return a url
     * @throws java.net.MalformedURLException should not happen if host is valid
     */
    public URL getURL() throws MalformedURLException {
        return new URL(useSSL ? "https" : "http", host, port, "/");
    }

    /**
     * Returns a Defect service client
     */
    public DefectService getDefectService() throws IOException {
        synchronized(this) {
            if (defectServiceService == null) {
                defectServiceService = new DefectServiceService(
                        new URL(getURL(), DEFECT_SERVICE_WSDL),
                        new QName(COVERITY_NAMESPACE, "DefectServiceService"));
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                DefectService defectService = defectServiceService.getDefectServicePort();
                attachAuthenticationHandler((BindingProvider) defectService);

                return defectService;
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }

    /**
     * Attach an authentication handler to the web service, that uses the configured user and password
     */
    private void attachAuthenticationHandler(BindingProvider service) {
        service.getBinding().setHandlerChain(Arrays.<Handler>asList(new ClientAuthenticationHandlerWSS(user, password)));
    }

    /**
     * Returns a Configuration service client
     */
    public ConfigurationService getConfigurationService() throws IOException {
        synchronized(this) {
            if (configurationServiceService == null) {
                // Create a Web Services port to the server
                configurationServiceService = new ConfigurationServiceService(
                        new URL(getURL(), CONFIGURATION_SERVICE_WSDL),
                        new QName(COVERITY_NAMESPACE, "ConfigurationServiceService"));
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            try {
                ConfigurationService configurationService = configurationServiceService.getConfigurationServicePort();
                attachAuthenticationHandler((BindingProvider)configurationService);

                return configurationService;
            } finally {
                Thread.currentThread().setContextClassLoader(cl);
            }
        }
    }


    /**
     * Returns all merged defects on a given project.
     */
    public List<MergedDefectDataObj> getDefects(String project) throws IOException, CovRemoteServiceException_Exception {
        ProjectScopeDefectFilterSpecDataObj filterSpec = new ProjectScopeDefectFilterSpecDataObj();
        ProjectIdDataObj projectId = new ProjectIdDataObj();
        projectId.setName(project);
        PageSpecDataObj pageSpec = new PageSpecDataObj();
        pageSpec.setPageSize(1000);

        List<MergedDefectDataObj> result = new ArrayList<MergedDefectDataObj>();
        int defectCount = 0;
        MergedDefectsPageDataObj defects = null;
        do {
            pageSpec.setStartIndex(defectCount);
            defects = getDefectService().getMergedDefectsForProjectScope(projectId, filterSpec, pageSpec);
            result.addAll(defects.getMergedDefects());
            defectCount += defects.getMergedDefects().size();
            LOG.info(MessageFormat.format("Fetching coverity defects for project \"{0}\" (fetched {1} of {2})",
                    project, defectCount, defects.getTotalNumberOfRecords()));
        } while(defectCount < defects.getTotalNumberOfRecords());

        return result;
    }

    /**
     * Returns a ProjectDataObj for a given project id.
     */
    public ProjectDataObj getProject(String projectId) throws IOException, CovRemoteServiceException_Exception {
        ProjectFilterSpecDataObj filterSpec = new ProjectFilterSpecDataObj();
        filterSpec.setNamePattern(projectId);
        List<ProjectDataObj> projects = getConfigurationService().getProjects(filterSpec);
        if(projects.size() == 0) {
            return null;
        } else {
            return projects.get(0);
        }
    }

    /**
     * Returns a map of <CID, StreamDefectDataObj>. It essentially calls getDefectService().getStreamDefects() on a
     * specific list of MergedDefectDataObj. Then it takes the resulting List<StreamDefectDataObj> and creates a map
     * with the CID of each element on that list as the key, and the actual object as value.
     */
    public Map<Long, StreamDefectDataObj> getStreamDefectsForMergedDefects(List<MergedDefectDataObj> defects) throws IOException, CovRemoteServiceException_Exception {
        Map<Long, MergedDefectDataObj> cids = new HashMap<Long, MergedDefectDataObj>();
        Map<Long, StreamDefectDataObj> sddos = new HashMap<Long, StreamDefectDataObj>();
        Map<Long, MergedDefectIdDataObj> mdidos = new HashMap<Long, MergedDefectIdDataObj>();

        StreamDefectFilterSpecDataObj filter = new StreamDefectFilterSpecDataObj();
        Set<String> streamList = new HashSet<String>();

        for(MergedDefectDataObj mddo : defects) {
            cids.put(mddo.getCid(), mddo);
            MergedDefectIdDataObj mdido = new MergedDefectIdDataObj();
            mdido.setCid(mddo.getCid());
            mdido.setMergeKey(mddo.getMergeKey());
            mdidos.put(mddo.getCid(), mdido);
            streamList.add(mddo.getLastDetectedStream());
        }

        for (String stream : streamList) {
            StreamIdDataObj streamIdDataObj = new StreamIdDataObj();
            streamIdDataObj.setName(stream);
            filter.getStreamIdList().add(streamIdDataObj);
        }
        filter.setIncludeDefectInstances(true);

        List<Long> cidList = new ArrayList<Long>(cids.keySet());

        for(int i = 0; i < cidList.size(); i += GET_STREAM_DEFECTS_MAX_CIDS) {
            List<Long> slice = cidList.subList(i, i + Math.min(GET_STREAM_DEFECTS_MAX_CIDS, cidList.size() - i));
            List<MergedDefectIdDataObj> sliceMergedDefectIdDataObj = new ArrayList<MergedDefectIdDataObj>();
            for(Long cid : slice){
                sliceMergedDefectIdDataObj.add(mdidos.get(cid));
            }

            try{
                List<StreamDefectDataObj> temp = getDefectService().getStreamDefects(sliceMergedDefectIdDataObj, filter);

                for(StreamDefectDataObj sddo : temp) {
                    MergedDefectDataObj curMergedDefectDataObj = cids.get(sddo.getCid());
                    StreamIdDataObj curStreamIdDataObj = sddo.getStreamId();

                    if (curMergedDefectDataObj != null && curStreamIdDataObj != null
                            && curMergedDefectDataObj.getLastDetectedStream().equals(curStreamIdDataObj.getName())) {
                        sddos.put(sddo.getCid(), sddo);
                    }
                }

                LOG.info(MessageFormat.format("Fetching coverity defect details (fetched {0} of {1})",
                        sddos.size(), cidList.size()));
            } catch (Exception ex) {
                LOG.error("Error occurred while fetching defect details.", ex);

                LOG.debug("===== MergeDefectIdDataObj information =====");
                LOG.debug("Size of SliceMergedDefectIdDataObj: " + sliceMergedDefectIdDataObj.size());
                for (MergedDefectIdDataObj mergedDefectIdDataObj : sliceMergedDefectIdDataObj) {
                    LOG.debug(MessageFormat.format("[Coverity] CID: {0}", mergedDefectIdDataObj.getCid()));
                }

                LOG.debug("\n====== StreamDefectFilterSpecDataObj information =====");
                for (StreamIdDataObj streamIdDataObj : filter.getStreamIdList()) {
                    LOG.debug(MessageFormat.format("[Coverity] Stream: {0}", streamIdDataObj.getName()));
                }

                break;
            }

        }

        return sddos;
    }
}

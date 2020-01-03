/*
 * Coverity Sonar Plugin
 * Copyright (c) 2020 Synopsys, Inc
 * support@coverity.com
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html.
 */
package org.sonar.plugins.coverity.ws;

import com.coverity.ws.v9.*;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

public class TestCIMClient extends CIMClient {

    private TestConfigurationService testConfigurationService;
    private TestDefectService testDefectService;

    public TestCIMClient() {
        this("test-host", 8443, "test-user", "password", true);
    }

    public TestCIMClient(String host, int port, String user, String password, boolean ssl) {
        super(host, port, user, password, ssl);
        testConfigurationService = new TestConfigurationService();
        testDefectService = new TestDefectService();
    }

    @Override
    public ConfigurationService getConfigurationService() throws IOException {
        return testConfigurationService;
    }

    @Override
    public DefectService getDefectService() throws IOException {
        return testDefectService;
    }

    public void setupProject(String projectName) {
        testConfigurationService.setupProject(projectName);
    }

    public void setupStream(String streamName){
        testConfigurationService.setupStream(streamName);
    }

    public void setupDefect(String domain, String checkerName, String streamName, List<String> filePathsForEvents) {
        testDefectService.setFilePathsForEvents(filePathsForEvents);
        testDefectService.addDefect(domain, checkerName, streamName);
    }

    public void configureMainEvent(String eventTag, String eventDescription){
        testDefectService.configureMainEvent(eventTag, eventDescription);
    }

    public static class TestConfigurationService implements ConfigurationService {
        private List<ProjectDataObj> projects;
        private List<StreamDataObj> streams;

        public TestConfigurationService() {

            this.projects = new ArrayList<>();
            this.streams = new ArrayList<>();
        }

        public void setupProject(String projectName) {
            final ProjectDataObj projectDataObj = new ProjectDataObj();
            ProjectIdDataObj projectIdDataObj = new ProjectIdDataObj();
            projectIdDataObj.setName(projectName);
            projectDataObj.setId(projectIdDataObj);
            projectDataObj.setProjectKey((long)projects.size());

            StreamDataObj streamDataObj = new StreamDataObj();
            StreamIdDataObj streamIdDataObj = new StreamIdDataObj();
            streamIdDataObj.setName(projectName + "-stream");
            streamDataObj.setId(streamIdDataObj);
            projectDataObj.getStreams().add(streamDataObj);

            projects.add(projectDataObj);
        }

        public void setupStream(String streamName){
            StreamDataObj streamDataObj = new StreamDataObj();
            StreamIdDataObj streamIdDataObj = new StreamIdDataObj();
            streamIdDataObj.setName(streamName);
            streamDataObj.setId(streamIdDataObj);

            streams.add(streamDataObj);
        }

        @Override
        public void updateAttribute(AttributeDefinitionIdDataObj attributeDefinitionId, AttributeDefinitionSpecDataObj attributeDefinitionSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteAttribute(AttributeDefinitionIdDataObj attributeDefinitionId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createComponentMap(ComponentMapSpecDataObj componentMapSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateComponentMap(ComponentMapIdDataObj componentMapId, ComponentMapSpecDataObj componentMapSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteComponentMap(ComponentMapIdDataObj componentMapId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateGroup(GroupIdDataObj groupId, GroupSpecDataObj groupSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteGroup(GroupIdDataObj groupId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createProject(ProjectSpecDataObj projectSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateProject(ProjectIdDataObj projectId, ProjectSpecDataObj projectSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteProject(ProjectIdDataObj projectId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createRole(RoleSpecDataObj roleSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateRole(RoleIdDataObj roleId, RoleSpecDataObj roleSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteRole(RoleIdDataObj roleId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createStream(StreamSpecDataObj streamSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateStream(StreamIdDataObj streamId, StreamSpecDataObj streamSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteStream(StreamIdDataObj streamId, boolean onlyIfEmpty) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createTriageStore(TriageStoreSpecDataObj triageStoreSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateTriageStore(TriageStoreIdDataObj triageStoreId, TriageStoreSpecDataObj triageStoreSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteTriageStore(TriageStoreIdDataObj triageStoreId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createUser(UserSpecDataObj userSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateUser(String username, UserSpecDataObj userSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteUser(String username) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public UserDataObj getUser(String username) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void mergeTriageStores(List<TriageStoreIdDataObj> srcTriageStoreIds, TriageStoreIdDataObj triageStoreId, boolean deleteSourceStores, boolean assignStreamsToTargetStore) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<PermissionDataObj> getAllPermissions() {
            throw new NotImplementedException();
        }

        @Override
        public List<LdapConfigurationDataObj> getAllLdapConfigurations() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<DeleteSnapshotJobInfoDataObj> getDeleteSnapshotJobInfo(List<SnapshotIdDataObj> snapshotId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<SnapshotIdDataObj> getSnapshotsForStream(StreamIdDataObj streamId, SnapshotFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void deleteLdapConfiguration(ServerDomainIdDataObj serverDomainIdDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<FeatureUpdateTimeDataObj> getLastUpdateTimes() {
            throw new NotImplementedException();
        }

        @Override
        public void setAcceptingNewCommits(boolean acceptNewCommits) {
            throw new NotImplementedException();
        }

        @Override
        public void deleteSnapshot(List<SnapshotIdDataObj> snapshotId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void executeNotification(String viewname) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public StreamDataObj copyStream(ProjectIdDataObj projectId, StreamIdDataObj sourceStreamId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createStreamInProject(ProjectIdDataObj projectId, StreamSpecDataObj streamSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setMessageOfTheDay(String message) {
            throw new NotImplementedException();
        }

        @Override
        public String getMessageOfTheDay() {
            throw new NotImplementedException();
        }

        @Override
        public List<ProjectDataObj> getProjects(ProjectFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            if (!StringUtils.isEmpty(filterSpec.getNamePattern()))
            {
                List<ProjectDataObj> matchingProjects = new ArrayList<>();
                for (ProjectDataObj project : projects) {
                    if (project.getId().getName().equals(filterSpec.getNamePattern()))
                        matchingProjects.add(project);
                }
                return matchingProjects;
            }

            return projects;
        }

        @Override
        public List<StreamDataObj> getStreams(StreamFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            if (!StringUtils.isEmpty(filterSpec.getNamePattern()))
            {
                List<StreamDataObj> matchingStreams = new ArrayList<>();
                for (StreamDataObj stream : streams) {
                    if (stream.getId().getName().equals(filterSpec.getNamePattern()))
                        matchingStreams.add(stream);
                }
                return matchingStreams;
            }

            return streams;
        }

        @Override
        public List<ComponentMapDataObj> getComponentMaps(ComponentMapFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<TriageStoreDataObj> getTriageStores(TriageStoreFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<LocalizedValueDataObj> getCategoryNames() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<String> getDefectStatuses() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<SnapshotInfoDataObj> getSnapshotInformation(List<SnapshotIdDataObj> snapshotIds) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateSnapshotInfo(SnapshotIdDataObj snapshotId, SnapshotInfoDataObj snapshotData) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public CommitStateDataObj getCommitState() {
            throw new NotImplementedException();
        }

        @Override
        public List<ServerDomainIdDataObj> getLdapServerDomains() {
            throw new NotImplementedException();
        }

        @Override
        public XMLGregorianCalendar getServerTime() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public ConfigurationDataObj getSystemConfig() {
            throw new NotImplementedException();
        }

        @Override
        public LicenseStateDataObj getLicenseState() {
            throw new NotImplementedException();
        }

        @Override
        public SnapshotPurgeDetailsObj getSnapshotPurgeDetails() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createLdapConfiguration(LdapConfigurationSpecDataObj ldapConfigurationSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateLdapConfiguration(ServerDomainIdDataObj serverDomainIdDataObj, LdapConfigurationSpecDataObj ldapConfigurationSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setSnapshotPurgeDetails(SnapshotPurgeDetailsObj purgeDetailsSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setBackupConfiguration(BackupConfigurationDataObj backupConfigurationDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<ProjectDataObj> getDeveloperStreamsProjects(ProjectFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public SignInSettingsDataObj getSignInConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public LoggingConfigurationDataObj getLoggingConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setLoggingConfiguration(LoggingConfigurationDataObj loggingConfigurationDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setSkeletonizationConfiguration(SkeletonizationConfigurationDataObj skeletonizationConfigurationDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void importLicense(LicenseSpecDataObj licenseSpecDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public BackupConfigurationDataObj getBackupConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public SkeletonizationConfigurationDataObj getSkeletonizationConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public LicenseDataObj getLicenseConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public String getArchitectureAnalysisConfiguration() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void setArchitectureAnalysisConfiguration(String architectureAnalysisConfiguration) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateSignInConfiguration(SignInSettingsDataObj signInSettingsDataObj) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<String> getCheckerNames() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public VersionDataObj getVersion() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createGroup(GroupSpecDataObj groupSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createAttribute(AttributeDefinitionSpecDataObj attributeDefinitionSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<LocalizedValueDataObj> getTypeNames() throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public AttributeDefinitionDataObj getAttribute(AttributeDefinitionIdDataObj attributeDefinitionId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public ComponentDataObj getComponent(ComponentIdDataObj componentId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public GroupsPageDataObj getGroups(GroupFilterSpecDataObj filterSpec, PageSpecDataObj pageSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public UsersPageDataObj getUsers(UserFilterSpecDataObj filterSpec, PageSpecDataObj pageSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public RoleDataObj getRole(RoleIdDataObj roleId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public GroupDataObj getGroup(GroupIdDataObj groupId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<RoleDataObj> getAllRoles() {
            throw new NotImplementedException();
        }

        @Override
        public List<String> notify(List<String> usernames, String subject, String message) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<AttributeDefinitionDataObj> getAttributes() {
            throw new NotImplementedException();
        }
    }

    public static class TestDefectService implements DefectService {
        private List<MergedDefectIdDataObj> mergedDefectIds = new ArrayList<>();
        private List<MergedDefectDataObj> mergedDefects = new ArrayList<>();
        private String mainEventTag;
        private String mainEventDescription;
        private List<String> filePaths;

        public TestDefectService(){
            this.mainEventDescription = "Event Description";
            this.mainEventTag = "Event Tag";
        }

        public void setFilePathsForEvents(List<String> filePaths){
            this.filePaths = filePaths;
        }

        public void addDefect(String domain, String checkerName, String streamName) {
            MergedDefectIdDataObj idDataObj = new MergedDefectIdDataObj();
            final long cid = (long) mergedDefects.size() + 1;
            idDataObj.setCid(cid);
            idDataObj.setMergeKey("MK_" + cid);
            mergedDefectIds.add(idDataObj);

            MergedDefectDataObj defectDataObj = new MergedDefectDataObj();
            defectDataObj.setCid(cid);
            defectDataObj.setMergeKey("MK_" + cid);
            defectDataObj.setCheckerName(checkerName);
            defectDataObj.setDomain(domain);
            defectDataObj.setDisplayCategory(checkerName + "(category)");
            defectDataObj.setDisplayType(checkerName + "(type)");
            defectDataObj.setFunctionDisplayName("defect_function_" + cid + "()");

            // set default attribute values for filtering
            defectDataObj.getDefectStateAttributeValues().add(newAttribute("Action", "Undecided"));
            defectDataObj.getDefectStateAttributeValues().add(newAttribute("Classification", "Unclassified"));
            defectDataObj.getDefectStateAttributeValues().add(newAttribute("Severity", "Unspecified"));
            defectDataObj.setDisplayImpact("Low");
            defectDataObj.setComponentName("Default.Other");
            defectDataObj.setLastDetectedStream(streamName);

            try {
                GregorianCalendar calender = new GregorianCalendar();
                calender.setTime(new SimpleDateFormat("yyyy-MM-dd").parse("2017-03-20"));
                defectDataObj.setFirstDetected(DatatypeFactory.newInstance().newXMLGregorianCalendar(calender));
            } catch (ParseException | DatatypeConfigurationException e) {
                // ignore exceptions on setting dates
            }

            mergedDefects.add(defectDataObj);
        }

        private DefectStateAttributeValueDataObj newAttribute(String name, String value){
            DefectStateAttributeValueDataObj attributeValueDataObj = new DefectStateAttributeValueDataObj();

            AttributeDefinitionIdDataObj attributeDefinitionId = new AttributeDefinitionIdDataObj();
            attributeDefinitionId.setName(name);
            attributeValueDataObj.setAttributeDefinitionId(attributeDefinitionId);

            AttributeValueIdDataObj attributeValueId = new AttributeValueIdDataObj();
            attributeValueId.setName(value);
            attributeValueDataObj.setAttributeValueId(attributeValueId);

            return attributeValueDataObj;
        }

        public void configureMainEvent(String eventTag, String eventDescription){
            this.mainEventTag = eventTag;
            this.mainEventDescription = eventDescription;
        }

        @Override
        public void updateDefectInstanceProperties(DefectInstanceIdDataObj defectInstanceId, List<PropertySpecDataObj> properties) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateStreamDefects(List<StreamDefectIdDataObj> streamDefectIds, DefectStateSpecDataObj defectStateSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<TriageHistoryDataObj> getTriageHistory(MergedDefectIdDataObj mergedDefectIdDataObj, List<TriageStoreIdDataObj> triageStoreIds) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<StreamDefectDataObj> getStreamDefects(List<MergedDefectIdDataObj> mergedDefectIdDataObjs, StreamDefectFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            List<StreamDefectDataObj> streamDefectDataObjs = new ArrayList<>();

            for (MergedDefectDataObj mergedDefectDataObj : mergedDefects) {
                StreamDefectDataObj streamDataObj = new StreamDefectDataObj();
                StreamDefectIdDataObj streamDefectIdDataObj = new StreamDefectIdDataObj();
                StreamIdDataObj streamIdDataObj = new StreamIdDataObj();

                streamDefectIdDataObj.setId(mergedDefectDataObj.getCid());

                streamDataObj.setId(streamDefectIdDataObj);
                streamDataObj.setCid(mergedDefectDataObj.getCid());
                streamDataObj.setCheckerName(mergedDefectDataObj.getCheckerName());
                streamDataObj.setDomain(mergedDefectDataObj.getDomain());

                streamIdDataObj.setName(mergedDefectDataObj.getLastDetectedStream());
                streamDataObj.setStreamId(streamIdDataObj);

                for (int i = 0 ; i < this.filePaths.size() ; i++){
                    DefectInstanceDataObj defectInstanceDataObj = new DefectInstanceDataObj();
                    defectInstanceDataObj.setCheckerName(mergedDefectDataObj.getCheckerName());
                    defectInstanceDataObj.setDomain(mergedDefectDataObj.getDomain());
                    final LocalizedValueDataObj impact = new LocalizedValueDataObj();
                    impact.setName(mergedDefectDataObj.getDisplayImpact());
                    impact.setDisplayName(mergedDefectDataObj.getDisplayImpact());
                    defectInstanceDataObj.setImpact(impact);
                    defectInstanceDataObj.setLongDescription("Defect Long Description");

                    EventDataObj event = new EventDataObj();
                    event.setLineNumber(i+1);
                    event.setEventTag(mainEventTag);
                    event.setEventDescription(mainEventDescription);
                    event.setMain(true);

                    FileIdDataObj fileIdDataObj = new FileIdDataObj();
                    fileIdDataObj.setFilePathname(filePaths.get(i));
                    event.setFileId(fileIdDataObj);

                    defectInstanceDataObj.getEvents().add(event);

                    streamDataObj.getDefectInstances().add(defectInstanceDataObj);
                }

                streamDefectDataObjs.add(streamDataObj);
            }

            return streamDefectDataObjs;
        }

        @Override
        public MergedDefectsPageDataObj getMergedDefectsForStreams(List<StreamIdDataObj> streamIds, MergedDefectFilterSpecDataObj filterSpec, PageSpecDataObj pageSpec, SnapshotScopeSpecDataObj snapshotScope) throws CovRemoteServiceException_Exception {
            MergedDefectsPageDataObj mergedDefectsPageDataObj = new MergedDefectsPageDataObj();

            final int totalRecords = mergedDefects.size();
            mergedDefectsPageDataObj.setTotalNumberOfRecords(totalRecords);

            int toIndex = pageSpec.getStartIndex() + pageSpec.getPageSize();
            if (toIndex > mergedDefects.size())
                toIndex = mergedDefects.size();

            List<MergedDefectIdDataObj> defectIds = mergedDefectIds.subList(pageSpec.getStartIndex(), toIndex);
            mergedDefectsPageDataObj.getMergedDefectIds().addAll(defectIds);

            List<MergedDefectDataObj> defects = mergedDefects.subList(pageSpec.getStartIndex(), toIndex);
            mergedDefectsPageDataObj.getMergedDefects().addAll(defects);

            return mergedDefectsPageDataObj;
        }

        @Override
        public List<DefectChangeDataObj> getMergedDefectHistory(MergedDefectIdDataObj mergedDefectIdDataObj, List<StreamIdDataObj> streamIds) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void updateTriageForCIDsInTriageStore(TriageStoreIdDataObj triageStore, List<MergedDefectIdDataObj> mergedDefectIdDataObjs, DefectStateSpecDataObj defectState) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<ProjectMetricsDataObj> getTrendRecordsForProject(ProjectIdDataObj projectId, ProjectTrendRecordFilterSpecDataObj filterSpec) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<ComponentMetricsDataObj> getComponentMetricsForProject(ProjectIdDataObj projectId, List<ComponentIdDataObj> componentIds) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public void createMergedDefect(String mergeKey, XMLGregorianCalendar dateOriginated, String externalPreventVersion, String internalPreventVersion, String checkerName, String domainName) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public FileContentsDataObj getFileContents(StreamIdDataObj streamId, FileIdDataObj fileId) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public MergedDefectsPageDataObj getMergedDefectsForSnapshotScope(ProjectIdDataObj projectId, SnapshotScopeDefectFilterSpecDataObj filterSpec, PageSpecDataObj pageSpec, SnapshotScopeSpecDataObj snapshotScope) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public List<DefectDetectionHistoryDataObj> getMergedDefectDetectionHistory(MergedDefectIdDataObj mergedDefectIdDataObj, List<StreamIdDataObj> streamIds) throws CovRemoteServiceException_Exception {
            throw new NotImplementedException();
        }

        @Override
        public MergedDefectsPageDataObj getMergedDefectsForProjectScope(ProjectIdDataObj projectId, ProjectScopeDefectFilterSpecDataObj filterSpec, PageSpecDataObj pageSpec) throws CovRemoteServiceException_Exception {
            MergedDefectsPageDataObj mergedDefectsPageDataObj = new MergedDefectsPageDataObj();

            final int totalRecords = mergedDefects.size();
            mergedDefectsPageDataObj.setTotalNumberOfRecords(totalRecords);

            int toIndex = pageSpec.getStartIndex() + pageSpec.getPageSize();
            if (toIndex > mergedDefects.size())
                toIndex = mergedDefects.size();

            List<MergedDefectIdDataObj> defectIds = mergedDefectIds.subList(pageSpec.getStartIndex(), toIndex);
            mergedDefectsPageDataObj.getMergedDefectIds().addAll(defectIds);

            List<MergedDefectDataObj> defects = mergedDefects.subList(pageSpec.getStartIndex(), toIndex);
            mergedDefectsPageDataObj.getMergedDefects().addAll(defects);

            return mergedDefectsPageDataObj;
        }
    }
}

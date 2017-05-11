package com.view26.ci.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.view26.ci.plugin.action.PushingResultAction;
import com.view26.ci.plugin.exception.SaveSettingException;
import com.view26.ci.plugin.model.Configuration;
import com.view26.ci.plugin.model.view26.Setting;
import com.view26.ci.plugin.utils.ClientRequestException;
import com.view26.ci.plugin.utils.HttpClientUtils;
import com.view26.ci.plugin.utils.JsonUtils;
import com.view26.ci.plugin.utils.ResponseEntity;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.tasks.Publisher;
import hudson.util.DescribableList;
import jenkins.model.Jenkins;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author trongle
 * @version 10/22/2015 11:05 PM trongle $
 * @since 1.0
 */
public class ConfigService {
  private static final Logger LOG = Logger.getLogger(ConfigService.class.getName());

  /**
   * Field name in json when get field setting
   */
  private static final String FIELD_ORIGIN_NAME = "original_name";
  /**
   * Origin value of environment field in field setting of testSuite in view26
   */
  private static final String FIELD_ENVIRONMENT_ORIGIN_NAME = "EnvironmentTestSuite";

  private ConfigService() {

  }

  /**
   * Validate view26 Url
   *
   * @param url
   * @return
   */
  public static Boolean validateview26Url(String url) {
    String versionUrl = String.format("%s%s", url, "/version");
    try {
      ResponseEntity entity = HttpClientUtils.get(versionUrl, null);
      if (!StringUtils.isEmpty(entity.getBody())) {
        JsonNode node = JsonUtils.readTree(entity.getBody());
        String name = JsonUtils.getText(node, "name");
        return "test-conductor".equalsIgnoreCase(name) || "${pom.name}".equalsIgnoreCase(name);
      }
    } catch (ClientRequestException e) {
      LOG.log(Level.WARNING, "Cannot connect to view26." + e.getMessage());
    }
    return false;
  }

  /**
   * Validate apiKey is valid in view26
   *
   * @param url
   * @param apiKey
   * @return
   */
  public static Boolean validateApiKey(String url, String apiKey) {
    return !StringUtils.isEmpty(OauthProvider.getAccessToken(url, apiKey));
  }

  /**
   * Build testSuite link to view26
   *
   * @param url
   * @param projectId
   * @param testSuiteId
   * @return
   */
  public static String formatTestSuiteLink(String url, Long projectId, Long testSuiteId) {
    return String.format("%s/p/%s/portal/project#tab=testexecution&object=2&id=%s",
      url, projectId, testSuiteId);
  }

  public static Configuration getPluginConfiguration(AbstractProject project) {
    DescribableList<Publisher, Descriptor<Publisher>> publishers = project.getPublishersList();

    for (int i = 0; i < publishers.size(); i++) {
      if (publishers.get(i) instanceof PushingResultAction) {
        return ((PushingResultAction) publishers.get(i)).getConfiguration();
      }
    }
    return null;
  }

  /**
   * @param view26Url
   * @param apiKey
   * @return
   */
  public static Object getProjects(String view26Url, String apiKey) {
    String url = String.format("%s/api/v3/projects?assigned=true", view26Url);
    try {
      ResponseEntity responseEntity = HttpClientUtils.get(url, OauthProvider.buildHeaders(view26Url, apiKey, null));
      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        return null;
      }
      return responseEntity.getBody();
    } catch (ClientRequestException e) {
      LOG.log(Level.WARNING, "Cannot get projects from: " + view26Url + "," + e.getMessage());
      return null;
    }
  }

  public static Object getProject(String view26Url, String accessToken, Long projectId) {
    String url = String.format("%s/api/v3/projects/%s", view26Url, projectId);
    try {
      ResponseEntity responseEntity = HttpClientUtils.get(url, OauthProvider.buildHeaders(accessToken, null));
      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        return null;
      }
      return responseEntity.getBody();
    } catch (ClientRequestException e) {
      LOG.log(Level.WARNING, "Cannot get project from: " + view26Url + "," + e.getMessage());
      return null;
    }
  }

  /**
   * @param view26Url
   * @param accessToken
   * @param projectId
   * @return
   */
  public static Object getReleases(String view26Url, String accessToken, Long projectId) {
    String url = String.format("%s/api/v3/projects/%s/releases?includeClosed=true", view26Url, projectId);
    try {
      ResponseEntity responseEntity = HttpClientUtils.get(url, OauthProvider.buildHeaders(accessToken, null));
      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        return null;
      }
      return responseEntity.getBody();
    } catch (ClientRequestException e) {
      LOG.log(Level.WARNING, "Cannot get release from: " + view26Url + "," + e.getMessage());
      return null;
    }
  }

  /**
   * Get environment values of testSuite
   *
   * @param view26Url
   * @param accessToken
   * @param projectId
   * @return
   */
  public static Object getEnvironments(String view26Url, String accessToken, Long projectId) {
    String url = String.format("%s/api/v3/projects/%s/settings/test-suites/fields?includeInactive=true", view26Url, projectId);
    try {
      ResponseEntity responseEntity = HttpClientUtils.get(url, OauthProvider.buildHeaders(accessToken, null));
      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        return null;
      }
      JSONArray fields = StringUtils.isEmpty(responseEntity.getBody()) ? null : JSONArray.fromObject(responseEntity.getBody());
      if (null == fields || fields.size() <= 0)
        return null;
      JSONObject envObject = null;
      for (int i = 0; i < fields.size(); i++) {
        JSONObject fieldObject = fields.getJSONObject(i);
        if (null != fieldObject && FIELD_ENVIRONMENT_ORIGIN_NAME.equalsIgnoreCase(fieldObject.getString(FIELD_ORIGIN_NAME))) {
          envObject = fieldObject;
          break;
        }
      }
      return envObject;
    } catch (ClientRequestException e) {
      LOG.log(Level.WARNING, "Cannot get environment values from: " + view26Url + "," + e.getMessage());
      return null;
    }
  }

  /**
   * Get saved configuration from view26
   *
   * @param setting
   * @param view26Url
   * @param accessToken
   * @return
   */
  public static Object getConfiguration(Setting setting, String view26Url, String accessToken) {
    Boolean getById = setting.getId() != null && setting.getId() > 0;

    String urlById = String.format("%s/api/v3/projects/%s/ci/%s", view26Url, setting.getProjectId(), setting.getId());
    String urlByProject = String.format("%s/api/v3/projects/%s/ci?server=%s&project=%s&type=jenkins&ciid=%s", view26Url, setting.getProjectId(),
      setting.getJenkinsServer(), HttpClientUtils.encode(setting.getJenkinsProjectName()), HttpClientUtils.encode(setting.getServerId()));

    try {
      Map<String, String> headers = OauthProvider.buildHeaders(accessToken, null);
      ResponseEntity responseEntity = HttpClientUtils.get(getById ? urlById : urlByProject, headers);

      if (HttpStatus.SC_OK != responseEntity.getStatusCode() && getById) {
        //in case not found by id, we try to get by project
        responseEntity = HttpClientUtils.get(urlByProject, headers);
      }

      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        LOG.log(Level.WARNING, String.format("Cannot get config from view26: %s, server: %s, project: %s, id: %s, serverId: %s, error: %s",
          view26Url, setting.getJenkinsServer(), setting.getJenkinsProjectName(), setting.getId(), setting.getServerId(), responseEntity.getBody()));
        return null;
      }
      LOG.info(String.format("Get config from view26: %s,%s", view26Url, responseEntity.getBody()));

      // if found by id, we check if setting belonging to jenkins
      // if ci_type equals => check serverId
      // if serverId empty => update
      // else if serverId equals => update otherwise=> create
      Setting res = JsonUtils.fromJson(responseEntity.getBody(), Setting.class);
      if (null != res && Constants.CI_TYPE.equalsIgnoreCase(res.getCiType())) {
        if (StringUtils.isEmpty(res.getServerId()) || setting.getServerId().equalsIgnoreCase(res.getServerId())) {
          return responseEntity.getBody();
        }
      }
      return null;
    } catch (ClientRequestException e) {
      return null;
    }
  }

  /**
   * Validate configuration from save or apply setting
   *
   * @param configuration
   * @param formData
   * @return
   */
  public static Configuration validateConfiguration(Configuration configuration, JSONObject formData) {
    //make id is 0 when name is empty, we get name from selectize field.

    configuration.setEnvironmentId(0);
    configuration.setEnvironmentName("");
    configuration.setProjectId(0);
    configuration.setProjectName("");
    configuration.setReleaseId(0);
    configuration.setId(0L);
    configuration.setModuleId(0);

    if (StringUtils.isEmpty(formData.getString("releaseName"))) {
      configuration.setReleaseName("");
    }
    return configuration;
  }

  /**
   * @param configuration
   * @return
   */
  public static Setting saveConfiguration(Configuration configuration)
    throws SaveSettingException {
    LOG.info("Save configuration to view26:" + configuration);
    try {
      //get access token
      String accessToken = "abc";

      //get saved setting from view26
      Setting setting = configuration.toSetting();
      setting.setServerId(getServerId(configuration.getJenkinsServerUrl()));
      Object savedObject = getConfiguration(setting, configuration.getUrl(), accessToken);
      Setting savedSetting = null == savedObject ? null : JsonUtils.fromJson(savedObject.toString(), Setting.class);

      //prepare for send request to view26
      Map<String, String> headers = null;
      setting.setCiType(Constants.CI_TYPE);
      ResponseEntity responseEntity = null;

      if (savedSetting != null && savedSetting.getId() > 0) {
        String url = String.format("%s/api/v3/projects/%s/ci/%s", configuration.getUrl(), configuration.getProjectId(), savedSetting.getId());
        responseEntity = HttpClientUtils.put(url, headers, JsonUtils.toJson(setting));
      } else {
        String url = String.format("%s/api/v3/projects/%s/ci", configuration.getUrl(), configuration.getProjectId());
        responseEntity = HttpClientUtils.post(url, headers, JsonUtils.toJson(setting));
      }

      if (HttpStatus.SC_OK != responseEntity.getStatusCode()) {
        LOG.log(Level.WARNING, String.format("Cannot save config to view26, statusCode:%s, error:%s",
          responseEntity.getStatusCode(), responseEntity.getBody()));
        throw new SaveSettingException(ConfigService.getErrorMessage(responseEntity.getBody()), responseEntity.getStatusCode());
      }
      Setting res = JsonUtils.fromJson(responseEntity.getBody(), Setting.class);
      LOG.info("Saved from view26:" + responseEntity.getBody());
      return res;
    } catch (ClientRequestException e) {
      throw new SaveSettingException(e.getMessage(), -1);
    }
  }

  /**
   * Get plugin version
   *
   * @return
   */
  public static String getBuildVersion() {
    Package pkg = ConfigService.class.getPackage();
    return StringUtils.isEmpty(pkg.getImplementationVersion()) ?
      pkg.getSpecificationVersion() : pkg.getImplementationVersion();
  }

  /**
   * Parse error message from {@link ResponseEntity}
   *
   * @param body
   * @return
   */
  public static String getErrorMessage(String body) {
    com.view26.ci.plugin.model.Error error = null;
    try {
      error = JsonUtils.parseJson(body, com.view26.ci.plugin.model.Error.class);
    } catch (IOException e) {
      error = null;
    }
    return null == error ? body : error.getMessage();
  }

  /**
   * Get jenkins instance id, when cannot get server id, we try to get mac address and port.
   * 1. get mac address:port
   * 2. if failed, we get server id
   * 3. if failed get by UUID
   *
   * @return
   */
  public static String getServerId(String jenkinsUrl) {
    String hmac = null;

    try {
      String macAddress = HttpClientUtils.getMacAddress();
      if (!StringUtils.isEmpty(macAddress)) {
        hmac = String.format("%s:%s", macAddress, HttpClientUtils.getPort(jenkinsUrl));
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Cannot get mac address:port:" + e.getMessage());
    }

    if (!StringUtils.isEmpty(hmac)) {
      return hmac;
    }
    try {
      hmac = Jenkins.getInstance().getLegacyInstanceId();
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Cannot get server id:" + e.getMessage());
    }
    return StringUtils.isEmpty(hmac) ? Constants.JENKINS_SERVER_ID_DEFAULT : hmac;
  }

}

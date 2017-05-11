package com.view26.ci.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import com.view26.ci.plugin.action.PushingResultAction;
import com.view26.ci.plugin.exception.SubmittedException;
import com.view26.ci.plugin.model.*;
import com.view26.ci.plugin.utils.ClientRequestException;
import com.view26.ci.plugin.utils.HttpClientUtils;
import com.view26.ci.plugin.utils.JsonUtils;
import com.view26.ci.plugin.utils.ResponseEntity;
import hudson.model.AbstractBuild;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @author anpham
 */
public class AutomationTestService {
//  private static final String AUTO_TEST_LOG_ENDPOINT = "%s/api/v3/projects/%s/test-runs/%s/auto-test-logs/ci/%s";
  private static final String AUTO_TEST_LOG_ENDPOINT = "%s/datasources/jenkins";
  private static final String API_SUBMIT_TASK_STATUS = "%s/api/v3/projects/queue-processing/%s";

  public static ResponseEntity push(String userName,String projectName, String buildNumber, String buildPath, List<AutomationTestResult> testResults, Configuration configuration, Map<String, String> headers)
    throws SubmittedException {

    if (testResults.size() <= 0)
      return null;

    SubmittedData wrapper = new SubmittedData();
      wrapper.setData(userName, projectName, buildNumber, buildPath, testResults, configuration);

    String url = String.format(AUTO_TEST_LOG_ENDPOINT, configuration.getUrl());

    ResponseEntity responseEntity = null;
    try {
      responseEntity = HttpClientUtils.post(url, headers, JsonUtils.toJson(wrapper));
    } catch (ClientRequestException e) {
      throw new SubmittedException(e.getMessage(), null == responseEntity ? 0 : responseEntity.getStatusCode());
    }
    return responseEntity;
  }

  /**
   * @param configuration
   * @param taskId
   * @param headers
   * @return
   * @throws ClientRequestException
   */
  public static ResponseEntity getTaskStatus(Configuration configuration, long taskId, Map<String, String> headers)
    throws ClientRequestException {
    String url = String.format(API_SUBMIT_TASK_STATUS, configuration.getUrl(), taskId);
    ResponseEntity responseEntity = null;
    try {
      responseEntity = HttpClientUtils.get(url, headers);
    } catch (ClientRequestException e) {
      throw e;
    }

    return responseEntity;
  }
}

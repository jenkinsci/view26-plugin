package com.view26.ci.plugin;

import com.view26.ci.plugin.exception.SubmittedException;
import com.view26.ci.plugin.model.*;
import com.view26.ci.plugin.utils.ClientRequestException;
import com.view26.ci.plugin.utils.HttpClientUtils;
import com.view26.ci.plugin.utils.JsonUtils;
import com.view26.ci.plugin.utils.ResponseEntity;

import java.util.*;


/**
 * @author aneeshia
 */
public class AutomationTestService {
  private static final String AUTO_TEST_LOG_ENDPOINT = "%s/datasources/jenkins";

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
}

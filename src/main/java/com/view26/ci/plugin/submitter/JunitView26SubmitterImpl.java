package com.view26.ci.plugin.submitter;

import com.view26.ci.plugin.AutomationTestService;
import com.view26.ci.plugin.ConfigService;
import com.view26.ci.plugin.Constants;
import com.view26.ci.plugin.OauthProvider;
import com.view26.ci.plugin.exception.StoreResultException;
import com.view26.ci.plugin.exception.SubmittedException;
import com.view26.ci.plugin.model.AutomationTestResponse;
import com.view26.ci.plugin.model.Configuration;
import com.view26.ci.plugin.model.SubmittedResult;
import com.view26.ci.plugin.model.view26.SubmittedTask;
import com.view26.ci.plugin.store.StoreResultService;
import com.view26.ci.plugin.store.StoreResultServiceImpl;
import com.view26.ci.plugin.utils.ClientRequestException;
import com.view26.ci.plugin.utils.JsonUtils;
import com.view26.ci.plugin.utils.LoggerUtils;
import com.view26.ci.plugin.utils.ResponseEntity;
import hudson.model.AbstractBuild;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aneeshia
 */
public class JunitView26SubmitterImpl implements JunitSubmitter {
  private static final Logger LOG = Logger.getLogger(com.view26.ci.plugin.submitter.JunitView26SubmitterImpl.class.getName());
  private StoreResultService storeResultService = new StoreResultServiceImpl();

  @Override public JunitSubmitterResult submit(JunitSubmitterRequest request) throws Exception {
    Map<String,String> tempHeaderMap = new HashMap<>();

    ResponseEntity responseEntity = AutomationTestService.push(request.getUserName(), request.getProjectName(), request.getBuildNumber(), request.getBuildPath(),
            request.getTestResults(), request.getConfiguration(), tempHeaderMap);

    JunitSubmitterResult result = null;

    Boolean isSubmitSuccess = ((responseEntity != null)&&(responseEntity.getStatusCode() == HttpStatus.SC_OK) ? true : false);
    String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

      result = new JunitSubmitterResult()
      .setNumberOfTestResult(request.getTestResults().size())
      .setTestSuiteName(request.getProjectName()+"_"+timeStamp)
      .setProjectName(request.getProjectName())
      .setSubmittedStatus(isSubmitSuccess ? JunitSubmitterResult.STATUS_SUCCESS : JunitSubmitterResult.STATUS_FAILED);


    if(request.getTestResults().size() == 0){
      result.setSubmittedStatus(JunitSubmitterResult.STATUS_SKIPPED);
    }
    return result;
  }

  @Override public SubmittedResult storeSubmittedResult(AbstractBuild build, JunitSubmitterResult result)
    throws StoreResultException {
    //get saved configuration
    Configuration configuration = ConfigService.getPluginConfiguration(build.getProject());
    String view26Url = configuration == null ? "" : configuration.getUrl();
    Long projectId = configuration == null ? 0L : configuration.getProjectId();

    SubmittedResult submitResult = new SubmittedResult()
      .setUrl(view26Url)
      .setProjectName(result.getProjectName())
      .setBuildNumber(build.getNumber())
      .setStatusBuild(build.getResult().toString())
      .setTestSuiteName(result.getTestSuiteName())
      .setSubmitStatus(result.getSubmittedStatus())
      .setNumberTestResult(result.getNumberOfTestResult());
    try {
      storeResultService.store(build.getProject(), submitResult);
      return submitResult;
    } catch (Exception e) {
      LOG.log(Level.WARNING, e.getMessage(), e);
      throw new StoreResultException("Cannot store result." + e.getMessage(), e);
    }
  }
}

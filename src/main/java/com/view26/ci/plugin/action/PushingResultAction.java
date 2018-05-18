package com.view26.ci.plugin.action;

import com.view26.ci.plugin.ConfigService;
import com.view26.ci.plugin.ResourceBundle;
import com.view26.ci.plugin.exception.StoreResultException;
import com.view26.ci.plugin.exception.SubmittedException;
import com.view26.ci.plugin.model.AutomationTestResult;
import com.view26.ci.plugin.model.Configuration;
import com.view26.ci.plugin.model.view26.Setting;
import com.view26.ci.plugin.parse.JunitTestResultParser;
import com.view26.ci.plugin.parse.ParseRequest;
import com.view26.ci.plugin.submitter.JunitSubmitter;
import com.view26.ci.plugin.submitter.JunitSubmitterRequest;
import com.view26.ci.plugin.submitter.JunitSubmitterResult;
import com.view26.ci.plugin.submitter.JunitView26SubmitterImpl;
import com.view26.ci.plugin.utils.HttpClientUtils;
import com.view26.ci.plugin.utils.JsonUtils;
import com.view26.ci.plugin.utils.LoggerUtils;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aneeshia
 */
public class PushingResultAction extends Notifier {
  private static final Logger LOG = Logger.getLogger(PushingResultAction.class.getName());
  private Configuration configuration;
  public PushingResultAction(Configuration configuration) {
    this.configuration = configuration;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  @Override
  public BuildStepMonitor getRequiredMonitorService() {
    return BuildStepMonitor.NONE;
  }

  @Override
  public DescriptorImpl getDescriptor() {
    return (DescriptorImpl) super.getDescriptor();
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
    throws InterruptedException, IOException {
    PrintStream logger = listener.getLogger();
    JunitSubmitter junitSubmitter = new JunitView26SubmitterImpl();
    if (Result.ABORTED.equals(build.getResult())) {
      LoggerUtils.formatWarn(logger, "Abort build action.");
      storeWhenNotSuccess(junitSubmitter, build, logger, JunitSubmitterResult.STATUS_CANCELED);
      return true;
    }
    showInfo(logger);
    if (!validateConfig(configuration)) {
      LoggerUtils.formatWarn(logger, "Invalid configuration to View26, reject submit test results.");
      storeWhenNotSuccess(junitSubmitter, build, logger, JunitSubmitterResult.STATUS_FAILED);
      return true;
    }
    List<AutomationTestResult> automationTestResults = readTestResults(build, launcher, listener, logger, junitSubmitter);
    if (automationTestResults.isEmpty())
      return true;

    JunitSubmitterResult result = submitTestResult(build, listener, junitSubmitter, automationTestResults);
    if (null == result) {
      //if have no test result, we do not break build flow
      return true;
    }
    storeResult(build, junitSubmitter, result, logger);
    LoggerUtils.formatHR(logger);
    return true;
  }

  private Boolean storeWhenNotSuccess(JunitSubmitter junitSubmitter, AbstractBuild build, PrintStream logger, String status) {
    try {
      String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

      junitSubmitter.storeSubmittedResult(build, new JunitSubmitterResult()
        .setTestSuiteName(build.getEnvironment().get("JOB_NAME")+"_"+timeStamp)
        .setNumberOfTestResult(0)
        .setSubmittedStatus(status));
    } catch (StoreResultException e) {
      LoggerUtils.formatError(logger, e.getMessage());
      e.printStackTrace(logger);
    } finally {
      return true;
    }
  }

  private void showInfo(PrintStream logger) {
    LoggerUtils.formatInfo(logger, "");
    LoggerUtils.formatHR(logger);
    LoggerUtils.formatInfo(logger, ResourceBundle.DISPLAY_NAME);
    LoggerUtils.formatInfo(logger, String.format("Build Version: %s", ConfigService.getBuildVersion()));
    LoggerUtils.formatHR(logger);
    LoggerUtils.formatInfo(logger, "Submit Junit test result to view26 at: %s", configuration.getUrl());
    LoggerUtils.formatInfo(logger, "With project: %s (id=%s).", configuration.getProjectName(), configuration.getProjectId());
    LoggerUtils.formatInfo(logger, "With release: %s (id=%s).", configuration.getReleaseName(), configuration.getReleaseId());
    if (configuration.getEnvironmentId() > 0) {
      LoggerUtils.formatInfo(logger, "With environment: %s (id=%s).", configuration.getEnvironmentName(), configuration.getEnvironmentId());
    } else {
      LoggerUtils.formatInfo(logger, "With no environment.");
    }
    LoggerUtils.formatInfo(logger, "");
  }

  private Boolean validateConfig(Configuration configuration) {
    return configuration != null &&
      !StringUtils.isEmpty(configuration.getUrl()) &&
      !StringUtils.isEmpty(configuration.getAppSecretKey()) &&
            !StringUtils.isEmpty(configuration.getReleaseName());
  }

  private List<AutomationTestResult> readTestResults(AbstractBuild build, Launcher launcher, BuildListener listener, PrintStream logger, JunitSubmitter junitSubmitter) {
    List<AutomationTestResult> automationTestResults;
    long start = System.currentTimeMillis();
    LoggerUtils.formatHR(logger);
    try {
      automationTestResults = JunitTestResultParser.parse(new ParseRequest()
        .setBuild(build)
        .setConfiguration(configuration)
        .setLauncher(launcher)
        .setListener(listener));
    } catch (Exception e) {
      LOG.log(Level.WARNING, e.getMessage());
      LoggerUtils.formatError(logger, e.getMessage());
      automationTestResults = Collections.emptyList();
    }
    if (automationTestResults.isEmpty()) {
      LoggerUtils.formatWarn(logger, "No JUnit test result found.");
      storeWhenNotSuccess(junitSubmitter, build, logger, JunitSubmitterResult.STATUS_SKIPPED);
      LoggerUtils.formatHR(logger);
      return Collections.emptyList();
    }
    LoggerUtils.formatInfo(logger, "JUnit test result found: %s, time elapsed: %s", automationTestResults.size(), LoggerUtils.elapsedTime(start));
    LoggerUtils.formatHR(logger);
    LoggerUtils.formatInfo(logger, "");
    return automationTestResults;
  }

  private JunitSubmitterResult submitTestResult(AbstractBuild build, BuildListener listener,
    JunitSubmitter junitSubmitter, List<AutomationTestResult> automationTestResults) {
    PrintStream logger = listener.getLogger();
    JunitSubmitterResult result = null;
    JunitSubmitterRequest tempReq = null;
    LoggerUtils.formatInfo(logger, "Begin submit test results to view26 at: " + JsonUtils.getCurrentDateString());
    long start = System.currentTimeMillis();
    try {

      tempReq =new JunitSubmitterRequest()
                .setConfiguration(configuration)
                .setTestResults(automationTestResults)
                .setBuildNumber(build.getNumber() + "")
                .setBuildPath(build.getUrl())
                .setProjectName(build.getEnvironment().get("JOB_NAME")) // For accessing Project name/ Job name

                .setListener(listener);
        try{
            String testerName = configuration.getTesterName();
            if(StringUtils.isNotBlank(testerName))
                tempReq.setUserName(testerName);
            else
                tempReq.setUserName(((Cause.UserIdCause) build.getCause(Cause.UserIdCause.class)).getUserName()); // For accessing username
        }catch (Exception e){
            tempReq.setUserName("anonymous");
        }
        result = junitSubmitter.submit(tempReq);

    } catch (SubmittedException e) {
      LoggerUtils.formatError(logger, "Cannot submit test results to View26:");
      LoggerUtils.formatError(logger, "   status code: " + e.getStatus());
      LoggerUtils.formatError(logger, "   error: " + e.getMessage());
    } catch (Exception e) {
      LoggerUtils.formatError(logger, "Cannot submit test results to View26:");
      LoggerUtils.formatError(logger, "   error: " + e.getMessage());
    } finally {

      Boolean isSuccess = (null != result) && JunitSubmitterResult.STATUS_SUCCESS.equals(result.getSubmittedStatus());
      LoggerUtils.formatHR(logger);
      LoggerUtils.formatInfo(logger, isSuccess ? "SUBMIT SUCCESS" : "SUBMIT FAILED");
      LoggerUtils.formatHR(logger);
      LoggerUtils.formatInfo(logger, "Time elapsed: %s", LoggerUtils.elapsedTime(start));
      LoggerUtils.formatInfo(logger, "End submit test results to view26 at: %s", JsonUtils.getCurrentDateString());
      LoggerUtils.formatInfo(logger, "");

    }

    return result;
  }

  private void storeResult(AbstractBuild build, JunitSubmitter junitSubmitter, JunitSubmitterResult result, PrintStream logger) {
    try {
      junitSubmitter.storeSubmittedResult(build, result);
      LoggerUtils.formatInfo(logger, "Store submission result to workspace success.");
    } catch (Exception e) {
      LoggerUtils.formatError(logger, "Cannot store submission result: " + e.getMessage());
      e.printStackTrace(logger);
    }
    LoggerUtils.formatInfo(logger, "");
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

    public DescriptorImpl() {
      super(PushingResultAction.class);
      load();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> jobType) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return ResourceBundle.DISPLAY_NAME;
    }

    @Override
    public String getHelpFile() {
      return ResourceBundle.CONFIG_HELP_FILE;
    }

    @Override
    public Publisher newInstance(StaplerRequest req, JSONObject formData) throws hudson.model.Descriptor.FormException {
      String testerName = "";
      try {
        testerName = User.current().getDisplayName();
      }catch(Exception e){}
      Configuration configuration = req.bindParameters(Configuration.class, "config.");
      configuration.setJenkinsServerUrl(HttpClientUtils.getServerUrl(req));
      configuration.setReadFromJenkins(true);
      if (!StringUtils.isBlank(testerName))
          configuration.setTesterName(testerName);
      configuration.setEachMethodAsTestCase(formData.getBoolean("eachMethodAsTestCase"));
      configuration = ConfigService.validateConfiguration(configuration, formData);

      if (!StringUtils.isEmpty(configuration.getUrl())) {
        Setting setting = null;
        String tempResponseBody = "{\"id\": 938,\"ci_server\": \"http://http://localhost:8080/jenkins\",\"ci_project\": \"poc new api\",\"project_id\": 38914,\"release_id\": 149229,\"module_id\": 2178693,\"environment_id\": 0,\"consolidate_test_run\": false,\"ci_type\": \"jenkins\",\"ciid\": \"ef4773f0470bf31b687b78a366daca88\"}";
        setting = JsonUtils.fromJson(tempResponseBody, Setting.class);
        configuration.setModuleId(setting.getModuleId());
        configuration.setId(setting.getId());
      }
      return new PushingResultAction(configuration);
    }

    public FormValidation doCheckAppSecretKey(@QueryParameter String value, @QueryParameter("config.url") final String url, @AncestorInPath AbstractProject project)
            throws IOException, ServletException {
      if (StringUtils.isEmpty(value))
        return FormValidation.error(ResourceBundle.MSG_INVALID_API_KEY);
      if (!ConfigService.validateApiKey(url, value))
        return FormValidation.error(ResourceBundle.MSG_INVALID_API_KEY);
      return FormValidation.ok();
    }

    public FormValidation doCheckReleaseName(@QueryParameter String value)
      throws IOException, ServletException {
      if (StringUtils.isBlank(value))
        return FormValidation.error(ResourceBundle.MSG_INVALID_RELEASE);
      return FormValidation.ok();
    }

    public FormValidation doCheckResultPattern(@QueryParameter String value)
      throws IOException, ServletException {
      if (StringUtils.isEmpty(value))
        return FormValidation.error("Please set a valid pattern");
      return FormValidation.ok();
    }

  }
}

package com.view26.ci.plugin.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import hudson.tasks.junit.CaseResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author aneeshia
 */
public class AutomationTestResult {
  private String name;
  private String status;
  private String priority = "Medium";
  private Float duration = 0.0f;

  @JsonProperty("exe_start_date")
  private Date executedStartDate;

  @JsonProperty("exe_end_date")
  private Date executedEndDate;

  @JsonProperty("automation_content")
  private String automationContent;

  @JsonProperty("release_name")
  private String releaseName;

  @JsonProperty("ci_tool")
  private String ciTool = "jenkins";

  @JsonProperty("exec_type")
  private String executionType = "Functional";

  @JsonProperty("test_type")
  private String testType = "Automation";

  @JsonProperty("build_number")
  private String buildNumber;

  @JsonProperty("build_path")
  private String buildPath;

  @JsonProperty("test_set")
  private String testSet;

  @JsonProperty("assignee")
  private String assignee = "admin";

  @JsonProperty("test_step_logs")
  private List<AutomationTestLog> testLogs;
  private List<AutomationAttachment> attachments;
  /**
   * total failed testStep
   */
  private int totalFailedTestSteps = 0;
  /**
   * total skipped testStep
   */
  private int totalSkippedTestSteps = 0;
  /**
   * total success testStep
   */
  private int totalSuccessTestSteps = 0;


  public AutomationTestResult() {
    testLogs = new ArrayList<>();
  }

  public String getTestSet() {
    return testSet;
  }
  public void setTestSet(String testSet) {
    this.testSet = testSet;
  }

  public String getBuildNumber() {
    return buildNumber;
  }
  public void setBuildNumber(String buildNumber) {
    this.buildNumber = buildNumber;
  }

  public String getBuildPath() {
    return buildPath;
  }
  public void setBuildPath(String buildPath) {
    this.buildPath = buildPath;
  }

  public String getAssignee() {
    return assignee;
  }
  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }

  public String getReleaseName() {
    return releaseName;
  }
  public void setReleaseName(String releaseName) {
    this.releaseName = releaseName;
  }

  public String getPriority() {
    return priority;
  }
  public void setPriority(String priority) {
    this.priority = priority;
  }

  public Float getDuration() {
    return duration;
  }
  public void setDuration(Float duration) {
    this.duration = duration;
  }

  public String getCiTool() {
    return ciTool;
  }
  public void setCiTool(String ciTool) {
    this.ciTool = ciTool;
  }

  public String getExecutionType() {
    return executionType;
  }
  public void setExecutionType(String executionType) {
    this.executionType = executionType;
  }

  public String getTestType() {
    return testType;
  }
  public void setTestType(String testType) {
    this.testType = testType;
  }


  public Date getExecutedStartDate() {
    return executedStartDate;
  }

  public void setExecutedStartDate(Date executedStartDate) {
    this.executedStartDate = executedStartDate;
  }

  public Date getExecutedEndDate() {
    return executedEndDate;
  }

  public void setExecutedEndDate(Date executedEndDate) {
    this.executedEndDate = executedEndDate;
  }

  public String getAutomationContent() {
    return automationContent;
  }

  public void setAutomationContent(String automationContent) {
    this.automationContent = automationContent;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public List<AutomationTestLog> getTestLogs() {
    return testLogs;
  }

  public void setTestLogs(List<AutomationTestLog> testLogs) {
    this.testLogs = testLogs;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<AutomationAttachment> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<AutomationAttachment> attachments) {
    this.attachments = attachments;
  }

  /**
   * Add testLog and resolve status of testResult
   *
   * @param automationTestLog
   * @return
   * @see "http://javadoc.jenkins-ci.org/hudson/tasks/junit/CaseResult.Status.html"
   */
  public AutomationTestLog addTestLog(AutomationTestLog automationTestLog) {
    testLogs.add(automationTestLog);
    String status = automationTestLog.getStatus();
    //regression the same as failed
    if (CaseResult.Status.FAILED.toString().equalsIgnoreCase(status) ||
      CaseResult.Status.REGRESSION.toString().equalsIgnoreCase(status)) {
      totalFailedTestSteps += 1;
    }
    if (CaseResult.Status.SKIPPED.toString().equalsIgnoreCase(status)) {
      totalSkippedTestSteps += 1;
    }
    //fixed the same as passed
    if (CaseResult.Status.PASSED.toString().equalsIgnoreCase(status) ||
      CaseResult.Status.FIXED.toString().equalsIgnoreCase(status)) {
      totalSuccessTestSteps += 1;
    }
    this.setStatus(status);
    if (totalFailedTestSteps >= 1) {
      //if there is one failed testStep, we mark testLog as failed
      this.setStatus(CaseResult.Status.FAILED.toString());
    } else {
      if (totalSuccessTestSteps >= 1) {
        //if there is one success testStep, we mark testLog as success
        this.setStatus(CaseResult.Status.PASSED.toString());
      } else if (totalSkippedTestSteps >= 1) {
        //if all of test is skipped, we mark testLog as skipped
        this.setStatus(CaseResult.Status.SKIPPED.toString());
      }
    }
    return automationTestLog;
  }
}

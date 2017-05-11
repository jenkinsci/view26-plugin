package com.view26.ci.plugin.submitter;

/**
 * @author trongle
 * @version 10/21/2015 2:37 PM trongle $
 * @since 1.0
 */
public class JunitSubmitterResult {
  public static final String STATUS_SUCCESS = "Completed";
  public static final String STATUS_CANCELED = "Canceled";
  public static final String STATUS_FAILED = "Failed";
  public static final String STATUS_SKIPPED = "Skipped";
  private String testSuiteName;
  private String projectName;
  private String submittedStatus;
  private Integer numberOfTestResult;

  public String getTestSuiteName() {
    return testSuiteName;
  }

  public JunitSubmitterResult setTestSuiteName(String testSuiteName) {
    this.testSuiteName = testSuiteName;
    return this;
  }
  public String getProjectName() {
    return projectName;
  }

  public JunitSubmitterResult setProjectName(String projectName) {
    this.projectName = projectName;
    return this;
  }

  public String getSubmittedStatus() {
    return submittedStatus;
  }

  public JunitSubmitterResult setSubmittedStatus(String submittedStatus) {
    this.submittedStatus = submittedStatus;
    return this;
  }

  public Integer getNumberOfTestResult() {
    return numberOfTestResult;
  }

  public JunitSubmitterResult setNumberOfTestResult(Integer numberOfTestResult) {
    this.numberOfTestResult = numberOfTestResult;
    return this;
  }
}

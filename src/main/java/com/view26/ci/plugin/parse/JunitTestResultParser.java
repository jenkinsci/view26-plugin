package com.view26.ci.plugin.parse;

import com.view26.ci.plugin.model.AutomationTestResult;
import com.view26.ci.plugin.utils.LoggerUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * @author aneeshia
 */
public class JunitTestResultParser {
  public static List<AutomationTestResult> parse(ParseRequest request) throws Exception {
    TestResultParser parser;
    Boolean readFromJenkins = request.getConfiguration().getReadFromJenkins();
    if (Boolean.FALSE.equals(readFromJenkins)) {
      LoggerUtils.formatInfo(request.getListener().getLogger(), "Read test results from jenkins.");
      //read result from testResult action from Jenkins
      parser = new PublishResultParser();
    } else {
      //scan with configured pattern or scan all
      if (!StringUtils.isBlank(request.getConfiguration().getResultPattern())) {
        parser = new PatternScanParser();
      } else {
        parser = new AutoScanParser();
      }
    }
    return parser.parse(request);
  }
}

package com.view26.ci.plugin.parse;

import java.util.List;

import com.view26.ci.plugin.model.AutomationTestResult;

/**
 * @author anpham
 *
 */
public interface TestResultParser {
  List<AutomationTestResult> parse(ParseRequest request) throws Exception;
}

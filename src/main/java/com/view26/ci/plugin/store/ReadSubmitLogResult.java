package com.view26.ci.plugin.store;

import com.view26.ci.plugin.model.SubmittedResult;

import java.util.Map;

/**
 * @author aneeshia
 */
public class ReadSubmitLogResult {
  private long total;
  private Map<Integer, SubmittedResult> results;

  public long getTotal() {
    return total;
  }

  public ReadSubmitLogResult setTotal(long total) {
    this.total = total;
    return this;
  }

  public Map<Integer, SubmittedResult> getResults() {
    return results;
  }

  public ReadSubmitLogResult setResults(Map<Integer, SubmittedResult> results) {
    this.results = results;
    return this;
  }
}

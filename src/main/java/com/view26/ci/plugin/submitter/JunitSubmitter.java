package com.view26.ci.plugin.submitter;

import com.view26.ci.plugin.exception.StoreResultException;
import com.view26.ci.plugin.model.SubmittedResult;
import hudson.model.AbstractBuild;
/**
 * @author aneeshia
 */
public interface JunitSubmitter {
  /**
   * Submit test result to view26
   *
   * @param junitSubmitterRequest
   * @return
   * @throws Exception
   */
  JunitSubmitterResult submit(JunitSubmitterRequest junitSubmitterRequest) throws Exception;

  /**
   * @param build
   * @param result
   * @return
   * @throws StoreResultException
   */
  SubmittedResult storeSubmittedResult(AbstractBuild build, JunitSubmitterResult result)
    throws StoreResultException;
}

package com.view26.ci.plugin.submitter;

import com.view26.ci.plugin.exception.StoreResultException;
import com.view26.ci.plugin.model.SubmittedResult;
import hudson.model.AbstractBuild;
/**
 * @author trongle
 * @version 10/21/2015 2:37 PM trongle $
 * @since 1.0
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

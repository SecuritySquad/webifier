package de.securitysquad.webifier.core;

import javax.servlet.http.HttpSession;

/**
 * Created by samuel on 08.11.16.
 */
public interface WebifierTestResultListener {
    void onTestResult(HttpSession session, String launchId, TesterResult result);

    void onError(HttpSession session, String line);
}

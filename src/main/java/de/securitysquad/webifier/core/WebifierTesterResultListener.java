package de.securitysquad.webifier.core;

import javax.servlet.http.HttpSession;

/**
 * Created by samuel on 08.11.16.
 */
public interface WebifierTesterResultListener {
    void onWaitingPositionChanged(HttpSession session, String id, int position);

    void onStarted(HttpSession session, String id);

    void onTestResult(HttpSession session, String launchId, WebifierTesterResult result);

    void onError(HttpSession session, String line);

    void onFinished(HttpSession session, String id);
}

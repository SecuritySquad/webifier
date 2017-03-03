package de.securitysquad.webifier.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.util.Assert;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Created by samuel on 10.02.17.
 */
public class WebifierTester {
    private final long creationIndex;
    private final String id;
    private final String command;
    private final HttpSession session;
    private final WebifierTesterResultListener listener;
    private final int timeoutInMinutes;
    private WebifierTesterState state;
    private Process testerProcess;
    private Thread testerThread;
    private Thread inputThread;
    private Thread errorThread;
    private String url;

    public WebifierTester(String id, String url, String command, HttpSession session, WebifierTesterResultListener listener, int timeoutInMinutes) {
        creationIndex = System.currentTimeMillis();
        Assert.notNull(id, "id must not be null!");
        Assert.notNull(url, "url must not be null!");
        Assert.notNull(command, "command must not be null!");
        this.id = id;
        this.url = url;
        this.command = command;
        this.session = session;
        this.listener = listener;
        this.timeoutInMinutes = timeoutInMinutes;
        state = WebifierTesterState.WAITING;
    }

    public void launch() {
        state = WebifierTesterState.RUNNING;
        fireStartedEvent();
        testerThread = new Thread(() -> {
            try {
                System.out.println(command);
                testerProcess = Runtime.getRuntime().exec(command);
                listenForInput(testerProcess.getInputStream());
                listenForError(testerProcess.getErrorStream());
                testerProcess.waitFor(timeoutInMinutes, TimeUnit.MINUTES);
                state = (testerProcess.exitValue() == 0) ? WebifierTesterState.FINISHED : WebifierTesterState.ERROR;
            } catch (IOException | InterruptedException e) {
                state = WebifierTesterState.ERROR;
                fireErrorEvent(ExceptionUtils.getStackTrace(e));
            }
        });
        testerThread.start();
    }

    public void setWaitingPosition(int waitingPosition) {
        fireWaitingPositionEvent(waitingPosition);
    }

    private void listenForInput(InputStream inputStream) {
        inputThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            ObjectMapper mapper = new ObjectMapper();
            try {
                String line;
                while ((line = br.readLine()) != null && !inputThread.isInterrupted()) {
                    System.out.println(line);
                    try {
                        WebifierTesterResult result = mapper.readValue(line, WebifierTesterResult.class);
                        result.setContent(line);
                        fireTestResultEvent(result);
                    } catch (IOException e) {
                        // no json line found
                    }
                }
            } catch (IOException e) {
                state = WebifierTesterState.ERROR;
                fireErrorEvent(ExceptionUtils.getStackTrace(e));
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void listenForError(InputStream errorStream) {
        errorThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line;
                while ((line = br.readLine()) != null && !errorThread.isInterrupted()) {
                    fireErrorEvent(line);
                }
            } catch (IOException e) {
                state = WebifierTesterState.ERROR;
                fireErrorEvent(ExceptionUtils.getStackTrace(e));
            }
        });
        errorThread.setDaemon(true);
        errorThread.start();
    }

    public WebifierTesterState getState() {
        return state;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public long getCreationIndex() {
        return creationIndex;
    }

    public void exit() {
        if (testerProcess.isAlive()) {
            testerProcess.destroy();
        }
        if (testerThread.isAlive()) {
            testerThread.interrupt();
        }
        if (inputThread.isAlive()) {
            inputThread.interrupt();
        }
        if (errorThread.isAlive()) {
            errorThread.interrupt();
        }
        fireFinishedEvent();
    }

    private void fireWaitingPositionEvent(int waitingPosition) {
        if (listener != null && session != null)
            listener.onWaitingPositionChanged(session, id, waitingPosition);
    }

    private void fireStartedEvent() {
        if (listener != null && session != null)
            listener.onStarted(session, id);
    }

    private void fireErrorEvent(String error) {
        if (listener != null && session != null)
            listener.onError(session, error);
    }

    private void fireTestResultEvent(WebifierTesterResult result) {
        if (listener != null && session != null)
            listener.onTestResult(session, result.getLaunchId(), result);
    }

    private void fireFinishedEvent() {
        if (listener != null && session != null)
            listener.onFinished(session, id);
    }
}

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

    public WebifierTester(String id, String command, HttpSession session, WebifierTesterResultListener listener, int timeoutInMinutes) {
        creationIndex = System.currentTimeMillis();
        Assert.notNull(id, "id must not be null!");
        Assert.notNull(command, "command must not be null!");
        Assert.notNull(session, "session must not be null!");
        Assert.notNull(listener, "listener must not be null!");
        this.id = id;
        this.command = command;
        this.session = session;
        this.listener = listener;
        this.timeoutInMinutes = timeoutInMinutes;
        state = WebifierTesterState.WAITING;
    }

    public void launch() {
        state = WebifierTesterState.RUNNING;
        listener.onStarted(session, id);
        testerThread = new Thread(() -> {
            try {
                System.out.println(command);
                testerProcess = Runtime.getRuntime().exec(command);
                listenForInput(testerProcess.getInputStream(), session, listener);
                listenForError(testerProcess.getErrorStream(), session, listener);
                testerProcess.waitFor(timeoutInMinutes, TimeUnit.MINUTES);
                state = (testerProcess.exitValue() == 0) ? WebifierTesterState.FINISHED : WebifierTesterState.ERROR;
            } catch (IOException | InterruptedException e) {
                state = WebifierTesterState.ERROR;
                listener.onError(session, ExceptionUtils.getStackTrace(e));
            }
        });
        testerThread.start();
    }

    public void setWaitingPosition(int waitingPosition) {
        listener.onWaitingPositionChanged(session, id, waitingPosition);
    }

    private void listenForInput(InputStream inputStream, HttpSession session, WebifierTesterResultListener listener) {
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
                        listener.onTestResult(session, result.getLaunchId(), result);
                    } catch (IOException e) {
                        // no json line found
                    }
                }
            } catch (IOException e) {
                state = WebifierTesterState.ERROR;
                listener.onError(session, ExceptionUtils.getStackTrace(e));
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();
    }

    private void listenForError(InputStream errorStream, HttpSession session, WebifierTesterResultListener listener) {
        errorThread = new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line;
                while ((line = br.readLine()) != null && !errorThread.isInterrupted()) {
                    listener.onError(session, line);
                }
            } catch (IOException e) {
                state = WebifierTesterState.ERROR;
                listener.onError(session, ExceptionUtils.getStackTrace(e));
            }
        });
        errorThread.setDaemon(true);
        errorThread.start();
    }

    public WebifierTesterState getState() {
        return state;
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
        listener.onFinished(session, id);
    }
}

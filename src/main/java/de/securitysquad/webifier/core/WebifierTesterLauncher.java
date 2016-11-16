package de.securitysquad.webifier.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.UUID;

/**
 * Created by samuel on 08.11.16.
 */
@Component
public class WebifierTesterLauncher {
    @Value("${tester.command}")
    private String testerCommand;

    public String launch(URL url, HttpSession session, WebifierTestResultListener listener) {
        String id = UUID.randomUUID().toString();
        String command = testerCommand.replace("#URL", url.toString()).replace("#ID", id);
        System.out.println(command);
        startTester(command, session, listener);
        return id;
    }

    private void startTester(String command, HttpSession session, WebifierTestResultListener listener) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            listenForInput(process.getInputStream(), session, listener);
            listenForError(process.getErrorStream(), session, listener);
        } catch (IOException e) {
            listener.onError(session, ExceptionUtils.getStackTrace(e));
        }
    }

    private void listenForInput(InputStream inputStream, HttpSession session, WebifierTestResultListener listener) {
        new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            ObjectMapper mapper = new ObjectMapper();
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                    TesterResult result = mapper.readValue(line, TesterResult.class);
                    result.setContent(line);
                    listener.onTestResult(session, result.getLaunchId(), result);
                }
            } catch (IOException e) {
                listener.onError(session, ExceptionUtils.getStackTrace(e));
            }
        }).start();
    }

    private void listenForError(InputStream errorStream, HttpSession session, WebifierTestResultListener listener) {
        new Thread(() -> {
            BufferedReader br = new BufferedReader(new InputStreamReader(errorStream));
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    listener.onError(session, line);
                }
            } catch (IOException e) {
                listener.onError(session, ExceptionUtils.getStackTrace(e));
            }
        }).start();
    }
}
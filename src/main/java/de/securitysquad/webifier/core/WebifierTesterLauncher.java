package de.securitysquad.webifier.core;

import de.securitysquad.webifier.config.WebifierConfig;
import de.securitysquad.webifier.config.WebifierTesterConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Predicate;

import static de.securitysquad.webifier.core.WebifierTesterState.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;

/**
 * Created by samuel on 08.11.16.
 */
@Component
public class WebifierTesterLauncher implements Runnable {
    private static final int MAX_QUEUE_SIZE = 150000;

    private final WebifierTesterConfig config;
    private final List<WebifierTester> queue;
    private final Thread testerProcessor;

    @Autowired
    public WebifierTesterLauncher(WebifierConfig config) {
        this.config = config.getTester();
        this.queue = Collections.synchronizedList(new ArrayList<>());
        this.testerProcessor = new Thread(this, "WebifierTesterLauncher");
        this.testerProcessor.start();
    }

    @PreDestroy
    public void destroy() {
        queue.forEach(WebifierTester::exit);
        testerProcessor.interrupt();
    }

    public synchronized String launch(String url) {
        return launch(url, null, null);
    }

    public synchronized String launch(String url, HttpSession session, WebifierTesterResultListener listener) {
        if (queue.size() >= MAX_QUEUE_SIZE) {
            return null;
        }
        if (queue.stream().anyMatch(test -> test.getUrl().equals(url))) {
            return null;
        }
        String id = UUID.randomUUID().toString();
        String command = config.getCommand().replace("#URL", url).replace("#ID", id);
        queue.add(new WebifierTester(id, url, command, session, listener, config.getTimeout()));
        return id;
    }

    public synchronized int getQueueSize() {
        return (int) queue.stream().filter(t -> t.getState() == WAITING).count();
    }

    @Override
    public void run() {
        Predicate<? super WebifierTester> waiting = t -> t.getState() == WAITING;
        Predicate<? super WebifierTester> exited = t -> asList(FINISHED, ERROR).contains(t.getState());
        while (!testerProcessor.isInterrupted()) {
            synchronized (queue) {
                queue.stream().filter(exited).collect(toList()).forEach(t -> {
                    t.exit();
                    queue.remove(t);
                    Runtime.getRuntime().gc();
                });
                List<WebifierTester> waitingTesters = queue.stream().filter(waiting).sorted(comparingLong(WebifierTester::getCreationIndex)).collect(toList());
                for (int index = 0; index < waitingTesters.size(); index++) {
                    waitingTesters.get(index).setWaitingPosition(index + 1);
                }
                if (queue.stream().mapToInt(t -> t.getState() == RUNNING ? 1 : 0).sum() < config.getParallel()) {
                    queue.stream().filter(waiting).min(comparingLong(WebifierTester::getCreationIndex)).ifPresent(WebifierTester::launch);
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                testerProcessor.interrupt();
            }
        }
    }
}
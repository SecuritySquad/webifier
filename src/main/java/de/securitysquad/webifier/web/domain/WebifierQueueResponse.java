package de.securitysquad.webifier.web.domain;

/**
 * Created by samuel on 02.03.17.
 */
public class WebifierQueueResponse {
    private int size;

    public WebifierQueueResponse(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }
}
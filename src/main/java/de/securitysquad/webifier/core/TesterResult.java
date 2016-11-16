package de.securitysquad.webifier.core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by samuel on 16.11.16.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TesterResult {
    @JsonProperty
    private String typ;
    @JsonProperty
    private String message;
    @JsonProperty("tester_id")
    private String launchId;
    private String content;

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLaunchId() {
        return launchId;
    }

    public void setLaunchId(String launchId) {
        this.launchId = launchId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

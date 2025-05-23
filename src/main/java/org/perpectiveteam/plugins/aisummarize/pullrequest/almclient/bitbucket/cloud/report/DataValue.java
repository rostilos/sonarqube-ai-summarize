package org.perpectiveteam.plugins.aisummarize.pullrequest.almclient.bitbucket.cloud.report;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.math.BigDecimal;

public interface DataValue extends Serializable {

    class Link implements DataValue {
        private final String linktext;
        private final String href;

        @JsonCreator
        public Link(@JsonProperty("linktext") String linktext, @JsonProperty("href") String href) {
            this.linktext = linktext;
            this.href = href;
        }

        public String getLinktext() {
            return linktext;
        }

        public String getHref() {
            return href;
        }
    }

    class CloudLink implements DataValue {
        private final String text;
        private final String href;

        @JsonCreator
        public CloudLink(@JsonProperty("text") String text, @JsonProperty("href") String href) {
            this.text = text;
            this.href = href;
        }

        public String getText() {
            return text;
        }

        public String getHref() {
            return href;
        }
    }

    class Text implements DataValue {
        private final String value;

        @JsonCreator
        public Text(@JsonProperty("value") String value) {
            this.value = value;
        }

        @JsonValue
        public String getValue() {
            return value;
        }
    }

    class Percentage implements DataValue {

        private final BigDecimal value;

        @JsonCreator
        public Percentage(@JsonProperty("value") BigDecimal value) {
            this.value = value;
        }

        @JsonValue
        public BigDecimal getValue() {
            return value;
        }
    }
}
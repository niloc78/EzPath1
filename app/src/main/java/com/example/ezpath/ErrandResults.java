package com.example.ezpath;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrandResults {
    @JsonProperty("html_attributions") String[] html_attributions;
    String next_page_token;
    Result[] results;
    Result bestPlace;

    public String[] getHtml_attribution() {
        return html_attributions;
    }

    public String getNext_page_token() {
        return next_page_token;
    }

    public Result[] getResults() {
        return results;
    }


    public void setHtml_attribution(String[] html_attributions) {
        this.html_attributions = html_attributions;
    }

    public void setNext_page_token(String next_page_token) {
        this.next_page_token = next_page_token;
    }

    public void setResults(Result[] results) {
        this.results = results;
    }
    public Result chooseBestPlace() {
        bestPlace = results[0];
        for (int i = 0; i < results.length - 1; i++) {
            if (results[i + 1].getRating() > bestPlace.getRating()) {
                bestPlace = results[i + 1];
            }
        }
        return bestPlace;
    }
}

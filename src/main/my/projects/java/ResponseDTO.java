package main.my.projects.java;

import org.json.JSONObject;

public class ResponseDTO {
    private final JSONObject result;
    private final Boolean success;

    public ResponseDTO(JSONObject result, Boolean success) {
        this.result = result;
        this.success = success;
    }

    public JSONObject getResult() {
        return result;
    }

    public Boolean getSuccess() {
        return success;
    }
}
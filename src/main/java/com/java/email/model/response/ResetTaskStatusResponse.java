package com.java.email.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResetTaskStatusResponse {
    @JsonProperty("email_task_id")
    private String emailTaskId;

    @JsonProperty("email_status")
    private String emailStatus;
}

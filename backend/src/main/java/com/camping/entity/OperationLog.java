package com.camping.entity;

import java.time.LocalDateTime;

/**
 * Operation log entity
 */
public class OperationLog {
    private Long logId;
    private String operation;
    private Long operatorId;
    private String operatorName;
    private String description;
    private String details;
    private LocalDateTime logTime;

    public OperationLog() {
    }

    public OperationLog(String operation,
            Long operatorId,
            String operatorName,
            String description,
            String details,
            LocalDateTime logTime) {
        this.operation = operation;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.description = description;
        this.details = details;
        this.logTime = logTime;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getLogTime() {
        return logTime;
    }

    public void setLogTime(LocalDateTime logTime) {
        this.logTime = logTime;
    }
}

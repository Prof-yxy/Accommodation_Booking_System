package com.camping.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 */
@Data
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
                        LocalDateTime logTime){
        this.operation = operation;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.description = description;
        this.details = details;
        this.logTime = logTime;
    }
}

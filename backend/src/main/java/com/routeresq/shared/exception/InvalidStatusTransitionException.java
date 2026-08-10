package com.routeresq.shared.exception;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(String message) {
        super(message);
    }

    public InvalidStatusTransitionException(String entityName, Object currentStatus, Object targetStatus) {
        super(String.format("Cannot transition %s status from '%s' to '%s'", entityName, currentStatus, targetStatus));
    }
}

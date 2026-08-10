package com.routeresq.analytics.model;

import com.routeresq.shared.model.BaseEntity;
import com.routeresq.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank(message = "Action is required")
    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    public AuditLog() {
    }

    public AuditLog(User user, String action, String details) {
        this.user = user;
        this.action = action;
        this.details = details;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private User user;
        private String action;
        private String details;

        public AuditLogBuilder user(User user) {
            this.user = user;
            return this;
        }

        public AuditLogBuilder action(String action) {
            this.action = action;
            return this;
        }

        public AuditLogBuilder details(String details) {
            this.details = details;
            return this;
        }

        public AuditLog build() {
            return new AuditLog(user, action, details);
        }
    }
}

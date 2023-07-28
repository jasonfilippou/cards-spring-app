package com.logicea.cardsapp.model;

import static com.logicea.cardsapp.util.Constants.GLOBAL_DATE_TIME_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<U> {

    @Column(name = "created_by", updatable = false)
    @CreatedBy
    protected U createdBy;

    @Column(name = "created_date_time", updatable = false)
    @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
    @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
    @CreatedDate
    protected LocalDateTime createdDateTime;

    @Column(name = "last_modified_by")
    @LastModifiedBy
    protected U lastModifiedBy;

    @Column(name = "last_modified_date_time")
    @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
    @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
    @LastModifiedDate
    protected LocalDateTime lastModifiedDateTime;
}

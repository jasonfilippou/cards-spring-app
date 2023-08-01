package com.logicea.cardsapp.model;

import static com.logicea.cardsapp.util.Constants.GLOBAL_DATE_TIME_PATTERN;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * An {@literal abstract} class extended by entity classes that allows them to automatically persist audit 
 * information for their stored interfaces. This information consists of:
 * <ul>
 *     <li>Creation Timestamp</li>
 *     <li>Creating User</li>
 *     <li>Last Modification Timestamp</li>
 *     <li>Last Modifying user</li>
 * </ul>
 * @param <U> the type that uniquely identifies a given user, e.g {@link String} for usernames.
 *           
 * @author jason 
 */
@MappedSuperclass
@Getter
@EntityListeners(AuditingEntityListener.class)
@Setter
public abstract class Auditable<U extends String> {

  @Column(name = "created_by", updatable = false, nullable = false)
  @Email
  @Size(min = 5, max = 50)
  @CreatedBy
  protected U createdBy;

  @Column(name = "created_date_time", updatable = false, nullable = false)
  @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @CreatedDate
  protected LocalDateTime createdDateTime;

  @Column(name = "last_modified_by")
  @LastModifiedBy
  @Email
  @Size(min = 5, max = 50)
  protected U lastModifiedBy;

  @Column(name = "last_modified_date_time")
  @DateTimeFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @JsonFormat(pattern = GLOBAL_DATE_TIME_PATTERN)
  @LastModifiedDate
  protected LocalDateTime lastModifiedDateTime;
}

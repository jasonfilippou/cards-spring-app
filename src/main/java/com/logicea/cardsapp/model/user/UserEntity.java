package com.logicea.cardsapp.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

/**
 * Database object for application users.
 *
 * @author jason
 *
 * @see UserDto
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "`USER`") // Need backticks because "USER" is a reserved table in H2 and tests are affected.
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email_address", unique = true)
    @NonNull
    @Email
    @NotBlank private String email;

    @Column(name = "password")
    @JsonIgnore
    @ToString.Exclude
    @NonNull @NotBlank private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    @NonNull private UserRole role;

    public UserEntity(@NonNull String email, @NonNull String password, @NonNull UserRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass =
                o instanceof HibernateProxy
                        ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                        : o.getClass();
        Class<?> thisEffectiveClass =
                this instanceof HibernateProxy
                        ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                        : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserEntity userEntity = (UserEntity) o;
        return getId() != null && Objects.equals(getId(), userEntity.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}

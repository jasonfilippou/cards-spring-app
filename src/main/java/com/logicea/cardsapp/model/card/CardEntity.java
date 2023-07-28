package com.logicea.cardsapp.model.card;

import com.logicea.cardsapp.model.Auditable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

/**
 * DB entity class for cards.
 * 
 * @author jason 
 * 
 * @see CardDto
 */
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "CARD")
public class CardEntity extends Auditable<String>{

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @NotBlank
    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;
    
    @Size(max = 100)
    @Column(name = "description", length = 100)
    private String description;

    @Pattern(regexp = "^#[a-zA-Z0-9]{6}$", message = "Color must start with '#' and end with exactly 6 alphanumerics",
            flags = Pattern.Flag.CASE_INSENSITIVE)
    @Column(name = "color", length = 7)
    private String color;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 11) 
    private CardStatus status = CardStatus.TODO;
    
    

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        CardEntity that = (CardEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return getClass().hashCode();
    }
}

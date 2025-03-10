package xyz.jiniux.aap.domain.model;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private PaymentState state;

    @Column(nullable = false)
    private PaymentMethod method;

    @Type(JsonType.class)
    @Column(columnDefinition = "json")
    private Object additionalInfo;
}

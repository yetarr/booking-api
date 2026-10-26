package dev.yetar.booking.membership;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Persistable;

import java.time.Instant;
import java.util.UUID;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@IdClass(MembershipId.class)
class Membership implements Persistable<MembershipId> {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "tenant_id")
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Transient
    private boolean newEntity = true;

    protected Membership(UUID userId, UUID tenantId, Role role) {
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
    }

    @Override
    public @Nullable MembershipId getId() {
        return new MembershipId(userId, tenantId);
    }

    @Override
    public boolean isNew() {
        return newEntity;
    }

    @PostPersist
    @PostLoad
    private void presisted() {
        newEntity = false;
    }
}

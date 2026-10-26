package dev.yetar.booking.membership;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class MembershipId implements Serializable {

    private UUID userId;
    private UUID tenantId;
}

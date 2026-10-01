/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.swagger.v3.oas.annotations.media.Schema
 */
package com.ridelink.ridemanagement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description="Ride lifecycle status")
public enum RideStatus {
    REQUESTED,
    ASSIGNED,
    ACCEPTED,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;


    public boolean isTerminal() {
        return this == COMPLETED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == REQUESTED || this == ASSIGNED || this == ACCEPTED;
    }

    public boolean canTransitionTo(RideStatus targetStatus) {
        if (targetStatus == null || this == targetStatus) {
            return false;
        }
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                if (targetStatus == ASSIGNED || targetStatus == CANCELLED) {
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                if (targetStatus == ACCEPTED || targetStatus == CANCELLED) {
                    yield true;
                }
                yield false;
            }
            case 2 -> {
                if (targetStatus == IN_PROGRESS || targetStatus == CANCELLED) {
                    yield true;
                }
                yield false;
            }
            case 3 -> {
                if (targetStatus == COMPLETED) {
                    yield true;
                }
                yield false;
            }
            case 4, 5 -> false;
        };
    }

    public Set<RideStatus> allowedNextStates() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> Set.of(ASSIGNED, CANCELLED);
            case 1 -> Set.of(ACCEPTED, CANCELLED);
            case 2 -> Set.of(IN_PROGRESS, CANCELLED);
            case 3 -> Set.of(COMPLETED);
            case 4, 5 -> Set.of();
        };
    }
}


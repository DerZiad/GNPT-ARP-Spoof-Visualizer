package org.npt.models;

import lombok.Builder;
import lombok.Getter;

@Builder
public record ChangeAfterRescan(org.npt.models.ChangeAfterRescan.Operation operation, Device device, Device parent) {

    public enum Operation {
        ADD, REMOVE
    }

}

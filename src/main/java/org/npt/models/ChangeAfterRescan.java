package org.npt.models;

import lombok.Builder;

@Builder
public record ChangeAfterRescan(org.npt.models.ChangeAfterRescan.Operation operation, Device device, Device parent) {

    public enum Operation {
        ADD, REMOVE
    }

}

package org.npt.models.ui;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.npt.models.KnownHost;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class IncomingOutgoingPacket {

    private Long incoming;
    private Long outgoing;
    private KnownHost knownHost;
}

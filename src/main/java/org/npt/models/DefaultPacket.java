package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class DefaultPacket {

    private String type;
    private String srcIp;
    private String dstIp;

}

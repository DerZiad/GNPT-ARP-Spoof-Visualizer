package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class Packet {

    private String type;
    private String srcIp;
    private String dstIp;

}

package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Connection {

    private Device firstDevice;
    private Device secondDevice;
}

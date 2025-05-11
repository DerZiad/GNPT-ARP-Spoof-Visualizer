package org.npt.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@ToString
public class KnownHost {

    private String name;

    private String iconPath;

    private List<String> ips;

}

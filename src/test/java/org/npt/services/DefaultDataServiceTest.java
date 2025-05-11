package org.npt.services;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.npt.data.DataService;
import org.npt.data.defaults.DefaultDataService;

@Disabled
public class DefaultDataServiceTest {

    @Test
    public void test() throws Exception {
        DataService dataService = DefaultDataService.getInstance();
        dataService.run();
        System.out.println(dataService.getDevices().size());
        for(int i = 0;i<dataService.getDevices().size();i++){

        }
    }
}

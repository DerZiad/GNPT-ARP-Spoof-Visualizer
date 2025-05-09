package org.npt.services.impl;

import org.npt.configuration.Configuration;
import org.npt.models.Device;
import org.npt.models.Gateway;
import org.npt.models.SelfDevice;
import org.npt.services.DataService;
import org.npt.services.DeviceService;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class DefaultDataService implements DataService {

    private static final Integer EXPECTED_CAPACITY = 3000;

    private static List<Device> devices;
    private static SelfDevice selfDevice;
    private static DataService dataService = null;

    private DefaultDataService() throws SocketException, UnknownHostException {
        devices = new ArrayList<>(EXPECTED_CAPACITY);
        final DeviceService deviceService = new DeviceServiceImpl();
        List<Gateway> gateways = deviceService.scanCurrentGateways();
        devices.addAll(gateways);
        selfDevice = deviceService.scanActualDevice(gateways);
    }


    @Override
    public List<Device> getDevices() {
        return devices;
    }

    @Override
    public void addDevice(Optional<Device> device) throws NullPointerException {
        device.ifPresentOrElse(devices::add, NullPointerException::new);
    }

    @Override
    public void removeByIndex(Optional<Integer> index) {
        index.ifPresentOrElse(devices::remove, NullPointerException::new);
    }

    @Override
    public void removeByObject(Optional<Device> device) throws NullPointerException {
        Integer index = null;
        final Device notNullDevice = device.orElseThrow(NullPointerException::new);
        for(int i = 0;i<devices.size();i++){
            Device device1 = devices.get(i);
            if(notNullDevice.equals(device1)){
                index = i;
                break;
            }
        }
        removeByIndex(Optional.ofNullable(index));
    }

    @Override
    public Optional<Device> getDevice(final Optional<Integer> index) {
        try {
            final Integer notNullIndex = index.orElseThrow(NullPointerException::new);
            return Optional.ofNullable(devices.get(notNullIndex));
        }catch (Exception e){
            return Optional.empty();
        }
    }

    @Override
    public <T> HashMap<Integer, T> getDevices(final Optional<Class<T>> tClass) {
        Class<T> classType = tClass.orElseThrow(NullPointerException::new);
        AtomicInteger index = new AtomicInteger(0);
        return devices.stream()
                .filter(classType::isInstance)
                .map(classType::cast)
                .collect(Collectors.toMap(
                        d -> index.getAndIncrement(),
                        d -> d,
                        (a, b) -> b,
                        HashMap::new
                ));
    }

    @Override
    public SelfDevice getSelfDevice() {
        return selfDevice;
    }

    public static DataService init() throws SocketException, UnknownHostException {
        if(dataService == null)
            dataService = new DefaultDataService();
        return dataService;
    }
}

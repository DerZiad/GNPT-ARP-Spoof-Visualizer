package org.npt.services;

import org.npt.models.Gateway;
import org.npt.models.IpAddress;
import org.npt.models.SelfDevice;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;

public interface DeviceService {

    public List<Gateway> scanCurrentGateways() throws SocketException, UnknownHostException;

    public SelfDevice scanActualDevice(List<Gateway> gateways) throws SocketException;

    public List<IpAddress> scanInterfaces() throws SocketException;
}

package org.npt.services;

public interface ArpSpoofStarter {

    public void stopSpoofing(String targetIp, String gatewayIp);

    public void startSpoofing(String scanInterface, String targetIp, String gatewayIp);
}

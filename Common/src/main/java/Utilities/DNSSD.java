package Utilities;

/**
 * Created by Austin on 1/25/2017.
 */

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class DNSSD {

    private RegisterService registerService;
    private DiscoverService discoverService;

    public DNSSD(){

    }

    public void registerService(String serviceType, String serviceName, int port, String serviceDescription) {
        registerService = new RegisterService(serviceType, serviceName, port, serviceDescription);
        registerService.start();
    }

    public void discoverService(String serviceType){
        discoverService = new DiscoverService(serviceType);
        discoverService.start();
    }
}

class RegisterService extends Thread {

    private String serviceType, serviceName, serviceDescription;
    private int port;

    public RegisterService(String serviceType, String serviceName, int port, String serviceDescription){
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.port = port;
    }

    @Override
    public void run(){
        JmDNS jmdns = null;
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }

        ServiceInfo service = ServiceInfo.create(serviceType, serviceName, port, serviceDescription);
        try {
            jmdns.registerService(service);
        } catch (IOException e) {
            // TODO
            e.printStackTrace();
        }
    }
}

class DiscoverService extends Thread {

    private String serviceType;

    public DiscoverService(String serviceType){
        this.serviceType = serviceType;
    }

    @Override
    public void run(){
        try {
            // Create a JmDNS instance
            JmDNS jmdns = JmDNS.create(InetAddress.getLocalHost());

            // Add a service listener
            jmdns.addServiceListener(serviceType, new SampleListener());

            // Wait a bit
            Thread.sleep(30000);
        } catch (UnknownHostException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
        }
    }
}

class SampleListener implements ServiceListener {
    @Override
    public void serviceAdded(ServiceEvent event) {
        System.out.println("Service added: " + event.getInfo());
    }

    @Override
    public void serviceRemoved(ServiceEvent event) {
        System.out.println("Service removed: " + event.getInfo());
    }

    @Override
    public void serviceResolved(ServiceEvent event) {
        System.out.println("Service resolved: " + event.getInfo());
    }
}
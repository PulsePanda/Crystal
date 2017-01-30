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


// TODO javadoc
public class DNSSD {

    private RegisterService registerService;
    private DiscoverService discoverService;

    public DNSSD() {
    }

    public void registerService(String serviceType, String serviceName, int port, String serviceDescription, InetAddress address) {
        System.out.println("DNSSD: Registering dns_sd service. Details: ServiceType-" + serviceType + "; ServiceName-"
                + serviceName + "; Port-" + port + "; ServiceDescription-" + serviceDescription + "; Address-" + address);
        registerService = new RegisterService(serviceType, serviceName, port, serviceDescription, address);
        registerService.start();
    }

    public void discoverService(String serviceType, InetAddress address) {
        System.out.println("DNSSD: Searching for dns_sd service. ServiceType-" + serviceType);
        discoverService = new DiscoverService(serviceType, address);
        discoverService.start();
    }

    public String getServiceInfo() {
        return discoverService.getServiceInfo();
    }

    public String getServiceName() {
        return discoverService.getServiceName();
    }

    public void closeRegisteredService() {
        System.out.println("DNSSD: Unregistering service.");
        registerService.close();
    }

    public void closeServiceDiscovery() {
        discoverService.closeServiceDiscovery();
        System.out.println("DNSSD: mdnsService discovery has been closed");
    }
}

class RegisterService extends Thread {

    private String serviceType, serviceName, serviceDescription;
    private int port;
    private JmDNS jmdns = null;
    private InetAddress address;

    public RegisterService(String serviceType, String serviceName, int port, String serviceDescription, InetAddress address) {
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.port = port;
        this.address = address;
    }

    @Override
    public void run() {
        try {
            jmdns = JmDNS.create(address);
            ServiceInfo service = ServiceInfo.create(serviceType, serviceName, port, serviceDescription);
            jmdns.registerService(service);
            System.out.println("DNSSD: Service registered.");
        } catch (IOException e) {
            System.err.println("DNSSD: Error setting up dns_sd for service broadcast. Details: " + e.getMessage());
        }
    }

    public void close() {
        jmdns.unregisterAllServices();
        try {
            jmdns.close();
        } catch (IOException e) {
            System.err.println("DNSSD: Error deregistering service. Details: " + e.getMessage());
        }
    }
}

class DiscoverService extends Thread {

    private String serviceType;
    private String serviceInfo = "";
    private String serviceName = "";
    private JmDNS mdnsService;
    private ServiceListener mdnsServiceListener;
    private InetAddress address;

    public DiscoverService(String serviceType, InetAddress address) {
        this.serviceType = serviceType;
        this.address = address;
    }

    @Override
    public void run() {
        try {
            // Create a JmDNS instance
            mdnsService = JmDNS.create(address);

            mdnsServiceListener = new ServiceListener() {
                @Override
                public void serviceAdded(ServiceEvent serviceEvent) {
                    // Test service is discovered. requestServiceInfo() will trigger serviceResolved() callback.
                    mdnsService.requestServiceInfo(serviceType, serviceEvent.getName());
                }

                @Override
                public void serviceRemoved(ServiceEvent serviceEvent) {
                    // Test service is disappeared.
                }

                @Override
                public void serviceResolved(ServiceEvent serviceEvent) {
                    // Test service info is resolved.
                    serviceInfo = serviceEvent.getInfo().getURL();
                    serviceName = serviceEvent.getName();
                }
            };

            // Add a service listener
            mdnsService.addServiceListener(serviceType, mdnsServiceListener);

            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
        } catch (UnknownHostException e) {
            System.err.println("DNSSD: Error setting up dns_sd service discovery. Details: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("DNSSD: Error setting up dns_sd service discovery. Details: " + e.getMessage());
        }
    }

    public void closeServiceDiscovery() {
        mdnsService.removeServiceListener(serviceType, mdnsServiceListener);
        try {
            mdnsService.close();
        } catch (IOException e) {
            System.err.println("DNSSD: Error closing service discovery. Details: " + e.getMessage());
        }
    }

    public String getServiceInfo() {
        return serviceInfo;
    }

    public String getServiceName() {
        return serviceName;
    }
}
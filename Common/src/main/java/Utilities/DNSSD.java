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

    public DNSSD() {
    }

    public void registerService(String serviceType, String serviceName, int port, String serviceDescription) {
        System.out.println("DNSSD: Registering dns_sd service. Details: ServiceType-" + serviceType + "; ServiceName-"
                + serviceName + "; Port-" + port + "; ServiceDescription-" + serviceDescription);
        registerService = new RegisterService(serviceType, serviceName, port, serviceDescription);
        registerService.start();
    }

    public void discoverService(String serviceType) {
        System.out.println("DNSSD: Searching for dns_sd service. ServiceType-" + serviceType);
        discoverService = new DiscoverService(serviceType);
        discoverService.start();
    }

    public String getServiceInfo() {
        return discoverService.getServiceInfo();
    }

    public void closeRegisteredService() {
        System.out.println("DNSSD: Unregistering service.");
        registerService.close();
    }
}

class RegisterService extends Thread {

    private String serviceType, serviceName, serviceDescription;
    private int port;
    private JmDNS jmdns = null;

    public RegisterService(String serviceType, String serviceName, int port, String serviceDescription) {
        this.serviceType = serviceType;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            jmdns = JmDNS.create(InetAddress.getLocalHost());
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
    private String serviceInfo;

    public DiscoverService(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public void run() {
        try {
            // Create a JmDNS instance
            JmDNS mdnsService = JmDNS.create();

            ServiceListener mdnsServiceListener = new ServiceListener() {
                public void serviceAdded(ServiceEvent serviceEvent) {
                    // Test service is discovered. requestServiceInfo() will trigger serviceResolved() callback.
                    mdnsService.requestServiceInfo(serviceType, serviceEvent.getName());
                }

                public void serviceRemoved(ServiceEvent serviceEvent) {
                    // Test service is disappeared.
                }

                public void serviceResolved(ServiceEvent serviceEvent) {
                    // Test service info is resolved.
                    serviceInfo = serviceEvent.getInfo().getURL();
                    // serviceURL is usually something like http://192.168.11.2:6666/my-service-name
                }
            };

            // Add a service listener
            mdnsService.addServiceListener(serviceType, mdnsServiceListener);

//            ServiceInfo[] infos = mdnsService.list(serviceType);

            mdnsService.removeServiceListener(serviceType, mdnsServiceListener);
            mdnsService.close();
        } catch (UnknownHostException e) {
            System.err.println("DNSSD: Error setting up dns_sd service discovery. Details: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("DNSSD: Error setting up dns_sd service discovery. Details: " + e.getMessage());
        }
    }

    public String getServiceInfo() {
        return serviceInfo;
    }
}
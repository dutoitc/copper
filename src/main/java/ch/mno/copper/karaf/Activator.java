package ch.mno.copper.karaf;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

// https://github.com/apache/karaf/tree/master/examples/karaf-servlet-example/karaf-servlet-example-registration
public class Activator implements BundleActivator {

    private ServiceTracker httpServiceTracker;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        httpServiceTracker = new ServiceTracker(bundleContext, HttpService.class.getName(), null) {
            @Override
            public Object addingService(ServiceReference ref) {
                HttpService httpService = (HttpService) bundleContext.getService(ref);
                try {
                    httpService.registerServlet("/servlet-example", new ExampleServlet(), null, null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return httpService;
            }

            public void removedService(ServiceReference ref, Object service) {
                try {
                    ((HttpService) service).unregister("/servlet-example");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        httpServiceTracker.open();
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        httpServiceTracker.close();
    }

}
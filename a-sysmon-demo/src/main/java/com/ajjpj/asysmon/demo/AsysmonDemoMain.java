package com.ajjpj.asysmon.demo;


import com.ajjpj.asysmon.ASysMon;
import com.ajjpj.asysmon.ASysMonConfigurer;
import com.ajjpj.asysmon.datasink.cyclicdump.ALog4JInfoCyclicMeasurementDumper;
import com.ajjpj.asysmon.datasink.log.AStdOutDataSink;
import com.ajjpj.asysmon.measure.special.AJmxGcMeasurerer;
import com.ajjpj.asysmon.measure.scalar.AJmxMemMeasurer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author arno
 */
public class AsysmonDemoMain {
    public static void main(String[] args) throws Exception {
        new DeadlockThread().start();

//        System.setProperty("com.ajjpj.asysmon.globallydisabled", "true");

        new ALog4JInfoCyclicMeasurementDumper(ASysMon.get(), 120);

        ASysMonConfigurer.addDataSink(ASysMon.get(), new AStdOutDataSink());
        ASysMonConfigurer.addThreadCountSupport(ASysMon.get()); //TODO unify the mechanism with GC support
        AJmxGcMeasurerer.init(ASysMon.get());
        ASysMonConfigurer.addScalarMeasurer(ASysMon.get(), new AJmxMemMeasurer());

//        ASysMonConfigurer.addDataSink(ASysMon.get(), new ALog4JDataSink());
//        ASysMonConfigurer.addDataSink(ASysMon.get(), new AHttpJsonOffloadingDataSink(ASysMon.get(), "http://localhost:8899/upload", "demo", "the-instance", 100, 1000, 1, 10*1000));

        final Server server = new Server(8080);

        final WebAppContext webapp = new WebAppContext();
        webapp.setContextPath("/");
        webapp.setWar("a-sysmon-demo/src/main/resources");
        server.setHandler(webapp);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(new Runnable() {
            @Override public void run() {
                System.gc();
            }
        }, 10, 6, TimeUnit.SECONDS);

        server.start();
        server.join();
    }
}



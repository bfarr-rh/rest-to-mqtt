package bf.integrator;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.LoggingLevel;

public class CamelRouteBuilder extends RouteBuilder {

    public void configure() throws Exception {
        onException()
            .handled(true)
            .maximumRedeliveries(2)
            .logStackTrace(true)
            .logExhausted(false)
            .log(LoggingLevel.ERROR, "Failed processing ${body}")
            // Register the 'error' meter
            .to("microprofile-metrics:meter:error");

        from("timer:test?fixedRate=true&period=30000")     // Poll every 30 seconds
        .routeId("Poll-NMS") // Label this route
        .to("https://raw.githubusercontent.com/bfarr-rh/rest-to-mqtt/master/sample.json")  //The URL to poll
        .log("Processing ${body}")  // Optional log message of what was returned
        .to("microprofile-metrics:meter:polled-rest-interface") // Record metric
        .to("paho:iotdatatopic?brokerurl=tcp://host.docker.internal:1883") // Send payload to the MQTT server with this iotdatatopic
        .log("Sent to MQTT")
        // Register the 'success' meter
        .to("microprofile-metrics:meter:success"); 
    }

}



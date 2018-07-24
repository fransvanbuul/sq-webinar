package io.axoniq.demo.sqwebinar;

import com.thoughtworks.xstream.XStream;
import org.axonframework.commandhandling.AsynchronousCommandBus;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.xml.XStreamSerializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

@SpringBootApplication
public class SqWebinarApplication {

    public static void main(String[] args) {
        SpringApplication.run(SqWebinarApplication.class, args);
    }

    /**
     * Setting up tracking event processing, making event handlers asynchronous.
     */
    @Autowired
    public void configure(EventProcessingConfiguration configuration) {
        configuration.usingTrackingProcessors();
    }

    /**
     * Setting up asynchronous command handling (which is similar to the behaviour we would
     * see on AxonHub / a distributed command bus).
     */
    @Bean
    public CommandBus commandBus(TransactionManager transactionManager, AxonConfiguration axonConfiguration) {
        AsynchronousCommandBus commandBus = new AsynchronousCommandBus(Executors.newCachedThreadPool(),
                transactionManager,
                axonConfiguration.messageMonitor(CommandBus.class, "commandBus"));
        commandBus.registerHandlerInterceptor(new CorrelationDataInterceptor<>(
                axonConfiguration.correlationDataProviders()));
        return commandBus;
    }

    /**
     * Correctly configuring the XStream serializer to avoid security warnings.
     */
    @Autowired
    public void configure(Serializer serializer) {
        if(serializer instanceof XStreamSerializer) {
            XStream xStream = ((XStreamSerializer)serializer).getXStream();
            XStream.setupDefaultSecurity(xStream);
            xStream.allowTypesByWildcard(new String[] { "io.axoniq.demo.**", "org.axonframework.**" });
        }
    }

}

package gr.wackydevelopers.camel.routes;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.SagaPropagation;
import org.apache.camel.service.lra.LRASagaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gr.wackydevelopers.camel.services.ServiceOne;
import gr.wackydevelopers.camel.services.ServiceTwo;

@Component
public class SagaRoute extends RouteBuilder {
    
    @Value("${camel.lra.enabled:false}")
    private String lraEnabled;
    
    @Value("${camel.lra.coordinator-url}")
    private String lraCoordinatorUrl;
    
    @Value("${camel.lra.coordinator-context-path:/}")
    private String lraCoordinatorContextPath;
    
    @Value("${camel.lra.local-participant-url}")
    private String lraLocalParticipantUrl;
    
    @Value("${camel.lra.local-participant-context-path:/lra-participant}")
    private String lraLocalParticipantContextPath;

    @Override
    public void configure() throws Exception {
        
        if (Boolean.parseBoolean(lraEnabled)) {
            LRASagaService sagaService = new LRASagaService();
            sagaService.setCoordinatorUrl(lraCoordinatorUrl);
            sagaService.setCoordinatorContextPath(lraCoordinatorContextPath);
            sagaService.setLocalParticipantUrl(lraLocalParticipantUrl);
            sagaService.setLocalParticipantContextPath(lraLocalParticipantContextPath);
            sagaService.setCamelContext(getContext());
            getContext().addService(sagaService);
        }
        
        rest("/saga")
            .post()
            .route()
            .saga().propagation(SagaPropagation.REQUIRES_NEW)
                .to("direct:serviceOne")
                .to("direct:serviceTwo")
            .completion("direct:sagaCompleted")
            .compensation("direct:sagaCompensated")
            .to("direct:completed")
        .end();
    
        from("direct:serviceOne")
            .saga()
            .propagation(SagaPropagation.SUPPORTS)
            .compensation("direct:serviceOneCancel")
            .bean(ServiceOne.class, "performOperation()");
    
        from("direct:serviceOneCancel")
            .routeId("service-one-cancel")
            .log("Canceling one")
            .bean(ServiceOne.class, "cancelOperation(${header." + Exchange.SAGA_LONG_RUNNING_ACTION + "})");
    
        from("direct:serviceTwo")
            .saga()
            .propagation(SagaPropagation.SUPPORTS)
            .compensation("direct:serviceTwoCancel")
            .bean(ServiceTwo.class, "performOperation()");
    
        from("direct:serviceTwoCancel")
            .routeId("service-two-cancel")
            .log("Canceling two")
            .bean(ServiceTwo.class, "cancelOperation(${header." + Exchange.SAGA_LONG_RUNNING_ACTION + "})");
    
        from("direct:sagaCompleted")
            .routeId("saga-completion-route")
            .log("Saga Completed");
    
        from("direct:sagaCompensated")
            .routeId("saga-compensation-route")
            .log("Saga Compensated");
        
        from("direct:completed")
            .setBody(constant("Completed"));
    }
    
}

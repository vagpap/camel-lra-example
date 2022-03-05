# Apache Camel LRA example (SpringBoot)

This is a basic example for using LRA in Apache Camel.  

It can also be used as evidence of broken functionality in the camel-lra module from v3.7.3 onwards (v3.7.2 was the latest working version).  

## How to run

* Build the application and start is as a typical SpringBoot application.  

* Have [Narayana](https://www.narayana.io/) transacation manager running (to this example, it is assumed that it operates on localhost:5000)  

* Make a POST request towards http://localhost:8080/api/saga in order to start the saga.  

The services will randomly fail, thus triggering the compensation actions.  

## What is expected

### No exception occurs

In the application logs, expect the following entries:

```
g.w.camel.services.ServiceOne            : Operation performed from ServiceOne: ServiceOne~...
g.w.camel.services.ServiceTwo            : Operation performed from ServiceTwo: ServiceTwo~...
saga-completion-route                    : Saga Completed
```

As a response, one should expect the String "Completed"

### An exception occurs

In the application logs, expect the following entries:

```
g.w.camel.services.ServiceOne            : Operation performed from ServiceOne: ServiceOne~...
o.a.c.p.e.DefaultErrorHandler            : Failed delivery for (MessageId: ...
... 
java.lang.RuntimeException: ServiceTwo operation failed
...  
saga-compensation-route                  : Saga Compensated
service-one-cancel                       : Canceling one
g.w.camel.services.ServiceOne            : Operation Canceled from ServiceOne: http://localhost:50000/lra-coordinator/...  
service-two-cancel                       : Canceling two
g.w.camel.services.ServiceTwo            : Operation Canceled from ServiceTwo: http://localhost:50000/lra-coordinator/...
```

As a response, one should expect the exception

## Where is the issue

Using Apache Camel v3.7.2 (SpringBoot v2.4.2), everything works as expected.   
Using Apache Camel v3.14.1 (SpringBoot v2.6.3), no calls to either completion or compensation are made.  


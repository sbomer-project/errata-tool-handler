# Manual Testing Guide

This guide documents how to manually trigger the `UmbAdvisoryHandler` (via AMQP) and the `RestAdvisoryHandler` (via HTTP). 

This allows you to verify the end-to-end flow (Handler → Service) without relying on external events.


## 0. Prerequisites

Run all the necessary services locally using podman-compose from the root of the repository:
```shell script
bash ./hack/run-compose.sh
```

## 1. UMB Handler (AMQP)

To test the UMB handler, you must inject a simulated message directly into the ActiveMQ Artemis broker running in the pod.

**Prerequisites:**
* Shell access to the Artemis pod via `podman exec -it <artemis-pod-name> /bin/sh`.
* Broker running on port `8161` (Console/Jolokia).
* Default credentials: `admin:admin`.
* `sbomer.umb.ssl=false` in `application.properties` to disable SSL for local dev.

### The Trigger Command
Run this inside the **Artemis pod terminal**:

```bash
curl -H "Content-Type: application/json" \
     -H "Origin: http://$(hostname):8161" \
     -u admin:admin \
     -d '{
    "type": "exec",
    "mbean": "org.apache.activemq.artemis:broker=\"broker\",component=addresses,address=\"errata.activity.status\"",
    "operation": "sendMessage(java.util.Map,int,java.lang.String,boolean,java.lang.String,java.lang.String)",
    "arguments": [
      {"subject":"errata.activity.status"},
      4,
      "eyJlcnJhdGFfaWQiOjEyMzQ1LCJlcnJhdGFfc3RhdHVzIjoiUUUifQ==",
      false,
      "admin",
      "admin"
    ]
  }' \
  http://$(hostname):8161/console/jolokia/
  ```

  Note:
  `eyJlcnJhdGFfaWQiOjEyMzQ1LCJlcnJhdGFfc3RhdHVzIjoiUUUifQ==` is the Base64-encoded JSON payload:
  ```json
  {
    "errata_id": 12345,
    "errata_status": "QE"
  }
  ```




  
## 2. REST Handler (HTTP)
We can then invoke advisory generation manually with the request below:

```shell script
curl -i -X POST -H "Content-Type: application/json" -d '{"advisoryId": "1234"}' http://localhost:8080/v1/errata-tool/generate
```
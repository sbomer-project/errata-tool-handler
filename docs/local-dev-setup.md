# Manual Testing Guide

This guide documents how to manually trigger the `UmbAdvisoryHandler` (via AMQP) and the `RestAdvisoryHandler` (via HTTP). 

This allows you to verify the end-to-end flow (Handler → Service) without relying on external events.


## 0. Prerequisites

Run all the necessary services locally using podman-compose from the root of the repository:
```shell script
bash ./hack/run-compose.sh
```

## 1. UMB Handler (AMQP)

To test the UMB handler, you can use the helper script to inject a simulated message directly into the ActiveMQ Artemis broker from your local machine.

**Prerequisites:**
* The Artemis container must be running with port `8161` exposed (Console/Jolokia).
* `sbomer.umb.ssl=false` in `application.properties` (or `SBOMER_UMB_SSL=false` env var) to disable SSL for the application connection.



### The Trigger Command

Run the script from your host terminal:

```bash
./hack/trigger-umb.sh <ERRATA_ID> <STATUS>
```

Example:
```bash
./hack/trigger-umb.sh 1234 QE
```


## 2. REST Handler (HTTP)
We can then invoke advisory generation manually with the request below:

```shell script
curl -i -X POST -H "Content-Type: application/json" -d '{"advisoryId": "1234"}' http://localhost:8080/v1/errata-tool/generate
```
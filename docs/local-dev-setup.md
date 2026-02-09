# Manual Testing Guide

This guide documents how to manually trigger the `UmbAdvisoryHandler` (via AMQP) and the `RestAdvisoryHandler` (via HTTP). 

This allows you to verify the end-to-end flow (Handler → Service) without relying on external events.


## 0. Prerequisites

Run all the necessary services locally using the helm from `sbomer-local-dev` from the root of the repository:
```shell script
bash ./hack/setup-local-dev.sh
```
And then leaving the terminal open, run:
```shell script
bash ./hack/run-helm-with-local-build.sh
```

## 1. UMB Handler (AMQP)

To test the UMB handler, you can use the helper script to inject a simulated message directly into the ActiveMQ Artemis broker from your local machine.

**Prerequisites:**
* The Artemis container must be running with port `8161` exposed (Console/Jolokia).
* `sbomer.umb.ssl=false` in `application.properties` (or `SBOMER_UMB_SSL=false` env var) to disable SSL for the application connection.



### The Trigger Command

Run the script from your host terminal:

```bash
./hack/trigger-umb-minikube.sh <ERRATA_ID> <STATUS>
```

Example:
```bash
./hack/trigger-umb-minikube.sh 1234 QE
```


## 2. REST Handler (HTTP)
If running in minikube, have to be exposed via to send the request to the service:
```shell
kubectl port-forward svc/errata-tool-handler-errata-tool-handler-chart 8080:8080 -n sbomer-test
```
We can then invoke advisory generation manually with the request below:

```shell script
curl -i -X POST -H "Content-Type: application/json" -d '{"advisoryId": "1234"}' http://localhost:8080/v1/errata-tool/generate
```

# errata-tool-handler
A component of SBOMer NextGen that listens to advisories to invoke automatic SBOM generations from them, and to request generations for specific advisories

## Getting Started (Development)

### 1. Start the Infrastructure

Run the local dev setup script from the root of the project repository to set up the minikube environment:

```shell script
bash ./hack/setup-local-dev.sh
```

Then run the command below to start the podman-compose with the component build:

```bash
bash ./hack/run-compose-with-local-build.sh
```

This will spin up the manifest-storage-service on port 8085 along with the latest Quay images of the other components of the system.
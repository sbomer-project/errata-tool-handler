# errata-tool-handler
A component of SBOMer NextGen that listens to advisories to invoke automatic SBOM generations from them, and to request generations for specific advisories

## Getting Started (Development)

### 1. Start the Infrastructure on Minikube

Run the local dev setup script from the root of the project repository to set up the minikube environment:

```shell script
bash ./hack/setup-local-dev.sh
```

Then run the command below to start install the helm chart for sbomer-platform and errata-tool-handler in minikube:

```bash
bash ./hack/run-helm-with-local-build.sh
```

We can then run the port-forward command given on the last line of the script to access the local sbomer deployment through the API gateway.

### 2. Test the UMB Listener on Minikube

We can then run the script below to simulate a UMB advisory trigger:

```bash
bash ./hack/trigger-umb-minikube.sh
```
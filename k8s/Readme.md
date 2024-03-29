# Kubernetes Deployment

This directory contains resources for deploying Search and Record API to Kubernetes. 

## Requirements
- [Kustomize](https://kubectl.docs.kubernetes.io/installation/kustomize/) for generating Kubernetes manifests and managing environment-specific configurations. (For local development on windows, the executable can be copied from binaries to docker/resources/bin folder)
- [envsubst](https://linux.die.net/man/1/envsubst) for generating customization files.

## File Structure
This folder consists of a Kustomize base layer and two patch layers: for local development (`dev`) and cloud deployment (`cloud`). 
The files below are required for deployment:

 ```
 k8s:
  ├── base
  │    ├── deployment.yaml
  │    ├── kustomization.yaml
  │    ├── set.user.properties (to be manually copied and not submitted to repository)
  └── overlays
       ├── cloud
       │     ├── deployment_patch.properties.yaml#
       │     ├── deployment_patch.properties.yaml.template
       │     ├── hpa.properties.yaml#
       │     ├── hpa.properties.yaml.template
       │     ├── ingress.properties.yaml#
       │     ├── ingress.properties.yaml.template
       │     ├── service.yaml
       │     ├── kustomization.yaml
       └──  dev
             ├── nodeport.yaml
             ├── deployment_patch.yaml
             └── kustomization.yaml 
 ```
_# indicates a file not in version control_

### File naming scheme
File names have the following structure:

- `*.properties.yaml.template` contain environment variables that need to be substituted. These are used for generating YAML files read by Kustomize.
- `*.properties.yaml` are generated from a template by interpolating the environment variables within the template. These contain configurable settings and are not checked in to git.
- `*_patch*.yaml` "patch" resources created in the base layer. These don't have to configurable, eg. `overlays/dev/deployment_patch.yaml`
- `*.yaml` are plain Kubernetes YAML files that don't require any customization; however they could be "patched" in an overlay. eg. `base/deployment.yaml` is patched by both overlays.
- `kustomize.yaml` is the [kustomization file](https://kubectl.docs.kubernetes.io/references/kustomize/glossary/#kustomization) used for orchestrating Kustomize workflows.

## Deployment Instructions
For both environments:
- Copy a valid properties file to the `base` directory, and rename it to `set.user.properties`.

### Local Deployment
- Build the API from the project root directory: `mvn clean package -f ../pom.xml`
- Build a docker image from the project root (e.g. set-api): `docker build . -t europeana/set-api` 
- (Optional, for testing only) Deploy a mongo database to docker: `docker run -p 27017:27017 --name mongo4 -d mongo:4.4.15`
- If required, load the image into your local Kubernetes cluster. 
- Copy valid api configration properties file into k8s base folder (e.g. set-api/k8s/set.user.properties)
- To build the customised Kubernetes manifests run from the k8s folder (e.g. set-api/k8s) `kustomize build ./overlays/dev`
- Apply the manifests to the cluster run from the k8s folder: `kubectl apply -k ./overlays/dev`
- Export the api to host run (from any folder, requires to be run after each pod start): `kubectl port-forward --address localhost deployment/set-api-deployment 8080:8080`
- verify if the application is running, by opening the following URL in the browser on the host: http://localhost:8080/

Run `kubectl get deployment/set-api-deployment` to view the deployment's status. 
After deploying successfully the app will be available on `<cluster_host>:30000`, where `<cluster_host>`:
- is "localhost" for Docker Desktop and Kind
- can be retrieved by running `minikube ip` if using Minikube 

### IBM Cloud Deployment
`overlay/cloud` contains templates to be used for generating YAML files for Kustomize. 
The following environment variables are required:

| ENVIRONMENT VARIABLE | DESCRIPTION                                                                                     | USED BY                                                                         |
|----------------------|-------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------|
| MIN_REPLICAS         | Minimum number of replicas to run                                                               | `deployment_patch.properties.yaml.template`<br/> `hpa_properties.yaml.template` |
| MAX_REPLICAS         | Maximum number of replicas to run when auto scaling                                             | `hpa_properties.yaml.template`                                                  |
| MEMORY_REQUEST       | Amount of memory in megabytes to request during application deployment                          | `deployment_patch.properties.yaml.template`                                     |
| MEMORY_LIMIT         | Application memory limit in megabytes                                                           | `deployment_patch.properties.yaml.template`                                     |
| CPU_REQUEST          | Amount of CPU to request during application deployment, in milliCPU form. <br>1000 = 1 CPU core | `deployment_patch.properties.yaml.template`                                     |
| CPU_LIMIT            | Max CPU allocation for app, in milliCPU form. <br> 1000 = 1 CPU core                            | `deployment_patch.properties.yaml.template`                                     |
| SEARCH_API_HOSTNAME  | Ingress hostname, ie. a FQDN to be used for accessing the app                                   | `ingress.properties.yaml.template`                                              |

These variables can be provided either via a `.env` file, Jenkins job configuration, or the Linux `export`
command.

Generate the customization files with the following commands (while in `overlays/cloud`, but adjust paths accordingly):

```
envsubst < ingress.properties.yaml.template > ingress.properties.yaml
envsubst < hpa.properties.yaml.template > hpa.properties.yaml
envsubst < deployment_patch.properties.yaml.template > deployment_patch.properties.yaml
```
The YAML files created by these commands are used by Kustomize.

To view the customised Kubernetes manifests run `kustomize build overlays/cloud`. 

The manifests can then be applied to the cluster by running `kubectl apply -k overlays/cloud`.

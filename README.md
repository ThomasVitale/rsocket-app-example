# RSocket Application - Example

This section will describe how to deploy the application on a local Kubernetes cluster with `kind`.

## Prerequisites

* [kind](https://kind.sigs.k8s.io)
* [kapp](https://carvel.dev/kapp/)

## Set up the local environment

Create a new Kubernetes cluster using `kind`.

```shell
kind create cluster --config deployment/kind-config.yml
```

Deploy `kapp-controller` from the Carvel suite.

```shell
kapp deploy -a kapp-controller -f https://github.com/vmware-tanzu/carvel-kapp-controller/releases/download/v0.36.1/release.yml --yes
```

Install the packages necessary to build the platform.

```shell
kapp deploy -a platform-setup -f deployment/platform-setup --yes
```

## Deploy the application

Deploy the application as follows.

```shell
kapp deploy -a rsocket-app-example -f application/service.yml --yes
```

Once the application is up and running, you can visit it at http://127.0.0.1.nip.io.

## Delete the local cluster

Run this command to delete the local `kind` cluster.

```shell
kind delete cluster --name rsocket-cluster
```

# PipelineForge

**Local-first CI/CD, container engineering, and Kubernetes reliability lab**

PipelineForge is a hands-on DevOps portfolio project built around a small Java Spring Boot API for simulated communication-site monitoring. Its main purpose is to demonstrate how code changes are compiled, tested, packaged, containerized, verified, and prepared for reliable deployment using repeatable workflows.

**Scope:** Local development and experimentation on Windows with WSL2 Ubuntu and Docker Desktop. There is **no production deployment or public cloud infrastructure**.

## What is implemented

- **Spring Boot REST API (Java 21):** List communication sites and register a new site with input validation.
- **Automated tests:** JUnit, Spring MVC controller tests, service tests, and Gradle test reports.
- **Jenkins Pipeline as Code:** Git checkout, environment checks, compilation, unit tests, executable JAR packaging, and artifact archiving.
- **Distributed build execution:** A Linux Java build agent and a separate Docker-capable build agent.
- **Docker:** Multi-stage image build with a Java 21 JDK build stage and JRE runtime stage; runtime process runs as a non-root user.
- **Traceable image builds:** Jenkins tags local images using its build number and a short Git commit SHA.
- **Runtime smoke testing:** A Bash script runs each image temporarily, waits for `/actuator/health`, verifies `/api/sites`, and removes the test container.
- **Reliability exercises:** Deliberately failing JUnit assertions and misconfigured health exposure to verify that CI catches failures and recovery restores passing builds.

### Kubernetes work in progress

The `feat/kubernetes-deployment` branch contains local **kind** Kubernetes manifests (`Namespace`, `Deployment`, and `ClusterIP Service`) with startup, readiness, and liveness probes. This is an experimental **local deployment**, not automated production delivery. The files and commands below are available after that branch is merged (or when checked out locally).

## Architecture

```text
GitHub source / feature branches
           |
           v
    Jenkins Pipeline from SCM
           |
           +--> Linux Java agent
           |      |-- Gradle compile
           |      |-- JUnit tests and reports
           |      `-- Spring Boot JAR artifact
           |
           `--> Docker builder (WSL2 / Docker Desktop)
                  |-- Versioned local Docker image
                  `-- Start temporary container
                         |-- GET /actuator/health
                         `-- GET /api/sites

Experimental next step: load a local image into kind and manage it with
Kubernetes Deployment, Service, probes, rollout, and rollback commands.
```

## Tech stack

| Area | Technology |
| --- | --- |
| Application | Java 21, Spring Boot, Spring MVC, Actuator, Jakarta Validation |
| Build and tests | Gradle Wrapper, JUnit, MockMvc, Mockito |
| CI | Jenkins Declarative Pipeline, GitHub |
| Containers | Docker, Eclipse Temurin JDK/JRE, Bash |
| Local environment | Windows, WSL2 Ubuntu, Docker Desktop |
| Kubernetes (in progress) | kind, kubectl, Kubernetes manifests |

## Repository layout

```text
pipelineforge/
├── Jenkinsfile
├── README.md
├── scripts/
│   └── smoke-test-image.sh
├── services/
│   └── pipelineforge-api/
│       ├── Dockerfile
│       ├── .dockerignore
│       ├── gradlew
│       ├── build.gradle
│       └── src/
└── k8s/                  # On feat/kubernetes-deployment until merged
    ├── namespace.yaml
    ├── deployment.yaml
    └── service.yaml
```

## Quick start: run the API locally

**Prerequisites:** JDK 21 and Git. Use the included Gradle Wrapper; a separate Gradle installation is not required.

From **Windows PowerShell**:

```powershell
cd services/pipelineforge-api
.\gradlew.bat clean test
.\gradlew.bat bootRun
```

In another terminal:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
Invoke-RestMethod http://localhost:8080/api/sites
```

To register a sample site:

```powershell
$body = @{ name = 'Galway Site 01' } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8080/api/sites -Method Post -ContentType 'application/json' -Body $body
```

The API uses **in-memory** site storage for learning. New registrations are lost when the application restarts. This is not production persistence.

## Run with Docker

With Docker Desktop running, from the repository root:

```powershell
docker build -t pipelineforge-api:local ./services/pipelineforge-api
docker run --rm --name pipelineforge-api -p 127.0.0.1:18080:8080 pipelineforge-api:local
```

In another terminal:

```powershell
Invoke-RestMethod http://localhost:18080/actuator/health
Invoke-RestMethod http://localhost:18080/api/sites
```

Stop the foreground container with `Ctrl+C`. Port `18080` is used to avoid conflicts with a local Jenkins controller that may already use port `8080`.

### Run the smoke test independently

From **WSL2 Ubuntu**, with Docker and `curl` available:

```bash
bash scripts/smoke-test-image.sh pipelineforge-api:local
```

The script starts a temporary container on an available localhost port, verifies the health and site endpoints, and cleans up the container afterward.

## Jenkins CI pipeline

Jenkins reads [`Jenkinsfile`](Jenkinsfile) from this repository. The local setup uses:

- `linux-agent-1` for compiling and testing Java.
- A node labeled `docker-builder` for building and smoke-testing images with access to the local Docker engine.

Pipeline stages:

1. Checkout
2. Verify Environment
3. Compile
4. Unit Tests
5. Package
6. Build Docker Image
7. Smoke Test Docker Image

Jenkins publishes JUnit test results and archives the executable JAR on success. The Docker build records image metadata, including an image ID, as a build artifact. Images are stored in the **local Docker engine**, not pushed to a registry.

**Security:** Docker daemon access is powerful. The Docker builder is intended for trusted local code. Do not run untrusted pull requests with access to the host Docker daemon; do not publish credentials or Jenkins agent secrets.

## Kubernetes on kind (experimental)

After the Kubernetes manifest branch is available locally:

```bash
kind get clusters
# Create the cluster only if it does not already exist:
kind create cluster --name pipelineforge --wait 180s

kubectl get nodes --context kind-pipelineforge

docker build -t pipelineforge-api:k8s-v1 ./services/pipelineforge-api
kind load docker-image pipelineforge-api:k8s-v1 --name pipelineforge

kubectl --context kind-pipelineforge apply -f k8s/namespace.yaml
kubectl --context kind-pipelineforge apply -f k8s/deployment.yaml
kubectl --context kind-pipelineforge apply -f k8s/service.yaml

kubectl --context kind-pipelineforge -n pipelineforge rollout status deployment/pipelineforge-api --timeout=180s
kubectl --context kind-pipelineforge -n pipelineforge get pods,services
```

Use a separate terminal to forward the internal Service to a localhost port:

```bash
kubectl --context kind-pipelineforge -n pipelineforge port-forward --address 127.0.0.1 service/pipelineforge-api 18081:80
```

Then test:

```bash
curl -fsS http://127.0.0.1:18081/actuator/health
curl -fsS http://127.0.0.1:18081/api/sites
```

`kind load docker-image` is necessary because the local kind nodes have their own image storage. The manifest uses `imagePullPolicy: Never` to prevent fetching a non-published image from a remote registry.

To diagnose a Pod:

```bash
kubectl --context kind-pipelineforge -n pipelineforge get pods
kubectl --context kind-pipelineforge -n pipelineforge describe deployment pipelineforge-api
kubectl --context kind-pipelineforge -n pipelineforge logs deployment/pipelineforge-api --tail=100
```

## Reliability learning outcomes

The project intentionally explores two distinct failure classes:

1. **Build/test failure:** An incorrect JUnit assertion stops the pipeline before release packaging.
2. **Runtime verification failure:** A container can build and start but fail the smoke test if its health endpoint is unavailable. Jenkins then rejects the candidate.

A failed pipeline is considered useful evidence of a working quality gate, not a successful release. Controlled failures are performed on experiment branches and restored before returning to `main`.

## Roadmap

- [x] Spring Boot monitoring API and input validation
- [x] JUnit tests and Jenkins CI
- [x] Multi-stage Docker image build and versioned local images
- [x] Automated container smoke tests and failure-recovery exercises
- [ ] Merge local Kubernetes Deployment / Service manifests
- [ ] Practice Kubernetes rolling updates and rollback
- [ ] Add automated deployment verification with controlled promotion
- [ ] Introduce observability and metrics (Prometheus / Grafana)
- [ ] Explore infrastructure automation and optional read-only developer tooling

## Portfolio and evidence

Repository: [EmmaTsak/pipelineforge](https://github.com/EmmaTsak/pipelineforge)

The accompanying portfolio case study may include screenshots of actual Jenkins pipeline stages, Gradle test results, and controlled failure/recovery experiments. Screenshots should be labelled by outcome and sanitized to avoid revealing private credentials or unrelated local services.

## Disclaimer

PipelineForge is an educational, **local-only** engineering project, not Apple's Radar platform and not a production-critical communications service. Concepts are modelled through a simulated site-monitoring API and repeatable CI/CD exercises.

# SentinelOps Architecture

## Purpose

SentinelOps is a local-first DevOps learning platform
built around a simulated communication site monitoring
service.

## Planned Components

- Java Spring Boot monitoring API
- MySQL database
- Python heartbeat simulator
- Docker Compose local environment
- Jenkins CI/CD automation
- SonarQube code quality analysis
- Artifact management
- Prometheus and Grafana monitoring

## Data Flow

1. Simulated communication sites send heartbeat data.
2. The Java application validates incoming requests.
3. MySQL stores site and incident information.
4. Monitoring tools collect system health metrics.
5. Jenkins builds, tests and releases application versions.

## Deployment Strategy

All application services and infrastructure run locally.

GitHub stores source code, documentation and development
history. No public production environment is required.
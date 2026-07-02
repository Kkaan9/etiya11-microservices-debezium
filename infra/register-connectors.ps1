# Registers the Debezium outbox connectors (order + product) against the Kafka Connect
# REST API. Run this once after `podman compose -f podman-compose.yml up -d` and the
# kafka-connect container has finished starting.
#
# Usage:
#   cd infra
#   .\register-connectors.ps1

$ErrorActionPreference = "Stop"

$connectUrl = "http://localhost:8083"
$connectorDir = Join-Path $PSScriptRoot "debezium"
$connectorFiles = @("order-outbox-connector.json", "product-outbox-connector.json")

Write-Host "Waiting for Kafka Connect at $connectUrl ..."
$maxAttempts = 30
$attempt = 0
while ($true) {
    $attempt++
    try {
        Invoke-RestMethod -Uri "$connectUrl/connectors" -Method Get -TimeoutSec 5 | Out-Null
        break
    } catch {
        if ($attempt -ge $maxAttempts) {
            Write-Error "Kafka Connect did not become ready after $maxAttempts attempts."
            exit 1
        }
        Start-Sleep -Seconds 2
    }
}
Write-Host "Kafka Connect is ready."

foreach ($file in $connectorFiles) {
    $path = Join-Path $connectorDir $file
    $config = Get-Content -Raw -Path $path
    $name = (ConvertFrom-Json $config).name

    $existing = $null
    try {
        $existing = Invoke-RestMethod -Uri "$connectUrl/connectors/$name" -Method Get -TimeoutSec 5
    } catch {
        $existing = $null
    }

    if ($existing) {
        Write-Host "Connector '$name' already registered, updating config..."
        $configOnly = (ConvertFrom-Json $config).config | ConvertTo-Json -Depth 10
        Invoke-RestMethod -Uri "$connectUrl/connectors/$name/config" -Method Put -Body $configOnly -ContentType "application/json" | Out-Null
    } else {
        Write-Host "Registering connector '$name'..."
        Invoke-RestMethod -Uri "$connectUrl/connectors" -Method Post -Body $config -ContentType "application/json" | Out-Null
    }

    $status = $null
    for ($i = 0; $i -lt 10; $i++) {
        try {
            $status = Invoke-RestMethod -Uri "$connectUrl/connectors/$name/status" -Method Get -TimeoutSec 5
            break
        } catch {
            Start-Sleep -Seconds 1
        }
    }
    if ($status) {
        Write-Host "  state=$($status.connector.state)"
    } else {
        Write-Host "  (status not available yet, check manually)"
    }
}

Write-Host "Done. Check status with: Invoke-RestMethod http://localhost:8083/connectors/order-outbox-connector/status"

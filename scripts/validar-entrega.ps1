# Executar em um PowerShell normal, com o Docker Desktop ativo.
# Coleta evidencias e roda a suite Maven sem instalar Java/Maven no host.
# Nao faz commit, push, deploy, nem remove containers ou volumes.
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$evidenceRoot = Join-Path $projectRoot 'docs\evidencias\validacao-local'
New-Item -ItemType Directory -Force -Path $evidenceRoot | Out-Null
Push-Location $projectRoot
try {
    docker version 2>&1 | Tee-Object -FilePath (Join-Path $evidenceRoot 'docker-version.txt')
    if ($LASTEXITCODE -ne 0) { throw 'Docker indisponivel. Abra o Docker Desktop e tente novamente.' }
    docker compose version | Out-File (Join-Path $evidenceRoot 'compose-version.txt') -Encoding utf8
    docker compose config --quiet
    if ($LASTEXITCODE -ne 0) { throw 'Configuracao do Compose invalida.' }

    docker run --rm -v "${projectRoot}:/app" -w /app maven:3.9.9-eclipse-temurin-21 mvn -B clean verify 2>&1 |
        Tee-Object -FilePath (Join-Path $evidenceRoot 'maven-verify.txt')
    $mavenExit = $LASTEXITCODE
    if (Test-Path 'target\surefire-reports') {
        Copy-Item 'target\surefire-reports' $evidenceRoot -Recurse -Force
    }
    if ($mavenExit -ne 0) { throw "Maven retornou codigo $mavenExit. Consulte maven-verify.txt." }

    docker compose ps -a 2>&1 | Out-File (Join-Path $evidenceRoot 'containers.txt') -Encoding utf8
    $health = Invoke-RestMethod 'http://localhost:8080/actuator/health' -TimeoutSec 15
    $info = Invoke-RestMethod 'http://localhost:8080/actuator/info' -TimeoutSec 15
    $health | ConvertTo-Json -Depth 10 | Out-File (Join-Path $evidenceRoot 'health.json') -Encoding utf8
    $info | ConvertTo-Json -Depth 10 | Out-File (Join-Path $evidenceRoot 'info.json') -Encoding utf8
    if ($health.status -ne 'UP') { throw 'Health diferente de UP.' }
    if ($info.app.environment -ne 'local') { throw 'Ambiente diferente de local.' }
    [ordered]@{captured_at=(Get-Date -Format o);maven_exit=$mavenExit;health=$health.status;environment=$info.app.environment} |
        ConvertTo-Json | Out-File (Join-Path $evidenceRoot 'resultado.json') -Encoding utf8
    Write-Host "Validacao concluida. Evidencias em: $evidenceRoot"
} finally {
    Pop-Location
}

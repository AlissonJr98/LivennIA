param(
    [int]$VersaoCodigo = 2,
    [string]$VersaoNome = "1.1.0",
    [string]$Notas = "Nova atualizacao publicada pelo GitHub."
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

Write-Host "1. Compilando APK..." -ForegroundColor Cyan
.\gradlew.bat assembleDebug --console=plain

$apkOrigem = Join-Path $PSScriptRoot "app\build\outputs\apk\debug\app-debug.apk"
$pastaAtu = Join-Path $PSScriptRoot "atualizacao"
New-Item -ItemType Directory -Force -Path $pastaAtu | Out-Null
$apkDestino = Join-Path $pastaAtu "livennia.apk"

Copy-Item $apkOrigem $apkDestino -Force

$json = @{
    versaoCodigo = $VersaoCodigo
    versaoNome   = $VersaoNome
    notas        = $Notas
    apkUrl       = "https://raw.githubusercontent.com/AlissonJr98/LivennIA/main/atualizacao/livennia.apk"
} | ConvertTo-Json -Depth 4

Set-Content -Path (Join-Path $pastaAtu "versao.json") -Value $json -Encoding UTF8

Write-Host "2. Enviando para o GitHub..." -ForegroundColor Cyan
git add .
git commit -m "Release v$VersaoNome (build $VersaoCodigo) - $Notas"
git push origin main

Write-Host "Pronto! O aplicativo ja pode atualizar pelo GitHub para a v$VersaoNome." -ForegroundColor Green

@echo off
title LivennIA - Servidor + Cloudflare Tunnel
echo ========================================================
echo        Iniciando Servidor LivennIA com Cloudflare
echo ========================================================
echo.

start "LivennIA Server" cmd /k "python -m uvicorn servidor:app --host 0.0.0.0 --port 8000 --app-dir motor_historia_ia"

timeout /t 2 >nul

echo.
echo Iniciando Cloudflare Tunnel...
echo Guarde a URL "https://...trycloudflare.com" exibida abaixo
echo e cole nas Configuracoes do aplicativo LivennIA.
echo ========================================================
echo.

.\cloudflared.exe tunnel --url http://127.0.0.1:8000

pause

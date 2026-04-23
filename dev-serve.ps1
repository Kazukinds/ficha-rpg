# Dev server Eclipse — hot-reload + LAN + QR pro celular
# Uso: powershell -ExecutionPolicy Bypass -File dev-serve.ps1

$port = 8080

$ip = (Get-NetIPAddress -AddressFamily IPv4 -ErrorAction SilentlyContinue |
       Where-Object { $_.IPAddress -notmatch '^(127\.|169\.254\.)' -and $_.PrefixOrigin -ne 'WellKnown' } |
       Select-Object -First 1).IPAddress

if (-not $ip) { $ip = 'localhost' }

$url = "http://${ip}:${port}/"

Write-Host ""
Write-Host "============================================" -ForegroundColor Green
Write-Host " Eclipse Dev Server (hot-reload)" -ForegroundColor Green
Write-Host "============================================" -ForegroundColor Green
Write-Host ""
Write-Host " Local:  http://localhost:${port}/" -ForegroundColor Cyan
Write-Host " LAN:    $url" -ForegroundColor Yellow
Write-Host ""
Write-Host " Celular: conecte na mesma Wi-Fi e abra $url" -ForegroundColor Yellow
Write-Host " Reload automatico quando salvar arquivo" -ForegroundColor DarkGray
Write-Host ""
Write-Host " Chrome DevTools desktop: F12 -> Ctrl+Shift+M (device mode)"
Write-Host " Ctrl+C pra parar"
Write-Host "============================================" -ForegroundColor Green
Write-Host ""

try {
  Invoke-Expression "npx --yes qrcode-terminal `"$url`""
} catch {
  Write-Host "(QR code requer internet pra baixar qrcode-terminal)" -ForegroundColor DarkGray
}

Write-Host ""

# live-server: reload automatico em qualquer save, abre browser desktop, permite LAN
# --host 0.0.0.0 expoe pra LAN (celular)
# --port fixo 8080
# --no-browser desabilita o default (sobrescrevemos com URL especifico)
# --ignore pasta build/android
npx --yes live-server . `
  --host=0.0.0.0 `
  --port=$port `
  --open=/index.html `
  --watch=index.html,biblioteca.html,sw.js,manifest.webmanifest,icons,assets `
  --ignore="Android,android-python,build-app,.github,docs,widgets,node_modules"

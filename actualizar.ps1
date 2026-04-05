$ErrorActionPreference = "Stop"
$repo    = "Yiop8900/SistemaActivos"
$jarName = "Activos-1.0-SNAPSHOT.jar"
$jarPath = Join-Path $PSScriptRoot $jarName
$jreExe  = Join-Path $PSScriptRoot "jre\bin\javaw.exe"

function Mostrar-Error($msg) {
    $wshell = New-Object -ComObject Wscript.Shell
    $wshell.Popup($msg, 0, "Sistema de Activos", 0x10) | Out-Null
}

# Find Java (bundled JRE first, then system)
$javaExe = $null
if (Test-Path $jreExe) {
    $javaExe = $jreExe
} else {
    $j = Get-Command javaw -ErrorAction SilentlyContinue
    if ($j) { $javaExe = $j.Source }
}
if (-not $javaExe) {
    Mostrar-Error "Java no encontrado.`n`nDescarga Java 17 desde:`nhttps://adoptium.net/temurin/releases/?version=17"
    Start-Process "https://adoptium.net/temurin/releases/?version=17"
    exit 1
}

# Auto-update check
try {
    $headers = @{ "User-Agent" = "SistemaActivos-Launcher" }
    $release = Invoke-RestMethod -Uri "https://api.github.com/repos/$repo/releases/latest" -Headers $headers -UseBasicParsing
    $asset = $release.assets | Where-Object { $_.name -eq $jarName } | Select-Object -First 1
    if ($asset) {
        $localSize = if (Test-Path $jarPath) { (Get-Item $jarPath).Length } else { -1 }
        if ($localSize -ne $asset.size) {
            $tmp = "$jarPath.tmp"
            Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $tmp -UseBasicParsing
            Move-Item $tmp $jarPath -Force
        }
    }
} catch {
    # If update check fails, continue with local JAR
}

# Verify JAR exists
if (-not (Test-Path $jarPath)) {
    Mostrar-Error "No se encontro el archivo de la aplicacion.`nReinstala el programa desde GitHub."
    exit 1
}

# Launch
Start-Process $javaExe -ArgumentList "-jar `"$jarPath`""

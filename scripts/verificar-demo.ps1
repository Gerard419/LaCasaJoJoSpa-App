# Ejecutar desde la raíz: powershell -ExecutionPolicy Bypass -File .\scripts\verificar-demo.ps1
# No realiza cambios ni publica archivos. Sirve para comprobar que los 3 servicios estén accesibles.
$servicios = @(
    @{Nombre='usuarios'; Url='http://localhost:8081/api/usuarios'; Esperado=403},
    @{Nombre='menu';     Url='http://localhost:8082/api/platos';    Esperado=200},
    @{Nombre='pedidos';  Url='http://localhost:8083/api/pedidos';   Esperado=200}
)
$fallos = 0
foreach ($servicio in $servicios) {
    $codigo = $null
    try {
        $respuesta = Invoke-WebRequest -UseBasicParsing -Method Get -Uri $servicio.Url -TimeoutSec 5
        $codigo = [int]$respuesta.StatusCode
    } catch {
        if ($_.Exception.Response) {
            $codigo = [int]$_.Exception.Response.StatusCode
        }
    }
    if ($codigo -eq $servicio.Esperado) {
        Write-Host "OK: $($servicio.Nombre) responde HTTP $codigo" -ForegroundColor Green
    } else {
        Write-Host "FALLA: $($servicio.Nombre). Esperado $($servicio.Esperado), recibido $codigo" -ForegroundColor Red
        $fallos++
    }
}
if ($fallos -gt 0) { exit 1 } else { exit 0 }
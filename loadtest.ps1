# Script de test de charge pour Windows PowerShell
param(
    [int]$BookId = 1,
    [int]$Requests = 50
)

$ErrorActionPreference = "Stop"

# On répartit sur 3 instances
$Ports = @(8081, 8083, 8084)

Write-Host "== Load test ==" -ForegroundColor Cyan
Write-Host "BookId=$BookId Requests=$Requests"
Write-Host "Ports=$($Ports -join ', ')"
Write-Host ""

# Créer un répertoire temporaire
$tmpdir = Join-Path $env:TEMP "loadtest_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
New-Item -ItemType Directory -Path $tmpdir -Force | Out-Null

$successFile = Join-Path $tmpdir "success.txt"
$conflictFile = Join-Path $tmpdir "conflict.txt"
$otherFile = Join-Path $tmpdir "other.txt"

# Initialiser les fichiers
"" | Out-File -FilePath $successFile -Encoding utf8
"" | Out-File -FilePath $conflictFile -Encoding utf8
"" | Out-File -FilePath $otherFile -Encoding utf8

$jobs = @()

for ($i = 1; $i -le $Requests; $i++) {
    $port = $Ports[($i - 1) % 3]
    $url = "http://localhost:${port}/api/books/${BookId}/borrow"
    
    $job = Start-Job -ScriptBlock {
        param($url, $port, $tmpdir, $index)
        
        try {
            $bodyFile = Join-Path $tmpdir "body_$index.json"
            
            # Invoke-WebRequest pour obtenir status et body
            $response = Invoke-WebRequest -Uri $url -Method POST -UseBasicParsing -ErrorAction SilentlyContinue
            $status = $response.StatusCode
            $body = $response.Content
            
            $body | Out-File -FilePath $bodyFile -Encoding utf8
            
            return @{
                Port = $port
                Status = $status
                Body = $body
                Type = "success"
            }
        }
        catch {
            $status = $_.Exception.Response.StatusCode.value__
            $bodyFile = Join-Path $tmpdir "body_$index.json"
            
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $body = $reader.ReadToEnd()
                $reader.Close()
                $stream.Close()
            }
            catch {
                $body = ""
            }
            
            $body | Out-File -FilePath $bodyFile -Encoding utf8
            
            if ($status -eq 409) {
                return @{
                    Port = $port
                    Status = $status
                    Body = $body
                    Type = "conflict"
                }
            }
            else {
                return @{
                    Port = $port
                    Status = $status
                    Body = $body
                    Type = "other"
                }
            }
        }
    } -ArgumentList $url, $port, $tmpdir, $i
    
    $jobs += $job
}

Write-Host "Attente de la fin des $Requests requêtes..." -ForegroundColor Yellow

# Attendre tous les jobs
$results = $jobs | Wait-Job | Receive-Job

# Traiter les résultats
$successCount = 0
$conflictCount = 0
$otherCount = 0

foreach ($result in $results) {
    $line = "$($result.Port) $($result.Status) $($result.Body)"
    
    switch ($result.Type) {
        "success" {
            $line | Out-File -FilePath $successFile -Append -Encoding utf8
            $successCount++
        }
        "conflict" {
            $line | Out-File -FilePath $conflictFile -Append -Encoding utf8
            $conflictCount++
        }
        "other" {
            $line | Out-File -FilePath $otherFile -Append -Encoding utf8
            $otherCount++
        }
    }
}

# Nettoyer les jobs
$jobs | Remove-Job -Force

Write-Host ""
Write-Host "== Résultats ==" -ForegroundColor Green
Write-Host "Success (200):  $successCount"
Write-Host "Conflict (409): $conflictCount"
Write-Host "Other:          $otherCount"
Write-Host ""
Write-Host "Fichiers détails: $tmpdir" -ForegroundColor Cyan
Write-Host " - success.txt  : appels OK"
Write-Host " - conflict.txt : stock épuisé (normal)"
Write-Host " - other.txt    : erreurs à diagnostiquer"

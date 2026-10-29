<#
 * @author HoangTLT
#>

$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../frontend'))
Push-Location $project
try {
    if (-not (Test-Path 'node_modules')) {
        & npm.cmd ci
        if ($LASTEXITCODE -ne 0) { throw 'npm ci failed.' }
    }
    & npm.cmd run dev
} finally { Pop-Location }

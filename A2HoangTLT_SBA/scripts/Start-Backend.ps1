<#
 * @author HoangTLT
#>

param(
    [switch]$SqlLogin,
    [string]$SqlHost = 'localhost',
    [int]$Port = 1433
)
$ErrorActionPreference = 'Stop'
$project = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../backend'))
if (-not $SqlLogin) {
    $native = Join-Path $project 'native'
    New-Item -ItemType Directory -Force -Path $native | Out-Null
    $library = Join-Path $native 'mssql-jdbc_auth-12.10.2.x64.dll'
    if (-not (Test-Path -LiteralPath $library)) {
        Invoke-WebRequest 'https://repo.maven.apache.org/maven2/com/microsoft/sqlserver/mssql-jdbc_auth/12.10.2.x64/mssql-jdbc_auth-12.10.2.x64.dll' -OutFile $library
    }
    $env:DB_URL = "jdbc:sqlserver://${SqlHost}:$Port;databaseName=A2HoangTLT_SBA301;encrypt=true;trustServerCertificate=true;integratedSecurity=true"
    $env:DB_USERNAME = ''
    $env:DB_PASSWORD = ''
    $env:PATH = "$native;$env:PATH"
}
Push-Location $project
try {
    & '.\mvnw.cmd' '-B' '-ntp' 'spring-boot:run'
    if ($LASTEXITCODE -ne 0) { throw 'Backend failed to start. Check the database connection and Java installation.' }
} finally { Pop-Location }

<#
 * @author HoangTLT
#>

param(
    [string]$Server = '.\SQL2022',
    [string]$Database = 'A2HoangTLT_SBA301'
)
$ErrorActionPreference = 'Stop'
if ($Database -notmatch '^A2[A-Za-z0-9_]+$') { throw 'Database name must begin with A2 and contain only letters, numbers and underscores.' }
$connection = New-Object System.Data.SqlClient.SqlConnection "Server=$Server;Database=master;Integrated Security=True;TrustServerCertificate=True;Connection Timeout=10"
try {
    $connection.Open()
    $command = $connection.CreateCommand()
    $command.CommandText = "IF DB_ID(@name) IS NULL BEGIN EXEC('CREATE DATABASE [$Database]') END"
    [void]$command.Parameters.AddWithValue('@name', $Database)
    [void]$command.ExecuteNonQuery()
    Write-Host "Database $Database is ready on $Server. Spring Data JPA will create the tables."
} finally { $connection.Dispose() }

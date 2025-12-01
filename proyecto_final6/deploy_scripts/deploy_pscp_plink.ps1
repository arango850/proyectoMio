<#
deploy_pscp_plink.ps1
Alternative deploy script using PuTTY tools (pscp + plink). Requires pscp.exe and plink.exe in PATH.
This script will copy the ZIP to remote hosts and optionally run remote commands via plink.
It expects key-based authentication or will prompt for password per invocation.

Example usage:
.\deploy_pscp_plink.ps1 -Hosts ("x104m02".."x104m30") -User myuser -RemotePath C:\sitm -ZipPath ..\deploy_package.zip -StartWorkers
#>
param(
    [Parameter(Mandatory=$true)]
    [string[]] $Hosts,
    [Parameter(Mandatory=$true)]
    [string] $User,
    [string] $ZipPath = "..\deploy_package.zip",
    [string] $RemotePath = "C:\sitm",
    [switch] $StartWorkers,
    [int] $WorkerPort = 20001,
    [string] $WorkerScript = "deploy_scripts\\run-worker-javafx-fixed.cmd"
)

if (-not (Get-Command pscp -ErrorAction SilentlyContinue)) {
    Write-Error "pscp.exe not found in PATH. Install PuTTY or put pscp.exe in PATH."
    exit 2
}
if (-not (Get-Command plink -ErrorAction SilentlyContinue)) {
    Write-Warning "plink.exe not found in PATH. Remote start will prompt for authentication interactively if used."
}
if (-not (Test-Path -Path $ZipPath)) { Write-Error "ZipPath '$ZipPath' not found."; exit 2 }

foreach ($h in $Hosts) {
    Write-Host "==> Deploying to $h"
    $remoteZip = Join-Path -Path $RemotePath -ChildPath (Split-Path -Leaf $ZipPath)

    # Create remote path via plink (will prompt for password if no key)
    $mkdirCmd = "powershell -NoProfile -Command \"if (-not (Test-Path -Path '$RemotePath')) { New-Item -Path '$RemotePath' -ItemType Directory | Out-Null }\""
    & plink $User@$h -batch $mkdirCmd

    Write-Host " Copying $ZipPath -> $h:$remoteZip"
    & pscp -batch -scp $ZipPath "$User@$h:$remoteZip"

    # Expand archive remotely
    $expandCmd = "powershell -NoProfile -Command \"Expand-Archive -Path '$remoteZip' -DestinationPath '$RemotePath' -Force\""
    & plink $User@$h -batch $expandCmd

    if ($StartWorkers) {
        $workerScriptRemote = Join-Path -Path $RemotePath -ChildPath $WorkerScript
        $runCmd = "cmd /c `"%COMSPEC% /c `"$workerScriptRemote`" $h $RemotePath\\datagrams4history.csv $RemotePath\\outbox x104m01 > $RemotePath\\worker_start.log 2>&1`""
        & plink $User@$h -batch $runCmd
    }
}
Write-Host "Deploy completed." -ForegroundColor Green

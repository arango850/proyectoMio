<#
deploy_psremoting.ps1
Copies deploy_package.zip to a list of Windows hosts using PowerShell Remoting (WinRM / PS-Remoting).
Requires: PowerShell Remoting enabled on target hosts and network access.

Usage examples:
# Prompt for credentials and copy to default worker hosts, do NOT start processes:
.\deploy_psremoting.ps1 -Hosts ("x104m02".."x104m30") -ZipPath "..\deploy_package.zip" -DestinationPath "C:\sitm"

# With credentials and start workers after deploy:
$cred = Get-Credential
.\deploy_psremoting.ps1 -Hosts ("x104m02".."x104m30") -ZipPath "..\deploy_package.zip" -DestinationPath "C:\sitm" -Credential $cred -StartWorkers
#>
param(
    [Parameter(Mandatory=$true)]
    [string[]] $Hosts,

    [string] $ZipPath = "..\deploy_package.zip",
    [string] $DestinationPath = "C:\sitm",
    [System.Management.Automation.PSCredential] $Credential = $null,
    [switch] $StartWorkers,
    [int] $WorkerPort = 20001,
    [string] $WorkerScript = "deploy_scripts\\run-worker-javafx-fixed.cmd",
    [switch] $ForceRecreateSession
)

function Ensure-Session {
    param($computer, $cred)
    try {
        if ($ForceRecreateSession) { Remove-PSSession -ComputerName $computer -ErrorAction SilentlyContinue }
        $s = New-PSSession -ComputerName $computer -Credential $cred -ErrorAction Stop
        return $s
    } catch {
        Write-Error "Failed to create PSSession to $computer: $_"
        return $null
    }
}

if (-not (Test-Path -Path $ZipPath)) {
    Write-Error "ZipPath '$ZipPath' not found. Run this script from repository root or provide correct path."
    exit 2
}

if (-not $Credential) {
    Write-Host "Enter credentials for remote hosts:" -ForegroundColor Yellow
    $Credential = Get-Credential
}

foreach ($h in $Hosts) {
    Write-Host "==> Deploying to $h" -ForegroundColor Cyan
    $session = Ensure-Session -computer $h -cred $Credential
    if (-not $session) { continue }

    $remoteZip = Join-Path -Path $DestinationPath -ChildPath (Split-Path -Leaf $ZipPath)

    # Ensure destination dir
    Invoke-Command -Session $session -ScriptBlock {
        param($dest)
        if (-not (Test-Path -Path $dest)) { New-Item -Path $dest -ItemType Directory | Out-Null }
    } -ArgumentList $DestinationPath

    Write-Host " Copying zip to $h:$remoteZip"
    Copy-Item -ToSession $session -Path $ZipPath -Destination $remoteZip -Force -ErrorAction Stop

    Write-Host " Expanding archive on $h"
    Invoke-Command -Session $session -ScriptBlock {
        param($zip,$dest)
        try {
            Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction SilentlyContinue
            Expand-Archive -Path $zip -DestinationPath $dest -Force
            Write-Output "Expanded $zip to $dest"
        } catch {
            Write-Error "Failed to expand archive: $_"
            throw
        }
    } -ArgumentList $remoteZip,$DestinationPath

    if ($StartWorkers) {
        # Start worker script on remote host
        $workerScriptRemote = Join-Path -Path $DestinationPath -ChildPath $WorkerScript
        $workerHostArg = $h
        $cmd = "cmd.exe /c `"$workerScriptRemote`" $workerHostArg `"$DestinationPath\\datagrams4history.csv`" `"$DestinationPath\\outbox`" `"x104m01`" > `"$DestinationPath\\worker_start.log`" 2>&1"
        Write-Host " Starting worker on $h (background)"
        Invoke-Command -Session $session -ScriptBlock { param($c) Start-Process -FilePath cmd.exe -ArgumentList '/c', $c -WindowStyle Hidden } -ArgumentList $cmd
    }

    # Close session
    Remove-PSSession -Session $session
}

Write-Host "Deploy finished." -ForegroundColor Green

$ErrorActionPreference = "Stop"

$repoRoot = "D:\Development\Projects\NotesTakingApp\NotesTakingApp"
$target = Join-Path $repoRoot ".agents"
$sids = @(
    "S-1-5-21-2940798146-3405269374-4030305030-2457193577",
    "S-1-5-21-604852583-2710539897-3139010460-3241538012"
)

Set-Location $repoRoot

Write-Host "Target: $target"
Write-Host "Taking ownership..."
takeown /F $target /R /D Y | Out-Host

Write-Host "Removing explicit deny ACEs from .agents..."
$acl = Get-Acl $target
foreach ($sidText in $sids) {
    $sid = [System.Security.Principal.SecurityIdentifier]::new($sidText)
    $denyRules = @(
        $acl.Access | Where-Object {
            $_.IdentityReference -eq $sid -and
            $_.AccessControlType -eq [System.Security.AccessControl.AccessControlType]::Deny
        }
    )

    foreach ($rule in $denyRules) {
        [void]$acl.RemoveAccessRuleSpecific($rule)
        Write-Host "Removed deny: $sidText"
    }
}
Set-Acl -Path $target -AclObject $acl

Write-Host "Enabling inheritance and propagating child ACLs..."
icacls $target /inheritance:e /T /C | Out-Host

Write-Host "Granting access..."
icacls $target /grant:r "DESKTOP-9FGHILH\hwdav:(OI)(CI)F" /T /C | Out-Host
icacls $target /grant:r "DESKTOP-9FGHILH\CodexSandboxUsers:(OI)(CI)M" /T /C | Out-Host

Write-Host "Testing write access..."
$testPath = Join-Path $target "write-test.tmp"
Set-Content -Path $testPath -Value "test"
Remove-Item -Path $testPath

Write-Host "Done. Current .agents ACL:"
icacls $target | Out-Host

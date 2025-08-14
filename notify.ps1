# Universal Windows Toast Notification for Claude Code
# Usage: powershell -ExecutionPolicy Bypass -File "notify.ps1" -Message "Message" -Title "Title" -Type "Information"

param(
    [string]$Message = "Claude Code notification",
    [string]$Title = "Claude Code",
    [string]$Type = "Information"  # Information, Warning, Error, Question
)

Write-Host "[$Title] $Message"

# Map notification types to MessageBox icons
$IconType = switch ($Type.ToLower()) {
    "warning" { "Warning" }
    "error" { "Error" }
    "question" { "Question" }
    default { "Information" }
}

# Try Windows MessageBox Notification
try {
    Add-Type -AssemblyName System.Windows.Forms
    [System.Windows.Forms.MessageBox]::Show($Message, $Title, "OK", $IconType)
    Write-Host "Notification displayed successfully!"
} catch {
    Write-Host "Notification displayed in console only."
}
$base = Get-Location
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add('http://localhost:3000/')
$listener.Start()
Write-Host "Static server on http://localhost:3000 (Ctrl+C to stop)"
try {
  while ($listener.IsListening) {
    $context = $listener.GetContext()
    $path = $context.Request.Url.LocalPath.TrimStart('/')
    if ([string]::IsNullOrWhiteSpace($path)) { $path = 'index.html' }
    $file = Join-Path $base $path
    if (Test-Path $file) {
      $bytes = [System.IO.File]::ReadAllBytes($file)
      $context.Response.StatusCode = 200
      $ext = [System.IO.Path]::GetExtension($file)
      switch ($ext) {
        '.html' { $context.Response.ContentType = 'text/html' }
        '.js'   { $context.Response.ContentType = 'application/javascript' }
        '.css'  { $context.Response.ContentType = 'text/css' }
        '.png'  { $context.Response.ContentType = 'image/png' }
        '.jpg'  { $context.Response.ContentType = 'image/jpeg' }
        default { $context.Response.ContentType = 'application/octet-stream' }
      }
      $context.Response.OutputStream.Write($bytes, 0, $bytes.Length)
    }
    else {
      $context.Response.StatusCode = 404
    }
    $context.Response.Close()
  }
}
finally {
  $listener.Stop()
  $listener.Close()
}

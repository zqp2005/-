$body = '{"sessionId":"test123","message":"查一下报修进度"}'
$response = Invoke-RestMethod -Uri "http://localhost:8090/ai/chat" -Method POST -ContentType "application/json" -Body $body
$response
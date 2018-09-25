if "%1"=="--debug" (
  java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -jar %~dp0\guest-web-server\webserver\build\libs\mindstorms-webserver-all-1.0.0-SNAPSHOT.jar --port 80 --root %~dp0\guest-web-site\docs
) else (
  java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=n -jar %~dp0\guest-web-server\webserver\build\libs\mindstorms-webserver-1.0.0-SNAPSHOT.jar --port 80 --root %~dp0\guest-web-site\docs
)

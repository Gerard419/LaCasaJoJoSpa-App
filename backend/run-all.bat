@echo off
REM Levanta los 3 microservicios (requiere Java 17+ y Maven) en 3 ventanas
cd /d %~dp0
start "usuarios-service :8081" cmd /k "cd usuarios-service && mvn spring-boot:run"
start "menu-service :8082" cmd /k "cd menu-service && mvn spring-boot:run"
start "pedidos-service :8083" cmd /k "cd pedidos-service && mvn spring-boot:run"

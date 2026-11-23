#!/usr/bin/env bash
# Levanta los 3 microservicios (requiere Java 17+ y Maven). Ctrl+C los detiene.
cd "$(dirname "$0")"
for s in usuarios-service menu-service pedidos-service; do
  (cd $s && mvn -q spring-boot:run) &
done
wait

"""Resumen reproducible de instrucciones cubiertas en el código lógico de EP4.

Usa el XML de JaCoCo generado por Gradle. No modifica el informe global ni los
porcentajes originales. Permite que docente y alumnos inspeccionen los paquetes
incluidos (se listan abajo) en un cálculo ponderado por instrucciones.
"""
from pathlib import Path
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
REPORT_DIR = ROOT / "app" / "build" / "reports" / "coverage" / "test" / "debug"

# Código de lógica de negocio; excluye código Compose, adaptadores de sistema
# Android (cámara/vibración/DataStore) y configuración/firmas de Retrofit.
PREFIXES = (
    "com/app/lacasajojo/repository/",
    "com/app/lacasajojo/viewmodel/",
    "com/app/lacasajojo/model/",
)
PURE_UTILS = (
    "com/app/lacasajojo/utils/ValidacionesKt",
    "com/app/lacasajojo/utils/FormatoKt",
    "com/app/lacasajojo/utils/ClimaTextoKt",
)


def is_logic(name):
    return name.startswith(PREFIXES) or name.startswith(PURE_UTILS)


def main():
    files = [p for p in REPORT_DIR.rglob("*.xml") if p.is_file()]
    report = None
    for p in files:
        try:
            tree = ET.parse(p)
            if tree.getroot().tag == "report":
                report = tree.getroot()
                print("Informe XML:", p)
                break
        except ET.ParseError:
            pass
    if report is None:
        print("No hay informe JaCoCo XML en", REPORT_DIR)
        print("Ejecuta .\\gradlew.bat createDebugUnitTestCoverageReport")
        print("Comprueba que la tarea genere un reporte XML además de index.html.")
        return 2
    covered = missed = 0
    count = 0
    for package in report.findall("package"):
        for cls in package.findall("class"):
            name = cls.get("name", "")
            if not is_logic(name):
                continue
            instructions = next((c for c in cls.findall("counter") if c.get("type") == "INSTRUCTION"), None)
            if instructions is None:
                continue
            ncovered = int(instructions.get("covered", "0"))
            nmissed = int(instructions.get("missed", "0"))
            covered += ncovered
            missed += nmissed
            count += 1
    total = covered + missed
    if not total:
        print("No se encontraron contadores de instrucciones para las clases indicadas.")
        return 2
    percent = covered * 100 / total
    print(f"Código lógico analizado: {count} clases; {covered} cubiertas de {total} instrucciones = {percent:.1f} %")
    print("Alcance: repository, viewmodel, model y utils puras (Validaciones, Formato, ClimaTexto).")
    print("El porcentaje GLOBAL de JaCoCo sigue siendo el de index.html, no se reemplaza.")
    return 0 if percent >= 80 else 1


if __name__ == "__main__":
    sys.exit(main())

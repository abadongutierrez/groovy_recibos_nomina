## Script de groovy para sacar un reporte de recibos de nomina

### Usage:

```
groovy ReadRecibosNomina.groovy /path/al/directorio/con/los/zips
```

### Output

Archivo de texto con los siguientes datos:

```
Tipo, FechaInicialPago, FechaFinalPago, TotalPercepciones, TotalDeducciones, TotalNeto
```

el archivo de texto tiene le nombre `report.txt` y se crea en el mismo directorio donde `/path/al/directorio/con/los/zips`
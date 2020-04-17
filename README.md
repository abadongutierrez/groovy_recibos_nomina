## Scripts de groovy para sacar un reporte de recibos de nomina de Totales y Detalles

### Usage:

```
groovy ReadTotalesRecibosNomina.groovy /path/al/directorio/con/los/zipsOXmls
```

o

```
groovy ReadDetallesRecibosNomina.groovy /path/al/directorio/con/los/zipsOXmls
```

El script lee los archivos xml y zip del folder que se especifica y los copia o extrae a un subdirectorio `/extracted`.

### Output de ReadTotalesRecibosNomina.groovy

Archivo de texto con los siguientes datos:

```
Tipo, FechaInicialPago, FechaFinalPago, TotalPercepciones, TotalDeducciones, TotalNeto
```

el archivo de texto tiene le nombre `reporte_totales.txt` y se crea en el mismo directorio donde `/path/al/directorio/con/los/zipsOXmls`

### Output de ReadDetallesRecibosNomina.groovy

Archivo de texto con los siguientes datos:

```
TipoNomina, Instancia, FechaInicialPago, FechaFinalPago, Tipo, Clave, Concepto, PercepcionImporteGravado, PercepcionImporteExento, DeduccionImporte
```

el archivo de texto tiene le nombre `reporte_detalles.txt` y se crea en el mismo directorio donde `/path/al/directorio/con/los/zipsOXmls`
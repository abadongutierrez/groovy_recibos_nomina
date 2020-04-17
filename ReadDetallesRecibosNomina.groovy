import java.text.SimpleDateFormat

try {
    def baseDir = args[0]
    def extractedDir = args[0] + "/extracted"
    def reportFileName = args[0] + "/reporte_detalles.txt"

    def zipsDir = new File(baseDir)
    if (zipsDir.isDirectory()) {
        def ant = new AntBuilder()   // create an antbuilder
        zipsDir.listFiles().toList().forEach {
            if (it.name.endsWith(".zip")) {
                ant.unzip(src: it.absolutePath, dest: extractedDir, overwrite: "true")
            } else if (it.name.endsWith(".xml")) {
                ant.copy(file: it.absolutePath, todir: extractedDir, overwrite: "true")
            }
        }
    } else {
        println "Sorry, not a directory. Try again."
        System.exit(0)
    }

    def pattern = "yyyy-MM-dd";
    def simpleDateFormat = new SimpleDateFormat(pattern);

    def file = new File(extractedDir)
    if (file.isDirectory()) {
        def comprobantes = []
        file.listFiles().toList().forEach {
            if (it.name.endsWith(".xml")) {
                def comprobanteXml = new XmlSlurper().parseText(it.text)

                def nomina = new Nomina([
                        fechaInicialPago: new Date().parse('yyyy-MM-dd', comprobanteXml.Complemento.Nomina["@FechaInicialPago"].text()),
                        fechaFinalPago: new Date().parse('yyyy-MM-dd', comprobanteXml.Complemento.Nomina["@FechaFinalPago"].text()),
                        totalDeducciones: new BigDecimal(comprobanteXml.Complemento.Nomina["@TotalDeducciones"].text()),
                        totalPercepciones: new BigDecimal(comprobanteXml.Complemento.Nomina["@TotalPercepciones"].text()),
                        tipo: comprobanteXml.Complemento.Nomina["@TipoNomina"].text()])
                
                comprobanteXml.Complemento.Nomina.Percepciones.Percepcion.forEach {
                    def percepcion = new Percepcion([
                        tipo: it["@TipoPercepcion"].text(),
                        clave: it["@Clave"].text(),
                        concepto: it["@Concepto"].text(),
                        importeGravado: new BigDecimal(it["@ImporteGravado"].text()),
                        importeExento: new BigDecimal(it["@ImporteExento"].text())
                    ])
                    nomina.addPercepcion(percepcion)
                }

                comprobanteXml.Complemento.Nomina.Deducciones.Deduccion.forEach {
                    def deduccion = new Deduccion([
                        tipo: it["@TipoDeduccion"].text(),
                        clave: it["@Clave"].text(),
                        concepto: it["@Concepto"].text(),
                        importe: new BigDecimal(it["@Importe"].text())
                    ])
                    nomina.addDeduccion(deduccion)
                }

                def comprobante = new Comprobante([
                    emisor: new Persona([nombre: comprobanteXml.Emisor["@Nombre"], rfc: comprobanteXml.Emisor["@Rfc"]]),
                    receptor: new Persona([nombre: comprobanteXml.Receptor["@Nombre"], rfc: comprobanteXml.Receptor["@Rfc"]]),
                    nomina: nomina
                ])

                comprobantes.add(comprobante)
            }
        }
        def sorted = comprobantes.toSorted {a, b -> a.nomina.fechaInicialPago <=> b.nomina.fechaInicialPago }

        def reportFile = new File(reportFileName)
        print "Creating Report file ${reportFile.absolutePath} ... "
        if (reportFile.exists()) {
            reportFile.delete()
        }

        reportFile <<  "TipoNomina, Instancia, FechaInicialPago, FechaFinalPago, Tipo, Clave, Concepto, PercepcionImporteGravado, PercepcionImporteExento, DeduccionImporte\n"
        sorted.forEach { c ->
            c.nomina.percepciones.forEach { p ->
                reportFile << "$c.nomina.tipo, Percepcion, $c.nomina.formattedFechaInicialPago, $c.nomina.formattedFechaFinalPago, $p.tipo, $p.clave, $p.concepto, $p.importeGravado, $p.importeExento, 0.00\n"
            }
            c.nomina.deducciones.forEach { d ->
                reportFile << "$c.nomina.tipo, Deduccion, $c.nomina.formattedFechaInicialPago, $c.nomina.formattedFechaFinalPago, $d.tipo, $d.clave, $d.concepto, 0.00, 0.00, $d.importe\n"
            }
        }
        println "done"
        
        def extractedDirFile = new File(extractedDir)
        print "Deleting ${extractedDirFile.absolutePath} ... "
        if (extractedDirFile.exists()) {
            extractedDirFile.delete()
        } 
        println "done"
    } else {
        println "Sorry, /extracted dir not found. Try again."
        System.exit(0)
    }
} catch (Exception ex) {
    println "Error: ${ex.message}"
}

@groovy.transform.ToString
class Comprobante {
    Persona emisor
    Persona receptor
    Nomina nomina
}

@groovy.transform.Sortable(includes = ['fechaInicialPago', 'fechaFinalPago'])
@groovy.transform.ToString
class Nomina {
    Date fechaInicialPago
    Date fechaFinalPago
    BigDecimal totalDeducciones
    BigDecimal totalPercepciones
    String tipo
    def deducciones = []
    def percepciones = []

    def getTotalNeto() {
        return totalPercepciones - totalDeducciones
    }

    def getFormattedFechaInicialPago() {
        return new SimpleDateFormat('yyyy-MM-dd').format(fechaInicialPago)
    }

    def getFormattedFechaFinalPago() {
        return new SimpleDateFormat('yyyy-MM-dd').format(fechaFinalPago)
    }

    def addDeduccion(d) {
        deducciones.add(d)
    }

    def addPercepcion(p) {
        percepciones.add(p)
    }
}

@groovy.transform.Sortable
class Persona {
    String nombre
    String rfc

    String toString() {
        return "${nombre} - ${rfc}"
    }
}

class Percepcion {
    String tipo
    String clave
    String concepto
    BigDecimal importeGravado
    BigDecimal importeExento
}

class Deduccion {
    String tipo
    String clave
    String concepto
    String importe
}



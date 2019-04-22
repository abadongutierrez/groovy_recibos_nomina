import java.text.SimpleDateFormat

try {
    def baseDir = args[0]
    def extractedDir = args[0] + "/extracted"
    def reportFileName = args[0] + "/report.txt"

    def zipsDir = new File(baseDir)
    if (zipsDir.isDirectory()) {
        def ant = new AntBuilder()   // create an antbuilder
        zipsDir.listFiles().toList().forEach {
            if (it.name.endsWith(".zip")) {
                ant.unzip(src: it.absolutePath, dest: extractedDir, overwrite: "true")
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

                def comprobante = new Comprobante([
                    emisor: new Persona([nombre: comprobanteXml.Emisor["@Nombre"], rfc: comprobanteXml.Emisor["@Rfc"]]),
                    receptor: new Persona([nombre: comprobanteXml.Receptor["@Nombre"], rfc: comprobanteXml.Receptor["@Rfc"]]),
                    nomina: new Nomina([
                        fechaInicialPago: new Date().parse('yyyy-MM-dd', comprobanteXml.Complemento.Nomina["@FechaInicialPago"].text()),
                        fechaFinalPago: new Date().parse('yyyy-MM-dd', comprobanteXml.Complemento.Nomina["@FechaFinalPago"].text()),
                        totalDeducciones: new BigDecimal(comprobanteXml.Complemento.Nomina["@TotalDeducciones"].text()),
                        totalPercepciones: new BigDecimal(comprobanteXml.Complemento.Nomina["@TotalPercepciones"].text()),
                        tipo: comprobanteXml.Complemento.Nomina["@TipoNomina"].text()
                    ])
                ])

                comprobantes.add(comprobante)
            }
        }
        def sorted = comprobantes.toSorted {a, b -> a.nomina.fechaInicialPago <=> b.nomina.fechaInicialPago }
        def acumulado = new BigDecimal(0.0)

        def reportFile = new File(reportFileName)
        print "Creating Report file ${reportFile.absolutePath} ... "
        if (reportFile.exists()) {
            reportFile.delete()
        }

        reportFile <<  "Tipo, FechaInicialPago, FechaFinalPago, TotalPercepciones, TotalDeducciones, TotalNeto\n"
        sorted.forEach {
            acumulado = acumulado + it.nomina.totalPercepciones
            reportFile << "$it.nomina.tipo, $it.nomina.formattedFechaInicialPago, $it.nomina.formattedFechaFinalPago, $it.nomina.totalPercepciones, $it.nomina.totalDeducciones, $it.nomina.totalNeto\n"
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

@groovy.transform.Sortable
@groovy.transform.ToString
class Comprobante {
    Persona emisor
    Persona receptor
    Nomina nomina
}

@groovy.transform.Sortable
@groovy.transform.ToString
class Nomina {
    Date fechaInicialPago
    Date fechaFinalPago
    BigDecimal totalDeducciones
    BigDecimal totalPercepciones
    String tipo

    def getTotalNeto() {
        return totalPercepciones - totalDeducciones
    }

    def getFormattedFechaInicialPago() {
        return new SimpleDateFormat('yyyy-MM-dd').format(fechaInicialPago)
    }

    def getFormattedFechaFinalPago() {
        return new SimpleDateFormat('yyyy-MM-dd').format(fechaFinalPago)
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



package com.centyllion.db

import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import java.nio.file.Path
import java.util.*
import kotlin.io.path.writeText

class Export: Common(
    name = "export",
    help = """
        Export a simulation to Netbiodyn format
    """,
) {

    val output: Path? by option("--output", "-o", help = "Output file (stdout is none is given)")
        .path()

    val id: UUID by argument()
        .convert { UUID.fromString(it) }

    override fun run() {

        val database = database()
        val result = database.databaseQueries.selectSimulation(id).executeAsOne()
        val content = buildString {
            appendLine("{")
            appendLine("  \"model\":${result.model},")
            appendLine("  \"simulation\":${result.simulation}")
            appendLine("}")
        }

        if (output != null) {
            // write to output
            echo("Writing simulation $id to $output")
            output!!.writeText(content)
        } else {
            // write to stdout
           echo(content)
        }
    }
}
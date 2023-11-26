package com.centyllion.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.convert
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import org.postgresql.ds.PGSimpleDataSource
import java.nio.file.Path
import java.util.*
import kotlin.io.path.writeText

fun main(args: Array<String>) = Db()
    .subcommands(Export())
    .main(args)

class Db: CliktCommand(
    name = "centyllion-db", printHelpOnEmptyArgs = true,
    help = """
       Interrogate centyllion legacy db
    """,
) {

    override fun run() {
    }
}

abstract class Common(name: String?, help: String = ""): CliktCommand(help = help, name = name) {

    val host: String by option("--host", "-h", envvar = "PGHOST", help = "Database host")
        .default("localhost")

    val port: Int by option("--port", "-p", envvar = "PGPORT", help = "Database port")
        .convert { it.toInt() }
        .default(5432)

    val dbname: String by option("--db", "-d", envvar = "PGDATABASE", help = "Database name")
        .default("netbiodyn")

    val user: String by option("--user", "-u", envvar = "PGUSER", help = "User name")
        .default("postgres")

    val password: String? by option("--password", "-w", envvar = "PGPASSWORD", help = "User password")

    fun database(): Database {
        val dataSource = PGSimpleDataSource()
        dataSource.serverNames = arrayOf(host)
        dataSource.portNumbers = IntArray(1) { port }
        dataSource.databaseName = dbname
        dataSource.user = user
        dataSource.password = password

        val driver: SqlDriver = dataSource.asJdbcDriver()
        return Database(driver)
    }

}

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

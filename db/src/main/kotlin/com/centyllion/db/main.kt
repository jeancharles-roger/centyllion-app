package com.centyllion.db

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import org.postgresql.ds.PGSimpleDataSource

fun main(args: Array<String>) = Db()
    .subcommands(Export(), Server())
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

    val pgHost: String by option(
        "--pg-host", envvar = "PGHOST", help = "Database host"
    ).default("localhost")

    val pgPort: Int by option(
        "--pg-port", envvar = "PGPORT", help = "Database port"
    )
        .convert { it.toInt() }
        .default(5432)

    val pgDbname: String by option(
        "--pg-db", envvar = "PGDATABASE", help = "Database name"
    ).default("netbiodyn")

    val pgUser: String by option(
        "--pg-user", envvar = "PGUSER", help = "User name"
    ).default("postgres")

    val pgPassword: String? by option(
        "--pg-password", envvar = "PGPASSWORD", help = "User password"
    )

    fun database(): Database {
        val dataSource = PGSimpleDataSource()
        dataSource.serverNames = arrayOf(pgHost)
        dataSource.portNumbers = IntArray(1) { pgPort }
        dataSource.databaseName = pgDbname
        dataSource.user = pgUser
        dataSource.password = pgPassword

        val driver: SqlDriver = dataSource.asJdbcDriver()
        return Database(driver)
    }

}


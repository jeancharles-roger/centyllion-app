package com.centyllion.backend.data

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction

private fun Transaction.createIndex(indexName: String, sourceTable: Table, searchedField: Column<*>) {
    val searchableTextColumnName = "searchable"

    // adds simulation description tsvector column
    exec("alter table ${sourceTable.tableName} add column $searchableTextColumnName tsvector")
    // updates existing simulation searches
    exec("update ${sourceTable.tableName} set $searchableTextColumnName = to_tsvector('english', ${searchedField.name})")
    // creates index table
    exec("create index $indexName on ${sourceTable.tableName} using gin ($searchableTextColumnName)")
    // creates trigger to update searchable
    exec("""
        create trigger ${indexName}update before insert or update
        on ${sourceTable.tableName} for each row execute procedure
        tsvector_update_trigger($searchableTextColumnName, 'pg_catalog.english', ${searchedField.name})
    """)
}

class Migration(
    val from: Int,
    val to: Int,
    val update: Transaction.() -> Unit
)

val migrations = listOf(
    Migration(0, 1) {
        createIndex(
            "simulationDescription_textIndex",
            DbSimulationDescriptions,
            DbSimulationDescriptions.simulation
        )
        createIndex(
            "modelDescription_textIndex",
            DbModelDescriptions,
            DbModelDescriptions.model
        )
    }
)

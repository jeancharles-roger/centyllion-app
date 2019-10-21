package com.centyllion.backend.data

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction

private fun Transaction.createIndex(
    indexName: String, sourceTable: Table,
    searchedField: Column<*>, searchableTextColumnName: String = "searchable",
    dictionary: String = "pg_catalog.english"
) {
    // adds simulation description tsvector column
    //exec("alter table ${sourceTable.tableName} add column $searchableTextColumnName tsvector")

    // updates existing simulation searches
    exec("update ${sourceTable.tableName} set $searchableTextColumnName = to_tsvector('$dictionary', ${searchedField.name})")
    // creates index table
    exec("create index $indexName on ${sourceTable.tableName} using gin ($searchableTextColumnName)")
    // creates trigger to update searchable
    exec("""
        create trigger ${indexName}update before insert or update
        on ${sourceTable.tableName} for each row execute procedure
        tsvector_update_trigger($searchableTextColumnName, '$dictionary', ${searchedField.name})
    """)
}

private fun Transaction.dropColumn(table: String, column: String) {
    exec("alter table $table drop column if exists \"$column\"")
}

class Migration(
    val to: Int,
    val update: Transaction.() -> Unit
)

val migrations = listOf(
    Migration(1) {
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
    },
    Migration(2) {
        createIndex(
            "modelDescription_tagIndex",
            DbModelDescriptions,
            DbModelDescriptions.tags,
            "tags_searchable",
            "pg_catalog.simple"
        )
    },
    Migration(3) {
        exec("update infodescriptions set \"readAccess\" = true")
        dropColumn("infodescriptions", "cloneAccess")
        dropColumn("users", "roles")
        dropColumn("users", "stripe")
        dropColumn("users", "subscription")
        dropColumn("users", "subscriptionUpdatedOn")
        exec("drop table if exists subscriptions")
    }
)

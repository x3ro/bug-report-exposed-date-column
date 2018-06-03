package example

import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

object Transactions : IntIdTable() {
    val name = varchar("name", 32)
    val date = date("date")
}

fun main(args: Array<String>) {
	TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
	Database.connect("jdbc:sqlite:example.sqlite", "org.sqlite.JDBC")
	TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        logger.addLogger(StdOutSqlLogger)
        SchemaUtils.create(Transactions)

        println("\nInserting example data")
        val someDate = DateTime.parse("2017-12-01")

        Transactions.insert {
            it[name] = "test"
            it[date] = someDate
        }

        println(Transactions.select { Transactions.date.eq(someDate) }.toList())
    }

    println("\nRetrieving without Exposed")
    val connection = DriverManager.getConnection("jdbc:sqlite:example.sqlite")
    val statement = connection.createStatement()
    val rs = statement.executeQuery("select * from Transactions")
    while (rs.next()) {
        // read the result set
        println("name = " + rs.getString("name"))
        println("date = " + rs.getString("date"))
    }
}

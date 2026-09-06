package ir.atiran.hamrah.viewer.data

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Read-only JDBC repository for the Atiran2 SQL Server database.
 *
 * The app connects directly to SQL Server using the DB user supplied in
 * settings (preset: 37.143.147.19 / 9595 / Atiran2 / AdminAn).
 * Every query is a plain SELECT — no INSERT/UPDATE/DELETE exists here.
 */
class AtiranDbRepository(private val db: AtiranDbSettings) {

    private val url: String by lazy { db.jdbcUrl() }

    suspend fun testConnection(): String = withContext(Dispatchers.IO) {
        open().use { c ->
            c.createStatement().executeQuery("SELECT DB_NAME(), @@VERSION").use { rs ->
                rs.next()
                "${rs.getString(1)} | ${rs.getString(2).take(70)}"
            }
        }
    }

    suspend fun dashboard(): DbDashboard = withContext(Dispatchers.IO) {
        open().use { c ->
            val customers = queryCustomers(c, limit = 3000)
            val products = queryProducts(c, limit = 5000)
            val inventory = queryInventory(c, limit = 8000)
            val checks = queryChecks(c, limit = 4000)
            val factors = queryFactors(c, limit = 5000)
            val visitors = queryVisitors(c)

            DbDashboard(
                customers = customers,
                products = products,
                inventory = inventory,
                checks = checks,
                factors = factors,
                visitors = visitors,
                customerCount = customers.size,
                productCount = products.size,
                checkCount = checks.size,
                factorCount = factors.size,
                visitorCount = visitors.size,
                totalDebt = customers.sumOf { it.man ?: 0.0 },
                totalCredit = customers.sumOf { it.cred ?: 0.0 },
                totalCheckAmount = checks.sumOf { it.amount },
                totalSales = factors.sumOf { it.allFel ?: it.sumLineAll ?: 0.0 },
                totalTax = factors.sumOf { it.tax ?: 0.0 },
                totalTakhfif = factors.sumOf { it.tafif ?: 0.0 },
            )
        }
    }

    // ------------------------------------------------------------------ open
    private fun open(): Connection {
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver")
        return DriverManager.getConnection(url, db.user, db.password)
    }

    // ------------------------------------------------------------------ customers
    private fun queryCustomers(c: Connection, limit: Int): List<DbCustomer> =
        runQuery(c, """
            SELECT TOP $limit SHMO, MONAME, tell1, tell2, cell, addre, sharh,
                   cred, man, active, group_rdf, vis_rdf, shomare_masir, lat, lng
            FROM Customer
            ORDER BY man DESC
        """) { rs ->
            DbCustomer(
                shmo = rs.intOrNull("SHMO"),
                name = rs.str("MONAME"),
                tell1 = rs.str("tell1"),
                tell2 = rs.str("tell2"),
                cell = rs.str("cell"),
                address = rs.str("addre"),
                sharh = rs.str("sharh"),
                cred = rs.dblOrNull("cred"),
                man = rs.dblOrNull("man"),
                active = rs.str("active"),
                groupRdf = rs.intOrNull("group_rdf"),
                visRdf = rs.intOrNull("vis_rdf"),
                masir = rs.intOrNull("shomare_masir"),
                lat = rs.dblOrNull("lat"),
                lng = rs.dblOrNull("lng"),
            )
        }

    private fun queryProducts(c: Connection, limit: Int): List<DbProduct> =
        runQuery(c, """
            SELECT TOP $limit shka, naka, coka, vahsanj, mohvah, mojkavah, mojkajoz,
                   bastebandi, tedbastebandi, buy_price, inventory_price, FinalSalePrice,
                   pavarez, ptax, group_rdf, active
            FROM inventory
            ORDER BY shka
        """) { rs ->
            DbProduct(
                shka = rs.longOrNull("shka") ?: 0,
                name = rs.str("naka"),
                code = rs.str("coka"),
                unit = rs.str("vahsanj"),
                mohvah = rs.longOrNull("mohvah"),
                mojkavah = rs.dblOrNull("mojkavah") ?: 0.0,
                mojkajoz = rs.intOrNull("mojkajoz") ?: 0,
                bast = rs.str("bastebandi"),
                tedBast = rs.dblOrNull("tedbastebandi") ?: 0.0,
                buyPrice = rs.dblOrNull("buy_price") ?: 0.0,
                invPrice = rs.dblOrNull("inventory_price") ?: 0.0,
                finalPrice = rs.dblOrNull("FinalSalePrice") ?: 0.0,
                pTax = rs.dblOrNull("ptax") ?: 0.0,
                pAvarez = rs.dblOrNull("pavarez") ?: 0.0,
                groupRdf = rs.intOrNull("group_rdf") ?: 0,
                active = rs.str("active"),
            )
        }

    private fun queryInventory(c: Connection, limit: Int): List<DbInventoryRow> =
        runQuery(c, """
            SELECT TOP $limit ia.shka, i.naka, ia.rdf_anbars, a.name, ia.mojkavah, ia.mojkajoz
            FROM inventory_anbars ia
            LEFT JOIN inventory i ON i.shka = ia.shka
            LEFT JOIN anbar a ON a.rdf_anbar = ia.rdf_anbars
            ORDER BY ia.mojkavah DESC
        """) { rs ->
            DbInventoryRow(
                shka = rs.longOrNull("shka") ?: 0,
                name = rs.str("naka"),
                anbar = rs.intOrNull("rdf_anbars") ?: 0,
                anbarName = rs.str("name"),
                moj = rs.dblOrNull("mojkavah") ?: 0.0,
                mojkajoz = rs.intOrNull("mojkajoz") ?: 0,
            )
        }

    private fun queryChecks(c: Connection, limit: Int): List<DbCheck> =
        runQuery(c, """
            SELECT TOP $limit rdf, getdate, sardate, getchkshhes, getchbank, getchkshobe,
                   shgetchk, getchkmab, shmo, chk_satus, getchkdis
            FROM getchk
            ORDER BY rdf DESC
        """) { rs ->
            DbCheck(
                rdf = rs.longOrNull("rdf") ?: 0,
                date = rs.str("getdate"),
                sarDate = rs.str("sardate"),
                issuer = rs.str("getchkshhes"),
                bank = rs.str("getchbank"),
                branch = rs.str("getchkshobe"),
                serial = rs.str("shgetchk"),
                amount = rs.dblOrNull("getchkmab") ?: 0.0,
                shmo = rs.longOrNull("shmo"),
                status = rs.intOrNull("chk_satus") ?: 0,
                description = rs.str("getchkdis"),
            )
        }

    private fun queryFactors(c: Connection, limit: Int): List<DbFactor> =
        runQuery(c, """
            SELECT TOP $limit r.rdf__, r.shfacfo, r.date, r.shmo, r.moname,
                   r.[all] AS all_fel, r.tafif, r.tax, r.avarez,
                   r.sumlineall, r.MabDaryaftFactor, r.tdf, r.Status, r.done_date, r.ismodify
            FROM sailfact r
            ORDER BY r.rdf__ DESC
        """) { rs ->
            DbFactor(
                rdf = rs.longOrNull("rdf__") ?: 0,
                shFactor = rs.longOrNull("shfacfo") ?: 0,
                date = rs.str("date"),
                shmo = rs.intOrNull("shmo"),
                customerName = rs.str("moname"),
                allFel = rs.dbl("all_fel"),
                tafif = rs.dbl("tafif"),
                tax = rs.dbl("tax"),
                avarez = rs.dbl("avarez"),
                sumLineAll = rs.dbl("sumlineall"),
                mabDaryaft = rs.dbl("MabDaryaftFactor"),
                tdf = rs.dbl("tdf"),
                status = rs.intOrNull("Status") ?: 0,
                doneDate = rs.str("done_date"),
                isModify = rs.str("ismodify"),
            )
        }

    private fun queryVisitors(c: Connection): List<DbVisitor> =
        runQuery(c, """
            SELECT vis_rdf, vis_name, vis_tell1, vis_tell2, vis_cell, active
            FROM visitor
            ORDER BY vis_name
        """) { rs ->
            DbVisitor(
                rdf = rs.intOrNull("vis_rdf") ?: 0,
                name = rs.str("vis_name"),
                tell1 = rs.str("vis_tell1"),
                tell2 = rs.str("vis_tell2"),
                cell = rs.str("vis_cell"),
                active = rs.str("active"),
            )
        }

    // ------------------------------------------------------------------ generic
    private inline fun <T> runQuery(c: Connection, sql: String, map: (ResultSet) -> T): List<T> =
        c.createStatement().use { st ->
            st.executeQuery(sql).use { rs ->
                buildList { while (rs.next()) add(map(rs)) }
            }
        }

    private fun ResultSet.str(col: String): String? = try { getString(col) } catch (_: Exception) { null }
    private fun ResultSet.intOrNull(col: String): Int? = try { getInt(col).takeUnless { wasNull() } } catch (_: Exception) { null }
    private fun ResultSet.longOrNull(col: String): Long? = try { getLong(col).takeUnless { wasNull() } } catch (_: Exception) { null }
    private fun ResultSet.dbl(col: String): Double = try { getDouble(col) } catch (_: Exception) { 0.0 }
    private fun ResultSet.dblOrNull(col: String): Double? = try { getDouble(col).takeUnless { wasNull() } } catch (_: Exception) { null }

    companion object {
        val OPTIONAL_TABLES = listOf("Customer", "inventory", "inventory_anbars", "anbar", "getchk", "sailfact", "visitor")
    }
}

// ------------------------------------------------------------------ config
data class AtiranDbSettings(
    val host: String = ConnectionPreset.DB_HOST,
    val port: String = ConnectionPreset.DB_PORT,
    val dbName: String = ConnectionPreset.DB_NAME,
    val user: String = ConnectionPreset.DB_USER,
    val password: String = ConnectionPreset.DB_PASSWORD,
) {
    fun jdbcUrl(): String =
        "jdbc:sqlserver://$host:$port;databaseName=$dbName;encrypt=false;trustServerCertificate=true;loginTimeout=15"
}

// ------------------------------------------------------------------ db models
data class DbCustomer(
    val shmo: Int?,
    val name: String?,
    val tell1: String?,
    val tell2: String?,
    val cell: String?,
    val address: String?,
    val sharh: String?,
    val cred: Double?,
    val man: Double?,
    val active: String?,
    val groupRdf: Int?,
    val visRdf: Int?,
    val masir: Int?,
    val lat: Double?,
    val lng: Double?,
)

data class DbProduct(
    val shka: Long,
    val name: String?,
    val code: String?,
    val unit: String?,
    val mohvah: Long?,
    val mojkavah: Double,
    val mojkajoz: Int,
    val bast: String?,
    val tedBast: Double,
    val buyPrice: Double,
    val invPrice: Double,
    val finalPrice: Double,
    val pTax: Double,
    val pAvarez: Double,
    val groupRdf: Int,
    val active: String?,
)

data class DbInventoryRow(
    val shka: Long,
    val name: String?,
    val anbar: Int,
    val anbarName: String?,
    val moj: Double,
    val mojkajoz: Int,
)

data class DbCheck(
    val rdf: Long,
    val date: String?,
    val sarDate: String?,
    val issuer: String?,
    val bank: String?,
    val branch: String?,
    val serial: String?,
    val amount: Double,
    val shmo: Long?,
    val status: Int,
    val description: String?,
)

data class DbFactor(
    val rdf: Long,
    val shFactor: Long,
    val date: String?,
    val shmo: Int?,
    val customerName: String?,
    val allFel: Double,
    val tafif: Double,
    val tax: Double,
    val avarez: Double,
    val sumLineAll: Double,
    val mabDaryaft: Double,
    val tdf: Double,
    val status: Int,
    val doneDate: String?,
    val isModify: String?,
)

data class DbVisitor(
    val rdf: Int,
    val name: String?,
    val tell1: String?,
    val tell2: String?,
    val cell: String?,
    val active: String?,
)

data class DbDashboard(
    val customers: List<DbCustomer>,
    val products: List<DbProduct>,
    val inventory: List<DbInventoryRow>,
    val checks: List<DbCheck>,
    val factors: List<DbFactor>,
    val visitors: List<DbVisitor>,
    val customerCount: Int,
    val productCount: Int,
    val checkCount: Int,
    val factorCount: Int,
    val visitorCount: Int,
    val totalDebt: Double,
    val totalCredit: Double,
    val totalCheckAmount: Double,
    val totalSales: Double,
    val totalTax: Double,
    val totalTakhfif: Double,
)

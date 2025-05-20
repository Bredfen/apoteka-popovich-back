package com.bredfen

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import org.bson.Document
import org.bson.types.ObjectId

@Serializable
/**
 * Data class representing a Product for MongoDB storage.
 * @property id Optional string representation of MongoDB ObjectId
 * @property title Title of the product
 * @property subtitle Subtitle or description of the product
 * @property price Price of the product
 * @property imageLink URL pointing to the product image
 */
data class Product(
    val id: String? = null,
    val title: String,
    val subtitle: String,
    val price: Double,
    val imageLink: String
) {
    /**
     * Convert this Product to a BSON Document, mapping 'id' to '_id' if present.
     */
    fun toDocument(): Document = Document().apply {
        id?.let { append("_id", ObjectId(it)) }
        append("title", title)
        append("subtitle", subtitle)
        append("price", price)
        append("imageLink", imageLink)
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        /**
         * Convert a BSON Document to a Product, extracting '_id' as 'id'.
         */
        fun fromDocument(document: Document): Product = Product(
            id = document.getObjectId("_id").toString(),
            title = document.getString("title"),
            subtitle = document.getString("subtitle"),
            price = document.getDouble("price"),
            imageLink = document.getString("imageLink")
        )
    }
}

/**
 * Service for CRUD operations on the 'products' collection.
 */
class ProductService(private val database: MongoDatabase) {
    private val collection: MongoCollection<Document>

    init {
        // Ensure 'products' collection exists
        val existing = database.listCollectionNames().toList()
        if (!existing.contains("products")) {
            database.createCollection("products")
        }
        collection = database.getCollection("products")
    }

    /**
     * Insert a new product and return its generated ID.
     */
    suspend fun create(product: Product): String = withContext(Dispatchers.IO) {
        val doc = product.toDocument()
        collection.insertOne(doc)
        doc.getObjectId("_id").toString()
    }

    /**
     * Find a product by its ID, returning null if not found.
     */
    suspend fun read(id: String): Product? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id)))
            .first()
            ?.let(Product::fromDocument)
    }

    /**
     * Replace an existing product document by ID, returning the old document or null.
     */
    suspend fun update(id: String, product: Product): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(
            Filters.eq("_id", ObjectId(id)),
            product.toDocument()
        )
    }

    /**
     * Delete a product by its ID, returning the deleted document or null.
     */
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }

    /**
     * Search for products by title or subtitle, returning a list of matching products.
     */
    suspend fun search(search: String?): List<Product> = withContext(Dispatchers.IO) {
        val filter = if (search != null) {
            Filters.or(
                Filters.regex("title", search, "i"),
                Filters.regex("subtitle", search, "i")
            )
        } else {
            Filters.empty()
        }
        collection.find(filter)
            .map(Product::fromDocument)
            .toList()
    }
}
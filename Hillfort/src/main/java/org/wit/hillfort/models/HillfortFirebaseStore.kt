package org.wit.hillfort.models

import android.content.Context
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.jetbrains.anko.AnkoLogger
import org.wit.hillfort.Hillforts
import org.wit.hillfort.activities.HillfortSharedPreferences
import org.wit.hillfort.helpers.exists
import org.wit.hillfort.helpers.read
import org.wit.hillfort.helpers.write
import java.util.*

val HILLFORT_JSON_FILE = "hillforts.json"
val gsonBuilder = GsonBuilder().setPrettyPrinting().create()
val listType = object : TypeToken<java.util.ArrayList<HillfortModel>>() {}.type


fun generateRandomHillfortId(): Long {
    return Random().nextLong()
}

class HillfortJSONStore : HillfortStore, AnkoLogger {

    var hillfortDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference



    val context: Context
    var hillforts = mutableListOf<HillfortModel>()

    constructor (context: Context) {
        this.context = context
        if (exists(context, HILLFORT_JSON_FILE)) {
            deserialize()
        }
    }

    override fun findAll(): MutableList<HillfortModel> {
        return hillforts
    }

    override fun create(hillfort: HillfortModel) {

        val mypreference = HillfortSharedPreferences(context)

        hillfort.id = generateRandomHillfortId()

        val key = hillfortDatabase.child("users").child(mypreference.getCurrentUserID().toString()).child("hillforts").push().key

        hillfort.fbId = key!!

        hillforts.add(hillfort)

        hillfortDatabase.child("users").child(mypreference.getCurrentUserID().toString()).child(Hillforts.FIREBASE_TASK).child(key).setValue(hillfort)

        serialize()
    }

    fun clear() {
        hillforts.clear()
    }

    override fun update(hillfort: HillfortModel) {

        val mypreference = HillfortSharedPreferences(context)

        var foundHillfort: HillfortModel? = hillforts.find { p -> p.id == hillfort.id }
        if (foundHillfort != null) {
            foundHillfort.title = hillfort.title
            foundHillfort.description = hillfort.description
            foundHillfort.addNotes = hillfort.addNotes
            foundHillfort.visited = hillfort.visited
            foundHillfort.favourited = hillfort.favourited
            foundHillfort.rating = hillfort.rating
            foundHillfort.dateVisited = hillfort.dateVisited
            foundHillfort.lat = hillfort.lat
            foundHillfort.lng = hillfort.lng
            foundHillfort.zoom = hillfort.zoom
            foundHillfort.address = hillfort.address
            foundHillfort.firstImage = hillfort.firstImage
            foundHillfort.secondImage = hillfort.secondImage
            foundHillfort.thirdImage = hillfort.thirdImage
            foundHillfort.fourthImage = hillfort.fourthImage
            serialize()
        }
        hillfortDatabase.child("users").child(mypreference.getCurrentUserID().toString()).child(Hillforts.FIREBASE_TASK).child(hillfort.fbId).setValue(hillfort)
    }

    override fun delete(hillfort: HillfortModel) {
        val mypreference = HillfortSharedPreferences(context)
        hillfortDatabase.child("users").child(mypreference.getCurrentUserID().toString()).child(Hillforts.FIREBASE_TASK).child(hillfort.fbId).removeValue()
        hillforts.remove(hillfort)
        serialize()
    }

    private fun serialize() {
        val jsonString = gsonBuilder.toJson(hillforts, listType)
        write(context, HILLFORT_JSON_FILE, jsonString)
    }

    private fun deserialize() {
        val jsonString = read(context, HILLFORT_JSON_FILE)
        hillforts = Gson().fromJson(jsonString, listType)
    }
}
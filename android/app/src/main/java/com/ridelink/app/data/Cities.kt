package com.ridelink.app.data

// v1 city lookup: no map picker / geocoding yet (that's the maps phase). The user picks from
// this list and we send the paired coordinates with the city name.
data class TunisianCity(val name: String, val lat: Double, val lon: Double)

object Cities {
    val ALL = listOf(
        TunisianCity("Tunis", 36.8065, 10.1815),
        TunisianCity("Sfax", 34.7406, 10.7603),
        TunisianCity("Sousse", 35.8256, 10.6084),
        TunisianCity("Monastir", 35.7780, 10.8262),
        TunisianCity("Hammamet", 36.4000, 10.6167),
        TunisianCity("Nabeul", 36.4513, 10.7357),
        TunisianCity("Gabes", 33.8815, 10.0982),
        TunisianCity("Bizerte", 37.2744, 9.8739),
        TunisianCity("Kairouan", 35.6781, 10.0963),
        TunisianCity("Gafsa", 34.4250, 8.7842),
        TunisianCity("Kasserine", 35.1676, 8.8365),
        TunisianCity("Medenine", 33.3549, 10.5055),
        TunisianCity("Tozeur", 33.9197, 8.1335),
        TunisianCity("Djerba", 33.8076, 10.8451),
        TunisianCity("Ariana", 36.8625, 10.1956),
        TunisianCity("Ben Arous", 36.7533, 10.2282),
    )
}

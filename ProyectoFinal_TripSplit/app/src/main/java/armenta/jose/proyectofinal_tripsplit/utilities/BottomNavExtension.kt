package armenta.jose.proyectofinal_tripsplit.utilities

import android.app.Activity
import android.content.Intent
import com.google.android.material.bottomnavigation.BottomNavigationView
import armenta.jose.proyectofinal_tripsplit.R
import armenta.jose.proyectofinal_tripsplit.RegistrarGastos
import armenta.jose.proyectofinal_tripsplit.pantalla_principal
import armenta.jose.proyectofinal_tripsplit.reporte_gastos


fun Activity.setupBottomNavigation(bottomNavigation: BottomNavigationView, currentScreenId: Int) {
    bottomNavigation.selectedItemId = currentScreenId

    bottomNavigation.setOnItemSelectedListener { item ->
        if (item.itemId == currentScreenId) {
            return@setOnItemSelectedListener true
        }

        when (item.itemId) {
            R.id.navigation_reporte_gastos -> {
                startActivity(Intent(this, reporte_gastos::class.java))
            }
            R.id.navigation_home -> {
                startActivity(Intent(this, pantalla_principal::class.java))
            }
            R.id.navigation_agregar_gasto -> {
                startActivity(Intent(this, RegistrarGastos::class.java))
            }
        }

        finish()

        true
    }
}

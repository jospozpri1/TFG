package com.dam.invernadero;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dam.invernadero.fragment.PreciosElectricidadFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import org.json.JSONException;
import org.json.JSONObject;
import com.dam.invernadero.R;


public class MainUsuarios extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private String stringjson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_usuarios);

        try {
            stringjson = getIntent().getStringExtra("userData");
            if (stringjson == null) throw new Exception("No se recibieron datos de usuario");
            Bundle bundle = new Bundle();
            bundle.putString("userData", stringjson);

            NavController navController = Navigation.findNavController(MainUsuarios.this, R.id.nav_host_fragment_content_main);

            navController.navigate(R.id.nav_inicio,bundle);

        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_inicio, R.id.nav_perfil, R.id.nav_precios_electricidad, R.id.nav_limites,
                R.id.nav_actuadores, R.id.nav_graficas,R.id.nav_eventos_motor)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        // Bandera para evitar loop infinito
        final boolean[] perfilYaNavegado = {false};
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            int id =destination.getId();
            if ((id == R.id.nav_perfil || id == R.id.nav_precios_electricidad || id == R.id.nav_limites||
                    id == R.id.nav_actuadores || id == R.id.nav_graficas || id == R.id.nav_eventos_motor)
                    && !perfilYaNavegado[0]) {
                PreciosElectricidadFragment fragment = new PreciosElectricidadFragment();
                perfilYaNavegado[0] = true;
                Bundle bundle = new Bundle();
                bundle.putString("userData", stringjson);
                navController.navigate(id, bundle);
            }else {
                perfilYaNavegado[0] = false; // Reseteamos cuando salimos del fragmento
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_usuarios, menu);
        return true;
    }
    public void boton_cerrar(MenuItem item) {
        Intent intent = new Intent(MainUsuarios.this, MainActivity.class);;
        startActivity(intent);
    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
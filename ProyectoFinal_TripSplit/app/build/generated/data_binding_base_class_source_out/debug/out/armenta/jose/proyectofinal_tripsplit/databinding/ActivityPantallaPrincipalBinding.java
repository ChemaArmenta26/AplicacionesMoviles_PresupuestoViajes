// Generated by view binder compiler. Do not edit!
package armenta.jose.proyectofinal_tripsplit.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import armenta.jose.proyectofinal_tripsplit.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityPantallaPrincipalBinding implements ViewBinding {
  @NonNull
  private final LinearLayout rootView;

  @NonNull
  public final ListView listaGastos;

  @NonNull
  public final ListView listaMontoPagarPersona;

  @NonNull
  public final BottomNavigationView navView;

  @NonNull
  public final TextView tvCodigoGrupo;

  @NonNull
  public final TextView tvNombreViaje;

  @NonNull
  public final TextView tvSaldoDisponbile;

  private ActivityPantallaPrincipalBinding(@NonNull LinearLayout rootView,
      @NonNull ListView listaGastos, @NonNull ListView listaMontoPagarPersona,
      @NonNull BottomNavigationView navView, @NonNull TextView tvCodigoGrupo,
      @NonNull TextView tvNombreViaje, @NonNull TextView tvSaldoDisponbile) {
    this.rootView = rootView;
    this.listaGastos = listaGastos;
    this.listaMontoPagarPersona = listaMontoPagarPersona;
    this.navView = navView;
    this.tvCodigoGrupo = tvCodigoGrupo;
    this.tvNombreViaje = tvNombreViaje;
    this.tvSaldoDisponbile = tvSaldoDisponbile;
  }

  @Override
  @NonNull
  public LinearLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityPantallaPrincipalBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityPantallaPrincipalBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_pantalla_principal, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityPantallaPrincipalBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.lista_gastos;
      ListView listaGastos = ViewBindings.findChildViewById(rootView, id);
      if (listaGastos == null) {
        break missingId;
      }

      id = R.id.lista_monto_pagar_persona;
      ListView listaMontoPagarPersona = ViewBindings.findChildViewById(rootView, id);
      if (listaMontoPagarPersona == null) {
        break missingId;
      }

      id = R.id.nav_view;
      BottomNavigationView navView = ViewBindings.findChildViewById(rootView, id);
      if (navView == null) {
        break missingId;
      }

      id = R.id.tv_codigo_grupo;
      TextView tvCodigoGrupo = ViewBindings.findChildViewById(rootView, id);
      if (tvCodigoGrupo == null) {
        break missingId;
      }

      id = R.id.tv_nombre_viaje;
      TextView tvNombreViaje = ViewBindings.findChildViewById(rootView, id);
      if (tvNombreViaje == null) {
        break missingId;
      }

      id = R.id.tvSaldoDisponbile;
      TextView tvSaldoDisponbile = ViewBindings.findChildViewById(rootView, id);
      if (tvSaldoDisponbile == null) {
        break missingId;
      }

      return new ActivityPantallaPrincipalBinding((LinearLayout) rootView, listaGastos,
          listaMontoPagarPersona, navView, tvCodigoGrupo, tvNombreViaje, tvSaldoDisponbile);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

// Generated by view binder compiler. Do not edit!
package armenta.jose.proyectofinal_tripsplit.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import armenta.jose.proyectofinal_tripsplit.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentTopBarBinding implements ViewBinding {
  @NonNull
  private final Toolbar rootView;

  @NonNull
  public final ImageView iconEdit;

  @NonNull
  public final ImageView iconLeft;

  @NonNull
  public final ImageView iconSettings;

  @NonNull
  public final ImageView iconUser;

  @NonNull
  public final ImageView tvAppTitle;

  private FragmentTopBarBinding(@NonNull Toolbar rootView, @NonNull ImageView iconEdit,
      @NonNull ImageView iconLeft, @NonNull ImageView iconSettings, @NonNull ImageView iconUser,
      @NonNull ImageView tvAppTitle) {
    this.rootView = rootView;
    this.iconEdit = iconEdit;
    this.iconLeft = iconLeft;
    this.iconSettings = iconSettings;
    this.iconUser = iconUser;
    this.tvAppTitle = tvAppTitle;
  }

  @Override
  @NonNull
  public Toolbar getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentTopBarBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentTopBarBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_top_bar, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentTopBarBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.icon_edit;
      ImageView iconEdit = ViewBindings.findChildViewById(rootView, id);
      if (iconEdit == null) {
        break missingId;
      }

      id = R.id.icon_left;
      ImageView iconLeft = ViewBindings.findChildViewById(rootView, id);
      if (iconLeft == null) {
        break missingId;
      }

      id = R.id.icon_settings;
      ImageView iconSettings = ViewBindings.findChildViewById(rootView, id);
      if (iconSettings == null) {
        break missingId;
      }

      id = R.id.icon_user;
      ImageView iconUser = ViewBindings.findChildViewById(rootView, id);
      if (iconUser == null) {
        break missingId;
      }

      id = R.id.tv_app_title;
      ImageView tvAppTitle = ViewBindings.findChildViewById(rootView, id);
      if (tvAppTitle == null) {
        break missingId;
      }

      return new FragmentTopBarBinding((Toolbar) rootView, iconEdit, iconLeft, iconSettings,
          iconUser, tvAppTitle);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}

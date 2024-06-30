package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getDrawableFromOverlay;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getOverlayForComponent;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getStringFromOverlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.IconsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.IconModel;

public class SettingsIcons extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initSettingsIconsItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private IconsAdapter initSettingsIconsItems() {
        ArrayList<IconModel> settingsIcons = new ArrayList<>();
        List<String> pack = getOverlayForComponent("ICS");
        for (int i = 0; i< pack.size(); i++) {
            String pkgName = pack.get(i).split("]")[1].replaceAll(" ", "");
            settingsIcons.add(
                    new IconModel(
                            getStringFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "theme_name"),
                            pkgName,
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "settings_wifi_ic"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "settings_personalise_ic"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "settings_power_manage_ic"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "settings_system_breeno_ic"),
                            pack.get(i).contains("[x]")));
        }
        settingsIcons.sort(Comparator.comparing(IconModel::getName));
        return new IconsAdapter(requireContext(), settingsIcons, loadingDialog, "ICS", false);
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_settings_icons_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }


}

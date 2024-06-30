package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.Dynamic.LIST_ICON_PACKS;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.checkOverlayEnabledAndEnable;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getDrawableFromOverlay;
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
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.overlay.manager.IconPackManager;

public class IconPacks extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_icon_pack_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initIconPackItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private IconsAdapter initIconPackItems() {
        ArrayList<IconModel> iconPacks = new ArrayList<>();
        List<String> packs = LIST_ICON_PACKS;
        for (int i = 0; i< packs.size(); i++) {
            String pkgName = packs.get(i).split("]")[1].replaceAll(" ", "");
            iconPacks.add(
                    new IconModel(
                            getStringFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "theme_name"),
                            pkgName,
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_3"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_wifi_signal_3"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_sys_data_bluetooth"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_sys_airplane_mode"),
                            packs.get(i).contains("[x]")));
        }

        iconPacks.sort(Comparator.comparing(IconModel::getName));

        return new IconsAdapter(requireContext(), iconPacks, loadingDialog, "IPSUI", new IconsAdapter.onButtonClick() {
            @Override
            public void onEnableClick(int position) {
                enableIconPack(position);
                checkOverlayEnabledAndEnable("SGIC");
                checkOverlayEnabledAndEnable("WIFI");
                AppUtils.restartScope("systemui");
            }

            @Override
            public void onDisableClick(int position) {
                disableIconPack(position);
                AppUtils.restartScope("systemui");
            }
        });
    }

    private void enableIconPack (int position) {
        // Enable icon pack
        IconPackManager.enableOverlay(position);
    }

    private void disableIconPack (int position) {
        // Disable icon pack
        IconPackManager.disableOverlay(position);
    }

}

package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewListOptionMemcItemBinding;
import it.dhd.oxygencustomizer.ui.models.MemcAppModel;

public class MemcAppAdapter extends RecyclerView.Adapter<MemcAppAdapter.ViewHolder> {

    private final List<MemcAppModel> itemList;
    private final List<MemcAppModel> filteredApps;
    private String filterText = "";
    private final OnItemClick mOnItemClick;

    public MemcAppAdapter(List<MemcAppModel> items, OnItemClick onItemClick) {
        items.sort(Comparator.comparing(MemcAppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        this.mOnItemClick = onItemClick;
        checkChange();
    }

    public MemcAppAdapter(List<MemcAppModel> items, OnItemClick onItemClick, boolean activity) {
        items.sort(Comparator.comparing(MemcAppModel::getAppName));
        this.itemList = items;
        this.filteredApps = new ArrayList<>(items);
        this.mOnItemClick = onItemClick;
        checkChange();
    }

    public void addItem(MemcAppModel item) {
        itemList.add(item);
        notifyDataSetChanged();
        filter(filterText);
    }

    public void removeItem(MemcAppModel item) {
        itemList.remove(item);
        notifyDataSetChanged();
        filter(filterText);
    }

    @NonNull
    @Override
    public MemcAppAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewListOptionMemcItemBinding binding = ViewListOptionMemcItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MemcAppAdapter.ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MemcAppModel model = filteredApps.get(holder.getBindingAdapterPosition());
        boolean shouldDrawDivider = getItemCount() > 1 && position < getItemCount() - 1;
        holder.setDrawDivider(shouldDrawDivider);
        if (position == 0) {
            holder.binding.memcItem.setBackgroundResource(filteredApps.size() == 1 ? R.drawable.preference_background_center : R.drawable.preference_background_top);
        } else if (position == filteredApps.size() - 1) {
            holder.binding.memcItem.setBackgroundResource(R.drawable.preference_background_bottom);
        } else {
            holder.binding.memcItem.setBackgroundResource(R.drawable.preference_background_middle);
        }
        holder.binding.appName.setText(model.getAppName());
        holder.binding.appPackage.setText(model.isActivity() ? model.getPackageName() + "\n" + model.getActivityName() : model.getPackageName());
        if (!model.isActivity()) {
            holder.binding.refreshRate.setVisibility(ViewGroup.VISIBLE);
            holder.binding.refreshRate.setText(String.format(getAppContext().getString(R.string.memc_refresh_rate), String.valueOf(model.getRefreshRate())));
        } else {
            holder.binding.refreshRate.setVisibility(ViewGroup.GONE);
        }
        holder.binding.memcConfig.setText(String.format(getAppContext().getString((R.string.memc_params)), model.getMemcConfig()));
        holder.binding.appIcon.setImageDrawable(model.getAppIcon());
        holder.binding.memcItem.setOnClickListener(v -> mOnItemClick.onItemClick(model));

    }

    @SuppressLint("NotifyDataSetChanged")
    private void checkChange() {
        filteredApps.sort((app1, app2) -> {
            if (app1.isEnabled() == app2.isEnabled()) {
                return app1.getAppName().compareTo(app2.getAppName());
            }
            return app1.isEnabled() ? -1 : 1;
        });
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return filteredApps.size();
    }

    public void filter(String text) {
        filteredApps.clear();
        filterText = text != null ? text : "";
        for (MemcAppModel app : itemList) {
            boolean matchesText = TextUtils.isEmpty(filterText) || app.getAppName().toLowerCase().contains(filterText.toLowerCase()) || app.getPackageName().toLowerCase().contains(filterText.toLowerCase());

            if ((matchesText) || app.isEnabled()) {
                filteredApps.add(app);
            }
        }
        checkChange();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements OplusRecyclerView.IOplusDividerDecorationInterface {

        private final ViewListOptionMemcItemBinding binding;
        private boolean drawDivider;
        private final int mDividerDefaultHorizontalPadding = getAppContext().getResources().getDimensionPixelSize(R.dimen.preference_divider_default_horizontal_padding);

        public ViewHolder(@NonNull ViewListOptionMemcItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void setDrawDivider(boolean drawDivider) {
            this.drawDivider = drawDivider;
        }

        @Override
        public boolean drawDivider() {
            return drawDivider;
        }

        @Override
        public View getDividerEndAlignView() {
            return null;
        }

        @Override
        public int getDividerEndInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

        @Override
        public View getDividerStartAlignView() {
            return this.binding.appName;
        }

        @Override
        public int getDividerStartInset() {
            return this.mDividerDefaultHorizontalPadding;
        }

    }

    public interface OnItemClick {
        void onItemClick(MemcAppModel model);
    }

}
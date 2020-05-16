package net.veldor.rutrackermobile.adapters;

import android.graphics.Color;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.selections.ViewListItem;
import net.veldor.rutrackermobile.ui.BrowserActivity;
import net.veldor.rutrackermobile.utils.Preferences;

import java.util.ArrayList;
import java.util.Locale;

public class BrowserAdapter extends RecyclerView.Adapter<BrowserAdapter.ViewHolder> {
    private final BrowserActivity mActivity;
    private ArrayList<ViewListItem> mItems = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public BrowserAdapter(BrowserActivity activity) {
        mActivity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (mLayoutInflater == null) {
            mLayoutInflater = LayoutInflater.from(parent.getContext());
        }
        View view = mLayoutInflater.inflate(R.layout.browser_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(ArrayList<ViewListItem> data) {
        mItems = data;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private ViewListItem mItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        void bind(ViewListItem item) {
            mItem = item;
            // обработаю элементы
            TextView type = mView.findViewById(R.id.elementType);
            if (type != null) {
                String elementType = mItem.getType();
                type.setText(mItem.getType());
                switch (elementType){
                    case "Категория":
                        mView.setBackgroundColor(mActivity.getResources().getColor(R.color.categoryColor));
                        break;
                    case "Раздел":
                        mView.setBackgroundColor(mActivity.getResources().getColor(R.color.partColor));
                        break;
                    case "Форум":
                        mView.setBackgroundColor(mActivity.getResources().getColor(R.color.forumColor));
                        break;
                    default:
                        mView.setBackgroundColor(Color.WHITE);
                }
            }
            TextView name = mView.findViewById(R.id.elementName);
            if (type != null) {
                name.setText(mItem.getName());
            }
            View root = mView.findViewById(R.id.rootView);
            if (root != null) {
                root.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // перейду по ссылке
                        if (!(mItem.getUrl() == null)) {
                            if(mItem.isPageLink()){
                                mActivity.viewTopic(App.RUTRACKER_BASE + mItem.getUrl());
                            }
                            else{
                                mActivity.loadPage(App.RUTRACKER_BASE + mItem.getUrl(), true);
                            }

                        }
                    }
                });
            }

            // если найдена ссылка на торрент- покажу поле для скачивания торрента, если нет- скрою
            ConstraintLayout torrentDlView = mView.findViewById(R.id.torrentDownloadContainer);
            if(torrentDlView != null){
                if(mItem.getTorrentUrl() != null){
                    torrentDlView.setVisibility(View.VISIBLE);
                    torrentDlView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(Preferences.getInstance().isUserLogged()){
                                mActivity.downloadTorrent(mItem.getTorrentUrl());
                            }
                            else{
                                Toast.makeText(mActivity, "Чтобы скачивать торрент-файлы- войдите в учётную запись!",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    // заполню размер раздачи
                    TextView distributionSizeView = mView.findViewById(R.id.distributionSize);
                    if(distributionSizeView != null){
                        distributionSizeView.setText(mItem.getTorrentSize());
                    }
                    TextView leechesView = mView.findViewById(R.id.leechesCount);
                    if(leechesView != null){
                        leechesView.setText(mItem.getLeeches());
                    }
                    TextView seedsView = mView.findViewById(R.id.seedsCount);
                    if(seedsView != null){
                        seedsView.setText(mItem.getSeeds());
                    }
                    TextView category = mView.findViewById(R.id.distributionCategory);
                    if(category != null){
                        if(mItem.getCategory() != null){
                            category.setText(String.format(Locale.ENGLISH, "%s / ", mItem.getCategory()));
                            if(mItem.getCategoryLink() != null){
                                mView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                                    @Override
                                    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                                        MenuItem searchThisCategory = menu.add("Поиск в " + mItem.getCategory());
                                        searchThisCategory.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                            @Override
                                            public boolean onMenuItemClick(MenuItem menuItem) {
                                               mActivity.loadPage(App.RUTRACKER_BASE + mItem.getCategoryLink(), true);
                                                return false;
                                            }
                                        });
                                    }
                                });
                            }
                        }
                        else{
                            category.setText("");
                        }
                    }
                }
                else{
                    torrentDlView.setVisibility(View.GONE);
                }
            }
        }
    }
}

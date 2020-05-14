package net.veldor.rutrackermobile.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.adapters.BrowserAdapter;
import net.veldor.rutrackermobile.selections.ViewListItem;
import net.veldor.rutrackermobile.utils.PageParser;
import net.veldor.rutrackermobile.utils.Preferences;
import net.veldor.rutrackermobile.utils.XMLHandler;
import net.veldor.rutrackermobile.view_models.BrowserViewModel;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Objects;

public class BrowserActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    private BrowserViewModel mMyViewModel;
    private BrowserAdapter mAdapter;
    private AlertDialog mPageLoadDialog;
    private String mLastLoadedPage;
    private ArrayList<String> autocompleteStrings;
    private ArrayAdapter<String> mSearchAdapter;
    private SearchView mSearchView;
    private static boolean sFirstLoad = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);
        RecyclerView recycler = findViewById(R.id.contentRecycler);
        mAdapter = new BrowserAdapter(this);
        recycler.setAdapter(mAdapter);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        mMyViewModel = new ViewModelProvider(this).get(BrowserViewModel.class);
        // получу данные стартовой страницы
        if(sFirstLoad){
            loadPage(App.RUTRACKER_MAIN_PAGE);
            sFirstLoad = false;
        }
        // проверю, залогинен ли пользователь, если нет- предложу войти
        checkUserLoggedIn();
        // буду отслеживать результаты загрузки страниц
        setupObservers();
        prepareSearch();
    }

    private void prepareSearch() {
        // подгружу автозаполнение поиска
        autocompleteStrings = mMyViewModel.getSearchAutocomplete();
    }

    private void setupObservers() {
        // observe http-response
        LiveData<String> responseData = App.getInstance().mLiveRequest;
        responseData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    Log.d("surprise", "BrowserActivity onChanged: data changed");
                    // обработаю данные и получу список элементов для отображения
                    ArrayList<ViewListItem> data = (new PageParser()).parsePage(s);
                    mAdapter.setItems(data);
                    hidePageLoadDialog();
                }
            }
        });

        // observe loaded page title
        LiveData<String> pageTitle = App.getInstance().mPageTitle;
        pageTitle.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    changeTitle(s);
                }
            }
        });
    }

    private void changeTitle(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(s);
        }
    }


    private void checkUserLoggedIn() {
        if (!Preferences.getInstance().isUserLogged()) {
            Snackbar updateSnackbar = Snackbar.make(findViewById(R.id.root), "Войдите в учётную запись", Snackbar.LENGTH_INDEFINITE);
            updateSnackbar.setAction(getString(R.string.snackbar_login_action_message), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(BrowserActivity.this, LoginActivity.class));
                }
            });
            updateSnackbar.setActionTextColor(getResources().getColor(android.R.color.white));
            updateSnackbar.show();
        }
    }

    public void loadPage(String url) {
        mLastLoadedPage = url;
        // Добавлю страницу в историю
        mMyViewModel.addToHistory(url);
        // закрою вид диалоговым окном загрузки
        showPageLoadDialog();
        mMyViewModel.loadPage(url);
    }

    private void showPageLoadDialog() {
        if (mPageLoadDialog == null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.waiter_dialog, null, false);
            mPageLoadDialog = dialogBuilder
                    .setCancelable(false)
                    .setView(view)
                    .create();
        }
        mPageLoadDialog.show();
    }

    private void hidePageLoadDialog() {
        if (mPageLoadDialog != null) {
            mPageLoadDialog.dismiss();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // нажата кнопка "назад". Если идёт загрузка страницы, сообщу,
            // что действие пока недоступно
            if (mPageLoadDialog.isShowing()) {
                Toast.makeText(this, getString(R.string.wait_for_page_load_message), Toast.LENGTH_SHORT).show();
                return true;
            } else {
                // загружу страницу из истории. Если истории нет- выйду из приложения
                String link = mMyViewModel.getFromHistory(mLastLoadedPage);
                if (link != null) {
                    showPageLoadDialog();
                    if(link.startsWith("https://rutracker.org/forum/tracker.php?&nm=")){
                        // выполняю поиск по заданному параметру
                        try {
                            mMyViewModel.makeSearch(URLDecoder.decode(link.substring(44), "windows-1251"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        mMyViewModel.loadPage(link);
                    }
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void viewTopic(String s) {
        mMyViewModel.viewTopic(s);
    }

    @Override
    @SuppressLint("RestrictedApi")
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.browser_menu, menu);

        // добавлю обработку поиска
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setOnQueryTextListener(this);

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int i) {
                return true;
            }

            @Override
            public boolean onSuggestionClick(int i) {
                String value = autocompleteStrings.get(i);
                mSearchView.setQuery(value, false);
                return true;
            }
        });

        SearchView.SearchAutoComplete mSearchAutoComplete = mSearchView.findViewById(R.id.search_src_text);
        mSearchAutoComplete.setDropDownAnchor(R.id.action_search);
        mSearchAutoComplete.setThreshold(0);
        mSearchAutoComplete.setDropDownBackgroundResource(R.color.design_default_color_background);
        mSearchAutoComplete.setDropDownHeight(getResources().getDisplayMetrics().heightPixels / 2);
        mSearchAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, autocompleteStrings);
        mSearchAutoComplete.setAdapter(mSearchAdapter);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchSettings:
                // открою окно с настройками поиска
                mMyViewModel.openSearchSettingsWindow();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!TextUtils.isEmpty(query.trim())) {
            // занесу значение в список автозаполнения
            if (XMLHandler.putSearchValue(query.trim())) {
                // обновлю список поиска
                autocompleteStrings = mMyViewModel.getSearchAutocomplete();
                mSearchAdapter.clear();
                mSearchAdapter.addAll(autocompleteStrings);
                mSearchAdapter.notifyDataSetChanged();
            }
            // ищу введённое значение
            mMyViewModel.makeSearch(query);
            mSearchView.onActionViewCollapsed();
            showPageLoadDialog();
            changeTitle("Поиск: " + query);
             // добавлю запрос в очередь
            try {
                String requestValue = "https://rutracker.org/forum/tracker.php?&nm=" + URLEncoder.encode(query.trim(), "windows-1251");
                mLastLoadedPage = requestValue;
                mMyViewModel.addToHistory(requestValue);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    public void downloadTorrent(String torrentUrl) {
        Toast.makeText(this, getString(R.string.torrent_loading_message), Toast.LENGTH_SHORT).show();
        mMyViewModel.downloadTorrent(torrentUrl);
    }
}

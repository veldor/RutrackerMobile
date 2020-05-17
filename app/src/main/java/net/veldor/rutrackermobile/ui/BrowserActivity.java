package net.veldor.rutrackermobile.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
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

import co.mobiwise.materialintro.animation.MaterialIntroListener;
import co.mobiwise.materialintro.shape.Focus;
import co.mobiwise.materialintro.shape.FocusGravity;
import co.mobiwise.materialintro.shape.ShapeType;
import co.mobiwise.materialintro.view.MaterialIntroView;

public class BrowserActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {
    public static int sPagesCount;
    public static ArrayList<String> sSearchResultsArray;
    public static String sLastSearchString;
    private BrowserViewModel mMyViewModel;
    private BrowserAdapter mAdapter;
    private AlertDialog mPageLoadDialog;
    private String mLastLoadedPage;
    private static int sCurrentPage = 1;
    private ArrayList<String> autocompleteStrings;
    private ArrayAdapter<String> mSearchAdapter;
    private SearchView mSearchView;
    private static boolean sFirstLoad = true;
    private LinearLayout mPager;
    private ConstraintLayout mPagerRoot;
    private ImageButton mBackButton, mForwardButton;
    private RecyclerView mRecycler;
    private View mHomeView;
    private View mScrollView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sPagesCount = -1;
        setContentView(R.layout.activity_browser);
        mPager = findViewById(R.id.pagesScrollView);
        mPagerRoot = findViewById(R.id.pagerView);
        mBackButton = findViewById(R.id.prevPage);
        mScrollView = findViewById(R.id.mainScrollView);
        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextPageNum = sCurrentPage - 1;
                if (mLastLoadedPage != null) {
                    if (mLastLoadedPage.startsWith("https://rutracker.org/forum/viewforum.php?f")) {
                        loadPage(mLastLoadedPage + "&start=" + (nextPageNum * 50 - 50), false, true);
                        sCurrentPage = nextPageNum;
                        return;
                    }
                } else {
                    mLastLoadedPage = App.getInstance().mHistory.peek();
                    if (mLastLoadedPage != null) {
                        loadPage(mLastLoadedPage + "&start=" + (nextPageNum * 50 - 50), false, true);
                        sCurrentPage = nextPageNum;
                        return;
                    }
                }
                if (sSearchResultsArray != null && sSearchResultsArray.size() > 0) {
                    int counter = 0;
                    while (counter <= sSearchResultsArray.size()) {
                        counter++;
                        if (sCurrentPage == counter) {
                            loadPage(App.RUTRACKER_BASE + sSearchResultsArray.get(counter - 1) + "&nm=" + sLastSearchString, false, true);
                            if (counter == 1) {
                                sCurrentPage = 1;
                            } else {
                                sCurrentPage = counter - 1;
                            }
                            return;
                        }
                    }
                }
            }
        });
        mForwardButton = findViewById(R.id.nextPage);
        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int nextPageNum = sCurrentPage + 1;
                if (mLastLoadedPage != null) {
                    if (mLastLoadedPage.startsWith("https://rutracker.org/forum/viewforum.php?f")) {
                        loadPage(mLastLoadedPage + "&start=" + (nextPageNum * 50 - 50), false, true);
                        sCurrentPage = nextPageNum;
                        return;
                    }
                } else {
                    mLastLoadedPage = App.getInstance().mHistory.peek();
                    if (mLastLoadedPage != null) {
                        loadPage(mLastLoadedPage + "&start=" + (nextPageNum * 50 - 50), false, true);
                        sCurrentPage = nextPageNum;
                    }
                    return;
                }
                if (sSearchResultsArray != null && sSearchResultsArray.size() > 0) {
                    int counter = 0;
                    while (counter <= sSearchResultsArray.size()) {
                        counter++;
                        if (sCurrentPage == counter) {
                            loadPage(App.RUTRACKER_BASE + sSearchResultsArray.get(counter + 1) + "&nm=" + sLastSearchString, false, true);
                            sCurrentPage = counter + 1;
                            return;
                        }
                    }
                }
            }
        });
        mRecycler = findViewById(R.id.contentRecycler);
        mAdapter = new BrowserAdapter(this);
        mRecycler.setAdapter(mAdapter);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mMyViewModel = new ViewModelProvider(this).get(BrowserViewModel.class);
        // получу данные стартовой страницы

        if (App.getInstance().externalUrl != null) {
            if (App.getInstance().externalUrl.startsWith("https://rutracker.org/forum/viewtopic.php?t=")) {
                viewTopic(App.getInstance().externalUrl);
            } else {
                loadPage(App.getInstance().externalUrl, true, true);
            }
            App.getInstance().externalUrl = null;
        } else if (sFirstLoad) {
            loadPage(App.RUTRACKER_MAIN_PAGE, true, true);
            sFirstLoad = false;
        }
        // проверю, залогинен ли пользователь, если нет- предложу войти
        checkUserLoggedIn();
        // буду отслеживать результаты загрузки страниц
        setupObservers();
        prepareSearch();

        showGuide();
    }

    private void showGuide() {
        // если гайд ещё не отображался
        if (!Preferences.getInstance().isSearchIntroShowed() && Preferences.getInstance().isRecyclerIntroViewed()) {
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mSearchView != null) {
                        // укажу на поле поиска
                        new MaterialIntroView.Builder(BrowserActivity.this)
                                .enableDotAnimation(false)
                                .enableIcon(false)
                                .setFocusGravity(FocusGravity.CENTER)
                                .setFocusType(Focus.MINIMUM)
                                .setDelayMillis(500)
                                .enableFadeAnimation(true)
                                .performClick(false)
                                .setListener(new MaterialIntroListener() {
                                    @Override
                                    public void onUserClicked(String materialIntroViewId) {
                                        mSearchView.setIconified(false);
                                        mSearchView.requestFocus();
                                    }
                                })
                                .setInfoText("Это поле поиска. Напишите сюда. что вы хотите найти")
                                .setTarget(mSearchView)
                                .setShape(ShapeType.CIRCLE)
                                .show();
                        Preferences.getInstance().setSearchIntroShowed();
                    }
                }
            }, 1000);
        }
        else if(Preferences.getInstance().isRecyclerIntroViewed() && Preferences.getInstance().isSearchIntroShowed() && !Preferences.getInstance().isHomeIntroViewed()){
            showHomeIntro();
        }
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
                    if (!Preferences.getInstance().isRecyclerIntroViewed()) {
                        showRecyclerIntro();
                    }
                    drawPages();
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

    private void drawPages() {
        mPager.removeAllViews();
        // проверю, что количество страниц больше нуля
        if (sPagesCount > 1) {
            mPagerRoot.setVisibility(View.VISIBLE);
            Button button;
            int pageNum = 1;
            while (sPagesCount > 0) {
                --sPagesCount;
                // если кнопка совпадает с текущим номером страницы- добавлю её
                if (pageNum == sCurrentPage) {
                    button = (Button) getLayoutInflater().inflate(R.layout.current_pager_button, mPager, false);
                    button.setText(String.valueOf(pageNum));
                    mPager.addView(button);
                } else {
                    // создам кнопку и добавлю её к списку
                    button = (Button) getLayoutInflater().inflate(R.layout.pager_button, mPager, false);
                    button.setText(String.valueOf(pageNum));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pageCount = Integer.parseInt(((Button) v).getText().toString());
                            // перейду на нужную страницу
                            if (mLastLoadedPage != null) {
                                if (mLastLoadedPage.startsWith("https://rutracker.org/forum/viewforum.php?f")) {
                                    loadPage(mLastLoadedPage + "&start=" + (pageCount * 50 - 50), false, true);
                                    sCurrentPage = pageCount;
                                }
                            } else {
                                mLastLoadedPage = App.getInstance().mHistory.peek();
                                if (mLastLoadedPage != null) {
                                    loadPage(mLastLoadedPage + "&start=" + (pageCount * 50 - 50), false, true);
                                    sCurrentPage = pageCount;
                                }
                            }
                        }
                    });
                    mPager.addView(button);
                }
                ++pageNum;
            }

            // теперь кнопки назад и вперёд.
            if (sCurrentPage == 1) {
                mBackButton.setEnabled(false);
            } else {
                mBackButton.setEnabled(true);
            }

            if (sCurrentPage == sPagesCount) {
                mForwardButton.setEnabled(false);
            } else {
                mForwardButton.setEnabled(true);
            }
        } else if (sSearchResultsArray != null && sSearchResultsArray.size() > 0) {
            mPagerRoot.setVisibility(View.VISIBLE);
            Button button;
            // добавлю данные в меню
            int pageNumber = 0;
            int countedPage = 0;
            while (pageNumber <= sSearchResultsArray.size()) {
                ++pageNumber;
                if (pageNumber == sCurrentPage) {
                    button = (Button) getLayoutInflater().inflate(R.layout.current_pager_button, mPager, false);
                    button.setText(String.valueOf(sCurrentPage));
                    mPager.addView(button);
                } else {
                    // создам кнопку и добавлю её к списку
                    button = (Button) getLayoutInflater().inflate(R.layout.pager_button, mPager, false);
                    button.setText(String.valueOf(pageNumber));
                    final int finalCountedPage = countedPage;
                    final int finalPageNumber = pageNumber;
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            loadPage(App.RUTRACKER_BASE + sSearchResultsArray.get(finalCountedPage) + "&nm=" + sLastSearchString, false, true);
                            sCurrentPage = finalPageNumber;
                        }
                    });
                    mPager.addView(button);
                    countedPage++;
                }
            }

            // теперь кнопки назад и вперёд.
            if (sCurrentPage == 1) {
                mBackButton.setEnabled(false);
            } else {
                mBackButton.setEnabled(true);
            }

            if (sCurrentPage == sSearchResultsArray.size() + 1) {
                mForwardButton.setEnabled(false);
            } else {
                mForwardButton.setEnabled(true);
            }
        } else {
            mPagerRoot.setVisibility(View.GONE);
        }
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

    public void loadPage(String url, boolean resetPager, boolean scrollToTop) {
        sSearchResultsArray = null;
        Log.d("surprise", "BrowserActivity loadPage: " + url);
        if(scrollToTop && mLastLoadedPage != null && !mLastLoadedPage.equals(App.RUTRACKER_MAIN_PAGE)){
            mScrollView.scrollTo(0,0);
        }

        if (resetPager) {
            sCurrentPage = 1;
        }
        // тут проверка- возможно, мы пытаемся открыть топик как ссылку. Если это так- открываю топик
        if (url.startsWith("https://rutracker.org/forum/viewtopic.php")) {
            // открою топик
            viewTopic(url);
        } else {
            mLastLoadedPage = url;
            // Добавлю страницу в историю
            mMyViewModel.addToHistory(url);
            // закрою вид диалоговым окном загрузки
            showPageLoadDialog();
            mMyViewModel.loadPage(url);
        }
    }

    private void showPageLoadDialog() {
        if (mPageLoadDialog == null) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.waiter_dialog, null, false);
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
                    if (link.startsWith("https://rutracker.org/forum/tracker.php?&nm=")) {
                        // выполняю поиск по заданному параметру
                        try {
                            mMyViewModel.makeSearch(URLDecoder.decode(link.substring(44), "windows-1251"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    } else {
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
            case R.id.goHome:
                // перейду на главную
                loadPage(App.RUTRACKER_MAIN_PAGE, true, true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        sCurrentPage = 1;
        // если не выполнен вход в учётную запись- уведомлю об этом, поиск невозможен
        if (!Preferences.getInstance().isUserLogged()) {
            Toast.makeText(App.getInstance(), "Для того, чтобы пользоваться поиском- нужно войти в учётную запись!", Toast.LENGTH_SHORT).show();
        } else {
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
                mRecycler.scrollToPosition(0);
                // добавлю запрос в очередь
                try {
                    String requestValue = "https://rutracker.org/forum/tracker.php?&nm=" + URLEncoder.encode(query.trim(), "windows-1251");
                    mLastLoadedPage = requestValue;
                    mMyViewModel.addToHistory(requestValue);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

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

    public void showLongPressIntro() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // попробую найти первый элемент в Recycler.
                final View firstValue = mRecycler.getChildAt(0);
                if (firstValue != null) {
                    new MaterialIntroView.Builder(BrowserActivity.this)
                            .enableDotAnimation(true)
                            .enableIcon(false)
                            .setFocusGravity(FocusGravity.LEFT)
                            .setFocusType(Focus.MINIMUM)
                            .setIdempotent(true)
                            .enableFadeAnimation(true)
                            .performClick(false)
                            .setListener(new MaterialIntroListener() {
                                @Override
                                public void onUserClicked(String materialIntroViewId) {
                                    firstValue.showContextMenu();
                                }
                            })
                            .setInfoText("Долгое нажатие открывает меню")
                            .setTarget(firstValue)
                            .setShape(ShapeType.RECTANGLE)
                            .setUsageId("long click use intro") //THIS SHOULD BE UNIQUE ID
                            .show();
                    Preferences.getInstance().setBrowserContextIntroShowed();
                }
            }
        }, 1000);
    }

    public void showRecyclerIntro() {

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // попробую найти первый элемент в Recycler.
                View firstValue = mRecycler.getChildAt(0);
                if (firstValue != null) {
                    new MaterialIntroView.Builder(BrowserActivity.this)
                            .enableDotAnimation(true)
                            .enableIcon(false)
                            .setFocusGravity(FocusGravity.LEFT)
                            .setFocusType(Focus.MINIMUM)
                            .setIdempotent(false)
                            .enableFadeAnimation(true)
                            .performClick(true)
                            .setInfoText("Нажмите на меня, чтобы выполнить запрос")
                            .setTarget(firstValue)
                            .setShape(ShapeType.RECTANGLE)
                            .setUsageId("recycler use intro") //THIS SHOULD BE UNIQUE ID
                            .show();
                    Preferences.getInstance().setRecyclerIntroShowed();
                }
            }
        }, 1000);
    }


    private void showHomeIntro() {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // попробую найти первый элемент в Recycler.
                mHomeView = findViewById(R.id.goHome);
                if (mHomeView != null) {
                    new MaterialIntroView.Builder(BrowserActivity.this)
                            .enableDotAnimation(false)
                            .enableIcon(false)
                            .setFocusGravity(FocusGravity.CENTER)
                            .setFocusType(Focus.MINIMUM)
                            .enableFadeAnimation(true)
                            .performClick(true)
                            .setInfoText("Нажмите на меня, чтобы вернуться на главную страницу")
                            .setTarget(mHomeView)
                            .setShape(ShapeType.CIRCLE)
                            .setUsageId("home use intro")
                            .show();
                    Preferences.getInstance().setHomeIntroShowed();
                }
                else{
                    Log.d("surprise", "BrowserActivity run: Home not found");
                }
            }
        }, 1000);
    }

    public void showDownloadTorrentIntro() {
        mRecycler.scrollToPosition(0);
        // попробую найти первый элемент в Recycler.
        View firstValue = mRecycler.getChildAt(0);
        if (firstValue != null) {
            // если он найден- найду в нём раздел скачивания торрента и удостоверюсь,
            // что он активирован
            View downloadTorrentView = firstValue.findViewById(R.id.torrentDownloadContainer);
            if(downloadTorrentView != null && downloadTorrentView.getVisibility() == View.VISIBLE){
                new MaterialIntroView.Builder(BrowserActivity.this)
                        .enableDotAnimation(true)
                        .enableIcon(false)
                        .setFocusGravity(FocusGravity.CENTER)
                        .setFocusType(Focus.MINIMUM)
                        .setIdempotent(false)
                        .enableFadeAnimation(true)
                        .performClick(true)
                        .setInfoText("Нажмите на меня, чтобы загрузить торрент")
                        .setTarget(downloadTorrentView)
                        .setShape(ShapeType.RECTANGLE)
                        .setUsageId("fast download torrent use intro") //THIS SHOULD BE UNIQUE ID
                        .show();
                Preferences.getInstance().setTorrentDownloadIntroViewed();
            }
            else{
                Log.d("surprise", "BrowserActivity showDownloadTorrentIntro: can't found download view");
            }
        }
    }
}

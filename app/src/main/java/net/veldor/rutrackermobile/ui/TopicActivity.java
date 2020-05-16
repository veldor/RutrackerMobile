package net.veldor.rutrackermobile.ui;

import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import net.veldor.rutrackermobile.App;
import net.veldor.rutrackermobile.R;
import net.veldor.rutrackermobile.utils.TopicParser;
import net.veldor.rutrackermobile.view_models.TopicViewModel;

import java.util.Objects;

public class TopicActivity extends AppCompatActivity {
public static final String TOPIC_URL = "topic_url";

    private TopicViewModel mViewModel;

    //liveData
    public static final MutableLiveData<String> liveTitle = new MutableLiveData<>();
    public static final MutableLiveData<String> liveSize = new MutableLiveData<>();
    public static final MutableLiveData<String> liveLeeches = new MutableLiveData<>();
    public static final MutableLiveData<String> liveSeeds = new MutableLiveData<>();
    public static final MutableLiveData<String> livePosterUrl = new MutableLiveData<>();
    public static final MutableLiveData<String> liveTorrentLink = new MutableLiveData<>();
    public static final MutableLiveData<String> liveMagnet = new MutableLiveData<>();
    public static final MutableLiveData<String> liveBody = new MutableLiveData<>();
    private TextView mTorrentNameView;
    private ImageView mPosterView;
    private TextView mTorrentSizeView;
    private TextView mTorrentSeedsView;
    private TextView mTorrentLeechesView;
    private Button mTorrentDownloadButton;
    private String mTorrentLink;
    private Button mMagnetButton;
    private String mMagnet;
    private TextView mTopicBody;
    private String mTopicUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        mViewModel = new ViewModelProvider(this).get(TopicViewModel.class);
        setupUI();
        requestData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearLiveData();
        setupObservers();
    }

    private void clearLiveData() {
        liveTitle.postValue(null);
        liveSize.postValue(null);
        liveLeeches.postValue(null);
        liveSeeds.postValue(null);
        livePosterUrl.postValue(null);
        liveTorrentLink.postValue(null);
        liveMagnet.postValue(null);
        livePosterUrl.postValue(null);
        liveBody.postValue(null);
        mTorrentDownloadButton.setEnabled(false);
        mMagnetButton.setEnabled(false);
    }

    private void setupUI() {
        mTorrentNameView = findViewById(R.id.distributionName);
        mTorrentSizeView = findViewById(R.id.fileSize);
        mTorrentSeedsView = findViewById(R.id.seedsCount);
        mTorrentLeechesView = findViewById(R.id.leechesCount);
        mPosterView = findViewById(R.id.posterView);
        mTorrentDownloadButton = findViewById(R.id.downloadTorrentButton);
        mTorrentDownloadButton.setEnabled(false);
        mMagnetButton = findViewById(R.id.loadMagnetButton);
        mMagnetButton.setEnabled(false);
        mTopicBody = findViewById(R.id.topicBody);
    }

    private void requestData() {
        // запрошу страницу
        mTopicUrl = getIntent().getStringExtra(TOPIC_URL);
        mViewModel.requestPage(mTopicUrl);
    }

    private void changeTitle(String s) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Objects.requireNonNull(getSupportActionBar()).setTitle(s);
        }
    }

    private void setupObservers() {
        // observe http-response
        LiveData<String> responseData = App.getInstance().mLiveTopicRequest;
        responseData.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if (s != null && !s.isEmpty()) {
                    // обработаю данные и получу список элементов для отображения
                    (new TopicParser()).parsePage(s);
                }
            }
        });

        liveTitle.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mTorrentNameView.setText(s);
                    if(s.contains("(")){
                        changeTitle(s.substring(0, s.indexOf("(")));
                    }
                    else{
                        changeTitle(s);
                    }
                }
                else{
                    mTorrentNameView.setText("");
                }
            }
        });
        liveSize.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mTorrentSizeView.setText(s);
                }
                else{
                    mTorrentSizeView.setText("");
                }
            }
        });
        liveSeeds.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mTorrentSeedsView.setText(s);
                }
                else{
                    mTorrentSeedsView.setText("");
                }
            }
        });
        liveLeeches.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mTorrentLeechesView.setText(s);
                }
                else{
                    mTorrentLeechesView.setText("");
                }
            }
        });
        livePosterUrl.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    // Загружу картинку
                    mViewModel.loadPoster(s, mPosterView);
                    mPosterView.setVisibility(View.VISIBLE);
                }
                else{
                    mPosterView.setImageDrawable(null);
                    mPosterView.setVisibility(View.GONE);
                }
            }
        });
        liveBody.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    Spanned body = Html.fromHtml(s);
                    mTopicBody.setText(body);
                }
                else{
                    mTopicBody.setText("");
                }
            }
        });

        liveTorrentLink.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mTorrentLink = s;
                    enableTorrentDownloadBtn();
                }
            }
        });
        liveMagnet.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(s != null){
                    mMagnet = s;
                    enableMagnetBtn();
                }
            }
        });
    }

    private void enableMagnetBtn() {
        mMagnetButton.setEnabled(true);
        mMagnetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.handleMagnet(mMagnet);
            }
        });
    }

    private void enableTorrentDownloadBtn() {

        mTorrentDownloadButton.setEnabled(true);
        mTorrentDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.downloadTorrent(mTorrentLink);
                Toast.makeText(TopicActivity.this, "Загрузка торрента начата",Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.topic_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.sharePage){
            // отправлю ссылку на страницу
            mViewModel.handleMagnet(mTopicUrl);
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.jackpan.stockcomputer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adbert.AdbertLoopADView;
import com.adbert.AdbertOrientation;
import com.adbert.ExpandVideoPosition;
import com.clickforce.ad.Listener.AdViewLinstener;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.messenger.MessengerThreadParams;
import com.facebook.messenger.MessengerUtils;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.firebase.auth.FirebaseAuth;
import com.jackpan.libs.mfirebaselib.MfiebaselibsClass;
import com.jackpan.libs.mfirebaselib.MfirebaeCallback;
import com.jackpan.stockcomputer.Activity.BaseAppCompatActivity;
import com.jackpan.stockcomputer.Activity.CalculateActivity;
import com.jackpan.stockcomputer.Activity.ShareStockNumberActivity;
import com.jackpan.stockcomputer.Data.MemberData;
import com.jackpan.stockcomputer.Data.MyApi;
import com.jackpan.stockcomputer.Data.NewsData;
import com.jackpan.stockcomputer.Kotlin.BuyAndSellActivity;
import com.jackpan.stockcomputer.Kotlin.NewDetailActivity;
import com.jackpan.stockcomputer.Kotlin.QueryStockPriceActivity;
import com.jackpan.stockcomputer.Kotlin.StockValueAddedRateActivity;
import com.jackpan.stockcomputer.Kotlin.ZeroStockActivity;
import com.vpadn.ads.VpadnAdRequest;
import com.vpadn.ads.VpadnAdSize;
import com.vpadn.ads.VpadnBanner;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.DataNode;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemClick;

public class MainActivity extends BaseAppCompatActivity implements MfirebaeCallback {
    private static final String TAG = "MainActivity";
    private VpadnBanner vponBanner = null;
    //Vpon TODO:  Banner ID
    private String bannerId = "8a8081824eb5519a014eca83ab981d91";
    private MessengerThreadParams mThreadParams;
    private boolean mPicking;
    private static final int REQUEST_CODE_SHARE_TO_MESSENGER = 1;
    private ProgressDialog mProgressDialog;
    private ArrayList<NewsData> newlist = new ArrayList<>();
    private MyAdapter mAdapter;
    private CallbackManager callbackManager;
    private AccessTokenTracker accessTokenTracker;
    private LoginManager loginManager;

    private MfiebaselibsClass mfiebaselibsClass;
    private ProfileTracker profileTracker;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private  ArrayList<String> nextPage = new ArrayList<>();
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.listView)
    ListView mListview;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.adLayout)
    RelativeLayout adBannerLayout;
    @BindView(R.id.adView)
    AdView mAdView;
    @BindView(R.id.adbertADView)
    AdbertLoopADView adbertView;
    @BindView(R.id.ad)
    com.clickforce.ad.AdView clickforceAd;
    @BindView(R.id.fbImg)
    ImageView mFbImageView;
//    @BindView(R.id.fbloginbutton)
//
//    LoginButton mFbLoginButton;
    @BindView(R.id.adView_page)
    AdView mPageAdView;
    @BindView(R.id.useraccount)
    TextView mUserAccountTextView;
    @BindView(R.id.userid)
    TextView mUserIdTextView;

    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        loginManager = LoginManager.getInstance();
        AppEventsLogger.activateApp(this);
        context = this;
        mfiebaselibsClass = new MfiebaselibsClass(this, MainActivity.this);
        mfiebaselibsClass.userLoginCheck();
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        checkNetWork();
        toolbar.setTitle(getResources().getString(R.string.activty_main_title));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        setAdmobBanner();
        setPageAdView();
        setVponBanner();
        setAdbertBanner();
        setClickForce();
        Intent intent = getIntent();
        if (Intent.ACTION_PICK.equals(intent.getAction())) {
            mThreadParams = MessengerUtils.getMessengerThreadParamsForIntent(intent);
            mPicking = true;
        }

        setNewsData();
             mAdapter= new MyAdapter(newlist);
        mListview.setAdapter(mAdapter);
   ;
//        fbLogin();
//        test("2344");
//        test2("2344");
//        getNewDetil();
//        setStockData();
        setWarningStock();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 設定 會員中心資料
     */

    private  void setMemberData(String Key,String firstname,String lastname,
                                String email,String birthday,String gender,
                                String memberlv,String photo,String location){
        HashMap<String,String> memberMap =new HashMap<>();
        memberMap.put(MemberData.KEY_ID,Key);
        memberMap.put(MemberData.KEY_FIRST_NAME, firstname);
        memberMap.put(MemberData.KEY_LAST_NAME,lastname);
        memberMap.put(MemberData.KEY_EMAIL,email);
        memberMap.put(MemberData.KEY_BIRTHDAY,birthday);
        memberMap.put(MemberData.KEY_GENDER,gender);
        memberMap.put(MemberData.KEY_MEMBERLV,memberlv);
        memberMap.put(MemberData.KEY_PHOTO,photo);
        memberMap.put(MemberData.KEY_LOCATION,location);



        mfiebaselibsClass.setFireBaseDB(MemberData.KEY_URL+Key,Key,memberMap);



    }





    //臉書登入
//    private void fbLogin() {
//        List<String> PERMISSIONS_PUBLISH = Arrays.asList("public_profile", "email", "user_friends","user_location","user_birthday", "user_likes");
//        mFbLoginButton.setReadPermissions(PERMISSIONS_PUBLISH);
//        mFbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
//            @Override
//            public void onSuccess(LoginResult loginResult) {
//                handleFacebookAccessToken(loginResult.getAccessToken());
//                setUsetProfile();
//                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
//
//                    @Override
//                    public void onCompleted(JSONObject object, GraphResponse response) {
//                        Log.d("LoginActivity", object.toString());
//                        // Get facebook data from login
//                        Bundle bFacebookData = getFacebookData(object);
//                    }
//                });
//                Bundle parameters = new Bundle();
//                parameters.putString("fields", "id, first_name, last_name, email,gender, birthday, location"); // Parámetros que pedimos a facebook
//                request.setParameters(parameters);
//                request.executeAsync();
//            }
//
//            @Override
//            public void onCancel() {
//
//            }
//
//            @Override
//            public void onError(FacebookException error) {
//                error.printStackTrace();
//                setLogger(error.getMessage());
//
//
//            }
//
//        });
//
//    }

//    private void handleFacebookAccessToken(AccessToken token) {
//
//
//        accessTokenTracker = new AccessTokenTracker() {
//            @Override
//            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
//                updateWithToken(newAccessToken);
//            }
//        };
//
//        // [START_EXCLUDE silent]
//
//        // [END_EXCLUDE]
//        auth = FirebaseAuth.getInstance();
//
//        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
//        auth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
//                        // If sign in fails, display a message to the user. If sign in succeeds
//                        // the auth state listener will be notified and logic to handle the
//                        // signed in user can be handled in the listener.
//                        if (!task.isSuccessful()) {
//                            Log.w(TAG, "signInWithCredential", task.getException());
//
//                        }
//
//                        // [START_EXCLUDE]
//
//                        // [END_EXCLUDE]
//                    }
//                });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        checkFbState();

    }

    private void checkFbState(){
        if (Profile.getCurrentProfile() != null) {
            Profile profile = Profile.getCurrentProfile();
            // 取得用戶大頭照
            Uri userPhoto = profile.getProfilePictureUri(300, 300);
            String id = profile.getId();
            String name = profile.getName();
            mUserAccountTextView.setText(name);
            mUserIdTextView.setText(id);
            MyApi.loadImage(String.valueOf(userPhoto), mFbImageView,context);
            MySharedPrefernces.saveUserId(context,id);
        }else {
            mFbImageView.setImageDrawable(null);
            mUserAccountTextView.setText("");
            mUserIdTextView.setText("");
            MySharedPrefernces.saveUserId(context,"");
        }

    }
    private void setUsetProfile() {
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (oldProfile != null) {
                    //登出後
//                    fbName.setText("");
                    mFbImageView.setImageBitmap(null);

                }

                if (currentProfile != null) {
                    //登入
//                    fbName.setText(currentProfile.getName());
                    MySharedPrefernces.saveUserPhoto(context, String.valueOf(currentProfile.getProfilePictureUri(150, 150)));
                    MyApi.loadImage(String.valueOf(currentProfile.getProfilePictureUri(150, 150)), mFbImageView,context);
                    String id = currentProfile.getId();
                    String name = currentProfile.getName();
                    mUserIdTextView.setText(id);
                    mUserAccountTextView.setText(name);

                }

            }
        };
        profileTracker.startTracking();
        if (profileTracker.isTracking()) {
            if (Profile.getCurrentProfile() == null) return;
            if (Profile.getCurrentProfile().getProfilePictureUri(150, 150) != null) {
                MyApi.loadImage(String.valueOf(Profile.getCurrentProfile().getProfilePictureUri(150, 150)), mFbImageView, context);


            }

        } else
            Log.d(getClass().getSimpleName(), "profile currentProfile Tracking: " + "no");

    }
    @OnClick(R.id.nav_camera)
    public  void setBuyAndSellActivity(){
        startActivity(BuyAndSellActivity.class);
    }
    @OnClick(R.id.nav_stockprice)
    public void setStockPiceActiviy(){

        startActivity(QueryStockPriceActivity.class);

    }
    @OnClick(R.id.nav_gallery)
    public void Click()
    {
        if(!checkUserId(context)){
            return;
        };
        startActivity(new Intent(this, ProfitAndLossActvity.class));
    }

    @OnClick(R.id.nav_stock_share)
    public void shareStockActivity() {
        startActivity(ShareStockNumberActivity.class);
    }

    @OnClick(R.id.nav_manage)
    public void PayActivity() {
        startActivity(new Intent(this, InAppBillingActivity.class));
    }

    @OnClick(R.id.nav_calculate)
    public void calculateActivity() {
        startActivity(CalculateActivity.class);
    }

//    @OnClick(R.id.messenger_send_button)
//    public void sendMessageButton() {
//        onMessengerButtonClicked();
//
//    }
    @OnClick(R.id.nav_price)
    public void setStockPriceActivity(){
        startActivity(StockValueAddedRateActivity.class);
    }
    @OnClick(R.id.zerostock)
    public void zeroStockActivity(){
        startActivity(ZeroStockActivity.class);
    }
    @OnClick(R.id.rightbutton)
    public void nextPageButton(){
        if(nextPage.size()>=2){
            setNewsData(nextPage.get(1),true);
        }else {
            setNewsData(nextPage.get(0),true);

        }
    }
    @OnClick(R.id.liftbutton)
    public void returnButton(){
        if(nextPage.size()>=2){
            setNewsData(nextPage.get(1),false);
        }else {
            showToast("已經是最後一頁！！");
            return;

        }
    }
    @OnItemClick(R.id.listView)
    public void listViewOnItemClick(int i){
        Bundle b = new Bundle();
        b.putString("url",newlist.get(i).getNewsDetail());

        startActivity(NewDetailActivity.class,b);


    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void setVponBanner() {
        //get your layout view for Vpon banner
        //create VpadnBanner instance
        vponBanner = new VpadnBanner(this, bannerId, VpadnAdSize.SMART_BANNER, "TW");
        VpadnAdRequest adRequest2 = new VpadnAdRequest();
        //set auto refresh to get banner
        adRequest2.setEnableAutoRefresh(true);
        //load vpon banner
        vponBanner.loadAd(adRequest2);
        //add vpon banner to your layout view
        adBannerLayout.addView(vponBanner);
    }

    private void setAdmobBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    private  void setPageAdView(){
        AdRequest adRequest = new AdRequest.Builder().build();
        mPageAdView.loadAd(adRequest);
    }

    private void setAdbertBanner() {
        adbertView.setMode(AdbertOrientation.NORMAL);
        adbertView.setExpandVideo(ExpandVideoPosition.BOTTOM);
        adbertView.setFullScreen(false);
        adbertView.setBannerSize(AdSize.BANNER);
        adbertView.setAPPID("20170619000001", "90cebe8ef120c8bb6ac2ce529dcb99af");
        adbertView.start();
    }

    private void setClickForce() {


        clickforceAd.getAd(6120, 320, 50, 0.8, 30);

        //Ad Load Callback
        clickforceAd.setOnAdViewLoaded(new AdViewLinstener() {
            @Override
            public void OnAdViewLoadFail() {
                Log.d(TAG, "請求廣告失敗");
            }

            @Override
            public void OnAdViewLoadSuccess() {
                //顯示banner廣告
                clickforceAd.show();
            }
        });

        clickforceAd.outputDebugInfo = true;

    }
//
//    private void onMessengerButtonClicked() {
//        // The URI can reference a file://, content://, or android.resource. Here we use
//        // android.resource for sample purposes.
//        Uri uri =
//                Uri.parse("android.resource://com.jackpan.stockcomputer/" + R.drawable.tree);
//        Log.d(TAG, "onMessengerButtonClicked: "+uri);
//
//        // Create the parameters for what we want to send to Messenger.
//        ShareToMessengerParams shareToMessengerParams =
//                ShareToMessengerParams.newBuilder(uri, "image/jpeg")
//                        .setMetaData("{ \"image\" : \"tree\" }")
//                        .build();
//        Log.d(TAG, "onMessengerButtonClicked: "+mPicking);
//
//        if (mPicking) {
//            // If we were launched from Messenger, we call MessengerUtils.finishShareToMessenger to return
//            // the content to Messenger.
//            MessengerUtils.finishShareToMessenger(this, shareToMessengerParams);
//        } else {
//            // Otherwise, we were launched directly (for example, user clicked the launcher icon). We
//            // initiate the broadcast flow in Messenger. If Messenger is not installed or Messenger needs
//            // to be upgraded, this will direct the user to the play store.
//            MessengerUtils.shareToMessenger(
//                    this,
//                    REQUEST_CODE_SHARE_TO_MESSENGER,
//                    shareToMessengerParams);
//        }
//    }
    private void test2(String number){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
//                    Log.d(TAG, "run: "+" "+number+".htm");
                    Document doc = Jsoup.connect("https://goodinfo.tw/StockInfo/StockDetail.asp?STOCK_ID=2344").get();
                    Element t = doc.select("tr[align=center][height=26px][bgcolor=#e7f3ff]").get(0);
//                    Log.d(TAG, "run: "+ t.select("td").get(0).text());
//                    for (Element td : t.select("td")) {
//                        Log.d(TAG, "run: "+td.text());
//                    }
                    Element e = doc.select("tr[align=center][height=26px][bgcolor=white]").get(0);
//                    Log.d(TAG, "run: "+ e.select("td").get(0).text());
//                    for (Element td : e.select("td")) {
//
//                        Log.d(TAG, "run: "+td.text());
//
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();




    }
    private void setWarningStock(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Document doc = Jsoup.connect("http://jow.win168.com.tw/Z/ZE/ZEW/ZEW.djhtm").get();
//                    for (Element element : doc.select("table[class=t01]>tbody>tr")) {
//                        for (int i = 2; i < element.select("td").size(); i++) {
//                            Log.d(TAG, "run: "+ element.select("td").get(i).text());
//
//
//                        }
//                    }
                    for (int i = 2; i < doc.select("table[class=t01]>tbody>tr").size(); i++) {
//                        Log.d(TAG, "run: "+doc.select("table[class=t01]>tbody>tr").get(i).text());
                        for (Element script : doc.select("table[class=t01]>tbody>tr").get(i).getElementsByTag("script")) {
//                            Log.d(TAG, "run: "+script.text());
//                            Log.d(TAG, "run: "+script.html().toString().replace("<!--",""));
                            MyApi.stockStringReplace(script.html().toString());
                            for (DataNode node : script.dataNodes()) {
//                                Log.d(TAG, "run: "+node.getWholeData().toString().replace("GenLink2stk",""));

                            }
                        }
                        for (Element td : doc.select("table[class=t01]>tbody>tr").get(i).select("td")) {

                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();


    }

    private void setStockData(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    Document doc = Jsoup.connect("http://www.tse.com.tw/exchangeReport/STOCK_DAY?response=html&date=20180128&stockNo=3019").get();
//                    for (Element element : doc.select("table>tbody>tr")) {
//                        Log.d(TAG, "run: "+element.text());
//
//
//                    }
                    Log.d(TAG, "run: "+doc.select("table>tbody>tr").size());
                    for (int i = 0; i < 1; i++) {
//                        for (int i1 = 0; i1 < doc.select("table>tbody>tr").get(i).select("td").size(); i1++) {
//                            Log.d(TAG, "run: "+doc.select("table>tbody>tr").get(i).select("td").get(i));
//                        }
                        for (Element td : doc.select("table>tbody>tr").get(0).select("td")) {
                            Log.d(TAG, "run: "+td.text());
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();

    }

    private void setNewsData() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.show();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    Document doc = Jsoup.connect("https://tw.stock.yahoo.com/news_list/url/d/e/N3.html?q=&pg=1").get();
                    for (Element mtext : doc.getElementsByClass("mtext")) {
                        nextPage.add(mtext.attr("onClick").toString());
                    }
                    if(!nextPage.get(0).equals("")){


                        for (Element table : doc.select("table#newListContainer")) {

                            for (Element tbody : table.select("tbody")) {


                                for (Element tr : tbody.select("tr")) {

                                    for (Element element : tr.select("td[valign=top]>a.mbody")) {
                                        NewsData n = new NewsData();
                                        n.setNewsTitle(element.text());
                                        n.setNewsDetail(element.getElementsByTag("a").attr("href").toString());
                                        newlist.add(n);
                                        }
//                                        NewsData n = new NewsData();
//
//                                        n.setNewsTitle(element.text());
//                                        newlist.add(n);
//
//                                    for (Element td : tr.select("td[valign=top]>span.mbody")) {
//                                        Log.d(TAG, "b: "+td.text());
//                                        n.setNewsDetail(td.text());
//
//                                    }


                                    if (newlist.size() >= 10) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {

                                                mAdapter.notifyDataSetChanged();
                                                mProgressDialog.dismiss();


                                            }
                                        });

                                        return;
                                    }
                                }


                            }

                        }



                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: " + e.getMessage());
                }


            }
        }.start();
    }
    String next;
     String ntx;
    private void setNewsData(String s,boolean isNextPage) {

        newlist.clear();

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {

                    if(nextPage.size()>=2){
                        if(isNextPage ==true){
                            next = nextPage.get(1).replace("location=","");
                            ntx = next.replace("\'","");
                        }else {
                            next = nextPage.get(0).replace("location=","");
                            ntx = next.replace("\'","");
                        }


                    }else {
                        next = nextPage.get(0).replace("location=","");
                        ntx = next.replace("\'","");
                    }

                    Log.d(TAG, "run: "+"https://tw.stock.yahoo.com/news_list/url/d/e/N3.html"+ntx+"");

                    Document doc = Jsoup.connect("https://tw.stock.yahoo.com/news_list/url/d/e/N3.html"+ntx+"").get();

                    nextPage.clear();
                    for (Element mtext : doc.getElementsByClass("mtext")) {
                        nextPage.add(mtext.attr("onClick").toString());


                    }

                    if(!nextPage.get(0).equals("")){
                        for (Element table : doc.select("table#newListContainer")) {

                            for (Element tbody : table.select("tbody")) {
                                for (Element tr : tbody.select("tr")) {
                                    Log.d(TAG, "run: "+tr.select("td[valign=top]>a.mbody").size());
                                    for (int i = 0; i < tr.select("td[valign=top]>a.mbody").size(); i++) {
                                        NewsData mNewsData = new NewsData();
                                        mNewsData.setNewsTitle(tr.select("td[valign=top]>a.mbody").get(i).text());
                                        mNewsData.setNewsDetail(tr.select("td[valign=top]>a.mbody").get(i).getElementsByTag("a").attr("href").toString());

                                        newlist.add(mNewsData);
                                        if (newlist.size() >= 10) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {

                                                    mAdapter.notifyDataSetChanged();


                                                }
                                            });

                                            return;
                                        }
                                    }


                                }
                            }
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "run: " + e.getMessage());
                }


            }
        }.start();
    }
    private  void   getNewDetil(String s,NewsData d){
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {

                    Document doc = Jsoup.connect(s).get();
                    for (Element element : doc.select("table[class=yui-text-left yui-table-wfix ynwsart]>tbody>tr>td>span")) {
//                        Log.d(TAG, "getNewDetil: "+element.text());
                    }
                    for (Element element : doc.select("p")) {
//                        Log.d(TAG, "getNewDetil: "+element.toString());
//                        Log.d(TAG, "getNewDetil: "+element.text());
                        if(!element.text().equals("")){
//                            newlist.add(mNewsData.setNewsDetail(element.text()));
                        }


                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private Bundle getFacebookData(JSONObject object) {

        try {
            Bundle bundle = new Bundle();
            String id = object.getString("id");
            String photo = "";
            MySharedPrefernces.saveUserId(context,id);

            try {
                URL profile_pic = new URL("https://graph.facebook.com/" + id + "/picture?width=200&height=150");
                Log.d(TAG, profile_pic + "");
                bundle.putString("profile_pic", profile_pic.toString());
                photo = profile_pic.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            }

            bundle.putString("idFacebook", id);
            if (object.has("first_name"))
                bundle.putString("first_name", object.getString("first_name"));
            String firstName = object.getString("first_name");
            Log.d(TAG, "getFacebookData: "+object.getString("first_name"));
            if (object.has("last_name"))
                bundle.putString("last_name", object.getString("last_name"));
            String lastName = object.getString("last_name");
            Log.d(TAG, "getFacebookData: "+object.getString("last_name"));
            if (object.has("email"))
                bundle.putString("email", object.getString("email"));
            Log.d(TAG, "getFacebookData: "+object.getString("email"));
            String mail = object.getString("email");
            if (object.has("gender"))
                bundle.putString("gender", object.getString("gender"));
            String gender = object.getString("gender");
            Log.d(TAG, "getFacebookData: "+object.getString("gender"));
            if (object.has("birthday"))
                bundle.putString("birthday", object.getString("birthday"));
            String birthday = object.getString("birthday");
            Log.d(TAG, "getFacebookData: "+MyApi.birthdayToTimeStamp(object.getString("birthday")));
//            MyApi.DateComparison(System.currentTimeMillis(),System.currentTimeMillis());

            if (object.has("location"))
                bundle.putString("location", object.getJSONObject("location").getString("name"));
            Log.d(TAG, "getFacebookData: "+object.getJSONObject("location").getString("name"));
            String location = object.getJSONObject("location").getString("name");
            setMemberData(id,firstName,lastName,mail, String.valueOf(MyApi.birthdayToTimeStamp(object.getString("birthday"))),gender,"",photo,location);
            return bundle;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void updateWithToken(AccessToken currentAccessToken) {

        if (currentAccessToken != null) {
        } else {
        }
    }

    @Override
    public void getDatabaseData(Object o) {

    }

    @Override
    public void getDeleteState(boolean b, String s, Object o) {

    }

    @Override
    public void createUserState(boolean b) {

    }

    @Override
    public void useLognState(boolean b) {
        Log.d(TAG, "useLognState: "+b);

    }

    @Override
    public void getuseLoginId(String s) {
        Log.d(TAG, "getuseLoginId: "+s);

    }

    @Override
    public void getuserLoginEmail(String s) {

    }

    @Override
    public void resetPassWordState(boolean b) {

    }

    @Override
    public void getFireBaseDBState(boolean b, String s) {

    }

    @Override
    public void getFirebaseStorageState(boolean b) {

    }

    @Override
    public void getFirebaseStorageType(String s, String s1) {

    }

    @Override
    public void getsSndPasswordResetEmailState(boolean b) {

    }

    @Override
    public void getUpdateUserName(boolean b) {

    }

    @Override
    public void getUserLogoutState(boolean b) {

    }
    public class MyAdapter extends BaseAdapter {
        private ArrayList<NewsData> mDatas;

        public MyAdapter(ArrayList<NewsData> datas) {
            mDatas = datas;
        }
        public void updateData(ArrayList<NewsData> datas) {
            mDatas = datas;
            notifyDataSetChanged();
        }
        @Override
        public int getCount() {
            return mDatas.size();
        }

        @Override
        public Object getItem(int position) {
            return mDatas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            NewsData data = mDatas.get(position);
            if(convertView!=null){
                viewHolder = (ViewHolder)convertView.getTag();
            }else {
                convertView = LayoutInflater.from(MainActivity.this).inflate(
                        R.layout.layout_homepage, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            }
            viewHolder.mTitleTextView.setText(data.getNewsTitle()+"");
            viewHolder.mDetailTextView.setText("(詳全文...)");

            return convertView;
        }

    }
    static class  ViewHolder{
        @BindView(R.id.title)
        TextView mTitleTextView;
        @BindView(R.id.detail)
        TextView mDetailTextView;

        public ViewHolder(View v){
            ButterKnife.bind(this,v);
        }
    }
}

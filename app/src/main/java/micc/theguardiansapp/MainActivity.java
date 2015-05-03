package micc.theguardiansapp;

import android.content.Intent;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import it.sephiroth.android.library.tooltip.TooltipManager;
import micc.theguardiansapp.audioPlayer.AudioPlayer;
import micc.theguardiansapp.audioPlayer.AudioPlayerListener;
import micc.theguardiansapp.beaconHelper.*;
import micc.theguardiansapp.dotsProgressBar.DotsProgressBar;
import micc.theguardiansapp.scrollPager.MyScrollPager;
import micc.theguardiansapp.scrollPager.ScrollPagerListener;


import com.daimajia.slider.library.Animations.*;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.estimote.sdk.Beacon;


import java.util.List;


public class MainActivity
        extends ActionBarActivity
        implements MyBeaconListener, ScrollPagerListener
{

    boolean SIMULATE_BEACON = false;

    private final static int DP_BEACON_TOOLTIP = 35;
    private static final int DRAWABLE_PLAY = R.drawable.play;
    private static final int DRAWABLE_STOP = R.drawable.stop;


    private static final int REFRESH_BEACON_DELAY = 5000;



    private static SensorManager sensorManager;
    private static Sensor proximitySensor;



    MyScrollPager scrollPager;
    private ScrollView scrollView;
    private ViewGroup contentView;
    private ViewGroup[] fragContainer;

    private boolean atLeastOneBeacon = false;

    private static final int REQUEST_ENABLE_BT = 1234;


    private BackgroundBeaconManager backgroundBeaconManager;

    private ForegroundBeaconManager beaconManager;


    private ImageButton btnFi;
    private ImageButton btnMi;
    private ImageButton btnNy;

    private final int nFragment = 4;

    private AudioPlayer[] audioPlayer = new AudioPlayer[4];
    private ImageButton[] audioButton = new ImageButton[4];
    String audioTooltipText[] = new String[4];



    boolean playing = false;


    TooltipManager tooltipManager;
    DotsProgressBar progressBar;


    ImageView statueImageView;

    SliderLayout slideShow1;
    SliderLayout slideShow2;
    SliderLayout slideShow3;
    MySmallTextSliderView tsv_slide1_1;
    MySmallTextSliderView tsv_slide1_2;
    MySmallTextSliderView tsv_slide2_1;
    MySmallTextSliderView tsv_slide2_2;
    MySmallTextSliderView tsv_slide3_1;


    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);




            setContentView(R.layout.activity_main);
            setTitle("Hero");


        btnFi = (ImageButton) findViewById(R.id.fab_FI);
        btnMi = (ImageButton) findViewById(R.id.fab_MI);
        btnNy = (ImageButton) findViewById(R.id.fab_NY);
        btnFi.setEnabled(false);
        btnMi.setEnabled(false);
        btnNy.setEnabled(false);


        progressBar = (DotsProgressBar) findViewById(R.id.dotsProgressBar);
            progressBar.setDotsCount(4);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setActiveDot(2);


            tooltipManager = TooltipManager.getInstance(this);



            setEventListeners();
            scrollView = (ScrollView) findViewById(R.id.scroll_view);
            contentView = (ViewGroup) findViewById(R.id.scrolledLayout);
            fragContainer = new ViewGroup[4];

            fragContainer[0] = (ViewGroup) findViewById(R.id.fragContainer0);
            fragContainer[1] = (ViewGroup) findViewById(R.id.fragContainer1);
            fragContainer[2] = (ViewGroup) findViewById(R.id.fragContainer2);
            fragContainer[3] = (ViewGroup) findViewById(R.id.fragContainer3);


            scrollPager = new MyScrollPager(scrollView, contentView, fragContainer, true, false);
            scrollPager.setOnScrollListener(this);
            scrollView.setOnTouchListener(scrollPager);
             scrollPager.setDotsPageProgressBar(progressBar);

            statueImageView = (ImageView) findViewById(R.id.statueImageView);

            scrollView.post(new Runnable() {
                public void run() {
//                    scrollView.scrollTo(0, contentView.getPaddingTop());
//                    scrollPager.setDotsPageProgressBar(progressBar);

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)statueImageView.getLayoutParams();
//                    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                    params.addRule(RelativeLayout.);
                    params.addRule(RelativeLayout.ALIGN_BOTTOM, R.id.fragContainer0);
                    statueImageView.setLayoutParams(params);

                    tooltipManager.remove(999);
                    tooltipManager.create(999)
                            .anchor(new Point((int)fragContainer[0].getWidth()/2, + dpToPx(DP_BEACON_TOOLTIP) ), TooltipManager.Gravity.BOTTOM)
                                    //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                            .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                            .closePolicy(TooltipManager.ClosePolicy.None, -1)
                            .text("Get closer to the hero and enjoy the additional content")
                            .toggleArrow(false)
                            .withCustomView(R.layout.custom_textview, false)
                            .maxWidth(400)
                            .showDelay(300)
                            .show();


                    if(SIMULATE_BEACON) activateBeaconContents();

                }
            });


            //FragmentHelper.setMainActivity(this);

//            Intent intent = new Intent(this, BeaconService.class);
//
//            if (intent != null) {
//                this.startService(intent);
//            }






//
//
//            TooltipManager.getInstance(this)
//                    .create(100)
//                    .anchor(new Point(500, 500), TooltipManager.Gravity.BOTTOM)
//                    .closePolicy(TooltipManager.ClosePolicy.TouchOutside, 3000)
//                    .activateDelay(800)
//                    .text("Something to display in the tooltip...")
//                    .maxWidth(500)
//                    .show();



            audioInit();

            slideShow1 = (SliderLayout) findViewById(R.id.activity_main_imageSlider_1);
            slideShow2 = (SliderLayout) findViewById(R.id.activity_main_imageSlider_2);
            slideShow3 = (SliderLayout) findViewById(R.id.activity_main_imageSlider_3);


            tsv_slide1_1 = new MySmallTextSliderView(this);
            tsv_slide1_2 = new MySmallTextSliderView(this);
            tsv_slide2_1 = new MySmallTextSliderView(this);
            tsv_slide2_2 = new MySmallTextSliderView(this);
            tsv_slide3_1 = new MySmallTextSliderView(this);

            initSlideShow1();
            initSlideShow2();
            initSlideShow3();





       // backgroundBeaconManager = new BackgroundBeaconManager(this);
        beaconManager = new ForegroundBeaconManager(this, this);





    }

    private void audioInit() {

        audioButton[0] = (ImageButton) findViewById(R.id.activity_main_audioButton0);
        audioButton[1] = (ImageButton) findViewById(R.id.activity_main_audioButton1);
        audioButton[2] = (ImageButton) findViewById(R.id.activity_main_audioButton2);
        audioButton[3] = (ImageButton) findViewById(R.id.activity_main_audioButton3);

        audioPlayer[0] = null;
        audioPlayer[1] = new AudioPlayer(getBaseContext());
        audioPlayer[2] = new AudioPlayer(getBaseContext());
        audioPlayer[3] = new AudioPlayer(getBaseContext());
        audioPlayer[1].loadAudio(R.raw.saracino_intro_1);
        audioPlayer[2].loadAudio(R.raw.saracino_intro_2);
        audioPlayer[3].loadAudio(R.raw.saracino_intro_3);

        audioTooltipText[0] = "The author: Antonio Pio Saracino";
        audioTooltipText[1] = "The author: Antonio Pio Saracino";
        audioTooltipText[2] = "The author: Antonio Pio Saracino";
        audioTooltipText[3] = "The author: Antonio Pio Saracino";


        audioButton[0].setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                scrollPager.gotoFragment(1);
                audioButton[1].performClick();
            }
        });

        for(int i = 1; i < 4; i ++ )
        {

            final int index = i;

            tooltipManager.hide(i);

            audioButton[i].setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    playing = !playing;
                    if(playing) {
                        audioPlay(index);
                    }
                    else audioStop(index);
                    //audioToggle(index);

                }
            });

            audioPlayer[index].setAudioPlayerListener(new AudioPlayerListener() {
                @Override
                public void onCompletion(boolean inEarpieceMode) {
// non funziona! il gestore di eventi non viene richiamato fino a che non si riattiva lo schermo
//                    if(inEarpieceMode)
//                    {
//                        audioButton[index].setImageResource(DRAWABLE_PLAY);
//                        audioButton[(index + 1) % nFragment].setImageResource(DRAWABLE_STOP);
//                        audioPlayer[(index + 1) % nFragment].play();
//                    }
                    audioCompleted(index);
                    scrollPager.gotoFragment((index + 1) % nFragment);


                }
                @Override
                public void onPaused() { }
                @Override
                public void onStopped() {
                    audioStop(index);
                }
            });

        }

    }

    private void audioToggle(int index)
    {

        if(audioPlayer[index].isPlaying())
            audioStop(index);
        else audioPlay(index);
    }
    private void audioPlay(int index)
    {
        if(index != 0 && index <= nFragment) {
            audioPlayer[index].play();
            audioButton[index].setImageResource(DRAWABLE_STOP);

            tooltipManager.create(index)
                    .anchor(new Point((int)scrollView.getWidth()/2, (int)scrollView.getHeight() - dpToPx(35) ), TooltipManager.Gravity.TOP)
                            //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                    .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                    .closePolicy(TooltipManager.ClosePolicy.None, -1)
                    .text(audioTooltipText[index])
                    .toggleArrow(false)
                    .withCustomView(R.layout.custom_textview, false)
                    .maxWidth(400)
                    .showDelay(300)
                    .show();
        }

        switch(index)
        {
            case 1:
                cycleSlideShow1();
                break;
            case 2:
                cycleSlideShow2();
                break;
            case 3:
                cycleSlideShow3();
        }
    }
    private void audioCompleted(int index)
    {
        tooltipManager.hide(index);

        if( index != 0 && index <= nFragment)
        {
            audioButton[index].setImageResource(DRAWABLE_PLAY);

        }
        stopCycleSlideShow1();
        stopCycleSlideShow2();
        stopCycleSlideShow3();
//        switch(index)
//        {
//            case 1:
//                stopCycleSlideShow1();
//                break;
//            case 2:
//                stopCycleSlideShow2();
//                break;
//            case 3:
//                stopCycleSlideShow3();
//        }

    }
    private void audioStop(int index)
    {
        tooltipManager.hide(index);

        if( index != 0 && index <= nFragment)
        {
            if(audioPlayer[index].isPlaying())
                audioPlayer[index].stop();

            audioButton[index].setImageResource(DRAWABLE_PLAY);
        }


        stopCycleSlideShow1();
        stopCycleSlideShow2();
        stopCycleSlideShow3();

//        switch(index)
//        {
//            case 1:
//                stopCycleSlideShow1();
//                break;
//            case 2:
//                stopCycleSlideShow2();
//                break;
//            case 3:
//                stopCycleSlideShow3();
//        }

    }






    private void unloadSlideShow1() {
//        slideShow1.removeAllSliders();
//        slideShow1.addSlider(tsv_slide1_1);
    }
    private void initSlideShow1(){
        tsv_slide1_1
                .description("The artist: Antonio Pio Saracino")
                .image(R.drawable.antonio_pio)
                .setScaleType(BaseSliderView.ScaleType.CenterInside);
        tsv_slide1_2
                .description("Bryant Park in New York City")
                .image(R.drawable.bryant_park)
                .setScaleType(BaseSliderView.ScaleType.CenterInside);
        //slideShow1.setPresetTransformer(SliderLayout.Transformer.DepthPage);
        slideShow1.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //slideShow1.setCustomAnimation(new com.daimajia.slider.library.Animations.DescriptionAnimation());


        slideShow1.addSlider(tsv_slide1_1);
        slideShow1.addSlider(tsv_slide1_2);
        slideShow1.stopAutoCycle();
        slideShow1.setCurrentPosition(0);


    }
    private void loadSlideShow1() {
//        slideShow1.addSlider(tsv_slide1_2);
//        unloadSlideShow2();
    }
    private void cycleSlideShow1() {
        slideShow1.setCurrentPosition(0);
        slideShow1.startAutoCycle(15000, 17000, false);
    }
    private void stopCycleSlideShow1() {
        slideShow1.stopAutoCycle();
    }


    private void unloadSlideShow2() {
//        slideShow1.removeSliderAt(1);
    }
    private void initSlideShow2() {
        tsv_slide2_1
                .description("The HERO")
                .image(R.drawable.hero)
                .setScaleType(BaseSliderView.ScaleType.CenterInside);
        tsv_slide2_2
                .description("SUPERHERO")
                .image(R.drawable.hero_cape)
                .setScaleType(BaseSliderView.ScaleType.CenterInside);
        //slideShow2.setPresetTransformer(SliderLayout.Transformer.DepthPage);
        slideShow2.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //slideShow2.setCustomAnimation(new com.daimajia.slider.library.Animations.DescriptionAnimation());

        slideShow2.addSlider(tsv_slide2_1);
        slideShow2.addSlider(tsv_slide2_2);
        slideShow2.stopAutoCycle();
        slideShow2.setCurrentPosition(0);



    }
    private void loadSlideShow2() {
//        slideShow2.addSlider(tsv_slide2_2);
//        unloadSlideShow1();
    }
    private void cycleSlideShow2() {
        slideShow2.setCurrentPosition(0);
        slideShow2.startAutoCycle(12000, 12000,true );
    }
    private void stopCycleSlideShow2() {
        slideShow2.stopAutoCycle();
    }

    private void initSlideShow3(){
        tsv_slide3_1
                .description("Accademia Gallery in Florence")
                .image(R.drawable.accademia)
                .setScaleType(BaseSliderView.ScaleType.CenterInside);
        //slideShow3.setPresetTransformer(SliderLayout.Transformer.DepthPage);
        slideShow3.setPresetIndicator(SliderLayout.PresetIndicators.Right_Bottom);
        //slideShow3.setCustomAnimation(new com.daimajia.slider.library.Animations.DescriptionAnimation());

        slideShow3.addSlider(tsv_slide3_1);
        slideShow2.stopAutoCycle();

    }
    private void loadSlideShow3() {
//        unloadSlideShow1();
//        unloadSlideShow2();
    }
    private void cycleSlideShow3() {
        //slideShow1.setCurrentPosition(0);
        slideShow3.stopAutoCycle();
    }
    private void stopCycleSlideShow3() {
    slideShow3.stopAutoCycle();
    }




    @Override
    public void onFragmentChanged(int oldFragment, int newFragment) {


        if(playing)
        {
            if(oldFragment != 0)
                audioStop(oldFragment);

            if(newFragment != 0)
                audioPlay(newFragment);
        }

        tooltipManager.remove(999);



        switch(newFragment)
        {
            case 0:
                playing = false;
                if( beaconized)
                    tooltipManager.create(999)
                        .anchor(new Point((int)fragContainer[0].getWidth()/2, + dpToPx(DP_BEACON_TOOLTIP) ), TooltipManager.Gravity.BOTTOM)
                                //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                        .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                        .closePolicy(TooltipManager.ClosePolicy.None, -1)
                        .text("You are approaching the Hero, enjoy additional app contents")
                        .toggleArrow(false)
                        .withCustomView(R.layout.custom_textview_dark, true)
                        .maxWidth(400)
                        .showDelay(300)
                        .show();

                else
                    tooltipManager.create(999)
                            .anchor(new Point((int)fragContainer[0].getWidth()/2, + dpToPx(DP_BEACON_TOOLTIP) ), TooltipManager.Gravity.BOTTOM)
                                    //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                            .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                            .closePolicy(TooltipManager.ClosePolicy.None, -1)
                            .text("Get closer to the Hero and enjoy the additional content")
                            .toggleArrow(false)
                            .withCustomView(R.layout.custom_textview, true)
                            .maxWidth(400)
                            .showDelay(300)
                            .show();
            case 1:
                loadSlideShow1();
                break;
            case 2:
                loadSlideShow2();
                break;
            case 3:
                loadSlideShow3();
                break;
        }

    }

    @Override
    public void onPageChanged(int oldPage, int newPage, int oldFragment, int newFragment) {

    }


    @Override
    protected void onStart() {
        super.onStart();
        beaconManager.start();
        for (int i = 1; i < 4; i++) {
            audioPlayer[i].onActivityStarted();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        beaconManager.stop();

        for(int i = 1; i < 4; i++) {
            audioPlayer[i].onActivityStopped();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    private void setEventListeners() {
        ImageButton miButton = (ImageButton) findViewById(R.id.fab_MI);
        ImageButton fiButton = (ImageButton) findViewById(R.id.fab_FI);
        ImageButton nyButton = (ImageButton) findViewById(R.id.fab_NY);

        miButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                      //  v.setBackground();


                }
                return false;
            }
        });
        miButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMI();
            }
        });

        fiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFI();
            }
        });

        nyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickNY();
            }
        });

    }

    private void onClickMI() {
        Intent intent = new Intent(this, MiActivity.class);
        startActivity(intent);
    }

    private void onClickFI() {
        Intent intent = new Intent(this, FiActivity.class);
        startActivity(intent);
    }

    private void onClickNY() {
        Intent intent = new Intent(this, NYCActivity.class);
        startActivity(intent);
    }


    private boolean beaconized = false;
    private void deactivateBeaconContents(){

        beaconized = false;
        tooltipManager.remove(999);
        tooltipManager.create(999)
                .anchor(new Point((int)fragContainer[0].getWidth()/2, + dpToPx(DP_BEACON_TOOLTIP) ), TooltipManager.Gravity.BOTTOM)
                        //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                .closePolicy(TooltipManager.ClosePolicy.None, -1)
                .text("Get closer to the Hero and enjoy the additional content")
                .toggleArrow(false)
                .withCustomView(R.layout.custom_textview, true)
                .maxWidth(400)
                .showDelay(300)
                .show();


        if(!SIMULATE_BEACON) {
            btnFi.setEnabled(false);
            btnMi.setEnabled(false);
            btnNy.setEnabled(false);
            btnFi.setImageResource(R.drawable.layout_city_button_fi);
            btnMi.setImageResource(R.drawable.layout_city_button_mi);
            btnNy.setImageResource(R.drawable.layout_city_button_ny);
        }
    }
    private void activateBeaconContents(){
        beaconized = true;

        tooltipManager.remove(999);

        tooltipManager.create(999)
                .anchor(new Point((int)fragContainer[0].getWidth()/2, + dpToPx(DP_BEACON_TOOLTIP) ), TooltipManager.Gravity.BOTTOM)
                        //.anchor(scrollView, TooltipManager.Gravity.CENTER)
                .actionBarSize(Utils.getActionBarSize(getBaseContext()))
                .closePolicy(TooltipManager.ClosePolicy.None, -1)
                .text("You are approaching the Hero, enjoy additional app contents")
                .toggleArrow(false)
                .withCustomView(R.layout.custom_textview_dark, true)
                .maxWidth(400)
                .showDelay(300)
                .show();

        btnFi.setEnabled(true);
        btnMi.setEnabled(true);
        btnNy.setEnabled(true);
        btnFi.setImageResource(R.drawable.layout_city_button_fi_enabled);
        btnMi.setImageResource(R.drawable.layout_city_button_mi_enabled);
        btnNy.setImageResource(R.drawable.layout_city_button_ny_enabled);

    }

    @Override
    public void onNewBeacons(List<Beacon> newInProximityBeaconList) {
        if(atLeastOneBeacon == false && beaconManager.getBeaconSize() > 0)
        {
            atLeastOneBeacon = true;
            activateBeaconContents();
        }
    }

    @Override
    public void onRemovedBeacons(List<Beacon> removedBeacons)
    {
        if(atLeastOneBeacon == true && beaconManager.getBeaconSize() == 0)
        {
            atLeastOneBeacon = false;
            deactivateBeaconContents();
        }
    }

}

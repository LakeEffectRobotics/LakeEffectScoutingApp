package ca.lakeeffect.scoutingapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.test.mock.MockDialogInterface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.Toast;

public class Field implements View.OnTouchListener {

    FieldUIPage fieldUIPage;

    SurfaceView surface;
    Bitmap field;

    //there's probably a better way of doing this
    Context context;
    LayoutInflater layoutInflater;

    final int WIDTH = 320;
    final int HEIGHT = 320;
    
    Rect[] fieldPlacements = new Rect[]{
            //makeRect(500, 350),
            //makeRect(2090, 50)
    };

    //the normal paint for the boxes
    Paint normal = new Paint();

    //the highlited paint for the boxes
    Paint highlited = new Paint();

    //for scaling collision detection
    float scale; //multiplier of how much it scaled

    int selected = -1; //currently selected item (-1 is ground)

    boolean currentScale = false;

    //code in surfacecreated can only be called once
    boolean alreadyCreated = false;

    Rect backgroundRect;
    Paint backgroundPaint;

    MediaPlayer fish;

    public Field(final FieldUIPage fieldUIPage, SurfaceView s, Bitmap field, Context c, LayoutInflater l) {
        this.fieldUIPage = fieldUIPage;
        surface = s;
        this.field = field;

        this.context = c;
        this.layoutInflater = l;

        fish = MediaPlayer.create(s.getContext(), R.raw.f42);
        fish.setLooping(true);

        normal.setColor(ResourcesCompat.getColor(s.getResources(), R.color.colorPrimary, null));
        normal.setStyle(Paint.Style.STROKE);

        highlited.setColor(Color.YELLOW);
        highlited.setStyle(Paint.Style.FILL);
        highlited.setAlpha(125);

        surface.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if(alreadyCreated){
                    redraw();
                    return;
                }
                alreadyCreated = true;

                backgroundRect = new Rect(0, 0, surface.getWidth(), surface.getHeight());

                //get theme background color
                backgroundPaint = new Paint();
                if (fieldUIPage.autoPage) {
                    TypedValue typedValue = new TypedValue();
                    Field.this.fieldUIPage.getContext().getTheme().resolveAttribute(R.attr.colorAuto, typedValue, true);
                    backgroundPaint.setColor(typedValue.data);
                } else {
                    TypedValue typedValue = new TypedValue();
                    Field.this.fieldUIPage.getContext().getTheme().resolveAttribute(android.R.attr.colorBackground, typedValue, true);
                    backgroundPaint.setColor(typedValue.data);
                }

                boolean scaleByHeight = false;

                //scaled with height
                float scaledWidth = (Field.this.field.getWidth() / (float) Field.this.field.getHeight()) * surface.getHeight();

                //scaled with width
                float scaledHeight = (Field.this.field.getHeight() / (float) Field.this.field.getWidth()) * surface.getWidth();

                if(scaledWidth > surface.getWidth()){ //scale by width
                    android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
                    lp.height = (int) (scaledHeight);

                    surface.setLayoutParams(lp);
                }else{ //scale by height

                    scaleByHeight = true;

                    android.view.ViewGroup.LayoutParams lp = surface.getLayoutParams();
                    lp.width = (int) (scaledWidth);

                    surface.setLayoutParams(lp);
                }

                Canvas canvas = holder.lockCanvas();

                scale = (float) Field.this.field.getHeight() / ((float) canvas.getHeight());

                if(!scaleByHeight) {
                    scale = (float) Field.this.field.getWidth() / ((float) canvas.getWidth());
                }

                    //set paint stroke based on screen size
                normal.setStrokeWidth(canvas.getHeight()/50);
                highlited.setStrokeWidth(canvas.getHeight()/50);

                if(!scaleByHeight){
                    Field.this.field = Bitmap.createScaledBitmap(Field.this.field, canvas.getWidth(), (int) (scaledHeight), true);
                } else {
                    Field.this.field = Bitmap.createScaledBitmap(Field.this.field, (int) (scaledWidth), canvas.getHeight(), true);
                }

                canvas.drawColor(Color.BLACK);

                drawImage(canvas);

                holder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

    }

    // called by the FieldUIPage class when the deselect button is hit
    public void deselect(){
        Canvas c = surface.getHolder().lockCanvas();

        if (c != null) {

            selected = -1;

            drawImage(c, selected);

            surface.getHolder().unlockCanvasAndPost(c);
        }

    }

    public Rect makeRect (int x, int y){
        return new Rect(x, y, x + WIDTH, y + HEIGHT);
    }

    //if the user says that blue starts on the left, the field should rotate 180
    //also if the user says that they are the blue alliance, then the rectangle rotate 180 again
    private boolean lastside = false;
    private boolean lastalliance = false;
    public void updateField(MainActivity mainActivity, boolean side, boolean alliance){

        if(side != lastside){

            //this all rotates the image
            Matrix matrix = new Matrix();
            matrix.postScale(-1, -1, field.getWidth(), field.getHeight());

            field = Bitmap.createBitmap(field, 0, 0, field.getWidth(), field.getHeight(), matrix, true);
            
            //Flip rectangles
            rotateRectangles(field);

            lastside = side;
        }
        if(alliance != lastalliance){
            //Rotate rectangles rectangles
            rotateRectangles(field);

            lastalliance = alliance;
        }

        //this redraw() down here because if just the alliance colour changes, then the if statement won't run
        redraw();
    }

    private void rotateRectangles(Bitmap field){
        for (int i=0; i<fieldPlacements.length; i++){
            int fieldWidth = (int)(field.getWidth() * scale);
            int fieldHeight = (int)(field.getHeight() * scale);
            int rectWidth = (int) (fieldPlacements[i].right - fieldPlacements[i].left);
            int rectHeight = (int) (fieldPlacements[i].bottom - fieldPlacements[i].top);
            fieldPlacements[i] = new Rect(fieldWidth - fieldPlacements[i].left - rectWidth, fieldHeight - fieldPlacements[i].top - rectHeight, fieldWidth - fieldPlacements[i].right + rectWidth, fieldHeight - fieldPlacements[i].bottom + rectHeight);
        }
    }

    @Override
    public boolean onTouch(final View v, final MotionEvent event) {
        System.out.println(event.getAction());
        if(event.getAction() == MotionEvent.ACTION_CANCEL){
            return false;
        }
        if(event.getAction() == MotionEvent.ACTION_UP) {
            if (v == surface) {
                Canvas c = surface.getHolder().lockCanvas();

                //play a bit
                if (fieldUIPage.autoPage) {
                    playSoundForXSeconds(fish, 250);
                }

                if (c != null) {
                    // the place selected, -1 if none
                    selected = -1;

                    Paint paint = new Paint();
                    paint.setColor(Color.RED);

                    //adds the offset position
                    float xpos = event.getX() - (c.getWidth() / 2 - field.getWidth() / 2);
                    if (xpos > 0 && xpos < field.getWidth()) {
                        //send toast

                        for (Rect rect : fieldPlacements) {
                            float unScaledXPos = xpos * scale;
                            float unScaledYPos = event.getY() * scale;

                            if (unScaledXPos > rect.left && unScaledXPos < rect.right && unScaledYPos > rect.top && unScaledYPos < rect.bottom) {

                                selected = java.util.Arrays.asList(fieldPlacements).indexOf(rect);
                            }
                        }
                    }

                    drawImage(c, selected);

                    c.drawCircle(event.getX(), event.getY(), 15, paint);
                    surface.getHolder().unlockCanvasAndPost(c);

                    //just print it out for debugging purposes
                    System.out.println(event.getX());
                    System.out.println(event.getY());

                    final long windowOpenedTime = System.currentTimeMillis();
                    if (selected == -1) {
                        System.out.println("Making a dialog");
                        //FieldUIPage.openMainInput();

                        final View mainInputView = layoutInflater.inflate(R.layout.main_input, null);

                        AlertDialog alertDialog = new android.app.AlertDialog.Builder(context)
                                .setTitle("Input")
                                .setView(mainInputView)
                                .setPositiveButton("Ok", (new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //make a new event
                                        float[] data = {
                                                ((RatingBar) mainInputView.findViewById(R.id.missedShots)).getRating(),
                                                ((RatingBar) mainInputView.findViewById(R.id.level1Shots)).getRating(),
                                                ((RatingBar) mainInputView.findViewById(R.id.level2Shots)).getRating(),
                                                ((RatingBar) mainInputView.findViewById(R.id.level3Shots)).getRating(),
                                                ((RatingBar) mainInputView.findViewById(R.id.missedPickups)).getRating(),
                                                ((RatingBar) mainInputView.findViewById(R.id.pickups)).getRating()};

                                        float[] location = {event.getX(), event.getY()};

                                        long[] time = {windowOpenedTime, System.currentTimeMillis()};

                                        fieldUIPage.addEvent(new Event(data, location, time, 0), "", false);
                                        Toast.makeText(context, "Event recorded", Toast.LENGTH_SHORT).show();
                                    }
                                }))
                                .setNegativeButton("Cancel", null)
                                .create();

                        alertDialog.show();
                        alertDialog.getWindow().setLayout(MainActivity.getScreenWidth() - 10, MainActivity.getScreenHeight() - 10);

                    } else {
                        final View spinnyPageView = layoutInflater.inflate(R.layout.spinny_boi_page, null);
                        new android.app.AlertDialog.Builder(context)
                                .setTitle("Input")
                                .setView(spinnyPageView)
                                .setPositiveButton("Ok", (new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //basically save what happened, but as an event too

                                        float[] data = {-1, -1, -1, -1, -1, -1};
                                        float[] location = {-1, -1};
                                        long[] time = {windowOpenedTime, System.currentTimeMillis()};

                                        //woah, repetitive code!
                                        //fixing this would be nice, but I don't know how
                                        int metadata = -1;
                                        if(((CheckBox) spinnyPageView.findViewById(R.id.rotationButtonSuccess)).isChecked()){
                                            metadata = 1;
                                        }
                                        if(((CheckBox) spinnyPageView.findViewById(R.id.rotationButtonFail)).isChecked()){
                                            metadata = 2;
                                        }
                                        if(((CheckBox) spinnyPageView.findViewById(R.id.colourButtonSuccess)).isChecked()){
                                            metadata = 3;
                                        }
                                        if(((CheckBox) spinnyPageView.findViewById(R.id.colourButtonFail)).isChecked()){
                                            metadata = 4;
                                        }

                                        fieldUIPage.addEvent(new Event(data, location, time, metadata), "", false);
                                        Toast.makeText(context, "TODO: change this", Toast.LENGTH_SHORT).show();
                                    }
                                }))
                                .setNegativeButton("Cancel", null)
                                .create()
                                .show();
                    }
                }

            }
        }
        return true;
    }

    public void redraw(){
        Canvas c = surface.getHolder().lockCanvas();

        if (c != null) {
            drawImage(c);

            surface.getHolder().unlockCanvasAndPost(c);
        }
    }

    public void drawImage(Canvas c) {
        drawImage(c, -1);
    }

    public void drawImage(Canvas c, int selected) {
        Bitmap fieldBitmap = field;

        //clear screen
        c.drawRect(backgroundRect, backgroundPaint);

        c.drawBitmap(fieldBitmap, 0, 0, null);

        for (Rect rect : fieldPlacements) {
            Rect scaledRect = scaleRect(rect, c);

            if(java.util.Arrays.asList(fieldPlacements).indexOf(rect) == selected){
                c.drawRect(scaledRect.left, scaledRect.top, scaledRect.right, scaledRect.bottom, highlited);
            }else {
                c.drawRect(scaledRect.left, scaledRect.top, scaledRect.right, scaledRect.bottom, normal);
            }
        }
    }

    public Rect scaleRect(Rect rect, Canvas c) {
        Rect scaledRect = new Rect(rect);

        scaledRect.left /= scale;
        scaledRect.top /= scale;

        scaledRect.right /= scale;
        scaledRect.bottom /= scale;

        return scaledRect;
    }

    //modified from: https://stackoverflow.com/questions/7383808/android-how-can-play-song-for-30-seconds-only-in-mediaplayer
    private void playSoundForXSeconds(final MediaPlayer mediaPlayer, int millis) {
        try {
            mediaPlayer.start();
        }catch(Exception e) {
            e.printStackTrace();
        }

        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                try {
                    mediaPlayer.pause();
                }catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }, millis);
    }

}
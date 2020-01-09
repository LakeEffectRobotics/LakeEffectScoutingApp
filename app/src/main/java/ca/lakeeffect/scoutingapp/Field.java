package ca.lakeeffect.scoutingapp;

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
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class Field implements View.OnTouchListener {

    FieldUIPage fieldUIPage;

    SurfaceView surface;
    Bitmap field;

    final int WIDTH = 320;
    final int HEIGHT = 320;
    
    Rect[] fieldPlacements = new Rect[]{
            makeRect(500, 350),
            makeRect(2090, 50)
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

    public Field(final FieldUIPage fieldUIPage, SurfaceView s, Bitmap field) {
        this.fieldUIPage = fieldUIPage;
        surface = s;
        this.field = field;

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

    //if the user says that blue starts on the left, the field should flip
    public void updateField(MainActivity mainActivity, boolean side){

        if(side){

            //this all flips the image
            Matrix matrix = new Matrix();
            matrix.postScale(-1, 1, field.getWidth(), field.getHeight());

            field = Bitmap.createBitmap(field, 0, 0, field.getWidth(), field.getHeight(), matrix, true);
            
            //Flip rectangles
            for (int i=0; i<fieldPlacements.length; i++){
                int fieldWidth = (int)(field.getWidth() * scale);
                int rectWidth = (int) (fieldPlacements[i].right - fieldPlacements[i].left);
                fieldPlacements[i] = new Rect(fieldWidth - fieldPlacements[i].left - rectWidth, fieldPlacements[i].top, fieldWidth - fieldPlacements[i].right + rectWidth, fieldPlacements[i].bottom);
            }
        }

        //this redraw() down here because if just the alliance colour changes, then the if statement won't run
        redraw();
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
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
            }

        }
        return false;
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
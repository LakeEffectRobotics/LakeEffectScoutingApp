package ca.lakeeffect.scoutingapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.ArrayList;

public class Field implements View.OnTouchListener {


    SurfaceView surface;
    Bitmap fieldRed, fieldBlue;

    //hello ajay, how's it going
    //I am typing in comments

    //This should work



    Rect[] fieldPlacements = new Rect[]{
            new Rect(37, 9, 183, 155),
            new Rect(44, 288, 190, 435),
            new Rect(37, 748, 183, 894),
            new Rect(303, 379, 431, 526),
            new Rect(426, 244, 572, 390),
            new Rect(426, 513, 572, 659),
            new Rect(816, 200, 961, 345),
            new Rect(816, 558, 961, 703),
            new Rect(1206, 244, 1351, 390),
            new Rect(1206, 513, 1351, 659),
            new Rect(1347, 379, 1474, 526),
            new Rect(1594, 9, 1740, 155),
            new Rect(1587, 469, 1733, 614),
            new Rect(1594, 748, 1740, 894)
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

    public Field(SurfaceView s, Bitmap fieldRed, Bitmap fieldBlue) {
        surface = s;
        this.fieldRed = fieldRed;
        this.fieldBlue = fieldBlue;

        normal.setColor(Color.RED);
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

                boolean scaleByHeight = false;

                //scaled with height
                float scaledWidth = (Field.this.fieldRed.getWidth() / (float) Field.this.fieldRed.getHeight()) * surface.getHeight();

                //scaled with width
                float scaledHeight = (Field.this.fieldRed.getHeight() / (float) Field.this.fieldRed.getWidth()) * surface.getWidth();

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

                scale = (float) Field.this.fieldRed.getHeight() / ((float) canvas.getHeight());

                if(!scaleByHeight) {
                    scale = (float) Field.this.fieldRed.getWidth() / ((float) canvas.getWidth());
                }

                    //set paint stroke based on screen size
                normal.setStrokeWidth(canvas.getHeight()/100);
                highlited.setStrokeWidth(canvas.getHeight()/100);

                if(!scaleByHeight){
                    Field.this.fieldRed = Bitmap.createScaledBitmap(Field.this.fieldRed, canvas.getWidth(), (int) (scaledHeight), true);
                    Field.this.fieldBlue = Bitmap.createScaledBitmap(Field.this.fieldBlue, canvas.getWidth(), (int) (scaledHeight), true);
                } else {
                    Field.this.fieldRed = Bitmap.createScaledBitmap(Field.this.fieldRed, (int) (scaledWidth), canvas.getHeight(), true);
                    Field.this.fieldBlue = Bitmap.createScaledBitmap(Field.this.fieldBlue, (int) (scaledWidth), canvas.getHeight(), true);
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

    // called by the TeleopPage class when the deselect button is hit
    public void deselect(){
        Canvas c = surface.getHolder().lockCanvas();

        if (c != null) {

            selected = -1;

            drawImage(c, selected);

            surface.getHolder().unlockCanvasAndPost(c);
        }

    }

    //when the user specifies they are on a certain side, the field needs to flip to accomodate
    public void updateField(MainActivity mainActivity, boolean side){
        //side false is red on the left, blue on the right
        //alliance false is red alliance, true is blue alliance
        //if alliance == false && side == true, flip image
        //if alliance == true && side == false, flip image
        //XOR alliance and side, somehow
        //alliance^side
        //boolean imageShouldBeFlipped = MainActivity.alliance ^ side;
        // ^ means XOR
        //now you know

        boolean imageShouldBeFlipped = (!MainActivity.alliance && side) || (MainActivity.alliance && !side);
        if ((imageShouldBeFlipped && !currentScale) || (!imageShouldBeFlipped && currentScale)){
            Matrix matrix = new Matrix();
            matrix.postScale(-1, 1, fieldRed.getWidth(), fieldRed.getHeight());

            fieldRed = Bitmap.createBitmap(fieldRed, 0, 0, fieldRed.getWidth(), fieldRed.getHeight(), matrix, true);
            fieldBlue = Bitmap.createBitmap(fieldBlue, 0, 0, fieldBlue.getWidth(), fieldBlue.getHeight(), matrix, true);

            currentScale = imageShouldBeFlipped;
        }

        //this redraw() down here because if just the alliance colour changes, then the if statement won't run
        redraw();
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        System.out.println(event.getX() + "\t" + event.getY());
        if (v == surface) {
            System.out.println("Field tapped");
            Canvas c = surface.getHolder().lockCanvas();

            if (c != null) {
                // the place selected, -1 if none
                selected = -1;

                Paint paint = new Paint();
                paint.setColor(Color.RED);

                //adds the offset position
                float xpos = event.getX() - (c.getWidth() / 2 - fieldRed.getWidth() / 2);
                if (xpos > 0 && xpos < fieldRed.getWidth()) {
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
        Bitmap field = fieldRed;
        if (MainActivity.alliance) {
            field = fieldBlue;
        }

        c.drawBitmap(field, 0, 0, null);

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

}
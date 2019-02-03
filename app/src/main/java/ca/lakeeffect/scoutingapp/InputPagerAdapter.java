package ca.lakeeffect.scoutingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ajay on 9/25/2016.
 *
 * Pager Adapter for the input pane
 */
public class InputPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGENUM = 3;

    public PregamePage pregamePage;
    public TeleopPage teleopPage;
    public PostgamePage postgamePage;

    //Instatiate pages
    public InputPagerAdapter(FragmentManager fm) {
        super(fm);
        pregamePage = new PregamePage();
        teleopPage = new TeleopPage();
        postgamePage = new PostgamePage();
    }
    
    //More instatiation
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                pregamePage = new PregamePage();
                return pregamePage;
            case 1:
                teleopPage = new TeleopPage();
                return teleopPage;
            case 2:
                postgamePage = new PostgamePage();
                return postgamePage;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGENUM;
    }

    //Set page titles
    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Pre-Game";
            case 1:
                return "TeleOp Round";
            case 2:
                return "Post-Game";
        }
        return "";
    }

    @Override
    public int getItemPosition(Object object){
        //Ignore sketchyness
        return POSITION_NONE;
    }
}

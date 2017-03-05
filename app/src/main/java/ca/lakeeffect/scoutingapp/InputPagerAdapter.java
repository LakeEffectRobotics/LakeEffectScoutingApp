package ca.lakeeffect.scoutingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Ajay on 9/25/2016.
 *
 * Pager Adapter for the input pane
 */
public class InputPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGENUM = 3;

    public AutoPage autoPage;
    public TeleopPage teleopPage;
    public EndgamePage endgamePage;


    public InputPagerAdapter(FragmentManager fm) {
        super(fm);
        autoPage = new AutoPage();
        teleopPage = new TeleopPage();
        endgamePage = new EndgamePage();
    }
    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                autoPage = new AutoPage();
                return autoPage;
            case 1:
                teleopPage = new TeleopPage();
                return teleopPage;
            case 2:
                endgamePage = new EndgamePage();
                return endgamePage;
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGENUM;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch(position){
            case 0:
                return "Autonomous Round";
            case 1:
                return "TeleOp Round";
            case 2:
                return "Endgame";
        }
        return "";
    }

    @Override
    public int getItemPosition(Object object){
        //Ignore sketchyness
        return POSITION_NONE;
    }
}

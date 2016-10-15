package ca.lakeeffect.scoutingapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ajay on 9/25/2016.
 *
 * Pager Adapter for the input pane
 */
public class InputPagerAdapter extends FragmentPagerAdapter{

    final int PAGENUM = 3;

    public InputPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new AutoPage();
            case 1:
                return new TeleopPage();
            case 2:
                return new EndgamePage();
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
}

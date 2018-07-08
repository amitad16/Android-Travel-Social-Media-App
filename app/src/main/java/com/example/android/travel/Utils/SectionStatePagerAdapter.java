package com.example.android.travel.Utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.widget.TextView;

import com.example.android.travel.R;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by user on 03-02-2018.
 */

public class SectionStatePagerAdapter extends FragmentStatePagerAdapter {

    /*
    To get Fragment Number, Name or the Fragment Itself
     */
    // List of Fragments that is to be shown in the List View
    private final List<Fragment> fragmentList = new ArrayList<>();
    // Fragment as Key ==>> Output Fragment Number as Integer
    private HashMap<Fragment, Integer> fragments = new HashMap<>();
    // Fragment Name as Key ==>> Output Fragment Number as Integer
    private HashMap<String , Integer> fragmentNumbers = new HashMap<>();
    // Fragment Number as Key ==>> Output Fragment Name as String
    private HashMap<Integer, String> fragmentNames = new HashMap<>();

    private TextView currentFragmentName;

    public SectionStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    /**
     * Adds Fragments to the ListView
     * @param fragment Fragment Class Name is Passed as method
     * @param fragmentName
     */
    public void addFragment(Fragment fragment, String fragmentName) {
        fragmentList.add(fragment);
        fragments.put(fragment, fragmentList.size() - 1);
        fragmentNumbers.put(fragmentName, fragmentList.size() - 1);
        fragmentNames.put(fragmentList.size(), fragmentName);
    }

    /**
     * Returns the fragment with the name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmnetNumber(String fragmentName) {
        if (fragmentNumbers.containsKey(fragmentName)) {
            return fragmentNumbers.get(fragmentName);
        } else {
            return null;
        }
    }

    /**
     * Returns the fragment with the fragment @param
     * @param fragment
     * @return
     */
    public Integer getFragmnetNumber(Fragment fragment) {
        if (fragmentNumbers.containsKey(fragment)) {
            return fragmentNumbers.get(fragment);
        } else {
            return null;
        }
    }

    /**
     * Returns the fragment with the name @param
     * @param fragmentNumber
     * @return
     */
    public String getFragmnetName(Integer fragmentNumber) {
        if (fragmentNames.containsKey(fragmentNumber)) {
            return fragmentNames.get(fragmentNumber);
        } else {
            return null;
        }
    }
}

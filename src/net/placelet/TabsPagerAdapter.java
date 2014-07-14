package net.placelet;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {
	Fragment[] fragments = new Fragment[2];

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
			case 0:
				fragments[0] = new CommunityFragment();
				return fragments[0];
			case 1:
				fragments[1] = new MessagesFragment();
				return fragments[1];
		}

		return null;
	}

	@Override
	public int getCount() {
		return 2;
	}

	public Fragment getFragment(int index) {
		return fragments[index];
	}

}
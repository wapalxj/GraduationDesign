package com.way.slidingmenu;

import android.view.View;
import android.view.ViewGroup.LayoutParams;


public interface SlidingActivityBase {
	
	/**
	 * Set the behind graduationdesign.muguihai.com.v023.view content to an explicit graduationdesign.muguihai.com.v023.view. This graduationdesign.muguihai.com.v023.view is placed directly into the behind graduationdesign.muguihai.com.v023.view 's graduationdesign.muguihai.com.v023.view hierarchy.
	 * It can itself be a complex graduationdesign.muguihai.com.v023.view hierarchy.
	 *
	 * @param view The desired content to display.
	 * @param layoutParams Layout parameters for the graduationdesign.muguihai.com.v023.view.
	 */
	public void setBehindContentView(View view, LayoutParams layoutParams);

	/**
	 * Set the behind graduationdesign.muguihai.com.v023.view content to an explicit graduationdesign.muguihai.com.v023.view. This graduationdesign.muguihai.com.v023.view is placed directly into the behind graduationdesign.muguihai.com.v023.view 's graduationdesign.muguihai.com.v023.view hierarchy.
	 * It can itself be a complex graduationdesign.muguihai.com.v023.view hierarchy. When calling this method, the layout parameters of the specified
	 * graduationdesign.muguihai.com.v023.view are ignored. Both the width and the height of the graduationdesign.muguihai.com.v023.view are set by default to MATCH_PARENT. To use your
	 * own layout parameters, invoke setContentView(android.graduationdesign.muguihai.com.v023.view.View, android.graduationdesign.muguihai.com.v023.view.ViewGroup.LayoutParams) instead.
	 *
	 * @param view The desired content to display.
	 */
	public void setBehindContentView(View view);

	/**
	 * Set the behind graduationdesign.muguihai.com.v023.view content from a layout resource. The resource will be inflated, adding all top-level views
	 * to the behind graduationdesign.muguihai.com.v023.view.
	 *
	 * @param layoutResID Resource ID to be inflated.
	 */
	public void setBehindContentView(int layoutResID);

	/**
	 * Gets the SlidingMenu associated with this activity.
	 *
	 * @return the SlidingMenu associated with this activity.
	 */
	public SlidingMenu getSlidingMenu();
		
	/**
	 * Toggle the SlidingMenu. If it is open, it will be closed, and vice versa.
	 */
	public void toggle();
	
	/**
	 * Close the SlidingMenu and show the content graduationdesign.muguihai.com.v023.view.
	 */
	public void showContent();
	
	/**
	 * Open the SlidingMenu and show the menu graduationdesign.muguihai.com.v023.view.
	 */
	public void showMenu();

	/**
	 * Open the SlidingMenu and show the secondary (right) menu graduationdesign.muguihai.com.v023.view. Will default to the regular menu
	 * if there is only one.
	 */
	public void showSecondaryMenu();
	
	/**
	 * Controls whether the ActionBar slides along with the above graduationdesign.muguihai.com.v023.view when the menu is opened,
	 * or if it stays in place.
	 *
	 * @param slidingActionBarEnabled True if you want the ActionBar to slide along with the SlidingMenu,
	 * false if you want the ActionBar to stay in place
	 */
	public void setSlidingActionBarEnabled(boolean slidingActionBarEnabled);
	
}
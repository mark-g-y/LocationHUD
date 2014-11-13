package com.locationhud.selectpoilist;

/**
 * Created by Mark on 12/11/2014.
 */
public interface EditPoiListOptionsDialogCallback {
    public void onEdit(String listName);
    public void onDelete(String listName, int childPosition);
}

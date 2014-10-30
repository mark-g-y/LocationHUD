package com.locationhud.map;

/**
 * Created by Mark on 23/10/2014.
 */
public interface ConfirmSelectedLocationDialogCallback {
    public void onCancel();
    public void onDelete();
    public void onYes(String data);
}

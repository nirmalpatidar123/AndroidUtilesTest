package com.github.nirmalpatidar123.views;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MultiChoiceSpinner extends Spinner implements
		OnMultiChoiceClickListener, OnCancelListener {

	private List<String> items;
    private boolean[] selected;
    private String defaultText;
    private MultiSpinnerListener listener;
	
	public MultiChoiceSpinner(Context context) {
		super(context);
	}

	public MultiChoiceSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MultiChoiceSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	 @Override
	    public void onCancel(DialogInterface dialog) {
	        // refresh text on spinner
	        StringBuffer spinnerBuffer = new StringBuffer();
	        boolean someUnselected = false;
	        for (int i = 0; i < items.size(); i++) {
	            if (selected[i] == true) {
	                spinnerBuffer.append(items.get(i));
	                spinnerBuffer.append(", ");
	            } else {
	                someUnselected = true;
	            }
	        }
	        String spinnerText;
	        if (someUnselected) {
	            spinnerText = spinnerBuffer.toString();
	            if (spinnerText.length() > 2)
	                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
	            
	            if(spinnerText.length()==0)
	            	spinnerText = defaultText;	            
	        } else {
	            spinnerText = defaultText;
	        }
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
	                android.R.layout.simple_spinner_item,
	                new String[] { spinnerText });
	        setAdapter(adapter);
	        listener.onItemsSelected(selected);
	    }	 
	 
	 @Override
	    public boolean performClick() {
	        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
	        builder.setMultiChoiceItems(
	                items.toArray(new CharSequence[items.size()]), selected, this);
	        builder.setPositiveButton(android.R.string.ok,
	                new DialogInterface.OnClickListener() {

	                    @Override
	                    public void onClick(DialogInterface dialog, int which) {
	                        dialog.cancel();
	                    }
	                });
	        builder.setOnCancelListener(this);
	        builder.show();
	        return true;
	    }
	 
	 public void setItems(List<String> items, String allText,
	            MultiSpinnerListener listener) {
	        this.items = items;
	        this.defaultText = allText;
	        this.listener = listener;

	        // all selected by default
	        selected = new boolean[items.size()];
	        for (int i = 0; i < selected.length; i++)
	            selected[i] = false;

	        // all text on the spinner
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
	                android.R.layout.simple_spinner_item, new String[] { allText });
	        setAdapter(adapter);
	    }	 
	 

	 public void setItems(List<String> items, String allText,
	            MultiSpinnerListener listener,boolean[] selected) {
	        this.items = items;
	        this.defaultText = allText;
	        this.listener = listener;

	        this.selected = selected;
	        
//	        // all selected by default
//	        selected = new boolean[items.size()];
//	        for (int i = 0; i < selected.length; i++)
//	            selected[i] = false;

	        // all text on the spinner
	        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
	                android.R.layout.simple_spinner_item, new String[] { allText });
	        setAdapter(adapter);
	    }	 
	 
	 
	@Override
	public void onClick(DialogInterface dialog, int which, boolean isChecked) {
		if (isChecked)
            selected[which] = true;
        else
            selected[which] = false;
	}
	
	public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected);
    }
}

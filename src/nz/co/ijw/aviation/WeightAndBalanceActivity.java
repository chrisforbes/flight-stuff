package nz.co.ijw.aviation;

import nz.co.ijw.aviation.WeightAndBalance.R;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class WeightAndBalanceActivity extends ListActivity {
	
	StationArrayAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView lv = getListView();
        
        final View footer = getLayoutInflater().inflate(R.layout.summary, null); 
        lv.addFooterView(footer, null, false);
        
        adapter = new StationArrayAdapter(this, R.layout.list_item, ITEMS);
        adapter.registerDataSetObserver(new DataSetObserver() { 
        	public void onChanged() {
        		double totalWeight = 0.0;
        		double totalMoment = 0.0;
        		
        		for( Station s : ITEMS ) {
        			totalWeight += s.getWeightInPounds();
        			totalMoment += s.getMoment();
        		}
        		
        		double cog = totalWeight > 0.0 ? (totalMoment / totalWeight) : 0.0;
        		
        		((TextView)footer.findViewById(R.id.weight_label)).setText(
        			String.format(unitPounds.Format, totalWeight));
        		((TextView)footer.findViewById(R.id.cog_label)).setText(
        			String.format(unitInches.Format, cog));
        		
        		CgEnvelopeView cgEnvelope = (CgEnvelopeView)footer.findViewById(R.id.cg_envelope);
        		cgEnvelope.Limits = LIMITS;
        		cgEnvelope.Position = new Station("Current", cog, totalWeight, unitPounds);
        		
        		// TODO: check against limits, and indicate whether we're within them.
        	};
        });
        
        setListAdapter(adapter);
        adapter.notifyDataSetChanged();
        lv.setTextFilterEnabled(false);
        
        // TODO
        setTitle("Weight & Balance: Cessna 152 ZK-FPH");
    }
    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	
    	final Station s = (Station)l.getAdapter().getItem(position);
    	final Dialog dialog = new Dialog(this);
    	dialog.setContentView(R.layout.station_editor);
    	dialog.setTitle(s.Label);
    	dialog.setCancelable(true);
    	
    	final TextView valueChooser = (TextView)dialog.findViewById(R.id.value_chooser);
    	valueChooser.setText(s.formatValue().split("\\s")[0]);
    	
    	final Spinner unitChooser = (Spinner)dialog.findViewById(R.id.unit_chooser); 
    	unitChooser.setAdapter(new ArrayAdapter<Unit>(this, R.layout.unit_chooser_item, UNITS));
    	
    	for( int i = 0; i < UNITS.length; i++)
    		if (s.Unit == UNITS[i])
    			unitChooser.setSelection(i);
    	
    	((Button)dialog.findViewById(R.id.button2)).setOnClickListener(
    			new OnClickListener() {
    				public void onClick(View v) {
    					// commit the change
    					s.Unit = (Unit) unitChooser.getSelectedItem();
    					s.Value = parseDoubleOrDefault(valueChooser.getText().toString(), 0.0);
    					
    					adapter.notifyDataSetChanged();
    					dialog.dismiss();
    				}
    			});
    	
    	dialog.show();
    }
    
    static double parseDoubleOrDefault(String s, double def) {
    	try { return Double.parseDouble(s); }
    	catch(NumberFormatException ex) { return def; }
    }
    
    static final Unit unitPounds = new Unit("Pounds", 1.0, "%.2f lb");
    static final Unit unitKilograms = new Unit("Kilograms", 2.2, "%.2f kg");
    static final Unit unitAvgasGallons = new Unit("US Gal (AVGAS)", 5.99, "%.1f gal");
    static final Unit unitAvgasLitres = new Unit("Litres (AVGAS)", 1.58, "%.1f L");
    
    // note: NOT in units array, it's NOT a unit you should be able to choose.
    static final Unit unitInches = new Unit("Inches aft of datum", 1.0, "%.2f in");
    
    static final Unit[] UNITS = { unitPounds, unitKilograms, unitAvgasGallons, unitAvgasLitres };
    
    static Station[] ITEMS = {
    	new Station("Basic Empty Weight", 30.229, 1228.39, unitPounds),
    	new Station("Pilot", 39.0, 0.0, unitKilograms),
    	new Station("Front Passenger", 39.0, 0.0, unitKilograms),
    	new Station("Baggage", 64.0, 0.0, unitKilograms),
    	new Station("Fuel (Legal VFR Reserve)", 42.0, 12.5, unitAvgasLitres),
    	new Station("Fuel (Additional)", 42.0, 0.0, unitAvgasLitres),
    };
    
    static Station[] LIMITS = {
    	new Station("1", 31.0, 1000, unitPounds),
    	new Station("2", 31.0, 1350, unitPounds),
    	new Station("3", 32.6, 1670, unitPounds),
    	new Station("4", 36.5, 1670, unitPounds),
    	new Station("5", 36.5, 1000, unitPounds)
    };
}

class Unit
{
	public final String Name;
	public final double Factor;
	public final String Format;
	
	public Unit(String name, double factor, String format) {
		Name = name;
		Factor = factor;
		Format = format;
	}
	
	@Override
	public String toString() { return Name; }
}

class Station
{
	public String Label;
	public double Arm;
	public double Value;
	public Unit Unit;
	
	public Station(String label, double arm, double value, Unit unit) {
		Label = label;
		Arm = arm;
		Value = value;
		Unit = unit;
	}
	
	public double getWeightInPounds() { return Value * Unit.Factor; }	
	public double getMoment() { return Arm * getWeightInPounds(); }
	public String formatValue()	{ return String.format(Unit.Format, Value); }
}

class StationArrayAdapter extends ArrayAdapter<Station>
{
	int resource;

	public StationArrayAdapter(Context context, int resource, Station[] objects) {
		super(context, resource, R.id.label, objects);
		this.resource = resource;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(resource, parent, false);
		}
		
		Station station = getItem(position);

		((TextView) convertView.findViewById(R.id.label)).setText(station.Label);
		((TextView) convertView.findViewById(R.id.weight)).setText(station.formatValue());
		
		return convertView;
	}
}
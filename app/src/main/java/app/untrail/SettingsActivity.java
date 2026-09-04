package app.untrail;
import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.view.*;
import android.widget.*;
import android.content.res.ColorStateList;

public class SettingsActivity extends UiActivity {
 private View accessButton,accessHelp;
 private Switch automatic;
 @Override protected void onCreate(Bundle state){
  super.onCreate(state);
  LinearLayout body=page();
  LinearLayout header=row(body);header.setGravity(Gravity.CENTER_VERTICAL);
  compactIcon(header,"Back",R.drawable.ic_back,this::finish);
  TextView title=label(header,"Settings",28,ink);title.setPadding(dp(16),0,0,0);title.setAccessibilityHeading(true);
  space(body,30);label(body,"Appearance",18,ink);
  RadioGroup modes=new RadioGroup(this);modes.setOrientation(1);body.addView(modes);
  String saved=getSharedPreferences("untrail",0).getString("appearance","system");
  for(String mode:new String[]{"system","light","dark"}){
   RadioButton option=new RadioButton(this);option.setId(View.generateViewId());option.setTag(mode);option.setText(mode.equals("system")?"System":mode.equals("light")?"Light":"Dark");option.setTextColor(ink);option.setTypeface(face);option.setTextSize(16);option.setMinHeight(dp(52));option.setButtonTintList(ColorStateList.valueOf(ink));modes.addView(option);option.setChecked(mode.equals(saved));
  }
  modes.setOnCheckedChangeListener((group,id)->{RadioButton selected=group.findViewById(id);if(selected==null)return;String mode=(String)selected.getTag();getSharedPreferences("untrail",0).edit().putString("appearance",mode).apply();recreate();});
  space(body,28);View divider=new View(this);divider.setBackgroundColor(focusColor);body.addView(divider,new LinearLayout.LayoutParams(-1,dp(1)));space(body,22);
  LinearLayout heading=row(body);heading.setGravity(Gravity.CENTER_VERTICAL);
  TextView draft=label(heading,"Draft cleaning",18,ink);draft.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1));
  String help="Removes trackers in WhatsApp drafts. Can undo within 7 seconds.";
  ImageButton info=compactIcon(heading,"About draft cleaning",R.drawable.ic_info,()->{});info.setScaleType(ImageView.ScaleType.CENTER);
  // Keep the 48dp touch target; confine feedback to a 28dp circle around the 18dp icon.
  android.graphics.drawable.GradientDrawable mask=new android.graphics.drawable.GradientDrawable();mask.setShape(android.graphics.drawable.GradientDrawable.OVAL);mask.setColor(0xFFFFFFFF);
  android.graphics.drawable.RippleDrawable ripple=new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(dark?0x28FFFFFF:0x28000000),null,mask);ripple.setRadius(dp(14));
  info.setBackground(new android.graphics.drawable.InsetDrawable(ripple,dp(10)));
  // Background insets can replace View padding. Center an intrinsically sized icon instead of scaling it into the remaining area.
  info.setPadding(0,0,0,0);
  info.setTooltipText(help);info.setOnClickListener(v->info.performLongClick());
  label(body,"Experimental",12,muted);
  automatic=new Switch(this);automatic.setText("Clean drafts automatically");automatic.setTextColor(ink);automatic.setTypeface(face);automatic.setMinHeight(dp(56));automatic.setChecked(getSharedPreferences("untrail",0).getBoolean("draft",false));
  int[][] states={new int[]{android.R.attr.state_checked},new int[]{}};
  automatic.setThumbTintList(new ColorStateList(states,new int[]{ink,muted}));automatic.setTrackTintList(new ColorStateList(states,new int[]{muted,surfaceColor}));body.addView(automatic);
  automatic.setOnCheckedChangeListener((v,enabled)->{
   if(!enabled){getSharedPreferences("untrail",0).edit().putBoolean("draft",false).apply();return;}
   if(accessEnabled()){getSharedPreferences("untrail",0).edit().putBoolean("draft",true).apply();return;}
   new AlertDialog.Builder(this).setTitle("Allow draft cleaning?").setMessage("Accessibility access lets Untrail read and edit the WhatsApp composer. Messages are never sent or saved. Enable Untrail draft cleaning on the next screen.").setPositiveButton("Enable access",(d,w)->{getSharedPreferences("untrail",0).edit().putBoolean("draft",true).apply();openAccessibilitySettings();}).setNegativeButton("Cancel",(d,w)->automatic.setChecked(false)).setOnCancelListener(d->automatic.setChecked(false)).show();
  });
  accessButton=button(body,"Enable Untrail access",this::openAccessibilitySettings);
  accessHelp=label(body,"Accessibility → Installed apps → Untrail draft cleaning → On",14,muted);
  refreshAccess();
 }
 private boolean accessEnabled(){
  android.view.accessibility.AccessibilityManager manager=(android.view.accessibility.AccessibilityManager)getSystemService(ACCESSIBILITY_SERVICE);
  for(android.accessibilityservice.AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
   android.content.pm.ServiceInfo service=info.getResolveInfo().serviceInfo;
   if(getPackageName().equals(service.packageName)&&DraftService.class.getName().equals(service.name))return true;
  }return false;
 }
 private void refreshAccess(){if(accessButton!=null){int visible=accessEnabled()?View.GONE:View.VISIBLE;accessButton.setVisibility(visible);accessHelp.setVisibility(visible);}}
 @Override protected void onResume(){super.onResume();refreshAccess();}
 private void openAccessibilitySettings(){
  Intent details=new Intent("android.settings.ACCESSIBILITY_DETAILS_SETTINGS").putExtra(Intent.EXTRA_COMPONENT_NAME,new ComponentName(this,DraftService.class).flattenToString());
  try{startActivity(details);}catch(ActivityNotFoundException|SecurityException unavailable){
   try{startActivity(new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS));}catch(ActivityNotFoundException e){Toast.makeText(this,"Accessibility → Installed apps → Untrail draft cleaning → On",Toast.LENGTH_LONG).show();}
  }
 }
}

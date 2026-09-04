package app.untrail;
import android.app.*;
import android.os.Bundle;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

public class UiActivity extends Activity {
 protected int ink,muted,accent,canvas,surfaceColor,focusColor;
 protected boolean dark;
 protected Typeface face;
 protected int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
 protected boolean useDark(){
  String mode=getSharedPreferences("untrail",0).getString("appearance","system");
  return mode.equals("dark") || mode.equals("system") && (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK)==android.content.res.Configuration.UI_MODE_NIGHT_YES;
 }
 @Override protected void onCreate(Bundle state){
  dark=useDark();setTheme(dark?R.style.AppDark:R.style.AppLight);super.onCreate(state);
  canvas=dark?0xFF121212:0xFFFAFAFA;ink=dark?0xFFF2F2F2:0xFF161616;accent=ink;
  muted=dark?0xFFAAAAAA:0xFF666666;surfaceColor=dark?0xFF202020:0xFFEDEDED;focusColor=dark?0xFF303030:0xFFE0E0E0;
  face=getResources().getFont(R.font.manrope);
  getWindow().setBackgroundDrawable(new ColorDrawable(canvas));

 }
 // The decor window is not available during early onCreate on all Android builds.
 // Apply bar appearance only after the activity has resumed and created its decor.
 @Override protected void onPostResume(){
  super.onPostResume();
  WindowInsetsController controller=getWindow().getDecorView().getWindowInsetsController();
  if(controller!=null){
   int lightBars=WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
   controller.setSystemBarsAppearance(dark?0:lightBars,lightBars);
  }
 }
 @Override protected void onResume(){super.onResume();if(dark!=useDark())recreate();}
 protected LinearLayout page(){
  ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(canvas);
  LinearLayout body=new LinearLayout(this);body.setOrientation(1);body.setFocusableInTouchMode(true);body.setPadding(dp(24),dp(20),dp(24),dp(28));
  ScrollView.LayoutParams lp=new ScrollView.LayoutParams(Math.min(getResources().getDisplayMetrics().widthPixels,dp(600)),-2);lp.gravity=Gravity.CENTER_HORIZONTAL;scroll.addView(body,lp);
  scroll.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets bars=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return i;});setContentView(scroll);return body;
 }
 protected GradientDrawable surface(){GradientDrawable d=new GradientDrawable();d.setColor(surfaceColor);d.setCornerRadius(dp(16));return d;}
 protected TextView label(LinearLayout p,String text,int size,int color){TextView v=new TextView(this);v.setText(text);v.setTextSize(size);v.setTypeface(face);v.setTextColor(color);v.setPadding(0,dp(6),0,dp(6));p.addView(v);return v;}
 protected void space(LinearLayout p,int h){p.addView(new View(this),new LinearLayout.LayoutParams(1,dp(h)));}
 protected LinearLayout row(LinearLayout p){LinearLayout r=new LinearLayout(this);r.setOrientation(0);p.addView(r,new LinearLayout.LayoutParams(-1,-2));return r;}
 protected android.graphics.drawable.Drawable buttonBackground(boolean primary) {
  GradientDrawable shape=new GradientDrawable();shape.setColor(primary?accent:surfaceColor);shape.setCornerRadius(dp(18));
  GradientDrawable focused=new GradientDrawable();focused.setColor(primary?accent:focusColor);focused.setCornerRadius(dp(18));focused.setStroke(dp(2),primary?ink:accent);
  android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();states.addState(new int[]{android.R.attr.state_focused},focused);states.addState(new int[]{},shape);
  return new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf((dark?0x28FFFFFF:0x28000000)),states,null);
 }
 protected void addButton(LinearLayout parent,View button) {
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(parent.getOrientation()==0?0:-1,dp(56),parent.getOrientation()==0?1:0);
  lp.topMargin=dp(8);if(parent.getOrientation()==0 && parent.getChildCount()>0)lp.setMarginStart(dp(8));parent.addView(button,lp);
 }
 protected Button button(LinearLayout p,String text,Runnable action){
  boolean primary=text.equals("WhatsApp");Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setTextSize(15);b.setTypeface(Typeface.create(face,600,false));
  b.setTextColor(primary?canvas:accent);b.setBackground(buttonBackground(primary));pressFeedback(b);b.setPadding(dp(12),0,dp(12),0);b.setOnClickListener(v->action.run());addButton(p,b);return b;
 }
 protected void pressFeedback(View view){
  if(!android.animation.ValueAnimator.areAnimatorsEnabled())return;
  android.animation.StateListAnimator states=new android.animation.StateListAnimator();
  for(boolean pressed:new boolean[]{true,false}){
   android.animation.AnimatorSet animation=new android.animation.AnimatorSet();float scale=pressed?0.98f:1f;
   animation.playTogether(android.animation.ObjectAnimator.ofFloat(view,"scaleX",scale),android.animation.ObjectAnimator.ofFloat(view,"scaleY",scale));animation.setDuration(pressed?90:140);animation.setInterpolator(new android.view.animation.PathInterpolator(0.2f,0f,0f,1f));states.addState(pressed?new int[]{android.R.attr.state_pressed,android.R.attr.state_enabled}:new int[]{},animation);
  }view.setStateListAnimator(states);
 }
 protected ImageButton compactIcon(LinearLayout p,String label,int resource,Runnable action){
  ImageButton b=new ImageButton(this);b.setImageResource(resource);b.setImageTintList(android.content.res.ColorStateList.valueOf(accent));b.setScaleType(ImageView.ScaleType.CENTER);b.setContentDescription(label);b.setTooltipText(label);b.setBackground(buttonBackground(false));pressFeedback(b);b.setOnClickListener(v->action.run());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(48),dp(48));lp.setMarginStart(dp(4));p.addView(b,lp);return b;
 }
}

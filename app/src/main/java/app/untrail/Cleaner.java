package app.untrail;

import java.net.URI;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.*;

/** Independently implemented, offline-only. Unknown parameters stay byte-for-byte intact. */
public final class Cleaner {
 private static final Pattern URL = Pattern.compile("https?://[^\\s<>\"\\u0000-\\u001f]+", Pattern.CASE_INSENSITIVE);
 private static final Set<String> GLOBAL = Set.of("fbclid","gclid","dclid","msclkid","ttclid","twclid","gbraid","wbraid","mc_cid","mc_eid");
 private static final Set<String> AMAZON = Set.of("amazon.com","amazon.in","amazon.co.uk","amazon.de","amazon.fr","amazon.it","amazon.es","amazon.ca","amazon.com.au","amazon.co.jp","amazon.com.br","amazon.com.mx","amazon.ae","amazon.sa","amazon.sg","amazon.nl","amazon.se","amazon.pl","amazon.com.tr","amazon.com.be","amazon.ie","amazon.co.za");
 private static final Set<String> GOOGLE = Set.of("google.com","google.co.in","google.co.uk","google.de","google.fr","google.ca","google.com.au","google.co.jp");
 private static final Set<String> AFFILIATE = Set.of("tag","ascsubtag","linkcode","creative","creativeasin","camp","ref","ref_");
 private static final Pattern PRODUCT = Pattern.compile("^/(?:[^/]+/)?(?:dp|gp/product|gp/aw/d)/([A-Za-z0-9]{10})(?:/.*)?$");
 public static final class Result {
  public final String text;
  public final List<String> removed;
  public final List<String> changes;
  Result(String text, List<String> removed) { this(text,removed,List.of()); }
  Result(String text,List<String> removed,List<String> changes){this.text=text;this.removed=Collections.unmodifiableList(removed);this.changes=Collections.unmodifiableList(changes);}
 }
 public static Result clean(String text) { return process(text,false); }
 /** Share mode removes surrounding prose but retains every valid HTTP(S) URL in order. */
 public static Result cleanForShare(String text) { return process(text,true); }
 private static Result process(String text,boolean linksOnly) {
  if(text==null) text="";
  if(text.length()>100000) return new Result(linksOnly?"":text, List.of());
  Matcher m=URL.matcher(text); StringBuffer out=new StringBuffer(); List<String> removed=new ArrayList<>(),changes=new ArrayList<>(),links=new ArrayList<>();
  while(m.find()) {
   String raw=m.group();
   int end=raw.length();
   // Only strip an explicit surrounding delimiter. Legal URL punctuation is data.
   if(m.start()>0) {
    char opener=text.charAt(m.start()-1);
    char closer=opener=='('?')':opener=='['?']':opener=='\''?'\'':opener=='“'?'”':0;
    if(closer!=0) {
     int candidate=end;
     while(candidate>0 && ".,!;:".indexOf(raw.charAt(candidate-1))>=0)candidate--;
     if(candidate>0 && raw.charAt(candidate-1)==closer) {
      int balance=0;
      for(int i=0;i<candidate;i++) {
       if(raw.charAt(i)==opener)balance++;
       if(raw.charAt(i)==closer)balance--;
      }
      if(opener==closer || balance<0)end=candidate-1;
     }
    }
   }
   String token=raw.substring(0,end),suffix=raw.substring(end);
   if(linksOnly&&!validHttp(token))continue;
   String cleaned=simplify(token,removed,changes);
   if(linksOnly)links.add(cleaned);else m.appendReplacement(out,Matcher.quoteReplacement(cleaned+suffix));
  }
  if(!linksOnly)m.appendTail(out);return new Result(linksOnly?String.join("\n",links):out.toString(),removed,changes);
 }
 private static boolean domain(String host,String domain) {return host.equals(domain)||host.endsWith("."+domain);}
 private static boolean validHttp(String url){
  try{URI u=new URI(url);return ("https".equalsIgnoreCase(u.getScheme())||"http".equalsIgnoreCase(u.getScheme()))&&u.getHost()!=null&&u.getRawUserInfo()==null;}catch(Exception e){return false;}
 }
 private static boolean matchesDomain(String host,Set<String> domains){for(String d:domains)if(domain(host,d))return true;return false;}
 private static boolean protectedUrl(URI uri)throws Exception{
  if(uri.getRawUserInfo()!=null)return true;
  if(uri.getRawQuery()!=null)for(String part:uri.getRawQuery().split("&",-1))if(Set.of("signature","sig","token","access_token","x-amz-signature","x-goog-signature").contains(key(part)))return true;
  return false;
 }
 private static String queryValue(URI uri,String wanted)throws Exception{
  if(uri.getRawQuery()==null)return null;
  String found=null;
  for(String part:uri.getRawQuery().split("&",-1)){
   int eq=part.indexOf('=');if(key(part).equals(wanted)){
    if(found!=null)throw new IllegalArgumentException("Ambiguous destination");
    found=eq<0?"":URLDecoder.decode(part.substring(eq+1),"UTF-8");
   }
  }return found;
 }
 private static String unwrap(String input){
  try{
   URI uri=new URI(input);if(!validHttp(input)||protectedUrl(uri))return input;
   String host=uri.getHost().toLowerCase(Locale.ROOT),path=uri.getRawPath(),destination=null;
   if((host.equals("l.facebook.com")||host.equals("lm.facebook.com")||host.equals("www.facebook.com")||host.equals("facebook.com"))&&path.equals("/l.php"))destination=queryValue(uri,"u");
   else if(matchesDomain(host,GOOGLE)&&path.equals("/url")){
    destination=queryValue(uri,"url");if(destination==null)destination=queryValue(uri,"q");
   }else if(matchesDomain(host,GOOGLE)&&path.startsWith("/amp/s/")){
    destination="https://"+path.substring(7)+(uri.getRawQuery()==null?"":"?"+uri.getRawQuery())+(uri.getRawFragment()==null?"":"#"+uri.getRawFragment());
   }
   return destination!=null&&validHttp(destination)?destination:input;
  }catch(Exception ignored){return input;}
 }
 private static String simplify(String input,List<String> removed,List<String> changes){
  String current=input;
  for(int depth=0;depth<5;depth++){
   String next=unwrap(current);if(next.equals(current))break;
   changes.add("Redirect unwrapped");current=next;
  }
  try{
   URI uri=new URI(current);
   if(uri.getHost()!=null&&matchesDomain(uri.getHost().toLowerCase(Locale.ROOT),AMAZON)&&!protectedUrl(uri)){
    Matcher product=PRODUCT.matcher(uri.getRawPath());
    if(product.matches()){
     String path="/dp/"+product.group(1);
     if(!path.equals(uri.getRawPath())){
      current=uri.getScheme()+"://"+uri.getRawAuthority()+path+(uri.getRawQuery()==null?"":"?"+uri.getRawQuery())+(uri.getRawFragment()==null?"":"#"+uri.getRawFragment());
      changes.add("Amazon product URL shortened");
     }
    }
   }
  }catch(Exception ignored){}
  return cleanUrl(current,removed);
 }
 private static String cleanUrl(String input,List<String> removed) {
  try {
   URI uri=new URI(input); String host=uri.getHost();
   if(host==null || uri.getRawUserInfo()!=null || uri.getRawQuery()==null) return input;
   host=host.toLowerCase(Locale.ROOT);
   String[] parts=uri.getRawQuery().split("&",-1);
   // Signed URLs and credential-bearing links are preserved rather than invalidated.
   for(String part:parts) {
    String key=key(part);
    if(Set.of("signature","sig","token","access_token","x-amz-signature","x-goog-signature").contains(key)) return input;
   }
   List<String> kept=new ArrayList<>(), dropped=new ArrayList<>();
   for(String part:parts) {
    String key=key(part); boolean drop=key.startsWith("utm_")||GLOBAL.contains(key);
    if(domain(host,"instagram.com")) drop|=Set.of("igsh","igsi","igshid","ig_rid").contains(key);
    if(domain(host,"facebook.com")||domain(host,"fb.watch")) drop|=Set.of("mibextid","rdid","__tn__","refsrc").contains(key);
    if(domain(host,"youtube.com")||domain(host,"youtu.be")) drop|=Set.of("si","feature").contains(key);
    if(domain(host,"tiktok.com")) drop|=Set.of("_t","_r","share_app_id","share_link_id","sender_device","u_code").contains(key);
    if(domain(host,"x.com")||domain(host,"twitter.com")) drop|=Set.of("s","t","ref_src","ref_url").contains(key);
    if(domain(host,"spotify.com")) drop|=key.equals("si");
    if(matchesDomain(host,AMAZON)){
     drop|=AFFILIATE.contains(key);
     if(PRODUCT.matcher(uri.getRawPath()).matches())drop|=key.startsWith("pd_rd_")||key.startsWith("pf_rd_")||Set.of("qid","sr","sprefix","crid","dib","dib_tag").contains(key);
    }
    if(drop) dropped.add(key); else kept.add(part);
   }
   if(dropped.isEmpty()) return input;
   int q=input.indexOf('?'), hash=input.indexOf('#',q);
   String result=input.substring(0,q)+(kept.isEmpty()?"":"?"+String.join("&",kept))+(hash<0?"":input.substring(hash));
   removed.addAll(dropped); return result;
  } catch(Exception ignored) { return input; }
 }
 private static String key(String part) throws Exception { int i=part.indexOf('='); return URLDecoder.decode(i<0?part:part.substring(0,i),"UTF-8").toLowerCase(Locale.ROOT); }
}

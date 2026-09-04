import app.untrail.Cleaner;
public class CleanerTest {
 static int count;
 static void check(String input,String expected){String actual=Cleaner.clean(input).text;if(!expected.equals(actual))throw new AssertionError(input+"\nExpected: "+expected+"\nActual: "+actual);count++;}
 static void share(String input,String expected){String actual=Cleaner.cleanForShare(input).text;if(!actual.equals(expected))throw new AssertionError("Share mismatch: "+actual+" != "+expected);count++;}
 static String enc(String s){try{return java.net.URLEncoder.encode(s,"UTF-8");}catch(Exception e){throw new RuntimeException(e);}}
 public static void main(String[] args){
 check("https://www.instagram.com/reel/abc/?igsh=123&img_index=2","https://www.instagram.com/reel/abc/?img_index=2");
 check("https://youtu.be/abc?si=123&t=42#chapter","https://youtu.be/abc?t=42#chapter");
 check("https://facebook.com/story.php?story_fbid=123&id=456&mibextid=abc","https://facebook.com/story.php?story_fbid=123&id=456");
 check("https://example.com/login?redirect_uri=x&client_id=a","https://example.com/login?redirect_uri=x&client_id=a");
 check("Look (https://instagram.com/p/abc?igsh=123). Nice! https://example.com/?utm_source=x&q=a%2Bb", "Look (https://instagram.com/p/abc). Nice! https://example.com/?q=a%2Bb");
 check("https://evilinstagram.com/?igsh=123","https://evilinstagram.com/?igsh=123");
 check("https://instagram.com.evil.test/?igsh=123","https://instagram.com.evil.test/?igsh=123");
 check("https://example.com/?sig=abc&utm_source=x","https://example.com/?sig=abc&utm_source=x");
 check("https://example.com/?q=one&q=two&utm_source=x#utm_source=x","https://example.com/?q=one&q=two#utm_source=x");
 check("https://example.com/?%75tm_source=x&q=a+b","https://example.com/?q=a+b");
 check("https://example.com/?bad=%XX&utm_source=x","https://example.com/?bad=%XX&utm_source=x");
 check("hello there","hello there");check("","");
 check("https://instagram.com/share/abc","https://instagram.com/share/abc");
 String x="https://instagram.com/p/abc?igsh=123&img_index=2";check(Cleaner.clean(x).text,Cleaner.clean(x).text);

 check("https://l.facebook.com/l.php?u="+enc("https://example.org/article?utm_source=fb&id=7")+"&h=123","https://example.org/article?id=7");
 check("https://lm.facebook.com/l.php?u="+enc("https://example.org/a?q=one%2Btwo"),"https://example.org/a?q=one%2Btwo");
 check("https://www.google.com/url?q="+enc("https://youtu.be/abc?si=123&t=45")+"&sa=D","https://youtu.be/abc?t=45");
 check("https://www.google.co.in/url?url="+enc("https://example.org/p?utm_source=g"),"https://example.org/p");
 check("https://l.facebook.com/l.php?u="+enc("https://www.google.com/url?q="+enc("https://example.org/p?utm_source=fb")),"https://example.org/p");
 check("https://www.google.com/amp/s/example.org/story?utm_source=g&id=7","https://example.org/story?id=7");
 check("https://example.org/story/amp?amp=1","https://example.org/story/amp?amp=1");
 check("https://www.google.com.evil.org/url?q="+enc("https://example.org"),"https://www.google.com.evil.org/url?q="+enc("https://example.org"));
 check("https://l.facebook.com/l.php?u=javascript%3Aalert%281%29","https://l.facebook.com/l.php?u=javascript%3Aalert%281%29");
 check("https://www.google.com/url?q=https%3A%2F%2Fone.test&q=https%3A%2F%2Ftwo.test","https://www.google.com/url?q=https%3A%2F%2Fone.test&q=https%3A%2F%2Ftwo.test");
 check("https://www.amazon.in/Some-Product/dp/B012345678/ref=abc?tag=store-21&linkCode=abc&th=1&psc=1&smid=SELLER","https://www.amazon.in/dp/B012345678?th=1&psc=1&smid=SELLER");
 check("https://www.amazon.com/gp/product/B012345678?ascsubtag=track&pd_rd_r=abc&pf_rd_p=def&qid=3","https://www.amazon.com/dp/B012345678");
 check("https://www.amazon.co.uk/gp/aw/d/B012345678/ref=abc","https://www.amazon.co.uk/dp/B012345678");
 check("https://www.amazon.in/s?k=books&rh=n%3A123&tag=ref-21","https://www.amazon.in/s?k=books&rh=n%3A123");
 check("https://amazon.com.evil.test/title/dp/B012345678?tag=store","https://amazon.com.evil.test/title/dp/B012345678?tag=store");
 check("https://amazon.com/gp/product/B012345678?signature=abc&tag=store","https://amazon.com/gp/product/B012345678?signature=abc&tag=store");
 check("https://amzn.to/abc123","https://amzn.to/abc123");
 share("Hey, check out this product: <https://www.amazon.in/item/dp/B012345678?tag=store-21>","https://www.amazon.in/dp/B012345678");
 share("We're watching a movie! Seat C12. https://tickets.example/booking?token=abc&booking=123&utm_source=mail", "https://tickets.example/booking?token=abc&booking=123&utm_source=mail");
 share("First: https://instagram.com/p/abc?igsh=123 Then: (https://youtu.be/xyz?si=123&t=30).","https://instagram.com/p/abc\nhttps://youtu.be/xyz?t=30");
 share("Here's your movie https://www.google.com/url?q="+enc("https://example.org/ticket?id=7&utm_source=ads"),"https://example.org/ticket?id=7");
 share("Movie at 7, seats C12 and C13","");share("http://%XX","");share("","");share(null,"");
 check("My message: https://amazon.in/dp/B012345678?tag=store", "My message: https://amazon.in/dp/B012345678");
 share("Same https://example.org/ and again https://example.org/", "https://example.org/\nhttps://example.org/");
 share("x".repeat(100001),"");
 String nested="https://example.org/p?utm_source=x";for(int i=0;i<8;i++)nested="https://www.google.com/url?q="+enc(nested);
 if(Cleaner.clean(nested).changes.size()!=5)throw new AssertionError("Unwrap depth exceeded");count++;
 for(String ending:new String[]{"!",".",",",";",":","'", ")"}){
  String token="https://example.org/download?token=abc"+ending;
  share(token,token);share("Your ticket: "+token,token);
 }
 share("(https://example.org/path(foo)).","https://example.org/path(foo)");
 share("https://example.org/path!","https://example.org/path!");
 String ambiguous="https://www.google.com/url?url=https%3A%2F%2Fone.test&url=https%3A%2F%2Ftwo.test&q=https%3A%2F%2Fthree.test";
 check(ambiguous,ambiguous);
 String emptyDuplicate="https://www.google.com/url?url&url=https%3A%2F%2Fone.test&q=https%3A%2F%2Fthree.test";
 check(emptyDuplicate,emptyDuplicate);
 String longUrl="https://example.org/"+")".repeat(99980);
 long started=System.nanoTime();share(longUrl,longUrl);
 long elapsed=(System.nanoTime()-started)/1000000;
 if(elapsed>1500)throw new AssertionError("Long URL took "+elapsed+" ms");
 System.out.println("Long URL: "+elapsed+" ms");
 System.out.println(count+" cleaning checks passed");
 }
}

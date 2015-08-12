import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.jsoup.HttpStatusException



try{
    Globals.threadInterval = Globals.TOTAL_USERS / Globals.threadCount
    println "thread count: ${Globals.threadCount}, thread interval: ${Globals.threadInterval}"
    Arrays.fill(Globals.threadFinished, Boolean.FALSE)
    for(int i in 0..(Globals.threadCount-1)){
        new Thread(new Scraper(i, i*Globals.threadInterval,(i+1)*Globals.threadInterval)).start()
        println "Made thread ${i}"
    }
} catch(Exception e){
    e.printStackTrace()
}

              
class ExecutionFinishedException extends Exception{
    public ExecutionFinishedException(String s){
        super(s)
    }
}

public class Globals{
    static ArrayList<ForumUser> users = new ArrayList<ForumUser>()
    static int TOTAL_USERS = 1100000
    static int threadCount = 16
    static int threadInterval
    static boolean[] threadFinished = new boolean[threadCount]
}

public class Scraper implements Runnable {
    private int currentId
    private int stopId
    private int id
    public Scraper(int id, int startId, stopId){
        this.currentId = startId
        this.stopId = stopId
        this.id = id
    }
    public void run(){
        //ArrayList<ForumUser> users = new ArrayList<ForumUser>()
        while(currentId<stopId){
            try{
                Thread.sleep(100)
                Document doc = Jsoup.connect("https://esea.net/users/${currentId}")
                    .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                    .timeout(0)
                    .get()
                if(!(doc.getElementsByTag("title").text() == "ESEA") && getPostCount(doc) > 9999){
                    ForumUser current = parseUser(doc, currentId)
                    //println current.toString()
                    Globals.users.add(current)
                    println "${new Date()} - added user ${currentId}"
                    println "Thread ${id} process: ${(100*(currentId % Globals.threadInterval))/Globals.threadInterval}%"
                }
            } catch(Exception e){
                e.printStackTrace()
                currentId--
            }   
            currentId++
        }    
        int finishedCount = 0
        Globals.threadFinished.each{
            finishedCount++
        }
		println "Finished thread ${id}!"
		printRanks()
        if(finishedCount == Globals.threadCount-1){
            println "FINAL RANKS:"
			printRanks()
        }
		Globals.threadFinished[id] = true
    }
    public static void printRanks(){
        Collections.sort(Globals.users)
        for(int count in 0..99){
            println "rank ${count+1}: ${Globals.users.getAt(count)?.toString()}\n"
            count++
        }
    }
    public static int getPostCount(doc){
        String html = doc.toString()
        //post count
        
        String selection = html.substring(html.indexOf('Posts:'),
            html.indexOf('Recent Visitors'))
            
        int postCount = Integer.parseInt(selection.replaceAll("[\\D]", ""))
        return postCount
    }
    public static ForumUser parseUser(Document doc, int id){
        String html = doc.toString()
        //post count
        
        String selection = html.substring(html.indexOf('Posts:'),
            html.indexOf('Recent Visitors'))
            
        int postCount = Integer.parseInt(selection.replaceAll("[\\D]", ""))
        
        //println "posts: ${postCount}"
        
        //date joined
        String dateRaw = html.substring(html.indexOf('Joined:'),
            html.indexOf('Posts:'))
        String dateJoined = dateRaw.substring(dateRaw.indexOf('"data">')+8, dateRaw.indexOf('</div>')).trim()
        
        //println "date joined: " + dateJoined
        
        //karma
        Element karmaRaw = doc.getElementById("karma-${id}")
        float karma = Float.parseFloat(karmaRaw.text().replace(',', ''))//remove commas
        //println "karma: " + karma
        
        //handle
        Elements handleRaw = doc.getElementsByTag("title")
        String handle = handleRaw.text().substring(15)
        //println handle
        
        ForumUser user = new ForumUser(handle, postCount, karma, dateJoined)
    }
}

public ForumUser parseUser2(Document doc, int id){
    Element profileInfo = doc.getElementById("profile-info")
    html = profileInfo.toString()
    //println html
    
    String selection = html.substring(html.indexOf('Posts:'))
        
    int postCount = Integer.parseInt(selection.replaceAll("[\\D]", ""))
    return new ForumUser(id, postCount)
}

class ForumUser implements Comparable{    
    String handle
    int posts
    float karma
    String dateJoined   
    int id
    
    public ForumUser(int id, int posts){
        this.id = id
        this.posts = posts
    }
    public ForumUser(String handle, int posts, float karma, String dateJoined){
        this.handle = handle
        this.posts = posts
        this.karma = karma
        this.dateJoined = dateJoined
    }   
    public String toString(){
        String result =  "${handle}\nposts: ${posts}\nkarma: ${karma}\ndate joined: ${dateJoined}"
    }
    public String toStringBasic(){
        String result =  "${id}\nposts: ${posts}"
    }
    public int compare(ForumUser f) {
        // TODO Auto-generated method stub
        if(this.posts == f.posts){
            return 0
        }
        else if(this.posts > f.posts){
            return -1
        }
        else{
            return 1
        }
    }
    public int compareTo(Object o) {
        // TODO Auto-generated method stub
        return compare(o)
    }
     
}

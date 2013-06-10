package fi.helsinki.cs.server;

import java.util.Iterator;
import java.util.TreeSet;

public class Observ {
	private String uuid1;
	private String uuid2;
	private long ts_s;
	private long ts_d;
	private TreeSet<String> ms;
	private int gt;	//-1:non-colocation, 0:unknown, 1:colocation
	
	public Observ(){
		this.uuid1 = "";
		this.uuid2 = "";
		ts_s = System.currentTimeMillis();
		ts_d = 0;
		ms = new TreeSet<String>();
		gt = 0;
	}
	
	public void setUuid(String uuid1, String uuid2){
		this.uuid1 = uuid1;
		this.uuid2 = uuid2;
	}
	
	public String getUuid1(){
		return this.uuid1;
	}
	
	public String getUuid2(){
		return this.uuid2;
	}
	
	public void setTS_S(){
		this.ts_s = System.currentTimeMillis();
	}
	
	public long getTS_S(){
		return this.ts_s;
	}
	
	public void setTS_D(long delta){
		this.ts_d = delta;
	}
	
	public long getTS_D(){
		return this.ts_d;
	}
	
	public void addModality(String mod){
		if(!this.ms.contains(mod))
			this.ms.add(mod);
	}
	
	public TreeSet<String> getModality(){
		return this.ms;
	}
	
	public TreeSet<String> intersect(TreeSet<String> ts){
		Iterator<String> it = ts.iterator();
		TreeSet<String> x = new TreeSet<String>();
		String tmp = null;
		while(it.hasNext()){
			if(this.ms.contains(tmp = it.next())){
				x.add(tmp);
			}
		}
		this.ms = x;
		return x;
	}
	
	public String getModalityS(){
		Iterator<String> it = this.ms.iterator();
		String tmp = "";
		while(it.hasNext()){
			tmp += it.next();
			if(it.hasNext()){
				tmp += ",";
			}
		}
		return this.ms.toString();
	}
	
	public void setGt(int gt){
		this.gt = gt;
	}
	
	public int getGt(){
		return this.gt;
	}
	
	public boolean isPrepared(){
		return (!this.ms.isEmpty() && this.uuid1.length() != 0);
	}
}
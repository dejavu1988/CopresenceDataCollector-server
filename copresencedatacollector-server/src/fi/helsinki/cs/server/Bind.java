package fi.helsinki.cs.server;

import java.util.Vector;

public class Bind {
	private Vector<Device> bond;
	private int qnum;
	
	public Bind(){
		this.bond = new Vector<Device>(2);		
		this.qnum = -1;
	}
	
	public Vector<Device> getBond(){
		return this.bond;
	}
	
	public void set1st(Device device, int qnum){
		this.bond.add(0,device);
		this.qnum = qnum;
	}
	
	public boolean set2nd(Device device, int qnum){
		if(this.bond.get(0) != device){
			if(this.qnum == qnum){
				this.bond.add(1,device);
				return true;
			}
		}
		return false;
	}
	
	public boolean isPrepared(){
		return (this.bond.get(1) != null);
	}
	
}

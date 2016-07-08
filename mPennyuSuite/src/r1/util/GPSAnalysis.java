package r1.util;

import r1.util.iCCConstants.StatusVehicle;

public class GPSAnalysis 
{
	double dLat1;
	double dLon1;
	double dLat2;
	double dLon2;	
	int nDistance = 0;
	int tickCount = 0;
  StatusVehicle lastStatus = StatusVehicle.Stop;
  StatusVehicle curStatus = StatusVehicle.Stop;    
  int retryCount = 0;
  int courseBasis = 0;
  
  public GPSAnalysis()
  {
  	dLat1 = -91;
    dLon1 = -181;
  }
  
	public double getdLat1() {
		return dLat1;
	}
	public void setdLat1(double dLat1) {
		this.dLat1 = dLat1;
	}
	public double getdLon1() {
		return dLon1;
	}
	public void setdLon1(double dLon1) {
		this.dLon1 = dLon1;
	}
	public double getdLat2() {
		return dLat2;
	}
	public void setdLat2(double dLat2) {
		this.dLat2 = dLat2;
	}
	public double getdLon2() {
		return dLon2;
	}
	public void setdLon2(double dLon2) {
		this.dLon2 = dLon2;
	}
	public int getnDistance() {
		return nDistance;
	}
	public void setnDistance(int nDistance) {
		this.nDistance = nDistance;
	}
	public int getTickCount() {
		return tickCount;
	}
	public void setTickCount(int tickCount) {
		this.tickCount = tickCount;
	}
	public StatusVehicle getLastStatus() {
		return lastStatus;
	}
	public void setLastStatus(StatusVehicle lastStatus) {
		this.lastStatus = lastStatus;
	}
	public StatusVehicle getCurStatus() {
		return curStatus;
	}
	public void setCurStatus(StatusVehicle curStatus) {
		this.curStatus = curStatus;
	}
	public int getRetryCount() {
		return retryCount;
	}
	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}
	public int getCourseBasis() {
		return courseBasis;
	}
	public void setCourseBasis(int courseBasis) {
		this.courseBasis = courseBasis;
	}  
}

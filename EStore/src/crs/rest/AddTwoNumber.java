package crs.rest;

import atg.nucleus.GenericService;

public class AddTwoNumber extends GenericService{
	
	public double addTwo(double first, double second) {
		vlogDebug("Inside AddTwoNumber.addTwo");
		return first + second;
	}

}

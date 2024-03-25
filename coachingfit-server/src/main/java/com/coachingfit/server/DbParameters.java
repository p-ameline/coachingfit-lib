package com.coachingfit.server;

import com.primege.server.DbParametersModel;

public class DbParameters extends DbParametersModel
{	
	// public static String _sTrace = "/var/lib/tomcat8/logs/coachingfit.log" ;
	public static String _sTrace = "C:\\Documents\\En_cours\\Consulting\\sites\\coachingfit.log" ;
	
	public DbParameters(String sBase, String sUser, String sPass, String sCSV, String sCSV_daily, String sArchetypesDir, String sVersion) {
		super(_sTrace, sBase, sUser, sPass, sCSV, sCSV_daily, sArchetypesDir, sVersion) ;
	}	
}

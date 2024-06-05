package com.coachingfit.server.handler ;

import junit.framework.TestCase ;

public class CoachingFitBuildCsvHandlerTest extends TestCase
{
  public void testConstructors() 
  {
  }
  
  public void testgetFileName() 
  {
  	assertEquals("",         CoachingFitBuildCsvHandler.getFileName(null)) ;
  	assertEquals("",         CoachingFitBuildCsvHandler.getFileName("")) ;
  	
  	assertEquals("test",         CoachingFitBuildCsvHandler.getFileName("test")) ;
  	assertEquals("Fichier_CSV_PM",         CoachingFitBuildCsvHandler.getFileName("Fichier CSV PM")) ;
  	assertEquals("Fichier_CSV_PM",         CoachingFitBuildCsvHandler.getFileName("Fichier  CSV   PM")) ;
  	
  	assertEquals("Fichier_CSV_PM",         CoachingFitBuildCsvHandler.getFileName("Fichier  CSV   PM ")) ;
  	assertEquals("Fichier_CSV_PM",         CoachingFitBuildCsvHandler.getFileName("Fichier  CSV   PM  ")) ;
  	assertEquals("Fichier_CSV_PM",         CoachingFitBuildCsvHandler.getFileName("  Fichier  CSV   PM  ")) ;
  }
}

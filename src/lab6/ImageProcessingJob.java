/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lab6;

import java.io.File;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Ada
 */
public class ImageProcessingJob {
    private File plik;
    //private String nazwa;
    private DoubleProperty postep;
    private SimpleStringProperty status;
    
    
    ImageProcessingJob(){}
   
    ImageProcessingJob(File pliczek,Double progres ,String stan){
        plik=pliczek;
        postep=new SimpleDoubleProperty(progres);
        status= new SimpleStringProperty(stan);         
    }
    
    public File getPlik(){
        return plik;
    }
    
    public void setPlik(File pliczek){
        plik=pliczek;
    }
    
    public DoubleProperty getPostep(){
        return postep;
    }
    
    public void setPostep(DoubleProperty liczba){
        postep=liczba;
        
    }
    
    public void setPostep(Double liczba){
        postep=new SimpleDoubleProperty(liczba);
    }
    
    public SimpleStringProperty getStatus(){
        return status;
    }
    public void setStatus(String stan){
        status=new SimpleStringProperty(stan);  
    }
    public void setStatus(SimpleStringProperty stan){
        status=stan;  
    }
    
    
}

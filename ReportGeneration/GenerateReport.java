
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.awt.geom.*;
import java.sql.*;
import java.text.*;
import java.util.Date;
import java.util.List;

import java.util.*;
import javax.xml.crypto.*;
import java.time.chrono.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;



public class GenerateReport {
    
    
  final public static char COMMA = ',';
  final public static String COMMA_STR = ",";
  final public static char ESCAPE_CHAR = '\\';
	
		public static  boolean requiresCompleteData = false;
		public static String SHOP_ID = "106757";
				
		
		public static HashMap<String,String> srr_metaData;
		public static HashMap<String, HashMap<String,ArrayList<String>>> map_sff_question;
		public static HashMap<String, HashMap<String,String> > map_sff_answers;
		public static ArrayList<String> arr_questionIds;
		

		public static String SFF_FORM_FILE_NAME = SHOP_ID+"/sff_forms_fields.csv";
		public static String SFF_FILE_NAME = SHOP_ID+"/sff_reviews.csv";//"sff_reviews_sample_2.csv";
		public static String SRR_FILE_NAME = SHOP_ID+"/srr_orders.csv";
		public static String SFF_SRR_FILE_NAME = "REPORT ("+SHOP_ID+").csv";

	
	    public static void main(String[] args) throws FileNotFoundException 
	    {
			parseFormFields();
			parseSRRData();
			parseSFF();
			mergeSFFData();
	    }
	
/*	 # ================== BUSINESS LOGIC METHODS ===================== */				

		public static  void mergeSFFData(){
			
			File file = new File (SFF_SRR_FILE_NAME);
			PrintWriter pw; 
	        StringBuilder sb = new StringBuilder();
			int lineNumber = 0;
	       
			try {
				file.createNewFile();
				pw = new PrintWriter(file); 
				setHeaderForNewCSV(pw,sb);

				if (requiresCompleteData){
					
					for(Map.Entry<String, String > m:srr_metaData.entrySet()){
//						if (lineNumber >= 273){
//							System.out.println("I am here");
//						}
						String orderId = m.getKey();						
						if (map_sff_answers.containsKey(orderId)){
							HashMap<String,String> review = map_sff_answers.get(orderId);
							addRow(pw, sb, orderId, review);
						}
						else{
							String metaData = m.getValue();
							addRow(pw, sb, orderId, metaData);
						}
						lineNumber++;	
					}
						
				}
				else{
					for(Map.Entry<String, HashMap<String,String> > m:map_sff_answers.entrySet()){
						HashMap<String,String> review = m.getValue();
						String orderId = m.getKey();
						addRow(pw, sb, orderId, review);
						lineNumber++;
					}	
				}

			pw.close();
			System.out.println("Total Lines wrote: " + lineNumber);		
	        System.out.println("Done!");

			} 
	        catch (FileNotFoundException e) 
	        {
	            e.printStackTrace();
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        } 
	        finally 
	        {

	        }

		
		}




	
/*	 # ================== CSV GENERATION METHODS ===================== */		
	
		public static  void setHeaderForNewCSV(PrintWriter pw, StringBuilder sb){
			
			sb.append("Order ID, Meta Data, Survey Completion");
			HashMap<String,ArrayList<String>> questionTexts 	= new HashMap<String,ArrayList<String>>();
			arr_questionIds 	= new ArrayList<String>();
			
			for(Map.Entry<String, HashMap<String,ArrayList<String>> > map:map_sff_question.entrySet()){

				
				HashMap<String,ArrayList<String>> mapQuestions = map.getValue();
				for(Map.Entry<String,ArrayList<String>> m:mapQuestions.entrySet()){
					if (!arr_questionIds.contains(m.getKey())){
                                            ArrayList<String> arr = m.getValue();
                                            questionTexts.put(m.getKey(), arr);
					    arr_questionIds.add(m.getKey());
					}
				}
			}
			
			for (int i = 0; i < arr_questionIds.size(); i++){
			 sb.append(',');
			 sb.append(arr_questionIds.get(i));
			}	
	         sb.append('\n');
	
                 System.out.println("Columns: "+arr_questionIds.size());
                 HashMap<Integer,String[]> mLines = new HashMap<Integer,String[]>();
                 for (int i = 0; i < arr_questionIds.size(); i++){
			ArrayList<String> mArray = questionTexts.get(arr_questionIds.get(i));
			for (int j = 0; j < mArray.size(); j++){
                            
                            if(mLines.containsKey(j)){

                                    String[] JsonArr_line  = mLines.get(j);
                                    String escaped =  mArray.get(j);
                                    JsonArr_line[i] = escaped.replaceAll(",", " ");
                                    //mStr=mStr.replaceAll("\\b" + arr_questionIds.get(i).trim()+ "\\b",mArray.get(j));
                                    mLines.put(j, JsonArr_line);
                                
                            }
                            else{

                                    String[] JsonArr_line = new String[arr_questionIds.size()];
                                                                     
                                    for (int x = 0; x < arr_questionIds.size(); x++){
                                        JsonArr_line[x] = " ";
                                    }
                           
                                    String escaped =  mArray.get(j);
                                     JsonArr_line[i] = escaped.replaceAll(",", " ");
                                    mLines.put(j, JsonArr_line);
                               
                            }
			                         
  			}	
	            }
                    System.out.println("Language Lines size: " + mLines.size());
                    
                    for (int x = 0; x < mLines.size(); x++){
                         StringBuilder csvBuilder = new StringBuilder();
                         csvBuilder.append(", , ,");
                            for (int y = 0; y < mLines.get(x).length; y++){
                         
                                 csvBuilder.append(mLines.get(x)[y]);
                                 csvBuilder.append(",");
                             
                            }
                            String csv = csvBuilder.toString();
                            System.out.println("line: " + csv);
                            sb.append(csv);
                            sb.append('\n');
                        }
                    
                         sb.append('\n');
			 pw.write(sb.toString());
			 sb.setLength(0);

		}
		
		
		private static String appendDQ(String str) {
			return "\"" + str + "\"";
		}

		public static void addRow(PrintWriter pw, StringBuilder sb, String orderId, HashMap<String,String> review ){
				
		        sb.append(orderId);
				sb.append(',');	
		        sb.append(review.get("metaData"));
				sb.append(',');	
		        sb.append(review.get("surveyCompletion"));
						
				for(int i = 0; i <  arr_questionIds.size(); i++ ){

					String questionId = arr_questionIds.get(i);
					if (review.containsKey(questionId)){
						sb.append(',');	
						String answer = review.get(questionId);
						sb.append(answer);	
					}
					else{
						sb.append(',');
						sb.append(' ');																				
					}
				}
		        sb.append('\n');

				pw.write(sb.toString());
				sb.setLength(0);

		}
		
		
		public static void addRow(PrintWriter pw, StringBuilder sb, String orderId, String metaData ){
				
		        sb.append(orderId);
				sb.append(',');	
		        sb.append(metaData);
				sb.append(',');	
		        sb.append(' ');
						
				for(int i = 0; i <  arr_questionIds.size(); i++ ){
						sb.append(',');
						sb.append(' ');																				
				}
		        sb.append('\n');

				pw.write(sb.toString());
				sb.setLength(0);

		}


		


/*	 # ================== PARSER METHODS ===================== */
		
		public static void parseFormFields(){
			map_sff_question = new HashMap<String,HashMap<String,ArrayList<String>>>(); 
	    	String csvFile = SFF_FORM_FILE_NAME;
	        BufferedReader br = null;
	        String line = "";
	        
	        try {

	            br = new BufferedReader(new FileReader(csvFile));
				br.readLine();
	            while ((line = br.readLine()) != null) 
	            {

	                String[] Line = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
	                if(Line.length == 3)
	                {
						HashMap<String,ArrayList<String>> map;
                                                ArrayList<String> arr;
						String formId = Line[0];
						String questionId = Line[1];
						String questionText = Line[2];
						
						if (map_sff_question.containsKey(formId)) {
							map = map_sff_question.get(formId);
                                                        if(map.containsKey(questionId))
                                                        arr = map.get(questionId);
                                                        else{
                                                            arr = new ArrayList<>();
                                                        }
						}
						else{
							map = new HashMap<String,ArrayList<String>>();
                                                        arr = new ArrayList<>();
						}
						arr.add(questionText);
						map.put(questionId, arr);
						map_sff_question.put(formId, map);

	                }
	            }

	        } 
	        catch (FileNotFoundException e) 
	        {  
	            e.printStackTrace();
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        } 
	        finally 
	        {
		
		
	            if (br != null) 
	            {
	                try 
	                {
	                    br.close();
	                } 
	                catch (IOException e) 
	                {
	                    e.printStackTrace();
	                }
	            }
	        }

		}
		
		public static void parseSFF(){
			
			BufferedReader br = null;
	        String line = "";
			long occurance = 0; // occurance is used to find out if the data of cell is complete w.r.t (")
			int lineNumber = 0;

			String metaData;
			DateFormat df =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			
			HashMap<String,List<Date>> map_reviewCreatedDate = new HashMap<String,List<Date>>();
			HashMap<String,List<Date>> map_reviewOpenedDate = new HashMap<String,List<Date>>();
			
			map_sff_answers = new HashMap<String,HashMap<String,String> >(); 
			try{
				
			
	            br = new BufferedReader(new FileReader(SFF_FILE_NAME));
				br.readLine();

	            while ((line = br.readLine()) != null) {

						occurance = line.chars().filter(num -> num == '"').count();
						while ( occurance % 2 != 0 ){
							line += br.readLine();
							occurance = line.chars().filter(num -> num == '"').count();
						}
//						if (lineNumber >= 1604){
//							System.out.println("Line Read: "+ line);	
//						}
//						System.out.println("Line Read: "+ lineNumber);
		
						String[] Line = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
		                if(Line.length == 7 )
		                {
								metaData = "";
								
								String formId = Line[0];
								String questionId = Line[1];
								String orderId = Line[2];
								String answer = Line[3];
								String convertedValue = Line[4];
								String created_at = Line[5];
								String opened_at = Line[6];
								created_at = created_at.replace("\"", "");						
								opened_at = opened_at.replace("\"", "");									
								if (srr_metaData.containsKey(orderId)){
									metaData = srr_metaData.get(orderId);
								}
								HashMap<String,String>  review;
								if(map_sff_answers.containsKey(orderId)){
									review = map_sff_answers.get(orderId);
									
									review.put(questionId, answer);//questionId, answer
									review.put("metaData", metaData);
									review.put("createdAt", created_at);//created_at
									review.put("openedAt", opened_at);
								}
								else{
									review = new HashMap<String,String> (); 
									review.put(questionId, answer);//
									review.put("metaData", metaData);//
									review.put("createdAt", created_at);//created_at
									review.put("openedAt", opened_at);									
								}
								
								map_sff_answers.put(orderId, review);								
								++lineNumber;
								
								
								// Storing CreatedAt for SurveyCompletion Time
								List<Date> arrCreatedDates ;
								if (map_reviewCreatedDate.containsKey(orderId)){
									arrCreatedDates = map_reviewCreatedDate.get(orderId);									
								}
								else{
									arrCreatedDates = new ArrayList<Date>();
								}
								
								// Storing OpenedAt for SurveyCompletion Time								
								List<Date> arrOpenedDates ;
								if (map_reviewOpenedDate.containsKey(orderId)){
									arrOpenedDates = map_reviewOpenedDate.get(orderId);									
								}
								else{
									arrOpenedDates = new ArrayList<Date>();
								}
								
								
								try {
									arrCreatedDates.add(df.parse(created_at));	
									if (opened_at.equals("NULL")){
										arrOpenedDates.add(df.parse(created_at));			
									}	
									else{
										arrOpenedDates.add(df.parse(opened_at));		
									}
									
								} catch (Exception e) {
									System.out.println("Exception: "+e);
								}

								map_reviewCreatedDate.put(orderId, arrCreatedDates);
								map_reviewOpenedDate.put(orderId, arrOpenedDates);								
								
								
								
		            	}
	        	}
				System.out.println("Total Line Read: "+ lineNumber);
			}
			catch (FileNotFoundException e) 
	        {
	            e.printStackTrace();
	        } 
	        catch (IOException e) 
	        {
	            e.printStackTrace();
	        } 
	        finally 
	        {
				calculateSurveyCompletionTime(map_reviewCreatedDate,map_reviewOpenedDate);
	            if (br != null) 
	            {
	                try 
	                {
	                    br.close();
	                } 
	                catch (IOException e) 
	                {
	                    e.printStackTrace();
	                }
	            }

	        }

			System.out.println("Total Reviews: "+map_sff_answers.size());
		
		}
		
	    public static void parseSRRData() 
	    {
				srr_metaData = new HashMap<String,String>(); 
		    	String csvFile = SRR_FILE_NAME;
		        BufferedReader br = null;
		        String line = "";
		        
		        try {

		            br = new BufferedReader(new FileReader(csvFile));
		            while ((line = br.readLine()) != null) 
		            {

		                String[] srrLine = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
		                if(srrLine.length == 2)
		                {
	    					 srr_metaData.put(srrLine[0], srrLine[1]);
		                }
		            }

		        } 
		        catch (FileNotFoundException e) 
		        {
		            e.printStackTrace();
		        } 
		        catch (IOException e) 
		        {
		            e.printStackTrace();
		        } 
		        finally 
		        {
			
					System.out.println("Total Orders: "+ srr_metaData.size());
		            if (br != null) 
		            {
		                try 
		                {
		                    br.close();
		                } 
		                catch (IOException e) 
		                {
		                    e.printStackTrace();
		                }
		            }
		        }
	    }
	
	

/*	 # ================== HELPER METHODS ===================== */


		public static long differenceBetweenDates(Date d1, Date d2){

			return ChronoUnit.SECONDS.between(d1.toInstant(),d2.toInstant());

		}
		
		
		public static void calculateSurveyCompletionTime(HashMap<String,List<Date>> map, HashMap<String,List<Date>> mapOpened){

	    	 for(Map.Entry<String, List<Date> > m:map.entrySet())
	    	 {
		
				String orderId = m.getKey();
				List<Date> arrCreated = m.getValue();
				List<Date> arrOpened = mapOpened.get(orderId);
				
				Date minDate;
				
//				for (int i = 0; i < arrOpened.size(); i++){
//					System.out.println(arrOpened.get(i));
//				}
				if (arrOpened.size() > 0){
					minDate = Collections.min(arrOpened);	
				}
				else{
					minDate = Collections.min(arrCreated);	
				}
				
				
				Date maxDate = Collections.max(arrCreated);

//				System.out.println("MinDate "+ minDate);
//				System.out.println("MaxDate "+ maxDate);				

				String completionTime = differenceBetweenDates(minDate, maxDate) + "s";
				if(map_sff_answers.containsKey(orderId)){
					HashMap<String,String> review = map_sff_answers.get(orderId);
					review.put("surveyCompletion", completionTime);//created_at
				}

	    	 }
			
		}	
	
	    
	    public static void printMap(HashMap<String,String> map)
	    {
	    	 for(Map.Entry m:map.entrySet())
	    	 {
	    		 System.out.println(m.getKey()+" "+m.getValue());  
	    	 }
	    }
		
		public static void printMapList(HashMap<String,ArrayList<String> > map)
	    {
	    	 for(Map.Entry<String, ArrayList<String> > m:map.entrySet())
	    	 {
	    		System.out.println(m.getKey()+" \n");
				ArrayList<String> arr = m.getValue();
				for(int i = 0; i < arr.size(); i++){
					System.out.println("["+i+"] => "+ arr.get(i) + "\n");
				}

	    	 }
	    }

                
 public static String escapeString(String str, char escapeChar, char charToEscape) {
    if (str == null) {
      return null;
    }
    StringBuilder result = new StringBuilder();
    for (int i=0; i<str.length(); i++) {
      char curChar = str.charAt(i);
      if (curChar == escapeChar || curChar == charToEscape) {
        // special char
        result.append(escapeChar);
      }
      result.append(curChar);
    }
    return result.toString();
  }
}




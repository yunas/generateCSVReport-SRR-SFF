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

/*
This code will store all answers against all order_id's in an array 
and then write in the file

*/

public class GenerateReport {
	
		public static HashMap<String,String> srr_metaData;
		public static HashMap<String, HashMap<String,String> > map_sff_question;
		public static HashMap<String, HashMap<String,String> > map_sff_answers;
		public static ArrayList<String> arr_questionIds;
		
		public static String SHOP_ID = "108612";
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
	
		
		public static long differenceBetweenDates(Date d1, Date d2){

			return ChronoUnit.SECONDS.between(d1.toInstant(),d2.toInstant());

		}
		
		
		public static void calculateSurveyCompletionTime(HashMap<String,List<Date>> map){

	    	 for(Map.Entry<String, List<Date> > m:map.entrySet())
	    	 {
				List<Date> arr = m.getValue();
				Date minDate = Collections.min(arr);
				Date maxDate = Collections.max(arr);
				
				String completionTime = differenceBetweenDates(minDate, maxDate) + "s";
				String orderId = m.getKey();
				
				if(map_sff_answers.containsKey(orderId)){
					HashMap<String,String> review = map_sff_answers.get(orderId);
					review.put("surveyCompletion", completionTime);//created_at
				}

	    	 }
			
		}
	
		public static void parseFormFields(){
			map_sff_question = new HashMap<String,HashMap<String,String> >(); 
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
						HashMap<String,String> map;
						String formId = Line[0];
						String questionId = Line[1];
						String questionText = Line[2];
						
						if (map_sff_question.containsKey(formId)) {
							map = map_sff_question.get(formId);
						}
						else{
							map = new HashMap<String,String>();
						}
						
						map.put(questionId, questionText);
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
	
		public static  void setHeaderForNewCSV(PrintWriter pw, StringBuilder sb){
			
			sb.append("Order ID, Meta Data, Survey Completion");
			ArrayList<String> questionTexts 	= new ArrayList<String>();
			arr_questionIds 	= new ArrayList<String>();
			
			for(Map.Entry<String, HashMap<String,String> > map:map_sff_question.entrySet()){

				
				HashMap<String,String> mapQuestions = map.getValue();
				for(Map.Entry<String,String> m:mapQuestions.entrySet()){
					if (!arr_questionIds.contains(m.getValue())){
						questionTexts.add(m.getValue());
						arr_questionIds.add(m.getKey());
					}
				}
			}
			
			for (int i = 0; i < arr_questionIds.size(); i++){
			 sb.append(',');
			 sb.append(arr_questionIds.get(i));
			}	
	         sb.append('\n');
	
			sb.append(", , ");
			for (int i = 0; i < questionTexts.size(); i++){
			 sb.append(',');
			 sb.append(questionTexts.get(i));
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


		public static void parseSFF(){
			
			BufferedReader br = null;
	        String line = "";
			long occurance = 0; // occurance is used to find out if the data of cell is complete w.r.t (")
			int lineNumber = 0;

			String metaData;
			DateFormat df =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			
			HashMap<String,List<Date>> map_reviewsDate = new HashMap<String,List<Date>>();
			
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
		                if(Line.length == 6 )
		                {
								metaData = "";
								
								String formId = Line[0];
								String questionId = Line[1];
								String orderId = Line[2];
								String answer = Line[3];
								String convertedValue = Line[4];
								String created_at = Line[5];
								created_at = created_at.replace("\"", "");								
								if (srr_metaData.containsKey(orderId)){
									metaData = srr_metaData.get(orderId);
								}
								HashMap<String,String>  review;
								if(map_sff_answers.containsKey(orderId)){
									review = map_sff_answers.get(orderId);
									
									review.put(questionId, answer);//questionId, answer
									review.put("metaData", metaData);
									review.put("createdAt", created_at);//created_at
								}
								else{
									review = new HashMap<String,String> (); 
									review.put(questionId, answer);//
									review.put("metaData", metaData);//
									review.put("createdAt", created_at);//created_at
								}
								
								List<Date> arrDates ;
								if (map_reviewsDate.containsKey(orderId)){
									arrDates = map_reviewsDate.get(orderId);									
								}
								else{
									arrDates = new ArrayList<Date>();
								}
								try {
									arrDates.add(df.parse(created_at));		
								} catch (Exception e) {
									System.out.println("Exception: "+e);
								}

								map_reviewsDate.put(orderId, arrDates);
								
								
								map_sff_answers.put(orderId, review);								
								++lineNumber;
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
				calculateSurveyCompletionTime(map_reviewsDate);
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


	public static  void mergeSFFData(){
		
		File file = new File (SFF_SRR_FILE_NAME);
		PrintWriter pw; 
        StringBuilder sb = new StringBuilder();
		int lineNumber = 0;
       
		try {
			file.createNewFile();
			pw = new PrintWriter(file); 
			setHeaderForNewCSV(pw,sb);

			for(Map.Entry<String, HashMap<String,String> > m:map_sff_answers.entrySet()){
				HashMap<String,String> review = m.getValue();
				String orderId = m.getKey();
				addRow(pw, sb, orderId, review);
				lineNumber++;
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

}


/*

# ================== USAGE INSTRUCTIONS =====================

 1. Grab CSV from SRR containing: Order id & Metadata columns only
 2. Name this file 'srr_orders'
 3. Grab CSV from SFF containing: Form id, question id, order id, answers, converted value, created_at only
 4. Name this file sff_reviews
 5. Grab CSV from SFF containing: Form id, question id, question text only
 6. Name this file sff_form_fields
 7. Group these files and place them in a folder as per shop id and place folder in same hirearchy as of GenerateReport.java
 8. Open source code and move to live 36 and update SHOP_ID and that's it.
 9. Run this code ( NO Other CHANGE, apart from point 8. )
 10. You will have a new file in same directory made called "REPORT(ShopID).CSV"
 
# ======================= SFF Query  ==========================

# 1 => COLLECT FORM QUESTION ID's & TEXT

select forms.id, form_fields.id, translations.translation
from translations
join form_fields on form_fields.id = translations.form_field_id
join forms on forms.id = translations.form_id
join shops on shops.id = forms.shop_id
where translations.translation_type = 'label'
and shops.shop_id = 101085


# 2 => COLLECT REVIEWS AGAINST PARTICULAR SHOP

SELECT feedback_details.form_id, feedback_details.form_field_id, feedbacks.order_id, feedback_details.answer, feedback_details.rating_converted_value, feedback_details.created_at
FROM sff.feedback_details
join sff.feedbacks on feedbacks.id = feedback_details.feedback_id
join sff.forms on forms.id = feedback_details.form_id
inner join sff.shops on shops.id = forms.shop_id
where shops.shop_id = 108617 
order by form_id DESC;

# ======================= SRR Query  ==========================
SELECT r.transaction_id, rm.value
FROM  `recipients` r
JOIN recipients_meta rm ON r.id = rm.recipient_id
WHERE shop_id IN ( Select id from shops where shop_id = 665 )

*/
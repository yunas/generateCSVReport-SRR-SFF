#  Code Usage Manual

 1. Grab CSV from SRR containing: Order id & Metadata columns only
 2. Name this file 'srr_orders'
 3. Grab CSV from SFF containing: Form id, question id, order id, answers, converted value, created_at only
 4. Name this file sff_reviews
 5. Grab CSV from SFF containing: Form id, question id, question text only
 6. Name this file sff_forms_fields
 7. Group these files and place them in a folder as per shop id and place folder in same hirearchy as of GenerateReport.java
 10. You will have a new file in same directory made called "REPORT(ShopID).CSV"
 11. Additonaly you will have a new directory created with name pattern "FILES[shop_id]" which contains survey based reports.
 

# Configurations

 1. Many configurations are now introduced to make output as desired and they are:
 2. Set setting "requiresCompleteData" to true maps Reviews on Orders and setting to false maps Orders on Reviews.
 3. Set setting "requiresQuestionText" to true if you wish to include question text in reports.
 4. Set setting "requiresQuestionMapping" to true if you wish to replace smartforms_question_id's with competitors question id
 5. Set setting "requiresMergedFormFieldsAndIPSOS" to true if you wish to generate a separate CSV 'sff_forms_fields_ipsos' which contains smartforms questions text info and maps respective question id of competitor's
 6. Set setting "SHOP_ID" to process for the respective shop.

 Note: Set point 4 & 5 to true only if you are provided with mapping_ekomi_ipsos.csv ( which in your case will be mapping of smartForms question id's and competitors's question id)
 

# SFF Query

###### Collect question & forms details survey based

```sql
select forms.survey_id, forms.id, form_fields.id, translations.translation
from translations
join form_fields on form_fields.id = translations.form_field_id
join forms on forms.id = translations.form_id
join shops on shops.id = forms.shop_id
where translations.translation_type = 'label'
and form_fields.status = 'active'
and forms.status = 'active'
and shops.shop_id = 106757
```


###### Collect reviews survey based 

```sql
SELECT forms.survey_id, feedback_details.form_id, feedback_details.form_field_id, feedbacks.order_id, feedback_details.answer, feedback_details.rating_converted_value, feedbacks.created_at, form_activity_streams.last_opened_at
FROM feedback_details
join feedbacks on feedbacks.id = feedback_details.feedback_id
join form_fields on form_fields.id = feedback_details.form_field_id
left join orders on orders.review_hash = feedbacks.review_hash
left join form_activity_streams on form_activity_streams.order_id = orders.id and feedbacks.form_id = form_activity_streams.form_id
join forms on forms.id = feedback_details.form_id
inner join sff.shops on shops.id = forms.shop_id
where shops.shop_id = 106757
and form_fields.status = 'active'
and forms.status = 'active'
order by form_id DESC;
```
###### SRR Query
```sql
SELECT r.transaction_id, rm.value
FROM  `recipients` r
JOIN recipients_meta rm ON r.id = rm.recipient_id
WHERE shop_id IN ( Select id from shops where shop_id = 665 )
```
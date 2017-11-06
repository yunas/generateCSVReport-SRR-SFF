#  Code Usage Manual

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
 
# SFF Query

###### COLLECT FORM QUESTION ID's & TEXT

```sql
select forms.id, form_fields.id, translations.translation
from translations
join form_fields on form_fields.id = translations.form_field_id
join forms on forms.id = translations.form_id
join shops on shops.id = forms.shop_id
where translations.translation_type = 'label'
and shops.shop_id = 101085
```


###### COLLECT REVIEWS AGAINST PARTICULAR SHOP

```sql
SELECT feedback_details.form_id, feedback_details.form_field_id, feedbacks.order_id, feedback_details.answer, feedback_details.rating_converted_value, feedback_details.created_at, form_activity_streams.last_opened_at
FROM feedback_details
join feedbacks on feedbacks.id = feedback_details.feedback_id
left join orders on orders.review_hash = feedbacks.review_hash
left join form_activity_streams on form_activity_streams.order_id = orders.id
join forms on forms.id = feedback_details.form_id
inner join sff.shops on shops.id = forms.shop_id
where shops.shop_id = 665
order by form_id DESC;
```
###### SRR Query
```sql
SELECT r.transaction_id, rm.value
FROM  `recipients` r
JOIN recipients_meta rm ON r.id = rm.recipient_id
WHERE shop_id IN ( Select id from shops where shop_id = 665 )
```
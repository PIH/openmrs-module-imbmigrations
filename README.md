imbmigrations
==========================


step  1

update orders set auto_expire_date=start_date where order_type_id=5 and auto_expire_date is null;

update orders, set encounter_id to all orders with start_date but no encounter_id

==== Arrangement ======
update orders o
join encounter e on o.patient_id=e.patient_id and o.start_date=e.encounter_datetime
set o.encounter_id=e.encounter_id

but some record remaining

create Encounter type "DRUG ORDER VISIT"=40 on my laptop

update start_date where is null in orders table

insert into encounter(encounter_type,patient_id,location_id,encounter_datetime, creator, date_created, uuid)
select 40,o.patient_id,26,o.start_date, 1, CURRENT_TIMESTAMP, UUID()
from orders o where o.encounter_id is null group by o.patient_id,o.start_date;


update orders o
join encounter e on o.patient_id=e.patient_id and o.start_date=e.encounter_datetime
set o.encounter_id=e.encounter_id



create a provider to serve as the "Unknown Provider" and set the global property "provider.unknownProviderUuid" to the uuid of this provider.


discontinued orders cannot have null discontinued_date values,

on my laptop I set this as discontinued_date=start_date where discontinued=1 and discontinued_date is null

step 2




check for

select distinct u.person_id from orders o,users u where o.orderer=u.user_id and u.person_id not in (select person_id from provider);

if return something then run this

insert into provider(person_id, identifier, creator, date_created, retired, retired_by, date_retired, retire_reason, uuid)
select distinct u.person_id, u.system_id, 1, CURRENT_TIMESTAMP, u.retired, u.retired_by, u.date_retired, u.retire_reason, UUID()
from orders o,users u where o.orderer=u.user_id and u.person_id not in (select person_id from provider);



ALTER TABLE `order_type`
ADD COLUMN `java_class_name` VARCHAR(255) DEFAULT NULL,
ADD COLUMN `parent` INT(11) DEFAULT NULL;


/usr/share/tomcat6/.OpenMRS$ sudo nano  order_entry_upgrade_settings.txt
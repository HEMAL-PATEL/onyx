-- Set export_date to current system time for already exported interviews (ONYX-821)
update participant
set export_date = sysdate()
where exported = true
and export_date is null;

-- Set the status to VALID and workstation to Unknown for all existing measure rows (ONYX-1004)
update measure set status = 'VALID', workstation = 'Unknown';

-- For all consent rows, update the time_start column to the parent interview's start_date (ONYX-1003)
update consent c, interview i SET c.time_start = i.start_date where c.interview_id = i.id;

-- ONYX-1049 In the improbable case that an instrument contains an empty string as barcode, which should not be possible.
update instrument set barcode = CONCAT(type, '_barcode') where barcode = "";
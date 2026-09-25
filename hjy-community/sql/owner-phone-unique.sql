-- Before applying this constraint, resolve every non-NULL duplicate phone manually.
-- Do not merge owner profiles or guess phone ownership from names.
SELECT owner_phone_number, COUNT(*) AS duplicate_count
FROM hjy_owner
WHERE owner_phone_number IS NOT NULL
GROUP BY owner_phone_number
HAVING COUNT(*) > 1;

-- Run only when the query above returns no rows. Multiple NULL values are allowed.
ALTER TABLE hjy_owner ADD UNIQUE KEY uk_hjy_owner_phone (owner_phone_number);

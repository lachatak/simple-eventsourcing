CREATE TABLE PUBLIC.journal (
   id                serial PRIMARY KEY,
   aggregate_id      varchar(100),
   aggregate_offset  bigint,
   manifest          varchar(100),
   data              jsonb,
   timestamp         timestamp without time zone DEFAULT(NOW()),
   CONSTRAINT aggregate_versioning UNIQUE(aggregate_id, aggregate_offset)
)
